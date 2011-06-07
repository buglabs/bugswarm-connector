package com.buglabs.bug.swarm.connector;

import java.io.IOException;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.simple.JSONArray;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.osgi.Activator;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper.EntityChangeListener;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmResourceModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.xmpp.ISwarmServerRequestListener;
import com.buglabs.bug.swarm.connector.xmpp.JSONElementCreator;
import com.buglabs.bug.swarm.connector.xmpp.Jid;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;

/**
 * The swarm connector client for BUGswarm system.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnector extends Thread implements EntityChangeListener, ISwarmServerRequestListener, InvitationListener {

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

	/**
	 * @param config Predefined configuration
	 */
	public BUGSwarmConnector(final Configuration config) {
		this.config = config;
		if (!config.isValid())
			throw new IllegalArgumentException("Invalid configuration");
	}

	@Override
	public void run() {
		try {
			// Initialize the clients used to communicate with swarm server
			if (!initialized)
				initialize();
			
			//Listen for invites from swarms
			Activator.getLog().log(LogService.LOG_DEBUG, "Registering to receive invites.");
			MultiUserChat.addInvitationListener(xmppClient.getConnection(), this);
			
			//Load data about server configuration and local configuration.
			Activator.getLog().log(LogService.LOG_DEBUG, "Getting member swarms.");
			List<SwarmModel> allSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(config.getResource());
			
			//Notify all swarms of presence.
			for (SwarmModel swarm : allSwarms) {
				Activator.getLog().log(LogService.LOG_DEBUG, "Joining swarm " + swarm.getId());
				xmppClient.joinSwarm(swarm.getId(), this);
			}
			
			//Send feed state to other swarm peers
			Activator.getLog().log(LogService.LOG_DEBUG, "Announcing local state to member swarms.");
			announceState(allSwarms);
			
			//Listen for local changes
			osgiHelper.addListener(this);
			
		} catch (Exception e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Error occurred while initializing swarm client.", e);
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
	 * @param allSwarms list of swarms to announce state to
	 * @throws XMPPException on XMPP error
	 */
	private void announceState(final List<SwarmModel> allSwarms) throws XMPPException {
		JSONArray document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds());
		
		//Notify all consumer-members of swarms of services, feeds, and modules.
		for (SwarmModel swarm : allSwarms) {
			Activator.getLog().log(LogService.LOG_DEBUG, "Announcing state " + document + " to swarm " + swarm.getId());
			xmppClient.announce(swarm.getId(), document);
		}
	}

	/**
	 * Initialize the connection to the swarm server.
	 * @return true if initialization successful
	 * @throws Exception 
	 */
	private boolean initialize() throws Exception {
		Activator.getLog().log(LogService.LOG_DEBUG, "Initializing " + BUGSwarmConnector.class.getSimpleName());
		wsClient = new SwarmWSClient(config.getHostname(Protocol.HTTP), config.getAPIKey());
		Throwable error = wsClient.isValid();
		if (error == null) {
			xmppClient = new SwarmXMPPClient(config);
			xmppClient.connect();
			
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
	
	@Override
	public void invitationReceived(final Connection conn, 
			final String room, final String inviter, final String reason, 
			final String password, final Message message) {
		
		Activator.getLog().log(LogService.LOG_INFO, "Recieved invitation for room " + room
				+ " from " + inviter + " for reason " + reason + " w message " + message);
		
		// TODO Implement this case
		// FIXME: Assuming the message content is the swarm to be joined.
		try {
			xmppClient.joinSwarm(message.getBody(), BUGSwarmConnector.this);
		} catch (Exception e) {
			Activator.getLog().log(LogService.LOG_ERROR, 
					"Error occurred while responding to invite from swarm server.", e);
		}
	}

	@Override
	public void change(final int eventType, final Object source) {
		//For now, every time a service, module, or feed changes locally, send the entire state to each interested party.
		//In the future it may be better to cache and determine delta and send only that.
		
		Activator.getLog().log(LogService.LOG_DEBUG, "Local feed notification.");
		try {
			//Load data about server configuration and local configuration.
			List<SwarmModel> allSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(config.getResource());
			
			broadcastState(allSwarms);
		} catch (Exception e) {
			Activator.getLog().log(LogService.LOG_ERROR, 
					"Error occurred while sending updated device state to swarm server.", e);
		}
	}

	/**
	 * Shutdown the connector and free any local and remote resources in use.
	 */
	public void shutdown() {
		//Stop listening to local events
		osgiHelper.removeListener(this);
		
		//Stop listening for new invitations from server
		MultiUserChat.removeInvitationListener(xmppClient.getConnection(), this);
		
		//Send unpresence and disconnect from server
		xmppClient.disconnect();
	}

	@Override
	public void feedListRequest(final Jid requestJid, final String swarmId) {
		JSONArray document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds());
		
		try {
			xmppClient.sendAllFeedsToUser(requestJid, swarmId, document);
		} catch (XMPPException e) {
			Activator.getLog().log(LogService.LOG_ERROR, 
					"Error occurred while sending feeds to " + requestJid, e);
		}
	}

	@Override
	public void feedListRequest(final Chat chat, final String swarmId) {
		JSONArray document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds());
		
		try {
			chat.sendMessage(document.toJSONString());
		} catch (XMPPException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Failed to send private message to " + chat.getParticipant(), e);
		}
		
		Activator.getLog().log(LogService.LOG_DEBUG, "Sent " + document.toJSONString() + " to " + chat.getParticipant());
	}
}
