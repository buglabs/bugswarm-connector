package com.buglabs.bug.swarm.connector.test;

import java.util.Random;

import com.buglabs.bug.swarm.connector.Configuration;
import com.buglabs.bug.swarm.connector.model.UserResourceModel;

/**
 * Unit tests for ISwarmWSClient implementation.
 * 
 * @author kgilmer
 *
 */
public final class AccountConfig {
	protected static final String XMPP_USERNAME2 = "connector_test2";
	protected static final String XMPP_USERNAME = "connector_test";
	private static final String SWARM_TEST_HOSTNAME_KEY = "report.misc";
	private static final int DEFAULT_XMPP_SERVER_PORT = 5222;
	private static final int DEFAULT_HTTP_SERVER_PORT = 80;
	
	protected static String testSwarmId;
	
	private static Configuration config;
	private static Configuration xmppconfig;
	private static Configuration config2;
	public static UserResourceModel testUserResource2;
	
	protected static final int CONNECTOR_INIT_SLEEP_MILLIS = 10000;
	public static final long CONNECTOR_FEED_CHANGE_SLEEP_MILLIS = 1000;
	
	/**
	 * @return
	 */
	protected static Configuration getConfiguration() {
		if (config == null) {
			config = new Configuration(getHostSystemProperty(), getAPIKeySystemProperty(), 
					XMPP_USERNAME, DEFAULT_HTTP_SERVER_PORT, DEFAULT_XMPP_SERVER_PORT);
		}
		
		return config;
	}	

	/**
	 * @return
	 */
	protected static Configuration getXmppConfiguration() {
		if (xmppconfig == null) {
			xmppconfig = new Configuration(getHostSystemProperty(), getAPIKeySystemProperty(), 
					XMPP_USERNAME, DEFAULT_HTTP_SERVER_PORT, DEFAULT_XMPP_SERVER_PORT);
		}
		
		return xmppconfig;
	}
	
	/**
	 * @return
	 */
	protected static Configuration getConfiguration2() {
		if (config2 == null) {
			config2 = new Configuration(getHostSystemProperty(), getAPIKey2SystemProperty(), 
					XMPP_USERNAME2, DEFAULT_HTTP_SERVER_PORT, DEFAULT_XMPP_SERVER_PORT);
		}
		
		return config2;
	}
	
	private static String getHostSystemProperty() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test host must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split(",")[0];
	}
	
	private static String getAPIKeySystemProperty() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split(",")[1];
	}

	private static String getAPIKey2SystemProperty() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split(",")[2];
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
