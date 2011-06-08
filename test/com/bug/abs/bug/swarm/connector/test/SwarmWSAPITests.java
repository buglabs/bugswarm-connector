package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;
import com.buglabs.util.simplerestclient.HTTPException;

/**
 * Unit tests for ISwarmWSClient implementation.
 * 
 * @author kgilmer
 *
 */
public class SwarmWSAPITests extends TestCase {

	/**
	 * @throws IOException on error
	 */
	public void testCreateSwarm() throws IOException {
		assertNotNull(AccountConfig.getConfiguration());
		
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
		assertNotNull(client.getSwarmResourceClient());
		assertNotNull(AccountConfig.getConfiguration().getResource());
		
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
		
		String id = client.create(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());
		
		assertNotNull(id);
		assertTrue(id.length() > 0);
		
		AccountConfig.testSwarmId = id;
		
		//Creator must be added as member to swarm.
		SwarmWSResponse response = 
			client.getSwarmResourceClient().add(id, MemberType.CONSUMER, id, AccountConfig.getConfiguration().getResource());
		
		assertNotNull(response);
		assertFalse(response.isError());
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testUpdateSwarm() throws IOException {
		testCreateSwarm();
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
		SwarmWSResponse rval = client.update(AccountConfig.testSwarmId, true, "new description");
				
		assertTrue(!rval.isError());
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testGet() throws IOException {
		testCreateSwarm();
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
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
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());

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
	public void testVerifyAPIKey() throws IOException {
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		assertNotNull(client);
		
		assertTrue(client.isValid() == null);
		
		client = new SwarmWSClient(AccountConfig.getConfiguration().getHostname(Protocol.HTTP), "ohmyisthiskeyvalid");
		
		assertFalse(client.isValid() == null);
	}

	/**
	 * @throws IOException on error
	 */
	public void testDestroy() throws IOException {
		testCreateSwarm();
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
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
