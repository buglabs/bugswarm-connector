package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.ISwarmClient;
import com.buglabs.bug.swarm.connector.ws.SwarmResourceModel;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;
import com.buglabs.util.simplerestclient.HTTPException;


/**
 * Tests for the Swarm Member WS API.
 * 
 * @author kgilmer
 *
 */
public class SwarmMemberWSAPITests extends TestCase {
	
	private static final MemberType DEFAULT_MEMBER_TYPE = MemberType.CONSUMER;
	
	@Override
	protected void setUp() throws Exception {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
		String id = client.create(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());
		AccountConfig.testSwarmId = id;
		
		//Delete all pre-existing swarms owned by test user.
		try {
			List<SwarmModel> swarms = client.list();
			
			for (SwarmModel sm : swarms) {
				if (sm.getUserId().equals(AccountConfig.getConfiguration().getUsername())) {
					client.destroy(sm.getId());
				}
			}
		} catch (HTTPException e) {
			//Ignore 404s.  They are not errors.  But unfortunately they have to be handled as errors since this is the REST way according to Camilo.
			if (e.getErrorCode() != 404)
				throw e;
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (AccountConfig.testSwarmId != null) {
			ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
			assertTrue(client.isValid() == null);
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
		
		//TODO: determine set of test users that can be created or assumed to exist.
		SwarmWSResponse rc = membersClient.add(AccountConfig.testSwarmId, DEFAULT_MEMBER_TYPE, AccountConfig.getConfiguration().getUsername(), AccountConfig.getConfiguration().getResource());
		
		assertTrue(!rc.isError());
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
		assertFalse(membersClient2.add(AccountConfig.testSwarmId, MemberType.PRODUCER, AccountConfig.getConfiguration2().getUsername(), AccountConfig.getConfiguration2().getResource()).isError());
		
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
