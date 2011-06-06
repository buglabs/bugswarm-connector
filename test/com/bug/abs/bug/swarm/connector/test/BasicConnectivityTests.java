package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.simple.JSONArray;

import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.ISwarmClient;
import com.buglabs.bug.swarm.connector.ws.SwarmResourceModel;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;
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
	private SwarmWSClient wsClient;
	private SwarmXMPPClient xmppClient;

	public void testConnectWSClient() {
		wsClient = new SwarmWSClient(AccountConfig.getConfiguration().getHostname(Protocol.HTTP), AccountConfig.getConfiguration()
				.getAPIKey());

		assertTrue(wsClient.isValid() == null);
	}

	public void testConnectXMPPClient() throws IOException, XMPPException {
		if (xmppClient != null && xmppClient.isConnected())
			xmppClient.disconnect();
		
		xmppClient = new SwarmXMPPClient(AccountConfig.getXmppConfiguration());
		xmppClient.connect();

		assertTrue(xmppClient.isConnected());
		assertTrue(xmppClient.getConnection() != null);
		assertTrue(xmppClient.getResource() != null);
		assertTrue(xmppClient.getHostname() != null);
		assertTrue(xmppClient.getUsername() != null);
	}

	/**
	 * @throws Exception
	 * 
	 */
	public void testConnectToSwarmServer() throws Exception {
		testConnectWSClient();
		testConnectXMPPClient();
	}

	/**
	 * Join a pre-existing swarm
	 * 
	 * @throws Exception
	 */
	public void testJoinMemberSwarms() throws Exception {
		testConnectToSwarmServer();

		List<SwarmModel> allSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(AccountConfig.getConfiguration().getResource());

		List<SwarmModel> connectedSwarms = new ArrayList<SwarmModel>();
		for (SwarmModel swarm : allSwarms) {
			try {
				xmppClient.joinSwarm(swarm.getId());
				connectedSwarms.add(swarm);
				System.out.println("Joined swarm: " + swarm.getId());
			} catch (XMPPException e) {
				// Some swarm joins are failing, not sure yet if this is valid
				// or invalid. For now assume it's
				// ok for some joins to fail and proceed with test.
				// TODO: confirm with Camilo this is alright.

				System.err.println("Failed to join swarm " + swarm.getName() + ": " + e.getMessage());
			}
		}

		assertTrue(connectedSwarms.size() > 0);

		/*
		 * 4. Advertise device capabilities (services and feeds) only to
		 * consumer members that have
		 */

		OSGiHelper osgi = OSGiHelper.getRef();

		for (SwarmModel swarm : connectedSwarms) {
			JSONArray advertisement = JSONElementCreator.createFeedArray(osgi.getBUGFeeds());
			System.out.println("Announcing " + advertisement + " to swarm: " + swarm.getId());
			xmppClient.announce(swarm.getId(), advertisement);
		}

		// At this point we should be connected to the swarm server, verify.
		for (SwarmModel swarm : connectedSwarms) {
			SwarmModel sd = wsClient.get(swarm.getId());

			// Check to see that I am a member of all swarms that I should be
			boolean iAmAMember = false;
			for (SwarmResourceModel member : sd.getMembers()) {
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
	 * Create and join a new swarm.
	 * 
	 * See https://github.com/buglabs/bugswarm/wiki/Swarms-API
	 * @throws Exception 
	 */
	public void testJoinNewSwarm() throws Exception {
		// Create a new swarm w ws api
		testConnectToSwarmServer();
		assertNotNull(wsClient);
		assertNotNull(xmppClient);
		
		String id = wsClient.create(AccountConfig.generateRandomSwarmName(), true, "A test swarm");
		assertNotNull(id);
		assertTrue(id.length() > 0);
		
		// Add my xmpp client as a member
		ISwarmResourcesClient resClient = wsClient.getSwarmResourceClient();
		assertNotNull(resClient);
		SwarmWSResponse response = resClient.add(id, ISwarmResourcesClient.MemberType.PRODUCER, xmppClient.getUsername() , xmppClient.getResource());
		assertFalse(response.isError());
		
		// Create the 'web' resource, this will be used later w/ feed api
		response = resClient.add(id, ISwarmResourcesClient.MemberType.CONSUMER, xmppClient.getUsername() , "web");
		assertFalse(response.isError());
		
		// Confirm swarm has two members
		SwarmModel swarmInfo = wsClient.get(id);
		assertTrue(swarmInfo.getMembers().size() == 2);
		
		// Join the swarm w/ the xmpp client
		xmppClient.joinSwarm(id);
		assertTrue(xmppClient.isPresent(id, xmppClient.getUsername()));
		
		xmppClient.disconnect();
		xmppClient = null;
		
		wsClient = null;
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
	 * Respond to swarm server request for binary content with HTTP POST of
	 * requested data.
	 */
	public void testRespondToXMPPBinaryChatMessage() {

	}

	/**
	 * Respond to a swarm server message over chat for content.
	 */
	public void testRespondToXMPPMUCChatMessage() {

	}
}
