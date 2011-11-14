package com.buglabs.bug.swarm.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.osgi.framework.ServiceEvent;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;
import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.model.FeedRequest;
import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.Activator;
import com.buglabs.bug.swarm.connector.osgi.BinaryFeed;
import com.buglabs.bug.swarm.connector.osgi.Feed;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper.OSGiServiceEventListener;
import com.buglabs.bug.swarm.connector.xmpp.ISwarmServerRequestListener;
import com.buglabs.bug.swarm.connector.xmpp.JSONElementCreator;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;
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
public class BUGSwarmConnector extends Thread implements OSGiServiceEventListener, ISwarmServerRequestListener {

	/**
	 * Used to convert seconds to milliseconds.
	 */
	private static final long MILLIS_IN_SECONDS = 1000;
	
	/**
	 * HTTP 404 response.
	 */
	private static final int HTTP_404 = 404;
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
	 * Instance of OSGiHelper.
	 */
	private OSGiHelper osgiHelper;
	/**
	 * List of all member swarms.
	 */
	private final List<SwarmModel> memberSwarms;
	/**
	 * Timer that manages all the active streaming feeds.
	 */
	private Timer timer;
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

	/**
	 * @param config
	 *            Predefined configuration
	 */
	public BUGSwarmConnector(final Configuration config) {
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
					// Notify all swarms of presence.
					for (SwarmModel swarm : allSwarms) {
						log.log(LogService.LOG_DEBUG, "Joining swarm " + swarm.getId());
						xmppClient.joinSwarm(swarm.getId(), this);
						memberSwarms.add(swarm);
					}
					
					// Send feed state to other swarm peers
					// This is disabled as it's only used by the Web UI which is not
					// currently available.
					
					log.log(LogService.LOG_DEBUG, "Announcing local state to member swarms.");
					announceState(allSwarms, null);					
				}
			} catch (HTTPException e) {
				if (e.getErrorCode() == HTTP_404)
					log.log(LogService.LOG_WARNING, "Not a member of any swarms, not publishing feeds.");
				else
					throw e;
			}

			// Listen for local changes
			osgiHelper.setListener(this);
			log.log(LogService.LOG_INFO, "Connector initialization complete.");

		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Error occurred while initializing swarm client.", e);
		}
	}
	
	/**
	 * Send feed information as public message to MUC rooms for all member swarms.
	 * 
	 * @param allSwarms list of swarms to announce to
	 * @param source specific feed to announce or null to announce all feeds.
	 * @throws XMPPException thrown on XMPP error
	 */
	private void announceState(final List<SwarmModel> allSwarms, String message) throws XMPPException {
		/*String document = null;
		if (source == null)
			document = JSONElementCreator.createCapabilitiesJson(osgiHelper.getBUGFeeds());
		else 
			document = JSONElementCreator.createFeedElement(source);*/
		
		//Notify all consumer-members of swarms of services, feeds, and modules.
		for (SwarmModel swarm : allSwarms) {			
			xmppClient.announce(swarm.getId(), message);
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

		osgiHelper = OSGiHelper.getRef();
		
		UserResourceModel resource = null;
		if (config.hasResource()) {
			Activator.getLog().log(LogService.LOG_DEBUG, "Using stored resource id: " + config.getResource());
			resource = wsClient.getUserResourceClient().get(
					config.getResource());			
		} else {
			Activator.getLog().log(LogService.LOG_DEBUG, "Creating a new resource on the server.");
			resource = wsClient.getUserResourceClient().add(
					"bug-" + System.currentTimeMillis(), 
					"BUG device", 
					"bug", 
					0, 0);
			config.setResourceId(resource.getResourceId());
			//Save resourceId in CA
			osgiHelper.setResourceId(resource.getResourceId());
			Activator.getLog().log(LogService.LOG_DEBUG, "New resource id: " + config.getResource());
		}
		
		if (resource == null) {
			//Set the persisted resourceid to null in the case of an invalid or deleted resource.  This will cause the connector to ask the server for a new one upon next start.
			osgiHelper.setResourceId(null);
			throw new IOException("Unable to get or create resource for device.");
		}
		
		xmppClient = new SwarmXMPPClient(config);
		xmppClient.connect(this);
						
		initialized = true;
		return true;		
	}

	/**
	 * @return Immutable list of swarms that client is a member of, for
	 *         read-only purposes.
	 */
	public List<SwarmModel> getMemberSwarms() {
		return Collections.unmodifiableList(memberSwarms);
	}

	@Override
	public void serviceEvent(final int eventType, final Object source) {
		// For now, every time a service, module, or feed changes locally, send
		// the entire state to each interested party.
		// In the future it may be better to cache and determine delta and send
		// only that.
		
		try {
			switch(eventType) {
			case ServiceEvent.REGISTERED:
			case ServiceEvent.MODIFIED:
				// A feed has changed.  Send the complete set of feeds to all member swarms.
				List<SwarmModel> swarms = wsClient.getSwarmResourceClient().getSwarmsByMember(config.getResource());
				String capabilities = JSONElementCreator.createCapabilitiesJson(osgiHelper.getBUGFeeds());
				
				for (SwarmModel swarm : swarms) 	
					xmppClient.announce(swarm.getId(), capabilities);				
				
				break;
			case ServiceEvent.UNREGISTERING:
				announceState(
						wsClient.getSwarmResourceClient().getSwarmsByMember(
								config.getResource()), null);
			default:
			}			
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending updated device state to swarm server.", e);
		}
		
	}

	/**
	 * Shutdown the connector and free any local and remote resources in use.
	 */
	public void shutdown() {
		if (timer != null) {
			timer.cancel();
		}
				
		// Stop listening to local service events
		if (osgiHelper != null) {
			osgiHelper.setListener(null);
			osgiHelper.shutdown();
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
			String document = JSONElementCreator.createCapabilitiesJson(osgiHelper.getBUGFeeds());
		
			xmppClient.sendAllFeedsToUser(requestJid, swarmId, document);
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending feeds to " + requestJid, e);
		} 
	}

	@Override
	public void feedListRequest(final Chat chat, final String swarmId) {
		try {
			String document = JSONElementCreator.createCapabilitiesJson(osgiHelper.getBUGFeeds());

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
		Feed feed = osgiHelper.getBUGFeed(feedRequest.getName());
		if (feed == null) {
			feed = osgiHelper.getBUGFeed(feedRequest.getName());
			log.log(LogService.LOG_WARNING, "Request for non-existant feed " + feedRequest.getName() + " from client " + jid);
			return;
		}
	
		if (timer == null) 
			timer = new Timer();
		
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
		
		//Cancel any tasks associated within a specific swarm to a specific resource.
		for (String taskKey : activeTasks.keySet()) {
			if (taskKey.contains(jid.getResource()) && taskKey.contains(jid.getUsername()))
				activeTasks.get(taskKey).cancel();		
		}
	}
}
