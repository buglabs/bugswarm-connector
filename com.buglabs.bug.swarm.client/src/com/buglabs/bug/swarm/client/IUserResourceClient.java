package com.buglabs.bug.swarm.client;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;

/**
 * This WS API is used to manage user resource information.  
 * Further details can be found here:
 * http://developer.bugswarm.net/restful_user_resources.html
 * 
 * @author kgilmer
 *
 */
public interface IUserResourceClient {

	/**
	 * 
	*	Add a resource.
	 * @param resourceName name of resource
	 * @param description description of resource
	 * @param machineType type of device
	 * @param longitude position of device at time of call, or 0.
	 * @param latitude position of device at time of call, or 0.
	 * @return WS response
	 * @throws IOException on I/O error
	 */	
	UserResourceModel add(String resourceName, String description, String machineType, float longitude, float latitude) throws IOException;
	
	/**
	 * 
	*	Update a resource.
	 * @param resourceId id of resource
	 * @param resourceName name of resource
	 * @param resourceDescription description of resource
	 * @param type type of resource
	 * @param machineType type of device
	 * @return WS response
	 * @throws IOException on I/O or authentication error.
	 */
	SwarmWSResponse update(String resourceId, String resourceName, String resourceDescription, MemberType type, String machineType) throws IOException;

	/**
	*	Get my resources.
	*	  	
	 * @return List of ResourceModel or empty list of no matches are found.
	 * @throws IOException on I/O or authentication error.
	 */
	List<UserResourceModel> get() throws IOException;
	
	/**
	*	Get a specific resource.
	*
	 * @param resourceId id of resource
	 * @return specific instance or null if no match found.
	 * @throws IOException on I/O or authentication error.
	 */
	UserResourceModel get(String resourceId) throws IOException;
	
	/**
	 *  Destroy a resource.
	 * @param resourceId id of resource
	 * @return WS response code
	 * @throws IOException on I/O or authentication error.
	 */
	SwarmWSResponse destroy(String resourceId) throws IOException;
	
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
	List<UserResourceModel> list() throws IOException;

	
}
