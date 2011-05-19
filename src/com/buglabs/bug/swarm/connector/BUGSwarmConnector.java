package com.buglabs.bug.swarm.connector;

import java.io.IOException;
import java.util.List;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.simple.JSONArray;

import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper.EntityChangeListener;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmResourceModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.xmpp.JSONElementCreator;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;

/**
 * The swarm connector client for BUGswarm system.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnector extends Thread implements EntityChangeListener {

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
			MultiUserChat.addInvitationListener(xmppClient.getConnection(), new SwarmInvitationListener());
			
			//Load data about server configuration and local configuration.
			List<SwarmModel> allSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(config.getResource());
			
			//Notify all swarms of presence.
			for (SwarmModel swarm : allSwarms)
				 xmppClient.joinSwarm(swarm.getId());
			
			
			broadcastState(allSwarms);
			
			osgiHelper.addListener(this);
			
		} catch (Exception e) {
			e.printStackTrace();
			//TODO handle errors
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
	public boolean initialize() throws Exception {
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
	
	/**
	 * Class to handle PMs from other clients.
	 * @author kgilmer
	 *
	 */
	private class SwarmInvitationListener implements InvitationListener {

		@Override
		public void invitationReceived(final Connection conn, 
				final String room, final String inviter, final String reason, 
				final String password, final Message message) {
			// TODO Implement this case
			// FIXME: Assuming the message content is the swarm to be joined.
			try {
				xmppClient.joinSwarm(message.getBody());
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void change(final int eventType, final Object source) {
		//For now, every time a service, module, or feed changes locally, send the entire state to each interested party.
		//In the future it may be better to cache and determine delta and send only that.
		
		try {
			//Load data about server configuration and local configuration.
			List<SwarmModel> allSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(config.getResource());
			
			broadcastState(allSwarms);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
