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
	 * The type of session.  See http://developer.bugswarm.net/participation_api.html.
	 *
	 */
	public enum SessionType {
		CONSUMPTION, PRODUCTION;
	}

	/**
	 * Send a payload to all member swarms.
	 * @param payload message payload
	 * @throws IOException on I/O error
	 */
	void send(Map<String, ?> payload) throws IOException;
	
	/**
	 * Send a payload to specific swarms.
	 * @param payload message payload 
	 * @param swarmIds set of swarm ids
	 * @throws IOException on I/O error
	 */
	void send(Map<String, ?> payload, String ... swarmIds) throws IOException;
	
	/**
	 * Send a payload to specific resources associated with specific swarms.
	 * @param payload message payload
	 * @param swarmAndResource set of swarm/resource pairs
	 * @throws IOException on I/O error
	 */
	void send(Map<String, ?> payload, List<Map.Entry<String, String>> swarmAndResource) throws IOException;
	
	/**
	 * Request a specific feed from a swarm.
	 * 
	 * @param feedName name of feed 
	 * @throws IOException on I/O error
	 */
	void request(String feedName) throws IOException;
	
	/**
	 * Request a specific feed from a swarm, updates sent upon interval (seconds).
	 * 
	 * @param feedName name of feed
	 * @param interval send feed every <> seconds
	 * @throws IOException on I/O error
	 */
	void request(String feedName, int interval) throws IOException;
	
	/**
	 * Cancel a previous request that had an interval set.
	 * 
	 * @param feedName name of feed
	 * @throws IOException on I/O error
	 */
	void cancelRequest(String feedName) throws IOException;
	
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
	 * @param listener listener
	 */
	void addListener(ISwarmMessageListener listener);
	
	/**
	 * Remove listener.
	 * @param listener listener
	 */
	void removeListener(ISwarmMessageListener listener);
	
	/**
	 * @return true if the connection is connected to the swarm server.
	 */
	boolean isConnected();
	
	/**
	 * Close the session.
	 */
	void close();
}
