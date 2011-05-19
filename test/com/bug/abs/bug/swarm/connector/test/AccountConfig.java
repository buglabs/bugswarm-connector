package com.bug.abs.bug.swarm.connector.test;

import java.util.Random;

import com.buglabs.bug.swarm.connector.Configuration;

/**
 * Unit tests for ISwarmWSClient implementation
 * 
 * @author kgilmer
 *
 */
public final class AccountConfig {
	protected static final String XMPP_USERNAME2 = "test";
	protected static final String API_KEY2 = "a0fc6588f11db4a1f024445e950ae6ae33bc0313";
	
	protected static final String API_KEY = "76aa4ce0d07e4a41e5018de58445b1e4d7812711";
	protected static final String SWARM_HOST = "bugswarm.net";
	
	protected static final String XMPP_USERNAME = "connector-test";
	//private static final String XMPP_RESOURCE = "Psi";
	
	protected static String testSwarmName;
	protected static String testSwarmId;
	// helper methods
	private static Configuration config;
	private static Configuration xmppconfig;
	
	protected static Configuration getConfiguration() {
		if (config == null) {
			config = new Configuration(SWARM_HOST, API_KEY, XMPP_USERNAME);
		}
		
		return config;
	}
	
	protected static Configuration getXmppConfiguration() {
		if (xmppconfig == null) {
			xmppconfig = new Configuration(SWARM_HOST, API_KEY, XMPP_USERNAME);
		}
		
		return xmppconfig;
	}
	
	protected static Configuration getConfiguration2() {
		if (config == null) {
			config = new Configuration(SWARM_HOST, API_KEY2, XMPP_USERNAME2);
		}
		
		return config;
	}

	protected static String getTestSwarmName() {		
		if (testSwarmName == null) {
			Random r = new Random();
			testSwarmName = "TestSwarm-" + AccountConfig.class.getSimpleName() + r.nextFloat();		
		}
		
		return testSwarmName;		
	}
	
	/**
	 * @return
	 */
	protected static String generateRandomSwarmName() {		
		Random r = new Random();
		return "TestSwarm-" + AccountConfig.class.getSimpleName() + r.nextFloat();					
	}

	protected static String getTestSwarmDescription() {
		return "TestSwarmDescription-" + AccountConfig.class.getSimpleName();
	}
}