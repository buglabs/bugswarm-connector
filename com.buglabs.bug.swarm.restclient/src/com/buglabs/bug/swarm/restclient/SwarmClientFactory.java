package com.buglabs.bug.swarm.restclient;

import java.io.IOException;
import java.net.UnknownHostException;

import com.buglabs.bug.swarm.restclient.impl.SwarmSessionImp;
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
	
	/**
	 * Create a session with a swarm server.  This session allows for participation (presence, messages) among swarm clients.
	 * 
	 * @param hostname of swarm server
	 * @param apiKey of user
	 * @param resourceId associated with session
	 * @param swarmIds swarms to connect with
	 * @return a swarm session
	 * @throws UnknownHostException if unable to resolve hostname
	 * @throws IOException on I/O error
	 */
	public static ISwarmSession createSwarmSession(String hostname, String apiKey, String resourceId, String ... swarmIds) throws UnknownHostException, IOException {
		return new SwarmSessionImp(hostname, apiKey, hostname, apiKey);
	}
}
