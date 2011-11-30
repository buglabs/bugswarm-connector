package com.buglabs.bug.swarm.client.test.configuration;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.IUserResourceClient;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.SwarmWSResponse;
import com.buglabs.bug.swarm.client.model.Configuration.Protocol;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;
import com.buglabs.bug.swarm.client.test.AccountConfig;


/**
 * Tests for the Resource WS API.
 * 
 * @author kgilmer
 *
 */
public class UserResourceWSAPITests extends TestCase {
	
	private static final String TEST_RESOURCE_NAME = "test_resource_name";
	private static final String TEST_RESOURCE_DESC = "test resource desc";
	private static final String TEST_RESOURCE_MACHINE_TYPE = "pc";
	private static final MemberType TEST_RESOURCE_TYPE = MemberType.PRODUCER;
	
	@Override
	protected void setUp() throws Exception {
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
		
		
		//Delete all pre-existing resources owned by the test user.
		IUserResourceClient resourceClient = client.getUserResourceClient();
		assertNotNull(resourceClient);
		
		List<UserResourceModel> resources = resourceClient.get();
	
		for (UserResourceModel model : resources)
			if (model.getUserId().equals(AccountConfig.getConfiguration().getUsername()))
				resourceClient.destroy(model.getResourceId());		
	
		String id = client.create(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());
		AccountConfig.testSwarmId = id;
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (AccountConfig.testSwarmId != null) {
			ISwarmClient client = SwarmClientFactory.getSwarmClient(
					AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
					AccountConfig.getConfiguration().getConfingurationAPIKey());
			client.destroy(AccountConfig.testSwarmId);
		}
	}
	/**
	 * This test must occur after testSwarmId is set by a previous test.
	 * @throws IOException 
	 */
	public void testAddResource() throws IOException {
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		IUserResourceClient rclient = client.getUserResourceClient();
		
		UserResourceModel urm = rclient.add(				
				TEST_RESOURCE_NAME, 
				TEST_RESOURCE_DESC, 
				TEST_RESOURCE_MACHINE_TYPE,
				0,
				0);
		
		assertNotNull(urm);
		
		List<UserResourceModel> resources = rclient.get();
		
		assertNotNull(resources);
		assertTrue(resources.size() == 1);
		
		UserResourceModel userrm = resources.get(0);
		
		assertTrue(userrm.getDescription().equals(TEST_RESOURCE_DESC));
		assertTrue(userrm.getMachineType().equals(TEST_RESOURCE_MACHINE_TYPE));
		assertTrue(userrm.getUserId().equals(AccountConfig.getConfiguration().getUsername()));
	}
	
	/**
	 * @throws IOException
	 */
	public void testDeleteResources() throws IOException {
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		IUserResourceClient rclient = client.getUserResourceClient();
		
		testAddResource();
		
		List<UserResourceModel> resources = rclient.get();
		
		assertNotNull(resources);
		assertTrue(resources.size() == 1);
		
		SwarmWSResponse response = rclient.destroy(resources.get(0).getResourceId());
		assertFalse(response.isError());
		
		resources = rclient.get();
		assertNotNull(resources);
		assertTrue(resources.size() == 0);
	}

}
