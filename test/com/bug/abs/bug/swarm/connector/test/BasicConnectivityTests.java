package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;

import org.jivesoftware.smackx.muc.MultiUserChat;

import com.buglabs.bug.swarm.connector.ws.ISwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;

import junit.framework.TestCase;

/**
 * See RMI http://redmine/issues/2312
 * 
 * @author kgilmer
 *
 */
public class BasicConnectivityTests extends TestCase {

	public static final String API_KEY = "a0fc6588f11db4a1f024445e950ae6ae33bc0313";
	public static final String SWARM_HOST = "http://api.bugswarm.net";
	
	/**
	 * Connect to a swarm server.
	 * 
	 * See https://github.com/buglabs/bugswarm/wiki/Advertise-Member-Capabilities
	 * @throws IOException 
	 */
	public void testConnectToSwarmServer() throws IOException {
		
		// ------- Steps 1, 2 - Authenticate w/ server.
		ISwarmWSClient wsClient = new SwarmWSClient(SWARM_HOST, API_KEY);
		
		assertTrue(wsClient.isValid());
		
		// ------- Step 3 - Send presence
		
		 //MultiUserChat muc2 = new MultiUserChat(getConnection(1), room);
		
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
