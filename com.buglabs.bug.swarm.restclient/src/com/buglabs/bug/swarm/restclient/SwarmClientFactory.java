package com.buglabs.bug.swarm.restclient;

import com.buglabs.bug.swarm.restclient.impl.SwarmWSClient;

/**
 * Factory for OSGi clients to get access to the implementation without
 * requiring access to the OSGi service registry.
 * 
 * @author kgilmer
 *
 */
public class SwarmClientFactory {

	/**
	 * Get a new instance of ISwarmClient
	 * @param hostname of swarm server
	 * @param apiKey of user
	 * @return new instance of ISwarmClient
	 */
	public static ISwarmClient getSwarmClient(String hostname, String apiKey) {
		return new SwarmWSClient(hostname, apiKey);
	}
	
	/**
	 * Get a new instance of ISwarmClient
	 * @param hostname of swarm server
	 * @param apiKey of user
	 * @return new instance of ISwarmClient
	 */
	public static ISwarmConfiguration getSwarmConfigurationClient(String hostname, String apiKey) {
		return new SwarmWSClient(hostname, apiKey);
	}
}
