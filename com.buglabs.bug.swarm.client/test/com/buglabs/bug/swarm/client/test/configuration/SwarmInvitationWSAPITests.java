package com.buglabs.bug.swarm.client.test.configuration;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmInviteClient.InvitationResponse;
import com.buglabs.bug.swarm.client.ISwarmInviteClient.InvitationState;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.model.Configuration.Protocol;
import com.buglabs.bug.swarm.client.model.Invitation;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;
import com.buglabs.bug.swarm.client.test.AccountConfig;

/**
 * Unit tests for ISwarmInvitationWSClient implementation.
 * 
 * @author kgilmer
 *
 */
public class SwarmInvitationWSAPITests extends TestCase {
	
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
			client2.getUserResourceClient().destroy(ur.getResourceId());
		
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
	public void testSendInvite() throws IOException {
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
	 * Test sending invitations.  Check that listing existing invites changes appropriately when
	 * we create new invitations.
	 * 
	 * @throws IOException
	 */
	public void testListSentInvitations() throws IOException {
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		assertNotNull(client);
		assertNotNull(client.getSwarmInviteClient());
		assertNotNull(AccountConfig.testSwarmId);
		assertNotNull(AccountConfig.testInviteId);
		
		List<Invitation> sentInvites = client.getSwarmInviteClient().getSentInvitations(AccountConfig.testSwarmId);
		
		assertNotNull(sentInvites);
		assertTrue(sentInvites.isEmpty() == true);

		testSendInvite();
		sentInvites = client.getSwarmInviteClient().getSentInvitations(AccountConfig.testSwarmId);
		
		assertNotNull(sentInvites);
		assertTrue(sentInvites.isEmpty() == false);
		assertTrue(sentInvites.size() == 1);
		Invitation invite = sentInvites.get(0);
		assertTrue(invite.getId().equals(AccountConfig.testInviteId));
		
		assertNotNull(invite);
		assertNotNull(invite.getId());
		assertNotNull(invite.getFromUser());
		assertNotNull(invite.getToUser());
		assertNotNull(invite.getResourceId());
		assertNotNull(invite.getStatus());
		assertTrue(invite.getStatus().equals(InvitationState.NEW));
		assertNotNull(invite.getDescription());
		assertTrue(invite.getDescription().equals(description));
	}
	
	/**
	 * Test recieving invitations.  Confirm that target of invitation properly receives invite.
	 * 
	 * @throws IOException
	 */
	public void testListRecievedInvitations() throws IOException {
		ISwarmClient client2 = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration2().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration2().getConfingurationAPIKey());
		assertNotNull(client2);
		assertNotNull(client2.getSwarmInviteClient());
		assertNotNull(AccountConfig.testSwarmId);
		assertNotNull(AccountConfig.testInviteId);
		
		testSendInvite();
		List<Invitation> receivedInvites = client2.getSwarmInviteClient().getRecievedInvitations();
		
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
	}
	
	/**
	 * Test accepting an invitation.  
	 * @throws IOException
	 */
	public void testAcceptInvitation() throws IOException {
		ISwarmClient client2 = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration2().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration2().getConfingurationAPIKey());
		assertNotNull(client2);
		assertNotNull(client2.getSwarmInviteClient());
		assertNotNull(AccountConfig.testSwarmId);
		assertNotNull(AccountConfig.testInviteId);
		
		testSendInvite();
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
	 * Test rejecting an invitation.
	 * 
	 * @throws IOException
	 */
	public void testRejectInvitation() throws IOException {
		ISwarmClient client2 = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration2().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration2().getConfingurationAPIKey());
		assertNotNull(client2);
		assertNotNull(client2.getSwarmInviteClient());
		assertNotNull(AccountConfig.testSwarmId);
		assertNotNull(AccountConfig.testInviteId);
		
		testSendInvite();
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
		
		Invitation acceptInvite = client2.getSwarmInviteClient().respond(invite.getResourceId(), invite.getId(), InvitationResponse.REJECT);
		
		assertNotNull(acceptInvite);
		assertTrue(acceptInvite.getStatus().equals(InvitationState.REJECTED));
		assertNull(acceptInvite.getAcceptedAt());
	}
}
