package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.touge.restclient.ReSTClient;

import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.restclient.ISwarmClient;
import com.buglabs.bug.swarm.restclient.ISwarmInviteClient.InvitationResponse;
import com.buglabs.bug.swarm.restclient.ISwarmInviteClient.InvitationState;
import com.buglabs.bug.swarm.restclient.ISwarmMessageListener;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.restclient.ISwarmSession;
import com.buglabs.bug.swarm.restclient.SwarmClientFactory;
import com.buglabs.bug.swarm.restclient.SwarmWSResponse;
import com.buglabs.bug.swarm.restclient.model.Invitation;
import com.buglabs.bug.swarm.restclient.model.SwarmModel;
import com.buglabs.bug.swarm.restclient.model.UserResourceModel;

/**
 * Unit tests for ISwarmParticipationClient implementation.
 * 
 * @author kgilmer
 *
 */
public class ParticipationAPITests extends TestCase {
	
	private static final String description = "invite test description";

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 * 
	 * For user 1:
	 * - Delete all existing swarms
	 * - Delete all existing resources
	 * - Create swarm, store id
	 * - Create resource, store id
	 * 
	 * For user 2:
	 * - Delete all existing swarms
	 * - Delete all existing resources
	 * - Create new resource, store id
	 * 
	 */
	@Override
	protected void setUp() throws Exception {
		assertNotNull(AccountConfig.getConfiguration());
		assertNotNull(AccountConfig.getConfiguration2());
		
		
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		//Delete all pre-existing swarms owned by test user.
		List<SwarmModel> swarms = client.list();
		
		for (SwarmModel sm : swarms) {
			if (sm.getUserId().equals(AccountConfig.getConfiguration().getUsername())) {
				client.destroy(sm.getId());
			}
		}		
		
		String id = client.create(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());
		AccountConfig.testSwarmId = id;
		
		UserResourceModel urc = client.getUserResourceClient().add(AccountConfig.generateRandomResourceName(), "user resource desc", "pc", 0, 0);
		AccountConfig.testUserResource = urc;
		
		SwarmWSResponse response = client.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.PRODUCER, urc.getResourceId());
		
		assertTrue(!response.isError());
		
