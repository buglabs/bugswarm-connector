package com.buglabs.bug.swarm.client.test.configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.SwarmWSResponse;
import com.buglabs.bug.swarm.client.model.Configuration.Protocol;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;
import com.buglabs.bug.swarm.client.test.AccountConfig;

/**
 * Unit tests for ISwarmWSClient implementation.
 * 
 * @author kgilmer
 *
 */
public class SwarmWSAPITests extends TestCase {

	public void testDeleteAllSwarms() throws IOException {
		assertNotNull(AccountConfig.getConfiguration());
		
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		for (SwarmModel sm : client.list())
			client.destroy(sm.getId());
		
		assertTrue(client.list().size() == 0);
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testCreateSwarm() throws IOException {
		assertNotNull(AccountConfig.getConfiguration());
		
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		assertNotNull(client.getSwarmResourceClient());
		
		//Delete all pre-existing resources owned by the test user.
		for (UserResourceModel ur : client.getUserResourceClient().list())
			client.getUserResourceClient().destroy(ur.getResourceId());
		
		assertTrue(client.getUserResourceClient().list().size() == 0);
		
		//Delete all pre-existing swarms owned by test user.			
		for (SwarmModel sm : client.list()) {
			if (sm.getUserId().equals(AccountConfig.getConfiguration().getUsername())) {
				client.destroy(sm.getId());
			}
		}
		
		String id = client.create(
				AccountConfig.generateRandomSwarmName(), 
				true, 
				AccountConfig.getTestSwarmDescription());
		
		System.out.println("Created swarm with id: " + id);
		assertNotNull(id);
		assertTrue(id.length() > 0);
		
		AccountConfig.testSwarmId = id;
		
		//Test that id now is in the list of swarms for client.
		boolean swarmIdFound = false;
		for (SwarmModel sm : client.list()) {
			if (sm.getId().equals(id)) {
				swarmIdFound = true;
			}
		}
		assertTrue(swarmIdFound);
		
		//Create user resource.
		/*
		 * "name": "My resource",
		    "machine_type": "pc",
		    "description": "My Resource description",
		    "position": {
		                    "longitude": 0,
		                    "latitude": 0
		    }
		 */
		UserResourceModel userResource = client.getUserResourceClient().add(
				AccountConfig.generateRandomResourceName(),
				"Test resource",
				"pc", 0, 0);				
		
		assertNotNull(userResource);	
		AccountConfig.getConfiguration().setResourceId(userResource.getResourceId());
		assertNotNull(AccountConfig.getConfiguration().getResource());
		
		//Make sure user resource is now present in list.
		boolean userResourceFound = false;
		for (UserResourceModel ur : client.getUserResourceClient().list())
			if (ur.getResourceId().equals(userResource.getResourceId()))
				userResourceFound = true;		
		assertTrue(userResourceFound);		
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testUpdateSwarm() throws IOException {
		testCreateSwarm();
		
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		SwarmWSResponse rval = client.update(AccountConfig.testSwarmId, true, "new description");
				
		assertTrue(!rval.isError());
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testGet() throws IOException {
		testCreateSwarm();
		
		
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		assertNotNull(client);
		assertNotNull(AccountConfig.testSwarmId);
		
		SwarmModel swarmInfo = client.get(AccountConfig.testSwarmId);
		
		assertTrue(swarmInfo != null);
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testList() throws IOException {
		testCreateSwarm();
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		assertNotNull(AccountConfig.testSwarmId);
		assertNotNull(client);
		
		List<SwarmModel> swarms = client.list();
				
		assertTrue(swarms != null);
		assertTrue(swarms.size() > 0);
		
		boolean swarmIdExists = false;
		for (SwarmModel sm : swarms) {
			System.out.println("testSwarmId: " + AccountConfig.testSwarmId + " SwarmModelId: " + sm.getId());
			if (sm.getId().equals(AccountConfig.testSwarmId)) {
				swarmIdExists = true;
				break;
			}
		}
		
		assertTrue(swarmIdExists);
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testDestroy() throws IOException {
		testCreateSwarm();
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		assertNotNull(client);
		assertNotNull(AccountConfig.testSwarmId);
		SwarmWSResponse rval = client.destroy(AccountConfig.testSwarmId);
				
		assertTrue(!rval.isError());
	}
	
	/**
	 * Test the SwarmWSResponse class.
	 */
	public void testSwarmWSResponse() {
		//Test that valid HTTP responses can be created.
		SwarmWSResponse rs = SwarmWSResponse.R200;
		
		assertNotNull(rs);
		assertEquals(rs.getCode(), 200);
		
		Integer[] validCodes = new Integer[] {200, 201, 400, 401, 403, 404, 409, 500};
		
		for (int i : Arrays.asList(validCodes)) {
			rs = SwarmWSResponse.fromCode(i);
			assertTrue(rs.getCode() == i);
			
			//Test that only 200, 201 are non-error codes.
			if (rs.getCode() == 200 || rs.getCode() == 201) {
				assertFalse(rs.isError());
			} else {
				assertTrue(rs.isError());
			}
		}
		
		//Test that invalid http code throws runtime exception.
		boolean caught = false;
		try {
			rs = SwarmWSResponse.fromCode(2);
		} catch (RuntimeException e) {
			caught = true;
		}
		
		assertTrue(caught);
	}
}
