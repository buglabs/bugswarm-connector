package com.bug.abs.bug.swarm.connector.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.ws.IMembersClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.ISwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmMemberModel;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;
import com.buglabs.bug.swarm.connector.xmpp.XmlMessageCreator;

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
		
		ISwarmWSClient wsClient = new SwarmWSClient(TestUtil.getConfiguration().getHostname(), TestUtil.getConfiguration().getAPIKey());
		
		assertTrue(wsClient.isValid() == null);
		List<SwarmModel> allSwarms = wsClient.getMembers().getSwarmsByMember(TestUtil.getConfiguration().getResource());
		
        /*
		 * 3. Join to swarms returned by step 1. (xmpp)
		 */		
		SwarmXMPPClient xmppClient = new SwarmXMPPClient(TestUtil.getXmppConfiguration());
		xmppClient.connect();
		
		assertTrue(xmppClient.isConnected());
		assertTrue(xmppClient.getConnection() != null);
		
		List<SwarmModel> connectedSwarms = new ArrayList<SwarmModel>();
		for (SwarmModel swarm: allSwarms) {
			try {
				 xmppClient.joinSwarm(swarm.getId());
				 connectedSwarms.add(swarm);
			} catch (XMPPException e) {
				System.err.println("Failed to join swarm " + swarm.getName() + ": " + e.getMessage());
			}
		}
		
		assertTrue(connectedSwarms.size() > 0);
		
        /* 
		 * 4. Advertise device capabilities (services and feeds) only to consumer members that have
		 */
		
		OSGiHelper osgi = OSGiHelper.getRef();
		
		for (SwarmModel swarm: connectedSwarms) 
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
