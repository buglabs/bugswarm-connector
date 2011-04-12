package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;

import junit.framework.TestCase;

/**
 * Unit tests for ISwarmWSClient implementation
 * 
 * @author kgilmer
 *
 */
public class SwarmWSAPITests extends TestCase {
	public static final String API_KEY = "a0fc6588f11db4a1f024445e950ae6ae33bc0313";
	public static final String SWARM_HOST = "http://api.bugswarm.net";
	
	private static String testSwarmName;
	private static String testSwarmId;
	
	public void testCreateSwarm() throws IOException {
		SwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		
		String id = client.create(getTestSwarmName(), true, getTestSwarmDescription());
		
		assertNotNull(id);
		assertTrue(id.length() > 0);
		
		testSwarmId = id;
	}
	
	public void testUpdateSwarm() throws IOException {
		SwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		
		int rval = client.update(testSwarmId, true, "new description");
				
		assertTrue(rval == 200);
	}
	
	public void testGet() throws IOException {
		SwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		
		SwarmModel swarmInfo = client.get(testSwarmId);
		
		assertTrue(swarmInfo != null);
	}
	
	public void testList() throws IOException {
		SwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		
		List<SwarmModel> swarms = client.list();
				
		assertTrue(swarms != null);
		assertTrue(swarms.size() > 0);
		
		boolean swarmIdExists = false;
		for (SwarmModel sm: swarms)
			if (sm.getId().equals(testSwarmId))
				swarmIdExists = true;
		
		assertTrue(swarmIdExists);
	}
	
	public void testVerifyAPIKey() throws IOException {
		SwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		
		assertTrue(client.isValid());
		
		client = new SwarmWSClient(SWARM_HOST, "ohmyisthiskeyvalid");
		
		assertFalse(client.isValid());
	}

	public void testDestroy() throws IOException {
		SwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		
		int rval = client.destroy(testSwarmId);
				
		assertTrue(rval == 200);
	}

	// Private helper methods

	private String getTestSwarmName() {		
		if (testSwarmName == null) {
			Random r = new Random();
			testSwarmName = "TestSwarm" + this.getClass().getSimpleName() + r.nextFloat();		
		}
		
		return testSwarmName;		
	}

	private String getTestSwarmDescription() {
		return "TestSwarmDescription" + this.getClass().getSimpleName();
	}
}
