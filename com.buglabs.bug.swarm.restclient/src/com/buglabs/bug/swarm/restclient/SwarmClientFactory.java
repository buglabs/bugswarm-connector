package com.buglabs.bug.swarm.restclient;

import com.buglabs.bug.swarm.restclient.impl.SwarmParticipationClient;
import com.buglabs.bug.swarm.restclient.impl.SwarmWSClient;

/**
 * Factory clients to get access to the client implementation.
 * 
 * @author kgilmer
 *
 */
public class SwarmClientFactory {
	
	private SwarmClientFactory() {
		
	}

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
	 * Get a new instance of ISwarmConfiguration client.  This offers identical functionality to ISwarmClient but offers a simpler flat API.
	 * @param hostname of swarm server
	 * @param apiKey of user
	 * @return new instance of ISwarmConfiguration
	 */
	public static ISwarmConfiguration getSwarmConfigurationClient(String hostname, String apiKey) {
		return new SwarmWSClient(hostname, apiKey);
	}
	
	public static ISwarmParticipation getParticipationClient(String hostname, String apiKey) {
		return new SwarmParticipationClient(hostname, apiKey);
	}
}
