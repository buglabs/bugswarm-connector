package com.buglabs.bug.swarm.restclient.test;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.restclient.ISwarmClient;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient;
import com.buglabs.bug.swarm.restclient.SwarmWSResponse;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.restclient.impl.SwarmWSClient;
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
		
		SwarmWSResponse rc = membersClient.add(
				AccountConfig.testSwarmId, 
				DEFAULT_MEMBER_TYPE, 
				AccountConfig.testUserResource2.getResourceId());
		
		if (rc.isError()) {
			System.err.println(rc.getMessage());
		}
		assertTrue(!rc.isError());
		
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
		
		//Now add a producer
		assertNotNull(membersClient2.add(
				AccountConfig.testSwarmId, 
				MemberType.PRODUCER, 				
				AccountConfig.getConfiguration2().getResource()));
		
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
