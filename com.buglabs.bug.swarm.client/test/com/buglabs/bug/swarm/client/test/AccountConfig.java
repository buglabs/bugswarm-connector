package com.buglabs.bug.swarm.client.test;



import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.buglabs.bug.swarm.client.ISwarmKeysClient;
import com.buglabs.bug.swarm.client.ISwarmKeysClient.KeyType;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.model.Configuration;
import com.buglabs.bug.swarm.client.model.SwarmKey;
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
			ISwarmKeysClient keyClient = SwarmClientFactory.getAPIKeyClient("api." + getHostSystemProperty());
			try {
				List<SwarmKey> keys = keyClient.create(getUsernameProperty(), getUsernameProperty(), null);
				
				String configurationKey = null;
				String participationKey = null;
				
				for (SwarmKey key : keys) {
					if (key.getType() == KeyType.CONFIGURATION)
						configurationKey = key.getKey();
					else if (key.getType() == KeyType.PARTICIPATION)
						participationKey = key.getKey();
				}
				
				if (configurationKey == null || participationKey == null)
					throw new IllegalStateException("Invalid API keys.");
				
				config = new Configuration(null, 
						getHostSystemProperty(), 
						configurationKey, 
						participationKey, 
						getUsernameProperty(),
						"devicelabel",
						DEFAULT_HTTP_SERVER_PORT, 
						DEFAULT_XMPP_SERVER_PORT);				
			} catch (IOException e) {
				throw new IllegalStateException("Unable to create API keys.", e);
			}
		}
		
		return config;
	}	

	/**
	 * @return
	 */
	public static Configuration getConfiguration2() {
		if (config2 == null) {
			ISwarmKeysClient keyClient = SwarmClientFactory.getAPIKeyClient("api." + getHostSystemProperty());
			try {
				List<SwarmKey> keys = keyClient.create(getUsernameProperty2(), getUsernameProperty2(), null);
				
				String configurationKey = null;
				String participationKey = null;
				
				for (SwarmKey key : keys) {
					if (key.getType() == KeyType.CONFIGURATION)
						configurationKey = key.getKey();
					else if (key.getType() == KeyType.PARTICIPATION)
						participationKey = key.getKey();
				}
				
				if (configurationKey == null || participationKey == null)
					throw new IllegalStateException("Invalid API keys.");
				
				config2 = new Configuration(null, 
						getHostSystemProperty(), 
						configurationKey, 
						participationKey, 
						"devicelabel2",
						getUsernameProperty2(),
						DEFAULT_HTTP_SERVER_PORT, 
						DEFAULT_XMPP_SERVER_PORT);				
			} catch (IOException e) {
				throw new IllegalStateException("Unable to create API keys.");
			}
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
	
	private static String getUsernameProperty2() {
		if (System.getProperty(SWARM_TEST_CONFIGURATION_KEY) == null)
			throw new RuntimeException("Test API Key must be defined to execute tests: " + SWARM_TEST_CONFIGURATION_KEY);
		
		return System.getProperty(SWARM_TEST_CONFIGURATION_KEY).split(",")[2];
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
