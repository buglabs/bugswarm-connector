package com.bug.abs.bug.swarm.connector.test;

import java.util.Random;

import com.buglabs.bug.swarm.connector.Configuration;

/**
 * Unit tests for ISwarmWSClient implementation.
 * 
 * @author kgilmer
 *
 */
public final class AccountConfig {
	protected static final String XMPP_USERNAME2 = "test";
	protected static final String API_KEY2 = "df6fc25c0edcb2d76a7930754f37c33c5d009705";
	
	protected static final String API_KEY = "7339d4a60c729308086341600d44c6424a4079cb"; 
	
	protected static final String SWARM_HOST = "bugswarm.net";
	
	protected static final String XMPP_USERNAME = "connector_test";
	
	protected static String testSwarmId;
	
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
