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
	private static final String XMPP_USERNAME2 = "test";
	private static final String API_KEY2 = "a0fc6588f11db4a1f024445e950ae6ae33bc0313";
	
	private static final String API_KEY = "76aa4ce0d07e4a41e5018de58445b1e4d7812711";
	private static final String SWARM_HOST = "api.bugswarm.net";
	
	private static final String XMPP_USERNAME = "connector-test";
	//private static final String XMPP_RESOURCE = "Psi";
	
	protected static String testSwarmName;
	protected static String testSwarmId;
	// helper methods
	private Configuration config;
	
	protected Configuration getConfiguration() {
		if (config == null) {
			config = new Configuration(SWARM_HOST, API_KEY, XMPP_USERNAME);
		}
		
		return config;
	}
	
	protected Configuration getConfiguration2() {
		if (config == null) {
			config = new Configuration(SWARM_HOST, API_KEY2, XMPP_USERNAME2);
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