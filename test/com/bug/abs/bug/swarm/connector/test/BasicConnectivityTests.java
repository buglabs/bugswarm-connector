package com.bug.abs.bug.swarm.connector.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.simple.JSONArray;

import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.ISwarmClient;
import com.buglabs.bug.swarm.connector.ws.SwarmResourceModel;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;
import com.buglabs.bug.swarm.connector.xmpp.JSONElementCreator;

/**
 * See RMI http://redmine/issues/2312
 * 
 * @author kgilmer
 *
 */
public class BasicConnectivityTests extends TestCase {

	
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
		
		ISwarmClient wsClient = new SwarmWSClient(AccountConfig.getConfiguration().getHostname(Protocol.HTTP), AccountConfig.getConfiguration().getAPIKey());
		
		assertTrue(wsClient.isValid() == null);
		List<SwarmModel> allSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(AccountConfig.getConfiguration().getResource());
		
        /*
		 * 3. Join to swarms returned by step 1. (xmpp)
		 */		
		SwarmXMPPClient xmppClient = new SwarmXMPPClient(AccountConfig.getXmppConfiguration());
		xmppClient.connect();
		
		assertTrue(xmppClient.isConnected());
		assertTrue(xmppClient.getConnection() != null);
		
		List<SwarmModel> connectedSwarms = new ArrayList<SwarmModel>();
		for (SwarmModel swarm: allSwarms) {
			try {
				 xmppClient.joinSwarm(swarm.getId());
				 connectedSwarms.add(swarm);
				 System.out.println("Joined swarm: " + swarm.getId());
			} catch (XMPPException e) {
				//Some swarm joins are failing, not sure yet if this is valid or invalid.  For now assume it's
				//ok for some joins to fail and proceed with test.
				//TODO: confirm with Camilo this is alright.
				
				System.err.println("Failed to join swarm " + swarm.getName() + ": " + e.getMessage());
			}
		}
		
		assertTrue(connectedSwarms.size() > 0);
		
        /* 
		 * 4. Advertise device capabilities (services and feeds) only to consumer members that have
		 */
		
		OSGiHelper osgi = OSGiHelper.getRef();
		
		for (SwarmModel swarm: connectedSwarms) 
			for (SwarmResourceModel member: swarm.getMembers()) {
				System.out.println("Looking at member " + member.getUserId() + " in swarm: " + swarm.getId());
				
				if (member.getType() == MemberType.CONSUMER &&  xmppClient.isPresent(swarm.getId(), member.getUserId())) {
					JSONArray advertisement = JSONElementCreator.createFeedArray(osgi.getBUGFeeds());
					System.out.println("Sending advertisement " + advertisement + " to member " + member.getUserId() + " in swarm: " + swarm.getId());
					
					xmppClient.advertise(
							swarm.getId(), 
							member.getUserId(), 
							advertisement);
				}
			}
		
		//At this point we should be connected to the swarm server, verify.
		for (SwarmModel swarm : connectedSwarms) {
			SwarmModel sd = wsClient.get(swarm.getId());
			
			//Check to see that I am a member of all swarms that I should be
			boolean iAmAMember = false;
			for (SwarmResourceModel member: sd.getMembers()) {
				System.out.println("Checking if  " + member.getUserId() + " belongs to " + swarm.getId());
				
				if (member.getUserId().equals(AccountConfig.getConfiguration().getUsername())) {
						iAmAMember = true;
						System.out.println("Confirmed that " + member.getUserId() + " belongs to " + swarm.getId());
				}
			}
				
			assertTrue(iAmAMember);
		}
			
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
