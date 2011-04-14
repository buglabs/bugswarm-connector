package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.buglabs.bug.swarm.connector.ws.ISwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;

import junit.framework.TestCase;

/**
 * See RMI http://redmine/issues/2312
 * 
 * @author kgilmer
 *
 */
public class BasicConnectivityTests extends TestCase {

	public static final String API_KEY = "a0fc6588f11db4a1f024445e950ae6ae33bc0313";
	public static final String XMPP_USERNAME = "test";
	public static final String SWARM_XMPP_HOST = "api.bugswarm.net";
	public static final String SWARM_WS_HOST = "http://api.bugswarm.net";
	protected MultiUserChat swarmRoom;
	protected ChatManager chatManager;
	
	/**
	 * Connect to a swarm server.
	 * 
	 * See https://github.com/buglabs/bugswarm/wiki/Advertise-Member-Capabilities
	 * @throws IOException 
	 * @throws XMPPException 
	 */
	public void testConnectToSwarmServer() throws IOException, XMPPException {
		
		// ------- Steps 1, 2 - Authenticate w/ server.
		ISwarmWSClient wsClient = new SwarmWSClient(SWARM_WS_HOST, API_KEY);
		
		assertTrue(wsClient.isValid());
		
		// ------- Step 3 - Send presence
		
		SwarmXMPPClient xmppClient = new SwarmXMPPClient(SwarmXMPPClient.createConfiguration(SWARM_XMPP_HOST, XMPP_USERNAME, API_KEY));
		xmppClient.connect();
		
		assertTrue(xmppClient.isConnected());
		assertTrue(xmppClient.getConnection() != null);
		
		final Lock lock = new ReentrantLock();
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
								
								lock.unlock();
							}
						});
					}
				});
				
				swarmRoom = new MultiUserChat(conn, room);
			}
		});
		
		lock.lock();
		//his 2nd call 2 lock will block until unlock is called once service, module, or feed data is sent from another user.
		lock.lock();
		
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
