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
import org.json.simple.JSONArray;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.model.FeedRequest;
import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.model.ResourceModel;
import com.buglabs.bug.swarm.connector.model.SwarmModel;
import com.buglabs.bug.swarm.connector.model.SwarmResourceModel;
import com.buglabs.bug.swarm.connector.osgi.Activator;
import com.buglabs.bug.swarm.connector.osgi.BinaryFeed;
import com.buglabs.bug.swarm.connector.osgi.Feed;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper.EntityChangeListener;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;
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
public class BUGSwarmConnector extends Thread implements EntityChangeListener, ISwarmServerRequestListener {

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
	private SwarmWSClient wsClient;
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
	private List<SwarmModel> memberSwarms;
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
			osgiHelper.addListener(this);
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
	private void announceState(final List<SwarmModel> allSwarms, Feed source) throws XMPPException {
		String document = null;
		if (source == null)
			document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds()).toJSONString();
		else 
			document = JSONElementCreator.createFeedElement(source).toJSONString();
		
		//Notify all consumer-members of swarms of services, feeds, and modules.
		for (SwarmModel swarm : allSwarms) {
			Activator.getLog().log(LogService.LOG_DEBUG, "Announcing state " + document + " to swarm " + swarm.getId());
			xmppClient.announce(swarm.getId(), document);
		}
	}

	/**
	 * Send the state of this device to all interested swarm members as private messages.
	 * 
	 * @param allSwarms
	 *            list of SwarmModel to send state to
	 * @throws XMPPException
	 *             upon XMPP failure
	 */
	private void broadcastState(final List<SwarmModel> allSwarms) throws XMPPException {
		JSONArray document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds());

		// Notify all consumer-members of swarms of services, feeds, and
		// modules.
		for (SwarmModel swarm : allSwarms)
			for (SwarmResourceModel member : swarm.getMembers())
				if (member.getType() == MemberType.CONSUMER && xmppClient.isPresent(swarm.getId(), member.getUserId())) {
					xmppClient.advertise(swarm.getId(), 
							(new Jid(member.getUserId(), xmppClient.getHostname(), 
									member.getResource())).toString(), document);
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
		wsClient = new SwarmWSClient(config.getHostname(Protocol.HTTP), config.getAPIKey());
		Throwable error = wsClient.isValid();
		if (error == null) {
			xmppClient = new SwarmXMPPClient(config);
			xmppClient.connect(this);

			osgiHelper = OSGiHelper.getRef();
			if (osgiHelper == null) {
				throw new IOException("Unable to get an OSGi context.");				
			}			
			
			SwarmWSResponse response = null;
			
			//See if the resource already exists on the server.
			ResourceModel resource = wsClient.getResourceClient().get(xmppClient.getResource());
			
			//If it does exist, update the existing record, otherwise add.
			if (resource == null)
				response = wsClient.getResourceClient().add(
						xmppClient.getResource(), xmppClient.getUsername(), 
						"BUG-Connector-Device", "A connector-enabled BUG device", 
						MemberType.PRODUCER, "BUG");
			else
				response = wsClient.getResourceClient().update(
						xmppClient.getResource(),  
						"BUG-Connector-Device", "A connector-enabled BUG device", 
						MemberType.PRODUCER, "BUG");
			
			if (response.isError()) 
				log.log(LogService.LOG_WARNING, 
						"Server returned an error when adding device resource: " + response.getMessage());
			
			if (response.isError() && response.getCode() != 409)
				throw new IOException(response.getMessage());
			else if (response.isError() && response.getCode() == 409)
				log.log(LogService.LOG_WARNING, 
						"Ignoring error 409 on add/update resource.");
							
			initialized = true;
			return true;
		} else {
			throw new IOException(error);
		}
	}

	/**
	 * @return Immutable list of swarms that client is a member of, for
	 *         read-only purposes.
	 */
	public List<SwarmModel> getMemberSwarms() {
		return Collections.unmodifiableList(memberSwarms);
	}

	@Override
	public void change(final int eventType, final Object source) {
		// For now, every time a service, module, or feed changes locally, send
		// the entire state to each interested party.
		// In the future it may be better to cache and determine delta and send
		// only that.
		
		try {
			switch(eventType) {
			case ServiceEvent.REGISTERED:
			case ServiceEvent.MODIFIED:
				// Load data about server configuration and local configuration.
				announceState(
						wsClient.getSwarmResourceClient().getSwarmsByMember(
								config.getResource()), Feed.createForType(source));
				break;
			case ServiceEvent.UNREGISTERING:
				// TODO: determine if message is required when feed is no longer available.
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
		String resourceId = null;
		if (timer != null) {
			timer.cancel();
		}
		
		if (xmppClient != null)
			resourceId = xmppClient.getResource();
		// Stop listening to local events
		if (osgiHelper != null)
			osgiHelper.removeListener(this);

		if (xmppClient != null) {
			for (SwarmModel sm : memberSwarms)
				xmppClient.leaveSwarm(sm.getId());
			// Send unpresence and disconnect from server
			xmppClient.disconnect();
		}
		
		if (wsClient != null && resourceId != null) {
			try {
				wsClient.getResourceClient().remove(resourceId);
			} catch (IOException e) {
				// Ignore shutdown error.
			}
		}	
		
		log.log(LogService.LOG_INFO, "Connector shutdown complete.");
	}

	@Override
	public void feedListRequest(final Jid requestJid, final String swarmId) {
		JSONArray document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds());

		try {
			xmppClient.sendAllFeedsToUser(requestJid, swarmId, document);
		} catch (XMPPException e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending feeds to " + requestJid, e);
		}
	}

	@Override
	public void feedListRequest(final Chat chat, final String swarmId) {
		JSONArray document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds());

		try {
			chat.sendMessage(document.toJSONString());
		} catch (XMPPException e) {
			log.log(LogService.LOG_ERROR, "Failed to send private message to " + chat.getParticipant(), e);
		}

		log.log(LogService.LOG_DEBUG, "Sent " + document.toJSONString() + " to " + chat.getParticipant());
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
		
		if (timer == null) {
			timer = new Timer();
		}

		//TODO: there needs to be a way for the swarm server to notify of when 
		//a feed that is in streaming mode should be shutdown.
		//When this happens the TimerTask needs to be canceled and the entry in activeTasks removed.
		TimerTask task = null;
		
		if (feed instanceof BinaryFeed) {
			task = new BinaryFeedResponseTask(wsClient, jid, swarmId, (BinaryFeed) feed, log);
		} else {
			task = new FeedResponseTask(xmppClient, jid, swarmId, feed, log);
		}
		
		if (feedRequest.hasFrequency() && !containsActiveTask(jid, swarmId, feed)) {
			if (activeTasks == null) {
				activeTasks = new HashMap<String, TimerTask>();
			}
			
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
}
