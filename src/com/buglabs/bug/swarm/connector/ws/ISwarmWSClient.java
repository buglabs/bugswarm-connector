package com.buglabs.bug.swarm.connector.ws;

import java.util.List;

/**
 * A contract for the BUGSwarm WS API.
 * See https://github.com/buglabs/bugswarm/wiki/Swarms-API
 * 
 * This API requires and implementation that holds API-KEY and swarm host state internally.
 * 
 * @author kgilmer
 *
 */
public interface ISwarmWSClient {
	/**
	 * Create a swarm.
	 * @param name
	 * @param isPublic
	 * @param description
	 * @return
	 */
	public String create(String name, boolean isPublic, String description);
	
	/**
	 * Update the description of a swarm.
	 * @param isPublic
	 * @param description
	 * @return HTTP response of operation.
	 */
	public int update(boolean isPublic, String description);
	
	/**
	 * Delete a swarm.
	 * @return HTTP response of operation.
	 */
	public int delete();
	
	/**
	 * Get all available swarms.
	 * @return A list of SwarmModel for all available swarms.
	 */
	public List<SwarmModel> list();
	
	/**
	 * Get info of a specific swarm.
	 * 
	 * @param swarmId
	 * @return
	 */
	public SwarmModel get(String swarmId);
}
