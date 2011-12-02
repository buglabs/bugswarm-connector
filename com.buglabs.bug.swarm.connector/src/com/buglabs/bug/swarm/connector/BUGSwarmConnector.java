package com.buglabs.bug.swarm.connector;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jivesoftware.smack.Chat;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import com.buglabs.bug.dragonfly.module.IModuleControl;
import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.model.Configuration;
import com.buglabs.bug.swarm.client.model.Configuration.Protocol;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;
import com.buglabs.bug.swarm.connector.model.BinaryFeed;
import com.buglabs.bug.swarm.connector.model.Feed;
import com.buglabs.bug.swarm.connector.model.FeedRequest;
import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.model.ServiceFeedAdapter;
import com.buglabs.bug.swarm.connector.osgi.Activator;
import com.buglabs.bug.swarm.connector.osgi.OSGiUtil;
import com.buglabs.bug.swarm.connector.osgi.OSGiUtil.ServiceMatcher;
import com.buglabs.bug.swarm.connector.osgi.OSGiUtil.ServiceVisitor;
import com.buglabs.bug.swarm.connector.xmpp.ISwarmServerRequestListener;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.simplerestclient.HTTPException;

/**
 * The swarm connector client for BUGswarm system. 
 * 
 * This class is the "center" of bugswarm-client and is responsible 
 * for the core of message handling and state management.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnector extends Thread implements ISwarmServerRequestListener, ServiceListener {

	/**
	 * Used to convert seconds to milliseconds.
	 */
	private static final long MILLIS_IN_SECONDS = 2000;
	
	/**
	 * HTTP 404 response.
	 */
	private static final int HTTP_404 = 404;

	/**
	 * Amount of time to wait before sending device state to swarm peers.
	 */
	private static final long LOCAL_CHANGE_DELAY_MILLIS = 1000;

	/**
	 * A string value swarm server expects when creating a new resource.
	 */
	private static final String SWARM_RESOURCE_TYPE_BUG = "bug";
	/**
	 * Configuration info for swarm server.
	 */
	private final Configuration config;
	/**
	 * Web service client to swarm server.
	 */
	private ISwarmClient wsClient;
	/**
	 * True if the initalize() method has been called, false otherwise.
	 */
	private boolean initialized = false;
	/**
	 * Instance of XMPP client.
	 */
	private SwarmXMPPClient xmppClient;
	
	/**
	 * List of all member swarms.
	 */
	private List<SwarmModel> memberSwarms;
	/**
	 * Timer that manages all the active streaming feeds.
	 */
	private Timer timer = new Timer();
	
	/**
	 * A lock used to filter bursts of OSGi service events and only send the BUG message when events are finished.
	 */
	private volatile Boolean localEventUpdate = false;
	
	private static ObjectMapper mapper = new ObjectMapper();
	/**
	 * A Map of active "streaming" feeds.  These feeds are running as TimerTasks in a Timer 
	 * and sending response messages to the swarm server at regular intervals.
	 */
	private Map<String, TimerTask> activeTasks;

	/**
	 * List of feed names that should not be exported from device.
	 */
	private List<String> blacklist;
	private static LogService log;

	private final BundleContext context;	

	/**
	 * @param config
	 *            Predefined configuration
	 */
	public BUGSwarmConnector(BundleContext context, final Configuration config) {
		if (context == null || config == null)
			throw new IllegalArgumentException("A constructor input parameter is null.");
		
		this.context = context;
		this.config = config;
		BUGSwarmConnector.log = Activator.getLog();
		this.memberSwarms = new ArrayList<SwarmModel>();
		if (!config.isValid())
			throw new IllegalArgumentException("Invalid configuration");
	}

	@Override
	public void run() {
		try {
			// Initialize the clients used to communicate with swarm server
			if (!initialized)
				initialize();
					
			// Load data about server configuration and local configuration.
			log.log(LogService.LOG_DEBUG, "Getting member swarms.");

			try {
				List<SwarmModel> allSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(config.getResource());

				if (allSwarms == null || allSwarms.size() == 0) {
					log.log(LogService.LOG_INFO, "User does not belong to any swarms.");
				} else {
					String capabilities = getCapabilities();
					// Notify all swarms of presence.
					for (SwarmModel swarm : allSwarms) {
						log.log(LogService.LOG_DEBUG, "Joining swarm " + swarm.getId());
						
						xmppClient.joinSwarm(swarm.getId(), this);
						memberSwarms.add(swarm);
						xmppClient.sendPublicMessage(swarm.getId(), capabilities);
					}								
				}
				
				//After we broadcast Feeds to all swarms, listen for local service changes so that updates can be sent.
				context.addServiceListener(this);
			} catch (HTTPException e) {
				if (e.getErrorCode() == HTTP_404)
					log.log(LogService.LOG_WARNING, "Not a member of any swarms, not publishing feeds.");
				else
					throw e;
			}
			log.log(LogService.LOG_INFO, "Connector initialization complete.");
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Error occurred while initializing swarm client.", e);
		}
	}
	
	/**
	 * Initialize the connection to the swarm server.
	 * 
	 * @return true if initialization successful
	 * @throws Exception on any connection or authentication error.
	 */
	private boolean initialize() throws Exception {
		log.log(LogService.LOG_DEBUG, "Initializing " + BUGSwarmConnector.class.getSimpleName());
		wsClient = SwarmClientFactory.getSwarmClient(
				config.getHostname(Protocol.HTTP), config.getConfingurationAPIKey());
		
		UserResourceModel resource = null;
		if (config.hasResource()) {
			Activator.getLog().log(LogService.LOG_DEBUG, "Using stored resource id: " + config.getResource());
			resource = wsClient.getUserResourceClient().get(
					config.getResource());			
		} else {
			Activator.getLog().log(LogService.LOG_DEBUG, "Creating a new resource on the server.");
			resource = wsClient.getUserResourceClient().add(
					config.getDeviceLabel(), 
					getResourceDescription(), 
					SWARM_RESOURCE_TYPE_BUG, 
					0, 0);
			config.setResourceId(resource.getResourceId());
			//Cache resource id.
			Activator.setResourceId(resource.getResourceId());
			
			Activator.getLog().log(LogService.LOG_DEBUG, "New resource id: " + config.getResource());
		}
		
		if (resource == null) {
			//Set the persisted resourceid to null in the case of an invalid or deleted resource.  This will cause the connector to ask the server for a new one upon next start.
			Activator.setResourceId(null);
			throw new IOException("Unable to get or create resource for device.");
		}
		
		xmppClient = new SwarmXMPPClient(config);
		xmppClient.connect(this);
						
		initialized = true;
		return true;		
	}

	private String getResourceDescription() {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		
		Date date = Calendar.getInstance().getTime();
		return "BUG device resource created on " + sdf.format(date) + ".";
	}
	
	/**
	 * @return Immutable list of swarms that client is a member of, for
	 *         read-only purposes.
	 */
	public List<SwarmModel> getMemberSwarms() {
		return Collections.unmodifiableList(memberSwarms);
	}

	/**
	 * Shutdown the connector and free any local and remote resources in use.
	 */
	public void shutdown() {
		context.removeServiceListener(this);
		
		if (timer != null) {
			timer.cancel();
			timer = null;
		}		
			
		if (xmppClient != null) {
			for (SwarmModel sm : memberSwarms)
				xmppClient.leaveSwarm(sm.getId());
			// Send unpresence and disconnect from server
			xmppClient.disconnect();
		}		
		
		log.log(LogService.LOG_INFO, "Connector shutdown complete.");
	}

	@Override
	public void feedListRequest(final Jid requestJid, final String swarmId) {
		try {
			String document = getCapabilities();
		
			xmppClient.sendAllFeedsToUser(requestJid, swarmId, document);
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending feeds to " + requestJid, e);
		} 
	}

	/**
	 * @return A JSon string of the Capabilities message
	 * 
	 * @throws JsonGenerationException on Json parsing error
	 * @throws JsonMappingException on Json parsing error
	 * @throws IOException on Json parsing error
	 */
	private String getCapabilities() throws JsonGenerationException, JsonMappingException, IOException {
		Map<String, Object> root = new HashMap<String, Object>();
		Map<String, Object> c = new HashMap<String, Object>();
		
		c.put("feeds", getFeedNames());
		c.put("modules", getModuleMap());
		
		root.put("capabilities", c);
			
		return mapper.writeValueAsString(root);
	}

	/**
	 * @return A map of String String, key being "slot" + slot #, value being the name of the module.
	 */
	private Map<String, String> getModuleMap() {
		final Map<String, String> mm = new HashMap<String, String>();
		
		OSGiUtil.onServices(context, IModuleControl.class.getName(), null, new ServiceVisitor<IModuleControl>() {

			@Override
			public void apply(ServiceReference sr, IModuleControl service) {
				mm.put("slot" + service.getSlotId(), service.getModuleName());
			}
		});
		
		return mm;
	}

	/**
	 * @return List of Feed instances of native (OSGi) feeds.
	 */
	private List<String> getFeedNames() {
		final List<String> feedNames = new ArrayList<String>();
		
		//Native Feeds
		OSGiUtil.onServices(context, Map.class.getName(), null, new ServiceVisitor<Map>() {

			@Override
			public void apply(ServiceReference sr, Map service) {
				if (sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY) != null)
					feedNames.add(sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY).toString());
			}
			
		});
		
		//BUG Web Services
		OSGiUtil.onServices(context, PublicWSProvider.class.getName(), null, new ServiceVisitor<PublicWSProvider>() {

			@Override
			public void apply(ServiceReference sr, PublicWSProvider service) {
				feedNames.add(service.getPublicName());
				if (sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY) != null)
					feedNames.add(sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY).toString());
			}
			
		});
		
		return feedNames;
	}

	@Override
	public void feedListRequest(final Chat chat, final String swarmId) {
		try {
			String document = getCapabilities();

			chat.sendMessage(document);
			log.log(LogService.LOG_DEBUG, "Sent " + document + " to " + chat.getParticipant());
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Failed to send private message to " + chat.getParticipant(), e);
		}

	}

	/**
	 * @return true if connector has been successfully initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void feedRequest(final Jid jid, final String swarmId, final FeedRequest feedRequest) {
		Feed feed = getBUGFeed(context, feedRequest.getName());
		
		if (feed == null) {			
			log.log(LogService.LOG_WARNING, "Request for non-existant feed " + feedRequest.getName() + " from client " + jid);
			return;
		}
			
		TimerTask task = null;
		
		if (feed instanceof BinaryFeed) {
			task = new BinaryFeedResponseTask(wsClient, jid, swarmId, (BinaryFeed) feed, log);
		} else {
			task = new FeedResponseTask(xmppClient, jid, swarmId, feed, log);
		}
		
		if (feedRequest.hasFrequency() && !containsActiveTask(jid, swarmId, feed)) {
			if (activeTasks == null) 
				activeTasks = new HashMap<String, TimerTask>();
			
			//TODO: this is not matching up with the blacklist
			activeTasks.put(jid.toString() + swarmId + feed.getName(), task);
			
			timer.schedule(task, 0, feedRequest.getFrequency() * MILLIS_IN_SECONDS);
		} else {		
			timer.schedule(task, 0);
		}
	}

	/**
	 * This method will search for native and service feeds.
	 * 
	 * @param name name of feed.
	 * @return Feed of type name or null if feed does not exist.
	 */
	public static Feed getBUGFeed(BundleContext context, final String name) {
		//Look for native feed that matches feed name.
		Map feed = (Map) OSGiUtil.getServiceInstance(
				context, Map.class.getName(), 
				OSGiUtil.createFilter(Feed.FEED_SERVICE_NAME_PROPERTY, name));
		
		if (feed != null)
			return new Feed(name, feed);
		
		//Look for web service that matches the feed name.
		PublicWSProvider webService = 
				OSGiUtil.getServiceInstance(context, PublicWSProvider.class.getName(), null, new ServiceMatcher<PublicWSProvider>() {

					@Override
					public boolean match(ServiceReference sr, PublicWSProvider service) {						
						return service.getPublicName().equals(name);
					}
			
		});		
		
		if (webService != null)
			return new ServiceFeedAdapter(webService);
			
		
		return null;
	}

	/**
	 * @param jid jid of recipient
	 * @param swarmId id of swarm
	 * @param feed feed instance
	 * @return true if there is an active task corresponding to jid, swarm, and feed, false otherwise.
	 */
	private boolean containsActiveTask(Jid jid, String swarmId, Feed feed) {
		if (activeTasks == null)
			return false;
		
		return activeTasks.containsKey(jid.toString() + swarmId + feed.getName());
	}

	@Override
	public void swarmInviteRequest(final Jid sender, final String swarmId) {
		if (memberOfSwarm(swarmId)) {
			log.log(LogService.LOG_DEBUG, "Recieved invitation for room " + swarmId + " from " + sender.toString());
			return;
		}

		log.log(LogService.LOG_DEBUG, "Recieved invitation for room " + swarmId + " from " + sender.toString());

		try {
			xmppClient.joinSwarm(swarmId, BUGSwarmConnector.this);
			SwarmModel swarmModel = wsClient.get(swarmId);
			memberSwarms.add(swarmModel);
			log.log(LogService.LOG_DEBUG, "Joined swarm " + swarmId);
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Error occurred while responding to invite from swarm server.", e);
		}
	}

	/**
	 * @param swarmId
	 *            id of swarm
	 * @return true if swarm is in set of memberSwarms, false otherwise.
	 */
	private boolean memberOfSwarm(final String swarmId) {
		// Linear search through active member swarms.
		for (SwarmModel sm : memberSwarms)
			if (sm.getId().equals(swarmId))
				return true;

		return false;
	}

	@Override
	public void feedMetaRequest(FeedRequest request, String swarmId) {
		if (request.getParams().containsKey("status")) {
			//This is to turn a feed off/on.
			boolean feedEnabled = !request.getParams().get("status").toString().equalsIgnoreCase("off");
			
			if (feedEnabled) {
				if (blacklist != null) {
					blacklist.remove(request.getName());
					log.log(LogService.LOG_INFO, "Removed " + request.getName() 
							+ " from blacklist on swarm: " + swarmId);
				}
			} else {
				if (blacklist == null)
					blacklist = new ArrayList<String>();
				
				blacklist.add(request.getName());
				log.log(LogService.LOG_INFO, "Added " + request.getName() + " from blacklist on swarm: " + swarmId);
				
				//TODO: fix this, they do not match
				if (activeTasks.containsKey(request.getName())) {
					TimerTask task = activeTasks.get(request.getName());
					task.cancel();
					log.log(LogService.LOG_INFO, "Cancelled streaming feed " 
							+ request.getName() + " from due to server request on swarm: " + swarmId);
				}
			}
			
			return;
		}
		
		log.log(LogService.LOG_ERROR, "Unhandled Meta Request: " + request.toString());
		
	}

	@Override
	public void cancelFeedRequests(Jid jid, String swarmId) {
		xmppClient.clearChatCache(jid.getResource());
		
		if (activeTasks == null)
			return;
		
		boolean cancelled = false;
		//Cancel any tasks associated within a specific swarm to a specific resource.
		for (String taskKey : activeTasks.keySet()) {
			if (taskKey.contains(jid.getResource()) && taskKey.contains(jid.getUsername())) {
				activeTasks.get(taskKey).cancel();		
				cancelled = true;
			}
		}
		
		if (cancelled)
			log.log(LogService.LOG_INFO, 
					"Cancelled active tasks for : " + jid.toString() + " in swarm " + swarmId);
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
	 * 
	 * This method is the primary hook into the OSGi service registry.  Swarm-events that originate on BUG all start with this method.
	 */
	@Override
	public void serviceChanged(ServiceEvent event) {
		// For now, every time a service, module, or feed is created/removed, send
		// the entire state to each member swarm.
		
		try {
			switch(event.getType()) {
			case ServiceEvent.REGISTERED:
				// A feed has been added.  Send the complete set of feeds to all member swarms.
			case ServiceEvent.UNREGISTERING:
				// A feed has been removed.  Send the complete set of feeds to all member swarms.		
				
				// A lock is used to only send one event every LOCAL_CHANGE_DELAY_MILLIS to the server.  A TimerTask is used to update
				// the swarm peers after the interval, and only one task is created within LOCAL_CHANGE_DELAY_MILLIS.
				synchronized (localEventUpdate) {
					if (!localEventUpdate && timer != null) {
						localEventUpdate = true;
					
						timer.schedule(new TimerTask() {
							
							@Override
							public void run() {
								try {
									String capabilities = getCapabilities();
									
									for (SwarmModel swarm : memberSwarms) 	
										xmppClient.sendPublicMessage(swarm.getId(), capabilities);		
								} catch (Exception e) {
									Activator.getLog().log(LogService.LOG_ERROR, "Error occurred while sending capabilities to member swarms.", e);
								} finally {
									localEventUpdate = false;
								}
							}
						}, LOCAL_CHANGE_DELAY_MILLIS);
					}
				}						
				
				break;	
			case ServiceEvent.MODIFIED:
				try {
					Feed feed = Feed.createForType(event.getServiceReference());
					
					if (feed != null) {							
						String message = mapper.writeValueAsString(feed);
						
						for (SwarmModel swarm : memberSwarms) 	
							xmppClient.sendPublicMessage(swarm.getId(), message);		
					}
				} catch (Exception e) {
					Activator.getLog().log(LogService.LOG_ERROR, "Error occurred while sending feed update to member swarms.", e);
				} finally {
					localEventUpdate = false;
				}
				break;
			default:
			}			
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending updated device state to swarm server.", e);
		}
	}

	@Override
	public void addMemberSwarm(String swarmId) {
		boolean member = false;
		for (SwarmModel sm : memberSwarms)
			if (sm.getId().equals(swarmId)) {
				member = true;
				break;
			}
		
		if (!member) {
			try {
				memberSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(config.getResource());
			} catch (IOException e) {
				log.log(LogService.LOG_ERROR, "Error occurred while updating member swarms.", e);
			}
		}
	}
}
