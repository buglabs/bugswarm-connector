package com.buglabs.bug.swarm.connector;

import java.util.List;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper.EntityChangeListener;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper.EntityChangeType;
import com.buglabs.bug.swarm.connector.ws.IMembersClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.SwarmMemberModel;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;
import com.buglabs.bug.swarm.connector.xmpp.XmlMessageCreator;
import com.buglabs.util.XmlNode;

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

	public BUGSwarmConnector(Configuration config) {
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
			
			//Load data about server configuration and local configuration.
			List<SwarmModel> allSwarms = wsClient.getMembers().getSwarmsByMember(config.getUsername());
			XmlNode document = XmlMessageCreator.createServiceModuleFeedDocument(
					osgiHelper.getBUGServices(), 
					osgiHelper.getBUGModules(), 
					osgiHelper.getBUGFeeds());
			
			//Listen for invites from swarms
			MultiUserChat.addInvitationListener(xmppClient.getConnection(), new SwarmInvitationListener());
			
			//Notify all swarms of presence.
			for (SwarmModel swarm: allSwarms)
				 xmppClient.joinSwarm(swarm.getId());
			
			//Notify all consumer-members of swarms of services, feeds, and modules.
			for (SwarmModel swarm: allSwarms) 
				for (SwarmMemberModel member: swarm.getMembers())
					if (member.getType() == MemberType.CONSUMER &&  xmppClient.isPresent(swarm.getId(), member.getUserId()))
						xmppClient.advertise(
								swarm.getId(), 
								member.getUserId(), 
								document);
			
			osgiHelper.addListener(this);
			
		} catch (Exception e) {
			e.printStackTrace();
			//TODO handle errors
		}
	}

	/**
	 * Initialize the connection to the swarm server.
	 * @throws Exception 
	 */
	public boolean initialize() throws Exception {
		wsClient = new SwarmWSClient(config.getHostname(), config.getApi_key());
		if (wsClient.isValid()) {
			xmppClient = new SwarmXMPPClient(config);
			xmppClient.connect();
			
			osgiHelper = OSGiHelper.getRef();
			if (osgiHelper != null) {
				initialized = true;
				return true;
			}
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
		public void invitationReceived(Connection conn, String room, String inviter, String reason, String password, Message message) {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public void change(int eventType, EntityChangeType type, Object source) {
		// TODO Auto-generated method stub
		
	}
}
