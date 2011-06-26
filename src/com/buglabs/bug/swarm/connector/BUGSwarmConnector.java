package com.buglabs.bug.swarm.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.osgi.Activator;
import com.buglabs.bug.swarm.connector.osgi.Feed;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper.EntityChangeListener;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmResourceModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.xmpp.GroupChatMessageRequestHandler;
import com.buglabs.bug.swarm.connector.xmpp.ISwarmServerRequestListener;
import com.buglabs.bug.swarm.connector.xmpp.JSONElementCreator;
import com.buglabs.bug.swarm.connector.xmpp.Jid;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;
import com.buglabs.util.simplerestclient.HTTPException;

/**
 * The swarm connector client for BUGswarm system.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnector extends Thread implements EntityChangeListener, ISwarmServerRequestListener {

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
	private SwarmXMPPClient xmppClient;
	private OSGiHelper osgiHelper;
	private List<SwarmModel> memberSwarms;
	private LogService log;

	/**
	 * @param config Predefined configuration
	 */
	public BUGSwarmConnector(final Configuration config) {
		this.config = config;
		this.log = Activator.getLog();
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
			
			//Load data about server configuration and local configuration.
			log.log(LogService.LOG_DEBUG, "Getting member swarms.");
			
			try {
				List<SwarmModel> allSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(config.getResource());
				
				//Notify all swarms of presence.
				for (SwarmModel swarm : allSwarms) {
					log.log(LogService.LOG_DEBUG, "Joining swarm " + swarm.getId());
					xmppClient.joinSwarm(swarm.getId(), this);
					memberSwarms.add(swarm);
				}
			} catch (HTTPException e) {
				if (e.getErrorCode() == 404)
					log.log(LogService.LOG_WARNING, "Not a member of any swarms, not publishing feeds.");
				else 
					throw e;
			}
			
			//Send feed state to other swarm peers
			//This is disabled as it's only used by the Web UI which is not currently available.
			/*log.log(LogService.LOG_DEBUG, "Announcing local state to member swarms.");
			announceState(allSwarms);*/
			
			//Listen for local changes
			osgiHelper.addListener(this);
			
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Error occurred while initializing swarm client.", e);
		}
	}

	/**
	 * Send the state of this device to all interested swarm members.
	 * 
	 * @param allSwarms list of SwarmModel to send state to
	 * @throws XMPPException upon XMPP failure
	 */
	private void broadcastState(final List<SwarmModel> allSwarms) throws XMPPException {
		JSONArray document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds());
		
		//Notify all consumer-members of swarms of services, feeds, and modules.
		for (SwarmModel swarm : allSwarms) 
			for (SwarmResourceModel member : swarm.getMembers())
				if (member.getType() == MemberType.CONSUMER &&  xmppClient.isPresent(swarm.getId(), member.getUserId()))
					xmppClient.advertise(
							swarm.getId(), 
							member.getUserId(), 
							document);
	}
	
	/**
	 * Initialize the connection to the swarm server.
	 * @return true if initialization successful
	 * @throws Exception 
	 */
	private boolean initialize() throws Exception {
		log.log(LogService.LOG_DEBUG, "Initializing " + BUGSwarmConnector.class.getSimpleName());
		wsClient = new SwarmWSClient(config.getHostname(Protocol.HTTP), config.getAPIKey());
		Throwable error = wsClient.isValid();
		if (error == null) {
			xmppClient = new SwarmXMPPClient(config);
			xmppClient.connect(this);
			
			osgiHelper = OSGiHelper.getRef();
			if (osgiHelper != null) {
				initialized = true;
				return true;
			}
		} else {
			throw new IOException(error);
		}
		
		return false;
	}
	
	/**
	 * @return Immutable list of swarms that client is a member of, for read-only purposes.
	 */
	public List<SwarmModel> getMemberSwarms() {
		return Collections.unmodifiableList(memberSwarms);
	}

	@Override
	public void change(final int eventType, final Object source) {
		//For now, every time a service, module, or feed changes locally, send the entire state to each interested party.
		//In the future it may be better to cache and determine delta and send only that.
		
		log.log(LogService.LOG_DEBUG, "Local feed notification.");
		try {
			//Load data about server configuration and local configuration.
			List<SwarmModel> allSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(config.getResource());
			
			broadcastState(allSwarms);
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, 
					"Error occurred while sending updated device state to swarm server.", e);
		}
	}

	/**
	 * Shutdown the connector and free any local and remote resources in use.
	 */
	public void shutdown() {
		//Stop listening to local events
		if (osgiHelper != null)
			osgiHelper.removeListener(this);
		
		if (xmppClient != null) {		
			//Send unpresence and disconnect from server
			xmppClient.disconnect();
		}
	}

	@Override
	public void feedListRequest(final Jid requestJid, final String swarmId) {
		JSONArray document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds());
		
		try {
			xmppClient.sendAllFeedsToUser(requestJid, swarmId, document);
		} catch (XMPPException e) {
			log.log(LogService.LOG_ERROR, 
					"Error occurred while sending feeds to " + requestJid, e);
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
	public void feedRequest(Jid jid, String swarmId, String feedRequestName) {
		Feed f = osgiHelper.getBUGFeed(feedRequestName);
		if (f == null) {
			f = osgiHelper.getBUGFeed(feedRequestName);
			log.log(LogService.LOG_ERROR, 
					"Request for non-existant feed " + feedRequestName + " from client " + jid);
			return;
		}
			
		JSONObject document = JSONElementCreator.createFeedElement(f);
		
		try {
			xmppClient.sendFeedToUser(jid, swarmId, document);
		} catch (XMPPException e) {
			log.log(LogService.LOG_ERROR, 
					"Error occurred while sending feeds to " + jid, e);
		}
	}

	@Override
	public void swarmInviteRequest(Jid sender, String roomId) {
		log.log(LogService.LOG_INFO, "Recieved invitation for room " + roomId
				+ " from " + sender.toString());
		
		// TODO Implement this case
		// FIXME: Assuming the message content is the swarm to be joined.
		try {
			xmppClient.joinSwarm(roomId, BUGSwarmConnector.this);
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, 
					"Error occurred while responding to invite from swarm server.", e);
		}
	}
}
