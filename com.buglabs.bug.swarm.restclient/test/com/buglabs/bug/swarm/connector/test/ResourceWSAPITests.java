package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.connector.model.SwarmModel;
import com.buglabs.bug.swarm.connector.model.UserResourceModel;
import com.buglabs.bug.swarm.connector.ws.ISwarmClient;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.IUserResourceClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;


/**
 * Tests for the Resource WS API.
 * 
 * @author kgilmer
 *
 */
public class ResourceWSAPITests extends TestCase {
	
	private static final String TEST_RESOURCE_ID = "test_resource_id";
	private static final String TEST_RESOURCE_NAME = "test_resource_name";
	private static final String TEST_RESOURCE_DESC = "test resource desc";
	private static final String TEST_RESOURCE_MACHINE_TYPE = "test_client";
	private static final MemberType TEST_RESOURCE_TYPE = MemberType.PRODUCER;
	
	@Override
	protected void setUp() throws Exception {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
		//Delete all pre-existing swarms owned by test user.
		List<SwarmModel> swarms = client.list();
		
		for (SwarmModel sm : swarms) {
			if (sm.getUserId().equals(AccountConfig.getConfiguration().getUsername())) {
				client.destroy(sm.getId());
			}
		}
		
		
		//Delete all pre-existing resources owned by the test user.
		IUserResourceClient resourceClient = client.getUserResourceClient();
		assertNotNull(resourceClient);
		
		List<UserResourceModel> resources = resourceClient.get((MemberType) null);
	
		for (UserResourceModel model : resources)
			if (model.getUserId().equals(AccountConfig.getConfiguration().getUsername()))
				resourceClient.remove(model.getResourceId());		
	
		String id = client.create(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());
		AccountConfig.testSwarmId = id;
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
	public void testAddResource() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		IUserResourceClient rclient = client.getUserResourceClient();
		
		UserResourceModel urm = rclient.add(				
				TEST_RESOURCE_NAME, 
				TEST_RESOURCE_DESC, 
				TEST_RESOURCE_MACHINE_TYPE,
				0,
				0);
		
		assertNotNull(urm);
		
		List<UserResourceModel> resources = rclient.get(TEST_RESOURCE_TYPE);
		
		assertNotNull(resources);
		assertTrue(resources.size() == 1);
		
		assertTrue(resources.iterator().next().getResourceId().equals(TEST_RESOURCE_ID));
		assertTrue(resources.iterator().next().getDescription().equals(TEST_RESOURCE_DESC));
		assertTrue(resources.iterator().next().getMachineType().equals(TEST_RESOURCE_MACHINE_TYPE));
		assertTrue(resources.iterator().next().getUserId().equals(AccountConfig.getConfiguration().getUsername()));
		assertTrue(resources.iterator().next().getType().equals(TEST_RESOURCE_TYPE));
	}
	
	public void testListProducerMembers() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		IUserResourceClient rclient = client.getUserResourceClient();
		
		testAddResource();
		
		List<UserResourceModel> resources = rclient.get(MemberType.CONSUMER);
		
		assertNotNull(resources);
		assertTrue(resources.size() == 0);
	}
	
	/**
	 * @throws IOException
	 */
	public void testDeleteResources() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		IUserResourceClient rclient = client.getUserResourceClient();
		
		testAddResource();
		
		List<UserResourceModel> resources = rclient.get(TEST_RESOURCE_TYPE);
		
		assertNotNull(resources);
		assertTrue(resources.size() == 1);
		
		SwarmWSResponse response = rclient.remove(TEST_RESOURCE_ID);
		assertFalse(response.isError());
		
		resources = rclient.get(TEST_RESOURCE_TYPE);
		assertNotNull(resources);
		assertTrue(resources.size() == 0);
	}

}
