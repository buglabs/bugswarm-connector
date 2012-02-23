package com.buglabs.bug.swarm.client;

import java.io.IOException;
import java.net.UnknownHostException;

import com.buglabs.bug.swarm.client.ISwarmSession.SessionType;
import com.buglabs.bug.swarm.client.impl.SwarmKeysWSClient;
import com.buglabs.bug.swarm.client.impl.SwarmSessionImp;
import com.buglabs.bug.swarm.client.impl.SwarmWSClient;

/**
 * Factory clients to get access to the client implementation.
 * 
 * @author kgilmer
 *
 */
public class SwarmClientFactory {
	
	/**
	 * Default port of swarm server participation API.
	 */
	private static final int DEFAULT_SWARM_SERVER_PORT = 80;
	private static boolean keepalive;

	private SwarmClientFactory() {
		//Static utility class.
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
	 * Create a production session with a swarm server.  This session allows for participation (presence, messages) among swarm clients.
	 * 
	 * @param hostname of swarm server
	 * @param apiKey of user
	 * @param resourceId associated with session
	 * @param swarmIds swarms to connect with
	 * @return a swarm session
	 * @throws UnknownHostException if unable to resolve hostname
	 * @throws IOException on I/O error
	 */
	public static ISwarmSession createProductionSession(String hostname, String apiKey, String resourceId, boolean keepalive, boolean autoreconnect, String ... swarmIds) throws UnknownHostException, IOException {
		
		return createSession(hostname, SessionType.PRODUCTION, apiKey, resourceId, keepalive, autoreconnect, swarmIds);
	}
	
	/**
	 * Create a consumption session with a swarm server.  This session allows for participation (presence, messages) among swarm clients.
	 * 
	 * @param hostname of swarm server
	 * @param apiKey of user
	 * @param resourceId associated with session
	 * @param swarmIds swarms to connect with
	 * @return a swarm session
	 * @throws UnknownHostException if unable to resolve hostname
	 * @throws IOException on I/O error
	 */
	public static ISwarmSession createConsumptionSession(String hostname, String apiKey, String resourceId, boolean keepalive, boolean autoreconnect, String ... swarmIds) throws UnknownHostException, IOException {
		
		return createSession(hostname, SessionType.CONSUMPTION, apiKey, resourceId, keepalive, autoreconnect, swarmIds);
	}
	
	private static ISwarmSession createSession(String hostname, SessionType type, String apiKey, String resourceId, boolean keepalive, boolean autoreconnect, String ... swarmIds) throws UnknownHostException, IOException {
		if (hostname.toLowerCase().startsWith("http://"))
			hostname = hostname.substring("http://".length());
		
		int port = DEFAULT_SWARM_SERVER_PORT;
		
		if (hostname.indexOf(':') > 0) {
			port = Integer.parseInt(hostname.split(":")[1]);
			hostname = hostname.split(":")[0];
		}
		return new SwarmSessionImp(hostname, type, port, apiKey, resourceId, keepalive, autoreconnect, swarmIds);
	}

	/**
	 * Get an instance of the API Keys client.
	 * @param hostname name of host.
	 * @return instance of API Keys client.
	 */
	public static ISwarmKeysClient getAPIKeyClient(String hostname) {
		return new SwarmKeysWSClient(hostname);
	}
}
