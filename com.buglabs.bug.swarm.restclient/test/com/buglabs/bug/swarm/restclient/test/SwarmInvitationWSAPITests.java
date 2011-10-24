package com.buglabs.bug.swarm.restclient.test;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.restclient.ISwarmClient;
import com.buglabs.bug.swarm.restclient.ISwarmInviteClient.InvitationResponse;
import com.buglabs.bug.swarm.restclient.ISwarmInviteClient.InvitationState;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.restclient.impl.SwarmWSClient;
import com.buglabs.bug.swarm.restclient.model.Invitation;
import com.buglabs.bug.swarm.restclient.model.SwarmModel;
import com.buglabs.bug.swarm.restclient.model.UserResourceModel;

/**
 * Unit tests for ISwarmInvitationWSClient implementation.
 * 
 * @author kgilmer
 *
 */
public class SwarmInvitationWSAPITests extends TestCase {
	
	private static final String description = "invite test description";

	@Override
	protected void setUp() throws Exception {
		assertNotNull(AccountConfig.getConfiguration());
		assertNotNull(AccountConfig.getConfiguration2());
		
		assertFalse(
				AccountConfig.getConfiguration().getAPIKey().equals(
						AccountConfig.getConfiguration2().getAPIKey()));
		
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
		//Delete all pre-existing swarms owned by test user.
		List<SwarmModel> swarms = client.list();
		
		for (SwarmModel sm : swarms) {
			if (sm.getUserId().equals(AccountConfig.getConfiguration().getUsername())) {
				client.destroy(sm.getId());
			}
		}		
		
		String id = client.create(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());
		AccountConfig.testSwarmId = id;
		
		//Determine that 2nd user can connect and delete any existing swarms.
		SwarmWSClient client2 = new SwarmWSClient(AccountConfig.getConfiguration2());
		
		//Delete all pre-existing swarms owned by test user.	
		for (SwarmModel sm : client2.list()) {
			if (sm.getUserId().equals(AccountConfig.getConfiguration2().getUsername())) {
				client2.destroy(sm.getId());
			}
		}
		
		for (UserResourceModel ur : client2.getUserResourceClient().list())
			client2.getUserResourceClient().remove(ur.getResourceId());
		
		UserResourceModel urc = client2.getUserResourceClient().add("3rd_resource", "user resource desc", "pc", 0, 0);
		AccountConfig.testUserResource2 = urc;
		
		//Confirm that original user still has swarm.
		assertTrue(client.list().size() == 1);
	}
	
	public void testSendInvite() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
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
	
	public void testListSentInvitations() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
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
	
	public void testListRecievedInvitations() throws IOException {
		ISwarmClient client2 = new SwarmWSClient(AccountConfig.getConfiguration2());
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
	
	public void testListRecievedInvitations2() throws IOException {
		ISwarmClient client2 = new SwarmWSClient(AccountConfig.getConfiguration2());
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
	}
	
	public void testAcceptInvitation() throws IOException {
		ISwarmClient client2 = new SwarmWSClient(AccountConfig.getConfiguration2());
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
	
	public void testRejectInvitation() throws IOException {
		ISwarmClient client2 = new SwarmWSClient(AccountConfig.getConfiguration2());
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
