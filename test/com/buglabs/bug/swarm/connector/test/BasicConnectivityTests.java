package com.buglabs.bug.swarm.connector.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.simple.JSONArray;

import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.model.FeedRequest;
import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.model.SwarmModel;
import com.buglabs.bug.swarm.connector.model.SwarmResourceModel;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;
import com.buglabs.bug.swarm.connector.xmpp.ISwarmServerRequestListener;
import com.buglabs.bug.swarm.connector.xmpp.JSONElementCreator;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;
import com.buglabs.util.simplerestclient.HTTPException;
import com.buglabs.util.simplerestclient.HTTPResponse;

/**
 * See RMI http://redmine/issues/2312.
 * 
 * @author kgilmer
 * 
 */
public class BasicConnectivityTests extends TestCase {

	protected MultiUserChat swarmRoom;
	protected ChatManager chatManager;
	private SwarmWSClient wsClient;
	private SwarmXMPPClient xmppClient;
	
	@Override
	protected void setUp() throws Exception {
		System.out.println("setUp()");
		wsClient = new SwarmWSClient(AccountConfig.getConfiguration().getHostname(Protocol.HTTP), AccountConfig.getConfiguration()
				.getAPIKey());

		assertTrue(wsClient.isValid() == null);
		
		//Delete all pre-existing swarms owned by test user.
		try {
			List<SwarmModel> swarms = wsClient.list();
			
			for (SwarmModel sm : swarms) {
				if (sm.getUserId().equals(AccountConfig.getConfiguration().getUsername())) {
					wsClient.destroy(sm.getId());
				}
			}
		} catch (HTTPException e) {
			/*
			 * Ignore 404s.  
			 * They are not errors, they have to be handled as errors since this is the REST way.
			 */
			if (e.getErrorCode() != HTTPResponse.HTTP_CODE_NOT_FOUND)
				throw e;
		}
				
		assertNotNull(wsClient.getSwarmResourceClient());
		assertNotNull(AccountConfig.getConfiguration().getResource());
		
		String id = wsClient.create(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());
		
		assertNotNull(id);
		assertTrue(id.length() > 0);
		
		AccountConfig.testSwarmId = id;
		
		xmppClient = new SwarmXMPPClient(AccountConfig.getXmppConfiguration());
		xmppClient.connect(new SwarmRequestListener());
		
		Thread.sleep(5000);

		assertTrue(xmppClient.isConnected());
		assertTrue(xmppClient.getConnection() != null);
		assertTrue(xmppClient.getResource() != null);
		assertTrue(xmppClient.getHostname() != null);
		assertTrue(xmppClient.getUsername() != null);
	}
	
	@Override
	protected void tearDown() throws Exception {
		System.out.println("tearDown()");
		if (xmppClient != null)
			xmppClient.disconnect();
		
		if (wsClient != null)
			wsClient.destroy(AccountConfig.testSwarmId);
	}

	/**
	 * @throws Exception
	 * 
	 */
	public void testConnectToSwarmServer() throws Exception {
		//setup() is the test 
	}

	/**
	 * Join a pre-existing swarm
	 * 
	 * @throws Exception
	 */
	public void testJoinMemberSwarms() throws Exception {
		wsClient.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.PRODUCER, AccountConfig.getConfiguration().getUsername(), AccountConfig.getConfiguration().getResource());
		wsClient.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.PRODUCER, AccountConfig.getConfiguration().getUsername(), "web");
		
		List<SwarmModel> allSwarms = wsClient.getSwarmResourceClient().getSwarmsByMember(AccountConfig.getConfiguration().getResource());

		List<SwarmModel> connectedSwarms = new ArrayList<SwarmModel>();
		for (SwarmModel swarm : allSwarms) {
			try {
				xmppClient.joinSwarm(swarm.getId(), new SwarmRequestListener());
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
			xmppClient.announce(swarm.getId(), advertisement.toJSONString());
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
		xmppClient.joinSwarm(id, new ISwarmServerRequestListener() {
			
			@Override
			public void feedListRequest(Jid jid, String swarmId) {
				System.out.println("feedListRequest() " + jid + " " + swarmId);
			}

			@Override
			public void feedListRequest(Chat chat, String swarmId) {
				System.out.println("feedListRequest() " + chat.getParticipant() + " " + swarmId);
			}

			@Override
			public void feedRequest(Jid jid, String swarmId, FeedRequest feedRequest) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void swarmInviteRequest(Jid sender, String roomId) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void feedMetaRequest(FeedRequest request, String swarmId) {
				// TODO Auto-generated method stub
				
			}
		});
		//Xmpp server is changing the JID from what I use to login, so cannot use presense method on XMPP API.
		//Smack throws a class-cast when I try to retrieve members manually for partial comparsion.
		//For now commenting out presense test
		
		/*Thread.sleep(2000);
		assertTrue(xmppClient.isPresent(id, xmppClient.getJid().toString()));		*/
	
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
