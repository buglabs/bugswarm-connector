package com.buglabs.bug.swarm.connector.test;

import java.util.Random;

import com.buglabs.bug.swarm.connector.Configuration;

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
	
	protected static final int CONNECTOR_INIT_SLEEP_MILLIS = 10000;
	public static final long CONNECTOR_FEED_CHANGE_SLEEP_MILLIS = 1000;
	
	/**
	 * @return
	 */
	protected static Configuration getConfiguration() {
		if (config == null) {
			config = new Configuration(getHostSystemProperty(), getConfigurationAPIKeySystemProperty(), 
					getProducerAPIKeySystemProperty(), 
					XMPP_USERNAME, DEFAULT_HTTP_SERVER_PORT, DEFAULT_XMPP_SERVER_PORT);
		}
		
		return config;
	}	

	/**
	 * @return
	 */
	protected static Configuration getConfiguration2() {
		if (config == null) {
			config = new Configuration(getHostSystemProperty2(), getConfigurationAPIKeySystemProperty2(), 
					getProducerAPIKeySystemProperty2(),
					XMPP_USERNAME2, DEFAULT_HTTP_SERVER_PORT, DEFAULT_XMPP_SERVER_PORT);
		}
		
		return config;
	}
	
	private static String getHostSystemProperty() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test host must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split(",")[0];
	}
	
	private static String getConfigurationAPIKeySystemProperty() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split(",")[1];
	}

	private static String getProducerAPIKeySystemProperty() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split(",")[2];
	}
	
	private static String getHostSystemProperty2() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test host must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split(",")[3];
	}
	
	private static String getConfigurationAPIKeySystemProperty2() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split(",")[4];
	}

	private static String getProducerAPIKeySystemProperty2() {
		if (System.getProperty(SWARM_TEST_HOSTNAME_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_HOSTNAME_KEY);
		
		return System.getProperty(SWARM_TEST_HOSTNAME_KEY).split(",")[5];
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
