package com.buglabs.bug.swarm.restclient.test;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.restclient.ISwarmClient;
import com.buglabs.bug.swarm.restclient.ISwarmInviteClient.InvitationResponse;
import com.buglabs.bug.swarm.restclient.ISwarmInviteClient.InvitationState;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient;
import com.buglabs.bug.swarm.restclient.SwarmWSResponse;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.restclient.impl.SwarmWSClient;
import com.buglabs.bug.swarm.restclient.model.Invitation;
import com.buglabs.bug.swarm.restclient.model.SwarmModel;
import com.buglabs.bug.swarm.restclient.model.SwarmResourceModel;
import com.buglabs.bug.swarm.restclient.model.UserResourceModel;


/**
 * Tests for the Swarm Member WS API.
 * 
 * @author kgilmer
 *
 */
public class SwarmResourceWSAPITests extends TestCase {
	
	private static final MemberType DEFAULT_MEMBER_TYPE = MemberType.CONSUMER;
	
	@Override
	protected void setUp() throws Exception {
		assertNotNull(AccountConfig.getConfiguration());
		assertNotNull(AccountConfig.getConfiguration2());
		
		assertFalse(AccountConfig.getConfiguration().getAPIKey().equals(AccountConfig.getConfiguration2().getAPIKey()));
		
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
	
	@Override
	protected void tearDown() throws Exception {
		if (AccountConfig.testSwarmId != null) {
			ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
			client.destroy(AccountConfig.testSwarmId);
		}
	}
	/**
	 * This test must occur after testSwarmId is set by a previous test.
	 * @throws IOException 
	 */
	public void testAddSwarmMember() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		ISwarmResourcesClient membersClient = ((SwarmWSClient) client).getSwarmResourceClient();
		assertNotNull(AccountConfig.testUserResource2);
		String inviteTarget = AccountConfig.getConfiguration2().getUsername();
		
		//Send invitation
		Invitation invite = client.getSwarmInviteClient().send(
				AccountConfig.testSwarmId, inviteTarget, 
				AccountConfig.testUserResource2.getResourceId(), 
				DEFAULT_MEMBER_TYPE, 
				"test");
		assertNotNull(invite);
		assertTrue(invite.getStatus() == InvitationState.NEW);
		
		ISwarmClient client2 = new SwarmWSClient(AccountConfig.getConfiguration2());
		
		assertTrue(client2.getSwarmInviteClient().getRecievedInvitations(AccountConfig.testUserResource2.getResourceId()).size() > 0);
		Invitation inviteResponse = client2.getSwarmInviteClient().respond(invite.getResourceId(), invite.getId(), InvitationResponse.ACCEPT);
		assertNotNull(inviteResponse);
		assertTrue(inviteResponse.getStatus() == InvitationState.ACCEPTED);
		
		// At this point the client2 resource should be a member of our test swarm.  Confirm...
		List<SwarmResourceModel> resources = client.getSwarmResourceClient().list(AccountConfig.testSwarmId, DEFAULT_MEMBER_TYPE);
		assertNotNull(resources);
		assertTrue(resources.size() > 0);
		boolean user2listed = false;
		for (SwarmResourceModel m : resources)
			if (m.getUserId().equals(AccountConfig.getConfiguration2().getUsername()))
				user2listed = true;
		
		assertTrue(user2listed);
		/*
		
		SwarmWSResponse rc = client.getSwarmResourceClient().add(
				AccountConfig.testSwarmId, 
				DEFAULT_MEMBER_TYPE, 
				AccountConfig.testUserResource2.getResourceId());
		
		if (rc.isError()) {
			System.err.println(rc.getMessage());
		}
		assertTrue(!rc.isError());*/
		
		//TODO: This test is failing, and am not sure why.
	}

	public void testListConsumerMembers() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		ISwarmResourcesClient membersClient = ((SwarmWSClient) client).getSwarmResourceClient();
		
		testAddSwarmMember();
		
		List<SwarmResourceModel> list = membersClient.list(AccountConfig.testSwarmId, MemberType.CONSUMER);
		
		assertNotNull(list);
		assertTrue(list.size() > 0);
		
