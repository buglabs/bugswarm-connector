package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;

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
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
		String id = client.create(AccountConfig.getTestSwarmName(), true, AccountConfig.getTestSwarmDescription());
		
		assertNotNull(id);
		assertTrue(id.length() > 0);
		
		AccountConfig.testSwarmId = id;
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testUpdateSwarm() throws IOException {
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
		SwarmWSResponse rval = client.update(AccountConfig.testSwarmId, true, "new description");
				
		assertTrue(!rval.isError());
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testGet() throws IOException {
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
		SwarmModel swarmInfo = client.get(AccountConfig.testSwarmId);
		
		assertTrue(swarmInfo != null);
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testList() throws IOException {
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
		List<SwarmModel> swarms = client.list();
				
		assertTrue(swarms != null);
		assertTrue(swarms.size() > 0);
		
		boolean swarmIdExists = false;
		for (SwarmModel sm : swarms)
			if (sm.getId().equals(AccountConfig.testSwarmId)) {
				swarmIdExists = true;
				break;
			}
		
		assertTrue(swarmIdExists);
	}
	
	/**
	 * @throws IOException on error
	 */
	public void testVerifyAPIKey() throws IOException {
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
		assertTrue(client.isValid() == null);
		
		client = new SwarmWSClient(AccountConfig.getConfiguration().getHostname(), "ohmyisthiskeyvalid");
		
		assertFalse(client.isValid() == null);
	}

	/**
	 * @throws IOException on error
	 */
	public void testDestroy() throws IOException {
		SwarmWSClient client = new SwarmWSClient(AccountConfig.getConfiguration());
		
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
