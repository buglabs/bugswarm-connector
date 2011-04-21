package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;

/**
 * Unit tests for ISwarmWSClient implementation
 * 
 * @author kgilmer
 *
 */
public class SwarmWSAPITests extends BaseWSAPICase {

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
}
