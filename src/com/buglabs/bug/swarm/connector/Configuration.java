package com.buglabs.bug.swarm.connector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
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
	public enum Protocol {
		HTTP, XMPP
	};
	
	private static final String HTTP_SCHEME = "HTTP://";
	private static final String XMPP_PREFIX = "xmpp.";
	private static final String HTTP_PREFIX = "api.";
	private static final String BUG20_SYSFS_MACADDR_FILE = "/sys/devices/platform/ehci-omap.0/usb1/1-2/1-2.4/1-2.4:1.0/net/eth0/address";
	/**
	 * Stores the configuration
	 */
	private Dictionary<String, String> config;
	private String resource;

	
	/**
	 * Create a configuration
	 * 
	 * @param hostname
	 * @param api_key
	 * @param resource
	 */
	public Configuration(String hostname, String api_key, String username) {
		if (hostname.contains("://"))
			throw new IllegalArgumentException("Hostname must note include a scheme.");
		
		config = new Hashtable<String, String>();
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER, hostname);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME, username);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_APIKEY, api_key);
		resource = getMachineResource();		
	}
	
	/**
	 * Determine some string that can be used to identify the client,
	 * Preferably across OS updates, etc..
	 * 
	 * @return
	 */
	private static String getMachineResource() {
		try {
			File f = new File(BUG20_SYSFS_MACADDR_FILE);
			if (f.exists() && f.isFile())
				return readFirstLine(f);
		    return InetAddress.getLocalHost().getHostName();		    
		} catch (Exception e) {
			Random r = new Random();
			return "UNKNOWNHOST-" + r.nextDouble();
		}
	}

	/**
	 * Return the first line of a file as a String.
	 * 
	 * @param f file to be read
	 * @return first line as String
	 * @throws IOException if there is an IO error
	 */
	private static String readFirstLine(File f) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		br.close();
		
		return line;
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
	public String getHostname(Protocol protocol) {
		switch (protocol) {
		case HTTP:
			return HTTP_SCHEME + HTTP_PREFIX + config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER);
		case XMPP:
			return XMPP_PREFIX + config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER);
		}
		
		throw new IllegalArgumentException("Unknown protocol");
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
		return resource;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " (" + config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER) + ", " + getUsername() + ", " + getAPIKey() + ", " + getResource() +")";
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
