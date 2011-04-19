package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.connector.ws.IMembersClient;
import com.buglabs.bug.swarm.connector.ws.IMembersClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.ISwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmMemberModel;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;


public class SwarmMemberWSAPITests extends BaseWSAPITests {
	
	public void testListConsumerMembers() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		String id = client.create(getTestSwarmName(), true, getTestSwarmDescription());
		
		assertNotNull(id);
		assertTrue(id.length() > 0);
		
		testSwarmId = id;
		
		List<SwarmMemberModel> list = membersClient.list(testSwarmId, MemberType.CONSUMER);
		
		assertNotNull(list);
		assertTrue(list.size() > 0);
		
		//TODO validate that a we are a member of the swarm since we created it.
	}
	
	public void testListProducerMembers() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		String id = client.create(getTestSwarmName(), true, getTestSwarmDescription());
		
		assertNotNull(id);
		assertTrue(id.length() > 0);
		
		testSwarmId = id;
		
		List<SwarmMemberModel> list = membersClient.list(testSwarmId, MemberType.PRODUCER);
		
		assertNotNull(list);
		assertTrue(list.size() > 0);
		
		//TODO validate that a we are a member of the swarm since we created it.
	}
	
	public void testListAllMembers() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		String id = client.create(getTestSwarmName(), true, getTestSwarmDescription());
		
		assertNotNull(id);
		assertTrue(id.length() > 0);
		
		testSwarmId = id;
		
		List<SwarmMemberModel> list = membersClient.list(testSwarmId, MemberType.ALL);
		
		assertNotNull(list);
		assertTrue(list.size() > 0);
		
		//TODO validate that a we are a member of the swarm since we created it.
	}
	
	/**
	 * This test must occur after testSwarmId is set by a previous test.
	 * @throws IOException 
	 */
	public void testAddSwarmMember() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		//TODO: determine set of test users that can be created or assumed to exist.
		int rc = membersClient.add(testSwarmId, MemberType.CONSUMER, "testUser", "Psi");

		assertTrue(rc == 200);
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void testRemoveSwarmMember() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		//TODO: determine set of test users that can be created or assumed to exist.
		int rc = membersClient.remove(testSwarmId, "testUser");

		assertTrue(rc == 200);
	}
	
	public void testListSwarmsForMembers() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		List<SwarmModel> members = membersClient.getSwarmsByMember("testUser", MemberType.PRODUCER);
		
		assertNotNull(members);
		assertTrue(members.size() > 0);
		
		members = membersClient.getSwarmsByMember("testUser", MemberType.CONSUMER);
			
		assertNotNull(members);
		assertTrue(members.size() > 0);
		
		members = membersClient.getSwarmsByMember("testUser", MemberType.ALL);
		
		assertNotNull(members);
		assertTrue(members.size() > 0);
	}
}
