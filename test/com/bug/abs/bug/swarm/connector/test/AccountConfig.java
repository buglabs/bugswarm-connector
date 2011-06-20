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
	
	//protected static final String API_KEY = "7339d4a60c729308086341600d44c6424a4079cb"; 
	
	//protected static final String SWARM_HOST = "bugswarm.net";
	
	protected static final String XMPP_USERNAME = "connector_test";
	private static final String SWARM_TEST_HOSTNAME_KEY = "report.misc";
	
	protected static String testSwarmId;
	
	private static Configuration config;
	private static Configuration xmppconfig;
	
	protected static Configuration getConfiguration() {
		if (config == null) {
			config = new Configuration(getHostSystemProperty(), getAPIKeySystemProperty(), XMPP_USERNAME);
		}
		
		return config;
	}	

	protected static Configuration getXmppConfiguration() {
		if (xmppconfig == null) {
			xmppconfig = new Configuration(getHostSystemProperty(), getAPIKeySystemProperty(), XMPP_USERNAME);
		}
		
		return xmppconfig;
	}
	
	protected static Configuration getConfiguration2() {
		if (config == null) {
			config = new Configuration(getHostSystemProperty(), API_KEY2, XMPP_USERNAME2);
		}
		
		return config;
	}
	
	private static String getHostSystemProperty() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test host must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split("&")[0];
	}
	
	private static String getAPIKeySystemProperty() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split("&")[1];
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
