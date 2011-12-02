package com.buglabs.bug.swarm.client.model;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;

import com.buglabs.bug.swarm.client.test.SwarmConfigKeys;

/**
 * Convenience class to capture the configuration of a swarm connection.
 * 
 * @author kgilmer
 */
public class Configuration {
	/**
	 * Default hostname for swarm server if none specified as a system property.
	 */
	public static final String DEFAULT_HOSTNAME = "bugswarm.net";

	/**
	 * Defines which protocol is requested when getting server URL.
	 * 
	 * @author kgilmer
	 * 
	 */
	public enum Protocol {
		HTTP, XMPP
	};

	private static final String HTTP_SCHEME = "HTTP://";
	private static final String XMPP_PREFIX = "";
	private static final String HTTP_PREFIX = "api.";	
	public static final int DEFAULT_XMPP_SERVER_PORT = 5222;
	public static final int DEFAULT_HTTP_SERVER_PORT = 80;
	private static final Random rnd = new Random();
	private static final String DEFAULT_DEVICE_LABEL = "BUG-" + Integer.toHexString(rnd.nextInt());
	
	/**
	 * Stores the configuration.
	 */
	private final Dictionary<String, Object> config;

	/**
	 * Create a configuration from discrete parameters.
	 * 
	 * @param resourceId resourceid as generated from the server or null if not available.
	 * @param hostname
	 *            name of host without scheme or prefix for protocol, such as
	 *            "api."
	 * @param consumerApiKey
	 *            API_KEY as defined by server
	 * @param producerApiKey
	 *            API_KEY as defined by server
	 * @param username
	 *            swarm username
	 * @param httpPort port of WS API server
	 * @param xmppPort port of messaging server
	 * 
	 */
	public Configuration(final String resourceId, final String hostname, final String consumerApiKey, 
			final String producerApiKey, final String username, final String deviceLabel, final int httpPort, final int xmppPort) {
		if (hostname.contains("://"))
			throw new IllegalArgumentException("Hostname must note include a scheme.");

		config = new Hashtable<String, Object>();
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER, hostname);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME, username);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY, consumerApiKey);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY, producerApiKey);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED, Boolean.toString(true));
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_HTTP_PORT, httpPort);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_XMPP_PORT, xmppPort);
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_DEVICE_LABEL, deviceLabel);
		
		if (resourceId != null)
			config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_RESOURCE_ID, resourceId);
	}

	/**
	 * @param protocol
	 *            The protocol type requested for hostname
	 * @return a server name with scheme
	 */
	public String getHostname(final Protocol protocol) {
		switch (protocol) {
		case HTTP:
			if (getHTTPPort() == DEFAULT_HTTP_SERVER_PORT)
				return HTTP_SCHEME + HTTP_PREFIX + config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER);
			else
				return HTTP_SCHEME + HTTP_PREFIX + config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER) 
							+ ":" + getHTTPPort();
		case XMPP:
			return XMPP_PREFIX + config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER);
		default:
			throw new IllegalArgumentException("Unknown protocol");
		}
	}
	
	/**
	 * @return HTTP port for WS API.
	 */
	public int getHTTPPort() {
		if (config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_HTTP_PORT) != null) {
			try {
				return Integer.parseInt(config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_HTTP_PORT).toString());
			} catch (NumberFormatException e) {			
			}
		}
		
		return DEFAULT_HTTP_SERVER_PORT;
	}
	
	/**
	 * @return XMPP Port for Messaging API.
	 */
	public int getXMPPPort() {
		if (config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_XMPP_PORT) != null) {
			try {
				return Integer.parseInt(config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_XMPP_PORT).toString());
			} catch (NumberFormatException e) {
			}
		}
		
		return DEFAULT_XMPP_SERVER_PORT;
	}

	/**
	 * @return the client API_KEY
	 */
	public String getConfingurationAPIKey() {
		if (config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY) == null)
			return "[NONE]";
		
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY).toString();
	}
	
	/**
	 * @return the producer API_KEY
	 */
	public String getParticipationAPIKey() {
		if (config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY) == null) 
			return "[NONE]";
		
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY).toString();
	}

	/**
	 * @return true if the resourceId has been set in the configuration.
	 */
	public boolean hasResource() {
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_RESOURCE_ID) != null;
	}
	
	/**
	 * @return The resource associated with device.
	 */
	public String getResource() {
		if (!hasResource())
			return null;
		
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_RESOURCE_ID).toString();
	}
	
	/**
	 * Set the resourceId for the device as generated by the server.
	 * 
	 * @param resourceId resource id
	 */
	public void setResourceId(String resourceId) {
		//TODO: consider making this only settable once.
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_RESOURCE_ID, resourceId);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " (" 
				+ config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER) + ", " + getUsername()
				+ ", " + getConfingurationAPIKey() + ", " + getParticipationAPIKey() + ", " + getResource() + ")";
	}
	
	/**
	 * @return the device label (nickname) for resource.
	 */
	public String getDeviceLabel() {
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_DEVICE_LABEL).toString();
	}

	/**
	 * @return the username as defined on client.
	 */
	public String getUsername() {
		return config.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME).toString();
	}

	/**
	 * A local-only check to see if a configuration appears to be valid.
	 * 
	 * @param config
	 *            Dictionary of configuration
	 * @return true if the dictionary has values for the necessary keys to
	 *         create a swarm connection.
	 */
	public static boolean isValid(final Dictionary<String, Object> config) {
		if (config.isEmpty())
			return false;

		if (!hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED)
				|| !hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY)
				|| !hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY)
				|| !hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME)
				|| !hasEntry(config, SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER))
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
	 * @param dictionary
	 *            Dictionary to check key
	 * @param key
	 *            to check in dictionary
	 * @return true if the passed dictionary contains a key and a value that
	 *         does not evaluate to empty string or null.
	 */
	private static boolean hasEntry(final Dictionary<String, Object> dictionary, final String key) {
		if (dictionary.get(key) == null)
			return false;

		if (dictionary.get(key) instanceof String && ((String) dictionary.get(key)).trim().length() == 0)
			return false;

		return true;
	}

	public void setParticipationAPIKey(String key) {
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY, key);
	}

	public void setConfingurationAPIKey(String key) {
		config.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY, key);
	}
}
