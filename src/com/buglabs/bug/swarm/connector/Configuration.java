package com.buglabs.bug.swarm.connector;

import java.util.Dictionary;
import java.util.Hashtable;

import com.buglabs.bug.swarm.connector.ui.SwarmConfigKeys;

/**
 * Convenience class to capture the configuration of a swarm connection.
 * @author kgilmer
 *
 */
public class Configuration {

	
	private Dictionary<String, String> config;

	/**
	 * @param hostname
	 * @param api_key
	 * @param username
	 */
	public Configuration(String hostname, String api_key, String username) {
		config = new Hashtable<String, String>();
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER, hostname);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_NICKNAME, api_key);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERKEY, username);		
	}
	
	/**
	 * @param config
	 */
	public Configuration(Dictionary<String, String> config) {
		this.config = config;
	}

	/**
	 * @return
	 */
	public String getHostname() {
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER);
	}
	
	/**
	 * @return
	 */
	public String getApi_key() {
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_NICKNAME);
	}
	
	/**
	 * @return
	 */
	public String getUsername() {
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERKEY);
	}
	
	/**
	 * A local-only check to see if a configuration appears to be valid.
	 * @param config
	 * @return true if the dictionary has values for the necessary keys to create a swarm connection.
	 */
	public static boolean isValid(Dictionary<String, String> config) {
		if (config.isEmpty())
			return false;
		
		if (!hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED) ||
			!hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_NICKNAME) ||
			!hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER) ||
			!hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERKEY))
			return false;
		
		if (!Boolean.parseBoolean(config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED).toString()))
			return false;
		
		return true;
	}
	
	/**
	 * @return true if this instance is valid.
	 */
	public boolean isValid() {
		return isValid(this.config);
	}
	
	/**
	 * @param d
	 * @param key
	 * @return true if the passed dictionary contains a key and a value that does not evaluate to empty string or null.
	 */
	private static boolean hasEntry(Dictionary<String, String> d, String key) {
		if (d.get(key) == null)
			return false;
		
		if (d.get(key).trim().length() == 0)
			return false;
		
		return true;
	}
}
