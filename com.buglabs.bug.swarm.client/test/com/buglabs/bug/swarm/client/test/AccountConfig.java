package com.buglabs.bug.swarm.client.test;



import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.buglabs.bug.swarm.client.model.UserResourceModel;

/**
 * Unit tests for ISwarmWSClient implementation.
 * 
 * Format for system property:
 * 
 * report.misc = [server_hostname],[user1],[c_apikey1],[p_apikey1],[user2],[c_apikey2],[p_apikey2]
 * 
 * @author kgilmer
 *
 */
public final class AccountConfig {
	private static final String SWARM_TEST_CONFIGURATION_KEY = "report.misc";
	private static final int DEFAULT_XMPP_SERVER_PORT = 5222;
	private static final int DEFAULT_HTTP_SERVER_PORT = 80;
	
	public static String testSwarmId;
	
	private static Configuration config;
	private static Random r;
	public static UserResourceModel testUserResource;
	public static UserResourceModel testUserResource2;
	public static UserResourceModel testUserResource1;
	private static Configuration config2;
	public static String testInviteId;
	
	public static final int CONNECTOR_INIT_SLEEP_MILLIS = 10000;
	public static final long CONNECTOR_FEED_CHANGE_SLEEP_MILLIS = 1000;
	
	/**
	 * @return
	 */
	public static Configuration getConfiguration() {
		if (config == null) {
			config = new Configuration(null, 
					getHostSystemProperty(), 
					getConfigurationAPIKeySystemProperty(), 
					getProducerAPIKeySystemProperty(), 
					getUsernameProperty(),
					DEFAULT_HTTP_SERVER_PORT, 
					DEFAULT_XMPP_SERVER_PORT);
		}
		
		return config;
	}	

	/**
	 * @return
	 */
	public static Configuration getConfiguration2() {
		if (config2 == null) {
			config2 = new Configuration(
					null,
					getHostSystemProperty(), 
					getConfigurationAPIKeySystemProperty2(), 
					getProducerAPIKeySystemProperty2(),
					getUsernameProperty2(), 
					DEFAULT_HTTP_SERVER_PORT, 
					DEFAULT_XMPP_SERVER_PORT);
		}
		
		return config2;
	}
	
	private static String getHostSystemProperty() {
		if (System.getProperty(SWARM_TEST_CONFIGURATION_KEY) == null)
			throw new RuntimeException("Test host must be defined to execute tests: " + SWARM_TEST_CONFIGURATION_KEY);
		
		return System.getProperty(SWARM_TEST_CONFIGURATION_KEY).split(",")[0];
	}
	
	private static String getUsernameProperty() {
		if (System.getProperty(SWARM_TEST_CONFIGURATION_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_CONFIGURATION_KEY);
		
		return System.getProperty(SWARM_TEST_CONFIGURATION_KEY).split(",")[1];
	}
	
	private static String getConfigurationAPIKeySystemProperty() {
		if (System.getProperty(SWARM_TEST_CONFIGURATION_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_CONFIGURATION_KEY);
		
		return System.getProperty(SWARM_TEST_CONFIGURATION_KEY).split(",")[2];
	}

	private static String getProducerAPIKeySystemProperty() {
		if (System.getProperty(SWARM_TEST_CONFIGURATION_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_CONFIGURATION_KEY);
		
		return System.getProperty(SWARM_TEST_CONFIGURATION_KEY).split(",")[3];
	}
	
	private static String getUsernameProperty2() {
		if (System.getProperty(SWARM_TEST_CONFIGURATION_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_CONFIGURATION_KEY);
		
		return System.getProperty(SWARM_TEST_CONFIGURATION_KEY).split(",")[4];
	}
	
	private static String getConfigurationAPIKeySystemProperty2() {
		if (System.getProperty(SWARM_TEST_CONFIGURATION_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_CONFIGURATION_KEY);
		
		return System.getProperty(SWARM_TEST_CONFIGURATION_KEY).split(",")[5];
	}

	private static String getProducerAPIKeySystemProperty2() {
		if (System.getProperty(SWARM_TEST_CONFIGURATION_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_CONFIGURATION_KEY);
		
		return System.getProperty(SWARM_TEST_CONFIGURATION_KEY).split(",")[6];
	}

	/**
	 * @return
	 */
	public static String generateRandomSwarmName() {		
		if (r == null)
			r = new Random();
		
		return "TestSwarm-" + AccountConfig.class.getSimpleName() + r.nextFloat();					
	}
	
	/**
	 * @return
	 */
	public static String generateRandomResourceName() {		
		if (r == null)
			r = new Random();
		
		return "TestResource-" + r.nextFloat();					
	}

	public static String getTestSwarmDescription() {
		return "TestSwarmDescription-" + AccountConfig.class.getSimpleName();
	}

	public static Map<String, ?> generateRandomPayload() {
		Map<String, String> m = new HashMap<String, String>();
		
		if (r == null)
			r = new Random();
		
		int kc = r.nextInt(10) + 5;
		
		for (int i = 0; i < kc; ++i)
			m.put("key-" + Float.toHexString(r.nextFloat()), Float.toHexString(r.nextFloat()));
		
		return m;
	}
}
