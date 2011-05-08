package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.connector.ws.IMembersClient;
import com.buglabs.bug.swarm.connector.ws.IMembersClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.ISwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmMemberModel;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;


/**
 * Tests for the Swarm Member WS API.
 * 
 * @author kgilmer
 *
 */
public class SwarmMemberWSAPITests extends TestCase {
	
	private static final MemberType DEFAULT_MEMBER_TYPE = MemberType.CONSUMER;
	
	/**
	 * This test must occur after testSwarmId is set by a previous test.
	 * @throws IOException 
	 */
	public void testAddSwarmMember() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		String id = client.create(TestUtil.getTestSwarmName(), true, TestUtil.getTestSwarmDescription());
		TestUtil.testSwarmId = id;
		
		//TODO: determine set of test users that can be created or assumed to exist.
		SwarmWSResponse rc = membersClient.add(TestUtil.testSwarmId, DEFAULT_MEMBER_TYPE, TestUtil.getConfiguration().getUsername(), TestUtil.getConfiguration().getResource());
		
		assertTrue(!rc.isError());
	}

	public void testListConsumerMembers() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		List<SwarmMemberModel> list = membersClient.list(TestUtil.testSwarmId, MemberType.CONSUMER);
		
		assertNotNull(list);
		assertTrue(list.size() > 0);
		
		//TODO validate that a we are a member of the swarm since we created it.
	}
	
	public void testListProducerMembers() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		List<SwarmMemberModel> list = membersClient.list(TestUtil.testSwarmId, MemberType.PRODUCER);
		
		assertNotNull(list);
		//There should be no PRODUCERS since we created only a CONSUMER
		assertTrue(list.size() == 0);
		
		//Create WS client for 2nd user.
		ISwarmWSClient client2 = new SwarmWSClient(TestUtil.getConfiguration2());
		IMembersClient membersClient2 = ((SwarmWSClient) client2).getMembers();
		
		//Now add a producer
		assertFalse(membersClient2.add(TestUtil.testSwarmId, MemberType.PRODUCER, TestUtil.getConfiguration2().getUsername(), TestUtil.getConfiguration2().getResource()).isError());
		
		list = membersClient.list(TestUtil.testSwarmId, MemberType.PRODUCER);
		assertNotNull(list);
		//There should be one PRODUCER now
		assertTrue(list.size() == 1);
		
		//TODO validate that a we are a member of the swarm since we created it.
	}
	
	/**
	 * @throws IOException
	 */
	public void testListSwarmsForMembers() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		List<SwarmModel> members = membersClient.getSwarmsByMember(TestUtil.getConfiguration().getResource());
		
		assertNotNull(members);
		assertTrue(members.size() > 0);
		
		//Create client for second user.
		client = new SwarmWSClient(TestUtil.getConfiguration2());
		membersClient = ((SwarmWSClient) client).getMembers();
		
		members = membersClient.getSwarmsByMember(TestUtil.getConfiguration2().getResource());
		
		assertNotNull(members);
		assertTrue(members.size() > 0);
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void testRemoveSwarmMember() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		//Get the current number of members.
		int count = membersClient.getSwarmsByMember(TestUtil.getConfiguration().getResource()).size();
		assertTrue(count > 0);
		//TODO: determine set of test users that can be created or assumed to exist.
		SwarmWSResponse rc = membersClient.remove(TestUtil.testSwarmId, DEFAULT_MEMBER_TYPE, TestUtil.getConfiguration().getUsername(), TestUtil.getConfiguration().getResource());
		assertFalse(rc.isError());

		//Make sure member count has decreased by one.
		assert(membersClient.getSwarmsByMember(TestUtil.getConfiguration().getResource()).size() == count - 1);
		
		client = new SwarmWSClient(TestUtil.getConfiguration2());
		membersClient = ((SwarmWSClient) client).getMembers();
		rc = membersClient.remove(TestUtil.testSwarmId, DEFAULT_MEMBER_TYPE, TestUtil.getConfiguration2().getUsername(), TestUtil.getConfiguration2().getResource());
		assertFalse(rc.isError());
	}
	
	public void t3stEmptySwarm() throws IOException {
		ISwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		IMembersClient membersClient = ((SwarmWSClient) client).getMembers();
		
		List<SwarmModel> members = membersClient.getSwarmsByMember(TestUtil.getConfiguration().getResource());
		
		assertNotNull(members);
		assertTrue(members.size() == 0);
	}
}
