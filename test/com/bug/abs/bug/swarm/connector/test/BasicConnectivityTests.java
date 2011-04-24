package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.ws.ISwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmMemberModel;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.IMembersClient.MemberType;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;
import com.buglabs.bug.swarm.connector.xmpp.XmlMessageCreator;

/**
 * See RMI http://redmine/issues/2312
 * 
 * @author kgilmer
 *
 */
public class BasicConnectivityTests extends TestCase {

	public static final String API_KEY = "a0fc6588f11db4a1f024445e950ae6ae33bc0313";
	public static final String XMPP_USERNAME = "bugtest";
	public static final String SWARM_XMPP_HOST = "xmpp.bugswarm.net";
	public static final String SWARM_WS_HOST = "http://api.bugswarm.net";
	protected MultiUserChat swarmRoom;
	protected ChatManager chatManager;
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void testConnectToSwarmServer() throws Exception {
		/*
		 * 1. Get the list of swarms where the device is a member. (through the Rest API)
		 * 2. Get the list of consumer members of every swarm returned by step 1. (through the Rest API)
		 */
		
		ISwarmWSClient wsClient = new SwarmWSClient(SWARM_WS_HOST, API_KEY);
		
		assertTrue(wsClient.isValid());
		List<SwarmModel> allSwarms = wsClient.getMembers().getSwarmsByMember(XMPP_USERNAME);
		
        /*
		 * 3. Join to swarms returned by step 1. (xmpp)
		 */
		
		SwarmXMPPClient xmppClient = new SwarmXMPPClient(SwarmXMPPClient.createConfiguration(SWARM_XMPP_HOST, XMPP_USERNAME, XMPP_USERNAME));
		xmppClient.connect();
		
		assertTrue(xmppClient.isConnected());
		assertTrue(xmppClient.getConnection() != null);
		
		for (SwarmModel swarm: allSwarms)
			 xmppClient.joinSwarm(swarm.getId());
		
        /* 
		 * 4. Advertise device capabilities (services and feeds) only to consumer members that have
		 */
		
		OSGiHelper osgi = OSGiHelper.getRef();
		
		for (SwarmModel swarm: allSwarms) 
			for (SwarmMemberModel member: swarm.getMembers())
				if (member.getType() == MemberType.CONSUMER &&  xmppClient.isPresent(swarm.getId(), member.getUserId()))
					xmppClient.advertise(
							swarm.getId(), 
							member.getUserId(), 
							XmlMessageCreator.createServiceModuleFeedDocument(
									osgi.getBUGServices(), 
									osgi.getBUGModules(), 
									osgi.getBUGFeeds()));
			
	}
	
	/**
	 * Connect to a swarm server.
	 * 
	 * See https://github.com/buglabs/bugswarm/wiki/Advertise-Member-Capabilities
	 * @throws IOException 
	 * @throws XMPPException 
	 * @throws InterruptedException 
	 */
	public void notestConnectToSwarmServerOld() throws IOException, XMPPException, InterruptedException {
		
		// ------- Steps 1, 2 - Authenticate w/ server.
		ISwarmWSClient wsClient = new SwarmWSClient(SWARM_WS_HOST, API_KEY);
		
		assertTrue(wsClient.isValid());
		
		// ------- Step 3 - Send presence
		
		SwarmXMPPClient xmppClient = new SwarmXMPPClient(SwarmXMPPClient.createConfiguration(SWARM_XMPP_HOST, XMPP_USERNAME, XMPP_USERNAME));
		xmppClient.connect();
		
		assertTrue(xmppClient.isConnected());
		assertTrue(xmppClient.getConnection() != null);
		
		final Object lock = new Object();
		final List<String> responders = new ArrayList<String>();
		
		// ------- Step 4 - 5 - Accept invitations and respond by joining rooms
		// Register to listen to MUC invites
		MultiUserChat.addInvitationListener(xmppClient.getConnection(), new InvitationListener() {
			
			@Override
			public void invitationReceived(Connection conn, String room, String inviter, String reason, String password, Message message) {
				// ------ Step 6 - receive private messages from other swarm members.
				
				chatManager = conn.getChatManager();
				chatManager.addChatListener(new ChatManagerListener() {
					
					@Override
					public void chatCreated(Chat chat, boolean createdLocally) {
						chat.addMessageListener(new MessageListener() {
							
							@Override
							public void processMessage(Chat chat, Message message) {
								//For now just print out that we recieved a message.  A more formal test is next.
								System.out.println("Received message: " + message.getBody() + " from " + chat.getParticipant());
								if (!responders.contains(chat.getParticipant()))
									responders.add(chat.getParticipant());
								
								synchronized (lock) {
									notify();
								}
							}
						});
					}
				});
				
				swarmRoom = new MultiUserChat(conn, room);
			}
		});
		
		synchronized (lock) {
			lock.wait();
		}
		
		assertTrue(swarmRoom != null);
		assertTrue(swarmRoom.isJoined());
		
		//Test that we got some response from another swarm member.  This code will not be reached unless a response happens so this is not necessary but good to document.
		assertTrue(responders.size() > 0);
	}
	
	/**
	 * Join a pre-existing swarm
	 */
	public void testJoinAvailableSwarm() {
		
	}
	
	/**
	 * Create and join a new swarm.
	 * 
	 * See https://github.com/buglabs/bugswarm/wiki/Swarms-API
	 */
	public void testJoinNewSwarm() {
		
	}
	
	/**
	 * Respond to a swarm server admin message over chat for content.
	 */
	public void testRespondToXMPPadminChatMessage() {
		
	}
	
	/**
	 * Respond to MUC invitation request from swarm server.
	 */
	public void testRespondToXMPPMUCRoomInvitation() {
		
	}
	
	/**
	 * Respond to swarm server request for binary content with HTTP POST of requested data.
	 */
	public void testRespondToXMPPBinaryChatMessage() {
		
	}
	
	/**
	 * Respond to a swarm server message over chat for content.
	 */
	public void testRespondToXMPPMUCChatMessage() {
		
	}
}