		//Determine that 2nd user can connect and delete any existing swarms.
		ISwarmClient client2 = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration2().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration2().getConfingurationAPIKey());
		
		//Delete all pre-existing swarms owned by test user.	
		for (SwarmModel sm : client2.list()) {
			if (sm.getUserId().equals(AccountConfig.getConfiguration2().getUsername())) {
				client2.destroy(sm.getId());
			}
		}
		
		for (UserResourceModel ur : client2.getUserResourceClient().list())
			client2.getUserResourceClient().remove(ur.getResourceId());
		
		urc = client2.getUserResourceClient().add(AccountConfig.generateRandomResourceName(), "user resource desc", "pc", 0, 0);
		AccountConfig.testUserResource2 = urc;
		
		//Confirm that original user still has swarm.
		assertTrue(client.list().size() == 1);
	}
	
	/**
	 * Test sending an invite.  Only confirms that server returns invite object as specified in documentation.
	 * 
	 * @throws IOException
	 */
	private void sendInvite() throws IOException {
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		assertNotNull(client);
		assertNotNull(client.getSwarmInviteClient());
		assertNotNull(AccountConfig.testSwarmId);
		
		Invitation invite = client.getSwarmInviteClient().send(
				AccountConfig.testSwarmId, 
				AccountConfig.getConfiguration2().getUsername(), 
				AccountConfig.testUserResource2.getResourceId(), 
				MemberType.CONSUMER, 
				description);
		
		assertNotNull(invite);
		assertNotNull(invite.getId());
		assertNotNull(invite.getFromUser());
		assertNotNull(invite.getToUser());
		assertNotNull(invite.getResourceId());
		assertNotNull(invite.getStatus());
		assertTrue(invite.getStatus().equals(InvitationState.NEW));
		assertNotNull(invite.getDescription());
		assertTrue(invite.getDescription().equals(description));
		
		AccountConfig.testInviteId = invite.getId();
	}
	

	/**
	 * Test accepting an invitation.  
	 * @throws IOException
	 */
	private void testStreamingTester() throws IOException {
		sendInvite();
		acceptInvitation();
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		UserResourceModel urc = client.getUserResourceClient().add("stream_resource", "stream resource desc", "pc", 0, 0);
		
		StreamingTester.test(
				"test.bugswarm.net", 
				AccountConfig.getConfiguration().getParticipationAPIKey(), 
				AccountConfig.testSwarmId, 
				urc.getResourceId());
		
		System.out.println("boo");
	}
	
	/**
	 * Test accepting an invitation.  
	 * @throws IOException
	 */
	public void testSwarmParticipationAPI() throws IOException {
		sendInvite();
		acceptInvitation();
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		UserResourceModel urc = client.getUserResourceClient().add("stream_resource", "stream resource desc", "pc", 0, 0);
		
		System.out.println("User 1 Key: " + AccountConfig.getConfiguration().getParticipationAPIKey());
 		ISwarmSession psession1 = SwarmClientFactory.getParticipationClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getParticipationAPIKey())
				.createSession(urc.getResourceId(), AccountConfig.testSwarmId);
		
		psession1.addListener(new ISwarmMessageListener() {
			
			@Override
			public void messageRecieved(Map<String, ?> payload, String fromSwarm, String fromResource, boolean isPublic) {
				System.out.println(fromSwarm);
			}
		});
		
		//psession1.join(AccountConfig.testSwarmId, urc.getResourceId());
		
		//Setup the second user.
		assertNotNull(AccountConfig.testUserResource2.getResourceId());
		System.out.println("User 2 Key: " + AccountConfig.getConfiguration2().getParticipationAPIKey());
		
		ISwarmSession psession2 = SwarmClientFactory.getParticipationClient(
				AccountConfig.getConfiguration2().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration2().getParticipationAPIKey())
				.createSession(AccountConfig.testUserResource2.getResourceId(), AccountConfig.testSwarmId);
		
		psession2.addListener(new ISwarmMessageListener() {
			
			@Override
			public void messageRecieved(Map<String, ?> payload, String fromSwarm, String fromResource, boolean isPublic) {
				System.out.println(fromSwarm);
			}
		});
		
		psession2.send(AccountConfig.generateRandomPayload());
		
		System.out.println("boo");
	}

	/**
	 * Test accepting an invitation.  
	 * @throws IOException
	 */
	private void acceptInvitation() throws IOException {
		ISwarmClient client2 = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration2().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration2().getConfingurationAPIKey());
		assertNotNull(client2);
		assertNotNull(client2.getSwarmInviteClient());
		assertNotNull(AccountConfig.testSwarmId);
		assertNotNull(AccountConfig.testInviteId);
		
		sendInvite();
		List<Invitation> receivedInvites = client2.getSwarmInviteClient().getRecievedInvitations(AccountConfig.testUserResource2.getResourceId());
		
		assertNotNull(receivedInvites);
		assertTrue(receivedInvites.isEmpty() == false);
		Invitation invite = null;
		
		for (Invitation i : receivedInvites)
			if (i.getId().equals(AccountConfig.testInviteId))
				invite = i;
		
		assertNotNull(invite);
		assertNotNull(invite.getId());
		assertNotNull(invite.getFromUser());
		assertNotNull(invite.getToUser());
		assertNotNull(invite.getResourceId());
		assertNotNull(invite.getStatus());
		assertTrue(invite.getStatus().equals(InvitationState.NEW));
		assertNotNull(invite.getDescription());
		assertTrue(invite.getDescription().equals(description));
		
		Invitation acceptInvite = client2.getSwarmInviteClient().respond(invite.getResourceId(), invite.getId(), InvitationResponse.ACCEPT);
		
		assertNotNull(acceptInvite);
		assertTrue(acceptInvite.getStatus().equals(InvitationState.ACCEPTED));
		assertNotNull(acceptInvite.getAcceptedAt());
	}
	
	/**
	 * Test accepting an invitation.  
	 * @throws IOException
	 */
	private void testAcceptInvitationWithParticipation() throws IOException {			
		sendInvite();
		acceptInvitation();
		
		ISwarmClient client2 = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration2().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration2().getConfingurationAPIKey());
		
		assertNotNull(client2);
		assertNotNull(client2.getSwarmInviteClient());
		assertNotNull(AccountConfig.testSwarmId);
		assertNotNull(AccountConfig.testInviteId);
		
		ISwarmSession psession2 = SwarmClientFactory.getParticipationClient(
				AccountConfig.getConfiguration2().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration2().getParticipationAPIKey())
				.createSession(AccountConfig.testUserResource2.getResourceId(), AccountConfig.testSwarmId);
		
		ISwarmSession psession1 = SwarmClientFactory.getParticipationClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getParticipationAPIKey())
				.createSession(AccountConfig.testUserResource.getResourceId(), AccountConfig.testSwarmId);
		
		psession2.addListener(new ISwarmMessageListener() {
			
			@Override
			public void messageRecieved(Map<String, ?> payload, String fromSwarm, String fromResource, boolean isPublic) {
				System.out.println(payload);
			}
		});
		
		psession1.addListener(new ISwarmMessageListener() {
			
			@Override
			public void messageRecieved(Map<String, ?> payload, String fromSwarm, String fromResource, boolean isPublic) {
				System.out.println(payload);
			}
		});
		
		List<Invitation> receivedInvites = client2.getSwarmInviteClient().getRecievedInvitations(AccountConfig.testUserResource2.getResourceId());
		
		assertNotNull(receivedInvites);
		assertTrue(receivedInvites.isEmpty() == false);
		Invitation invite = null;
		
		for (Invitation i : receivedInvites)
			if (i.getId().equals(AccountConfig.testInviteId))
				invite = i;
		
		assertNotNull(invite);
		assertNotNull(invite.getId());
		assertNotNull(invite.getFromUser());
		assertNotNull(invite.getToUser());
		assertNotNull(invite.getResourceId());
		assertNotNull(invite.getStatus());
		assertTrue(invite.getStatus().equals(InvitationState.NEW));
		assertNotNull(invite.getDescription());
		assertTrue(invite.getDescription().equals(description));
		
		Invitation acceptInvite = client2.getSwarmInviteClient().respond(invite.getResourceId(), invite.getId(), InvitationResponse.ACCEPT);
		
		assertNotNull(acceptInvite);
		assertTrue(acceptInvite.getStatus().equals(InvitationState.ACCEPTED));
		assertNotNull(acceptInvite.getAcceptedAt());
		
		psession2.send(ReSTClient.toMap("x", "asdf"));
		
		System.out.println("boo");
	}
}
