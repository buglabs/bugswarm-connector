package com.buglabs.bug.swarm.client;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.client.model.SwarmModel;

/**
 * A contract for the BUGSwarm WS API. See
 * https://github.com/buglabs/bugswarm/wiki/Swarms-API
 * 
 * This API requires and implementation that holds API-KEY and swarm host state
 * internally.
 * 
 * @author kgilmer
 * 
 */
public interface ISwarmClient {
	/**
	 * The character encoding for all messages in swarm protocol.
	 */
	public String SWARM_CHARACTER_ENCODING = "UTF-8";

	/**
	 * Create a swarm.
	 * 
	 * @param name
	 *            name of swarm
	 * @param isPublic
	 *            swarm can be public or private
	 * @param description
	 *            textual description of swarm
	 * @return the id of the newly created swarm
	 * @throws IOException on I/O error
	 */
	String create(String name, boolean isPublic, String description) throws IOException;
	
	/**
	 * Create a swarm.
	 * 
	 * @param name
	 *            name of swarm	 
	 * @param description
	 *            textual description of swarm
	 * @return the id of the newly created swarm
	 * @throws IOException on I/O error
	 */
	String create(String name, String description) throws IOException;

	/**
	 * Update the description of a swarm.
	 * 
	 * @param swarmId
	 *            id of swarm
	 * @param isPublic
	 *            swarm can be public or private
	 * @param description
	 *            description
	 * @return HTTP response of operation.
	 * @throws IOException on I/O error
	 */
	SwarmWSResponse update(String swarmId, boolean isPublic, String description) throws IOException;

	/**
	 * Delete a swarm.
	 * 
	 * @param swarmId
	 *            TODO
	 * @return HTTP response of operation.
	 * @throws IOException on I/O error
	 */
	SwarmWSResponse destroy(String swarmId) throws IOException;

	/**
	 * Get all available swarms.
	 * 
	 * @return A list of SwarmModel for all available swarms.
	 * @throws IOException on I/O error
	 */
	List<SwarmModel> list() throws IOException;

	/**
	 * Get info of a specific swarm.
	 * 
	 * @param swarmId
	 *            swarmId
	 * @return a SwarmModel instance for the given id, or throws HTTP 404 if
	 *         swarm does not exist.
	 * @throws IOException on I/O error
	 */
	SwarmModel get(String swarmId) throws IOException;

	/**
	 * Convenience method to return WS client for Swarm Membership API.
	 * 
	 * @return the WS API client for members
	 */
	ISwarmResourcesClient getSwarmResourceClient();
	
	/**
	 * @return an instance of an IResourceClient associated with the ISwarmClient's user and api key.
	 */
	IUserResourceClient getUserResourceClient();
	
	/**
	 * @return an instance of a ISwarmBinaryUploadClient.
	 */
	ISwarmBinaryUploadClient getSwarmBinaryUploadClient();
	
	/**
	 * @return an instance of ISwarmInviteClient.
	 */
	ISwarmInviteClient getSwarmInviteClient();
	
	/**
	 * @return an instance of ISwarmKeysClient.
	 */
	ISwarmKeysClient getSwarmKeysClient();
}
