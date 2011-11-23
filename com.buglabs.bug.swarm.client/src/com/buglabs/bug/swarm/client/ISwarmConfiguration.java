package com.buglabs.bug.swarm.client;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.client.ISwarmInviteClient.InvitationResponse;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.model.Invitation;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.SwarmResourceModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;

/**
 * This is the flat API for Swarm configuration.  It is the superset of the ISwarm*Client interfaces, and is provided as a simpler API.
 * 
 * @author kgilmer
 *
 */
public interface ISwarmConfiguration {

	/**
	 * Upload binary data to swarm.
	 * 
	 * @param userId user id
	 * @param resourceId resource id
	 * @param filename
	 *            Abstract name of file
	 * @param payload
	 *            byte array of binary data
	 * @return WS response
	 * @throws IOException
	 */
	SwarmWSResponse upload(String userId, String resourceId, String filename, byte[] payload) throws IOException;
	
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
	String createSwarm(String name, boolean isPublic, String description) throws IOException;

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
	SwarmWSResponse updateSwarm(String swarmId, boolean isPublic, String description) throws IOException;

	/**
	 * Delete a swarm.
	 * 
	 * @param swarmId
	 *            TODO
	 * @return HTTP response of operation.
	 * @throws IOException on I/O error
	 */
	SwarmWSResponse destroySwarm(String swarmId) throws IOException;

	/**
	 * Get all available swarms.
	 * 
	 * @return A list of SwarmModel for all available swarms.
	 * @throws IOException on I/O error
	 */
	List<SwarmModel> listSwarms() throws IOException;

	/**
	 * Get info of a specific swarm.
	 * 
	 * @param swarmId
	 *            swarmId
	 * @return a SwarmModel instance for the given id, or throws HTTP 404 if
	 *         swarm does not exist.
	 * @throws IOException on I/O error
	 */
	SwarmModel getSwarm(String swarmId) throws IOException;
	
	/**
	 * Send an invitation
	 * 
	 * @param swarmId swarm id
	 * @param user user id
	 * @param resourceId resource id
	 * @param resourceType resource type
	 * @param description description or null for no description
	 * @return fully populated invitation.
	 * @throws IOException on I/O or application error.
	 */	
	Invitation send(String swarmId, String user, String resourceId, MemberType resourceType, String description) throws IOException;
	
	/**
	 * Use this method to monitor the status of invitations sent to resources to join a given swarm.
	 * 
	 * @param swarmId swarm id
	 * @return List of Invitations
	 * @throws IOException on I/O or application error.
	 */
	List<Invitation> getSentInvitations(String swarmId) throws IOException;
	
	/**
	 * Use this method to keep track of all invitations you have received.
	 * 
	 * @return List of all invitations for user.
	 * @throws IOException on I/O error
	 */
	List<Invitation> getRecievedInvitations() throws IOException;
	
	/**
	 * Use this method to keep track of what invitations you have received for a specific resource.
	 * 
	 * @param resourceId resource id
	 * @return List of received invitations for specific resource.
	 * @throws IOException on I/O error
	 */
	List<Invitation> getRecievedInvitations(String resourceId) throws IOException;
	
	/**
	 * At any point in time, users may choose to respond to pending invitations for a given resource. Use this method to either accept or reject these invitations.
	 * 
	 * @param resourceId resource id
	 * @param invitationId invitation id
	 * @param action Action to take on response
	 * @return an Invitation type with response populated.
	 * @throws IOException on I/O error
	 */
	Invitation respond(String resourceId, String invitationId, InvitationResponse action) throws IOException;

	/**
	 * Lists existing resources in a swarm.
	 * @param swarmId
	 *            id of swarm
	 * @param type
	 *            consumer or producer
	 * @return List of SwarmMemberModel
	 * @throws IOException
	 *             on connection error
	 */
	List<SwarmResourceModel> listResources(String swarmId, ISwarmResourcesClient.MemberType type) throws IOException;

	/**
	 * Adds an existing resource to a swarm.
	 * 
	 * @param swarmId
	 *            id of swarm
	 * @param type
	 *            consumer or producer
	 * @param userId
	 *            user id
	 * @param resourceId resource id
	 * @return HTTP response of operation
	 * @throws IOException on I/O error
	 */
	SwarmWSResponse addResource(String swarmId, ISwarmResourcesClient.MemberType type, String resourceId) throws IOException;

	/**
	 * @param swarmId
	 *            id of swarm
	 * @param type
	 *            consumer or producer
	 * @param userId
	 *            user id
	 * @param resourceId id of resource
	 * @return HTTP response of operation
	 * @throws IOException on I/O error
	 */
	SwarmWSResponse removeResource(String swarmId, ISwarmResourcesClient.MemberType type, String userId, String resourceId) throws IOException;

	/**
	 * 
	 * Creates a new resource.
	 * @param resourceName name of resource
	 * @param description description of resource
	 * @param machineType type of device
	 * @param longitude position of device at time of call, or 0.
	 * @param latitude position of device at time of call, or 0.
	 * @return WS response
	 * @throws IOException on I/O error
	 */	
	UserResourceModel createResource(String resourceName, String description, String machineType, float longitude, float latitude) throws IOException;
	
	/**
	 * 
	 * Update a resource.
	 * @param resourceId id of resource
	 * @param resourceName name of resource
	 * @param resourceDescription description of resource
	 * @param type type of resource
	 * @param machineType type of device
	 * @return WS response
	 * @throws IOException on I/O or authentication error.
	 */
	SwarmWSResponse updateResource(String resourceId, String resourceName, String resourceDescription, MemberType type, String machineType) throws IOException;

	/**
	*	Get all owned resources.
	*	  	
	 * @return List of ResourceModel or empty list of no matches are found.
	 * @throws IOException on I/O or authentication error.
	 */
	List<UserResourceModel> getResources() throws IOException;
	
	/**
	*	Get a specific resource.
	*
	 * @param resourceId id of resource
	 * @return specific instance or null if no match found.
	 * @throws IOException on I/O or authentication error.
	 */
	UserResourceModel getResource(String resourceId) throws IOException;
	
	/**
	*	Remove a resource.
	 * @param resourceId id of resource
	 * @return WS response code
	 * @throws IOException on I/O or authentication error.
	 */
	SwarmWSResponse removeResource(String resourceId) throws IOException;
	
	/**
	 * 
	 *	Get the list of swarms where <resource_id> is a resource.
	 * @param resourceId id of resource
	 * @return List of SwarmResourceModel or empty list if no matches are found
	 * @throws IOException on I/O or authentication error.
	 */
	List<SwarmModel> getMemberSwarms(String resourceId) throws IOException;

	/**
	 * Get the list of resources for the user.
	 * 
	 * @return List of ResourceModel or empty list if no resources are available.
	 * @throws IOException 
	 */
	List<UserResourceModel> listResource() throws IOException;
}
