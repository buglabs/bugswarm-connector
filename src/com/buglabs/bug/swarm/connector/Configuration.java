package com.buglabs.bug.swarm.connector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;

import com.buglabs.bug.swarm.connector.ui.SwarmConfigKeys;

/**
 * Convenience class to capture the configuration of a swarm connection.
 * 
 * @author kgilmer
 */
public class Configuration {

	/**
	 * Stores the configuration
	 */
	private Dictionary<String, String> config;

	/**
	 * This configuration will create a member value from the machine.
	 * 
	 * @param hostname
	 * @param api_key
	 * @param resource
	 */
	public Configuration(String hostname, String api_key, String username) {
		this(hostname, api_key, username, getMachineResource());		
	}
	
	/**
	 * Create a configuration
	 * 
	 * @param hostname
	 * @param api_key
	 * @param resource
	 */
	public Configuration(String hostname, String api_key, String username, String resource) {
		config = new Hashtable<String, String>();
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER, hostname);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME, username);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_APIKEY, api_key);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_RESOURCE, resource);		
	}
	
	/**
	 * Determine some string that can be used to identify the client,
	 * Preferably across OS updates, etc..
	 * 
	 * @return
	 */
	private static String getMachineResource() {
		//TODO: have this get the ethernet mac address like it does in DP1.
		try {
		    return InetAddress.getLocalHost().getHostName();		    
		} catch (UnknownHostException e) {
			Random r = new Random();
			return "UNKNOWNHOST-" + r.nextDouble();
		}
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
	public String getAPIKey() {
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_APIKEY);
	}
	
	/**
	 * @return
	 */
	public String getResource() {
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_RESOURCE);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " (" + getHostname() + ", " + getUsername() + ", " + getAPIKey() + ", " + getResource() +")";
	}
	
	/**
	 * @return
	 */
	public String getUsername() {
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME);
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
			!hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_RESOURCE) ||
			!hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER) ||
			!hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_APIKEY) || 
			!hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME))
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
