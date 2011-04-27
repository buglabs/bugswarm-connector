package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.connector.ws.IMembersClient;
import com.buglabs.bug.swarm.connector.ws.ISwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmMemberModel;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.IMembersClient.MemberType;


/**
 * Tests for the Swarm Member WS API.
 * 
 * @author kgilmer
 *
 */
public class SwarmMemberWSAPITests extends BaseWSAPICase {
	
	private static final String DEFAULT_TEST_USER = "connector-test";
	private static final String DEFAULT_RESOURCE_STRING = "Psi";
	private static final MemberType DEFAULT_MEMBER_TYPE = MemberType.CONSUMER;
	private static final String DEFAULT_TEST_USER2 = "connector-test2";
	
	/**
	 * This test must occur after testSwarmId is set by a previous test.
	 * @throws IOException 
	 */
	public void testAddSwarmMember() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		String id = client.create(getTestSwarmName(), true, getTestSwarmDescription());
		testSwarmId = id;
		
		//TODO: determine set of test users that can be created or assumed to exist.
		int rc = membersClient.add(id, DEFAULT_MEMBER_TYPE, DEFAULT_TEST_USER2, DEFAULT_RESOURCE_STRING);

		
		
		assertTrue(rc == 201);
	}

	public void testListConsumerMembers() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		List<SwarmMemberModel> list = membersClient.list(testSwarmId, MemberType.CONSUMER);
		
		assertNotNull(list);
		assertTrue(list.size() > 0);
		
		//TODO validate that a we are a member of the swarm since we created it.
	}
	
	public void testListProducerMembers() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		List<SwarmMemberModel> list = membersClient.list(testSwarmId, MemberType.PRODUCER);
		
		assertNotNull(list);
		//There should be no PRODUCERS since we created only a CONSUMER
		assertTrue(list.size() == 0);
		
		//Now add a producer
		assertTrue(membersClient.add(testSwarmId, MemberType.PRODUCER, DEFAULT_TEST_USER, DEFAULT_RESOURCE_STRING) == 201);
		
		list = membersClient.list(testSwarmId, MemberType.PRODUCER);
		assertNotNull(list);
		//There should be no PRODUCERS since we created only a CONSUMER
		assertTrue(list.size() == 1);
		
		//TODO validate that a we are a member of the swarm since we created it.
	}
	
	/**
	 * @throws IOException
	 */
	public void testListSwarmsForMembers() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		List<SwarmModel> members = membersClient.getSwarmsByMember(DEFAULT_TEST_USER);
		
		assertNotNull(members);
		assertTrue(members.size() > 0);
		
		members = membersClient.getSwarmsByMember(DEFAULT_TEST_USER2);
		
		assertNotNull(members);
		assertTrue(members.size() > 0);
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void testRemoveSwarmMember() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		//Get the current number of members.
		int count = membersClient.getSwarmsByMember(DEFAULT_TEST_USER).size();
		assertTrue(count > 0);
		//TODO: determine set of test users that can be created or assumed to exist.
		int rc = membersClient.remove(testSwarmId, DEFAULT_MEMBER_TYPE, DEFAULT_TEST_USER, DEFAULT_RESOURCE_STRING);
		assertTrue(rc == 200);

		//Make sure member count has decreased by one.
		assert(membersClient.getSwarmsByMember(DEFAULT_TEST_USER).size() == count - 1);
	}
	
	public void testEmptySwarm() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		List<SwarmModel> members = membersClient.getSwarmsByMember(DEFAULT_TEST_USER);
		
		assertNotNull(members);
		assertTrue(members.size() == 0);
	}
}
