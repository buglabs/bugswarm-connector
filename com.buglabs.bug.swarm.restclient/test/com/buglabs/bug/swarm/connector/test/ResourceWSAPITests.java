package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import javax.xml.ws.http.HTTPException;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.connector.model.ResourceModel;
import com.buglabs.bug.swarm.connector.model.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.IResourceClient;
import com.buglabs.bug.swarm.connector.ws.ISwarmClient;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
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
		IResourceClient resourceClient = client.getResourceClient();
		assertNotNull(resourceClient);
		
		List<ResourceModel> resources = resourceClient.get((MemberType) null);
	
		for (ResourceModel model : resources)
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
		IResourceClient rclient = client.getResourceClient();
		
		SwarmWSResponse response = rclient.add(
				TEST_RESOURCE_ID, 
				AccountConfig.getConfiguration().getUsername(), 
				TEST_RESOURCE_NAME, 
				TEST_RESOURCE_DESC, 
				TEST_RESOURCE_TYPE, 
				TEST_RESOURCE_MACHINE_TYPE);
		
		assertNotNull(response);
		assertFalse(response.isError());
		
		List<ResourceModel> resources = rclient.get(TEST_RESOURCE_TYPE);
		
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
		IResourceClient rclient = client.getResourceClient();
		
		testAddResource();
		
		List<ResourceModel> resources = rclient.get(MemberType.CONSUMER);
		
		assertNotNull(resources);
		assertTrue(resources.size() == 0);
	}
	
	/**
	 * @throws IOException
	 */
	public void testDeleteResources() throws IOException {
		ISwarmClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		IResourceClient rclient = client.getResourceClient();
		
		testAddResource();
		
		List<ResourceModel> resources = rclient.get(TEST_RESOURCE_TYPE);
		
		assertNotNull(resources);
		assertTrue(resources.size() == 1);
		
		SwarmWSResponse response = rclient.remove(TEST_RESOURCE_ID);
		assertFalse(response.isError());
		
		resources = rclient.get(TEST_RESOURCE_TYPE);
		assertNotNull(resources);
		assertTrue(resources.size() == 0);
	}

}
