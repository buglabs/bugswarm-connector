package com.bug.abs.bug.swarm.connector.test;

import java.io.IOException;

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
	
	public void testCreateSwarm() throws IOException {
		SwarmWSClient client = new SwarmWSClient(SWARM_HOST, API_KEY);
		
		String id = client.create(getTestSwarmName(), true, getTestSwarmDescription());
		
		assertNotNull(id);
	}

	private String getTestSwarmName() {		
		return "TestSwarm" + this.getClass().getSimpleName();
	}

	private String getTestSwarmDescription() {
		return "TestSwarmDescription" + this.getClass().getSimpleName();
	}
}