		//TODO validate that a we are a member of the swarm since we created it.
	}
	
	public void testListProducerMembers() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		ISwarmResourcesClient membersClient = ((SwarmWSClient) client).getSwarmResourceClient();
		
		testAddSwarmMember();
		
		List<SwarmResourceModel> list = membersClient.list(AccountConfig.testSwarmId, MemberType.PRODUCER);
		
		assertNotNull(list);
		//There should be no PRODUCERS since we created only a CONSUMER
		assertTrue(list.size() == 0);
		
		//Create WS client for 2nd user.
		ISwarmClient client2 = new SwarmWSClient(AccountConfig.getConfiguration2());
		ISwarmResourcesClient membersClient2 = ((SwarmWSClient) client2).getSwarmResourceClient();
		UserResourceModel client2resource = null;
		if (client2.getUserResourceClient().list().size() == 0) {
			client2resource = client2.getUserResourceClient().add("test_resource", "test resource desc", "pc", 0, 0);			
		} else {
			client2resource = client2.getUserResourceClient().list().get(0);
		}
		assertNotNull(client2resource);
		
		//Now add a producer by inviting
		Invitation invite = client.getSwarmInviteClient().send(
				AccountConfig.testSwarmId, 
				AccountConfig.getConfiguration2().getUsername(), 
				client2resource.getResourceId(), 
				MemberType.PRODUCER, 
				"test desc");
		
		assertNotNull(invite);
		assertTrue(invite.getStatus() == InvitationState.NEW);
		
		//And accept the invitation as user2
		List<Invitation> invitiations = client2.getSwarmInviteClient().getRecievedInvitations(client2resource.getResourceId());
		for (Invitation inv : invitiations)
			client2.getSwarmInviteClient().respond(
					client2resource.getResourceId(), inv.getId(), InvitationResponse.ACCEPT);
		
		//We have accepted the invitation.  The original swarm should now list us as a producer of the swarm.
		list = membersClient.list(AccountConfig.testSwarmId, MemberType.PRODUCER);
		assertNotNull(list);
		//There should be one PRODUCER now
		assertTrue(list.size() == 1);
		
		//TODO validate that a we are a member of the swarm since we created it.
	}
	
	/**
	 * @throws IOException
	 */
	public void testListSwarmsForMembers() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		ISwarmResourcesClient membersClient = ((SwarmWSClient) client).getSwarmResourceClient();
		
		testAddSwarmMember();
		
		List<SwarmModel> members = membersClient.getSwarmsByMember(AccountConfig.getConfiguration().getResource());
		
		assertNotNull(members);
		assertTrue(members.size() > 0);
		
		//Create client for second user.
		client = new SwarmWSClient(AccountConfig.getConfiguration2());
		membersClient = ((SwarmWSClient) client).getSwarmResourceClient();
		
		members = membersClient.getSwarmsByMember(AccountConfig.getConfiguration2().getResource());
		
		assertNotNull(members);
		assertTrue(members.size() > 0);
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void testRemoveSwarmMember() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		ISwarmResourcesClient membersClient = ((SwarmWSClient) client).getSwarmResourceClient();
		
		testAddSwarmMember();
		
		//Get the current number of members.
		int count = membersClient.getSwarmsByMember(AccountConfig.getConfiguration().getResource()).size();
		assertTrue(count > 0);
		//TODO: determine set of test users that can be created or assumed to exist.
		SwarmWSResponse rc = membersClient.remove(AccountConfig.testSwarmId, DEFAULT_MEMBER_TYPE, AccountConfig.getConfiguration().getUsername(), AccountConfig.getConfiguration().getResource());
		assertFalse(rc.isError());

		//Make sure member count has decreased by one.
		assert(membersClient.getSwarmsByMember(AccountConfig.getConfiguration().getResource()).size() == count - 1);
		
		client = new SwarmWSClient(AccountConfig.getConfiguration2());
		membersClient = ((SwarmWSClient) client).getSwarmResourceClient();
		rc = membersClient.remove(AccountConfig.testSwarmId, DEFAULT_MEMBER_TYPE, AccountConfig.getConfiguration2().getUsername(), AccountConfig.getConfiguration2().getResource());
		assertFalse(rc.isError());
	}
}
