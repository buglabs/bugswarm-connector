package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;

/**
 * Unit tests for ISwarmWSClient implementation
 * 
 * @author kgilmer
 *
 */
public class SwarmWSAPITests extends TestCase {

	public void testCreateSwarm() throws IOException {
		SwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		
		String id = client.create(TestUtil.getTestSwarmName(), true, TestUtil.getTestSwarmDescription());
		
		assertNotNull(id);
		assertTrue(id.length() > 0);
		
		TestUtil.testSwarmId = id;
	}
	
	public void testUpdateSwarm() throws IOException {
		SwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		
		SwarmWSResponse rval = client.update(TestUtil.testSwarmId, true, "new description");
				
		assertTrue(!rval.isError());
	}
	
	public void testGet() throws IOException {
		SwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		
		SwarmModel swarmInfo = client.get(TestUtil.testSwarmId);
		
		assertTrue(swarmInfo != null);
	}
	
	public void testList() throws IOException {
		SwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		
		List<SwarmModel> swarms = client.list();
				
		assertTrue(swarms != null);
		assertTrue(swarms.size() > 0);
		
		boolean swarmIdExists = false;
		for (SwarmModel sm: swarms)
			if (sm.getId().equals(TestUtil.testSwarmId)) {
				swarmIdExists = true;
				break;
			}
		
		assertTrue(swarmIdExists);
	}
	
	public void testVerifyAPIKey() throws IOException {
		SwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		
		assertTrue(client.isValid() == null);
		
		client = new SwarmWSClient(TestUtil.getConfiguration().getHostname(), "ohmyisthiskeyvalid");
		
		assertFalse(client.isValid() == null);
	}

	public void testDestroy() throws IOException {
		SwarmWSClient client = new SwarmWSClient(TestUtil.getConfiguration());
		
		SwarmWSResponse rval = client.destroy(TestUtil.testSwarmId);
				
		assertTrue(!rval.isError());
	}
}
