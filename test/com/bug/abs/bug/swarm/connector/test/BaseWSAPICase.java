package com.bug.abs.bug.swarm.connector.test;

import java.util.Random;

import com.buglabs.bug.swarm.connector.Configuration;

import junit.framework.TestCase;

/**
 * Unit tests for ISwarmWSClient implementation
 * 
 * @author kgilmer
 *
 */
public abstract class BaseWSAPICase extends TestCase {
	private static final String API_KEY = "a0fc6588f11db4a1f024445e950ae6ae33bc0313";
	private static final String SWARM_HOST = "http://api.bugswarm.net";
	
	private static final String XMPP_USERNAME = "connector-test";
	private static final String XMPP_RESOURCE = "Psi";
	
	protected static String testSwarmName;
	protected static String testSwarmId;
	// helper methods
	private Configuration config;
	
	protected Configuration getConfiguration() {
		if (config == null) {
			config = new Configuration(SWARM_HOST, API_KEY, XMPP_USERNAME, XMPP_RESOURCE);
		}
		
		return config;
	}

	protected String getTestSwarmName() {		
		if (testSwarmName == null) {
			Random r = new Random();
			testSwarmName = "TestSwarm-" + this.getClass().getSimpleName() + r.nextFloat();		
		}
		
		return testSwarmName;		
	}
	
	/**
	 * @return
	 */
	protected String generateRandomSwarmName() {		

		Random r = new Random();
		return "TestSwarm-" + this.getClass().getSimpleName() + r.nextFloat();					
	}

	protected String getTestSwarmDescription() {
		return "TestSwarmDescription-" + this.getClass().getSimpleName();
	}
}