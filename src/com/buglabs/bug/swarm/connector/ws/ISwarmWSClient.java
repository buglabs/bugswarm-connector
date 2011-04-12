package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
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
	 * @throws IOException 
	 */
	public String create(String name, boolean isPublic, String description) throws IOException;
	
	/**
	 * Update the description of a swarm.
	 * @param isPublic
	 * @param description
	 * @return HTTP response of operation.
	 * @throws IOException 
	 */
	public int update(String swarmId, boolean isPublic, String description) throws IOException;
	
	/**
	 * Delete a swarm.
	 * @param swarmId TODO
	 * @return HTTP response of operation.
	 * @throws IOException 
	 */
	public int destroy(String swarmId) throws IOException;
	
	/**
	 * Get all available swarms.
	 * @return A list of SwarmModel for all available swarms.
	 * @throws IOException 
	 */
	public List<SwarmModel> list() throws IOException;
	
	/**
	 * Get info of a specific swarm.
	 * 
	 * @param swarmId
	 * @return
	 * @throws IOException 
	 */
	public SwarmModel get(String swarmId) throws IOException;
}
