package com.buglabs.bug.swarm.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Session interface for active connection to swarm server.  Used to send and receive participation messages
 * with the swarm server.
 * 
 * See http://developer.bugswarm.net/participation_api.html
 * 
 * @author kgilmer
 *
 */
public interface ISwarmSession {

	/**
	 * Send a payload to all member swarms.
	 * @param payload
	 * @throws IOException
	 */
	void send(Map<String, ?> payload) throws IOException;
	
	/**
	 * Send a payload to specific swarms.
	 * @param payload
	 * @param swarmIds
	 * @throws IOException
	 */
	void send(Map<String, ?> payload, String ... swarmIds) throws IOException;
	
	/**
	 * Send a payload to specific resources associated with specific swarms.
	 * @param payload
	 * @param swarmAndResource
	 * @throws IOException
	 */
	void send(Map<String, ?> payload, List<Map.Entry<String, String>> swarmAndResource) throws IOException;
	
	/**
	 * Join an existing swarm with an existing resource.
	 * @param swarmId
	 * @param resourceId
	 * @throws IOException
	 */
	void join(String swarmId, String resourceId) throws IOException;
	
	/**
	 * Listen to events from swarm server.
	 * 
	 * @param listener
	 */
	void addListener(ISwarmMessageListener listener);
	
	/**
	 * Remove listener.
	 * @param listener
	 */
	void removeListener(ISwarmMessageListener listener);
	
	/**
	 * @return true if the connection is connected to the swarm server.
	 */
	boolean isConnected();
	
	void close();
}
