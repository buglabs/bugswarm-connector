package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.connector.model.ResourceModel;
import com.buglabs.bug.swarm.connector.model.SwarmResourceModel;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;

/**
 * This WS API is used to manage device resource information.  Further details can be found here:
 * https://github.com/buglabs/bugswarm/wiki/Resources
 * 
 * @author kgilmer
 *
 */
public interface IResourceClient {

	/**
	 * 
	*	Add a resource.
	*	URL: http://api.bugswarm.net/resources
	*	Method: POST
	*	Headers: Content-Type: application/json
	*	Parameters: none
	*	Body:
	*	//type: producer, consumer, both. 
	*	//machine_type: bug, smartphone, pc
	*	{ "id": "00:1e:c2:0a:55:fd",
	*	  "user_id": "test",
	*	  "name": "bug20",
	*	  "description": "my first resource",
	*	  "type": "producer",
	*	  "machine_type": "bug",
	*	  "position": {
	*	       "longitude": 0,
	*	       "latitude": 0
	*	   }
	*	}
	*	Returns: HTTP Response Codes
	*	Example: Create a file called myresource.json that contains:
	*	{ "id": "00:1e:c2:0a:55:fd",
	*	  "user_id": "test",
	*	  "name": "bug20",
	*	  "description": "my first resource",
	*	  "type": "producer",
	*	  "machine_type": "bug"
	*	}
	*	then, using your own api key, execute:
	*
	*	curl -d@myresource.json --header "X-BugSwarmApiKey:833804b691b64068f6c6a716fea1fb51ae83b596" \
	*	--header "Content-Type:application/json" http://api.bugswarm.net/resources
	 * @param resourceId id of resource
	 * @param userId id of user
	 * @param resourceName name of resource
	 * @param description description of resource
	 * @param type type of resource
	 * @param machineType type of device
	 * @return WS response
	 * @throws IOException on I/O error
	 */
	SwarmWSResponse add(String resourceId, String userId, String resourceName, String description, MemberType type, String machineType) throws IOException;
	
	/**
	 * 
	*	Update a resource.
	*	URL: http://api.bugswarm.net/resources/**<resource_id>**
	*	Method: PUT
	*	Headers: Content-Type: application/json
	*	Parameters: none
	*	Body:
	*	//type: producer, consumer, both. 
	*	//machine_type: bug, smartphone, pc
	*	{ "name": "bug20_modified",
	*	  "description": "my first resource modified",
	*	  "type": "consumer",
	*	  "machine_type": "pc",
	*	  "position": {
	*	       "longitude": 0,
	*	       "latitude": 0
	*	   }
	*	}
	*	Returns: HTTP Response Codes
	*	Example: Create a file called myresource-update.json that contains:
	*	{ "name": "bug20 modified",
	*	  "description": "my first resource modified",
	*	  "type": "consumer",
	*	  "machine_type": "pc"
	*	}
	*	then, using your own api key, execute:
	*
	*	curl -X PUT -d@myresource-update.json --header "X-BugSwarmApiKey:833804b691b64068f6c6a716fea1fb51ae83b596" \
	*	--header "Content-Type:application/json" \
	*	http://api.bugswarm.net/resources/00:1e:c2:0a:55:fd
	*
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
	*	URL: http://api.bugswarm.net/resources
	*	Method: GET
	*	Parameters: type=producer or type=consumer
	*	Returns: An array of objects in json format.
	*	[{"position":{
	*	      "longitude":0,
	*	      "latitude":0 
	*	   },
	*	  "created_at":"2011-07-20T14:36:24.518Z",
	*	  "user_id":"test",
	*	  "type":"producer",
	*	  "name":"bug20_modified",
	*	  "modified_at":"2011-07-20T14:36:24.574Z",
	*	  "machine_type":"bug",
	*	  "id":"00:1e:c2:0a:55:fd",
	*	  "description":"my first resource modified.",
	*	  "_id":"4e26e7e866be7c0000000126"}]
	*	Example:
	*	curl --header "X-BugSwarmApiKey:810f2d5cab511f979242a6134e26a4c7e43f5a4b" \
	*	http://api.bugswarm.net/resources
	*
	*	curl --header "X-BugSwarmApiKey:810f2d5cab511f979242a6134e26a4c7e43f5a4b" \
	*	http://api.bugswarm.net/resources?type=consumer
	*
	*	curl --header "X-BugSwarmApiKey:810f2d5cab511f979242a6134e26a4c7e43f5a4b" \
	*	http://api.bugswarm.net/resources?type=producer
	 * 
	 * @param type null allowed.  Consumer, producer or null if all MemberTypes should be returned.
	 * @return List of ResourceModel or empty list of no matches are found.
	 * @throws IOException on I/O or authentication error.
	 */
	List<ResourceModel> get(MemberType type) throws IOException;
	
	/**
	*	Get a specific resource
	*	URL: http://api.bugswarm.net/resources/**<resource_id>**
	*	Method: GET
	*	Parameters: none
	*	Returns: An array of objects in json format.
	*	[{"position":{
	*	      "longitude":0,
	*	      "latitude":0 
	*	   },
	*	  "created_at":"2011-07-20T14:36:24.518Z",
	*	  "user_id":"test",
	*	  "type":"producer",
	*	  "name":"bug20_modified",
	*	  "modified_at":"2011-07-20T14:36:24.574Z",
	*	  "machine_type":"bug",
	*	  "id":"00:1e:c2:0a:55:fd",
	*	  "description":"my first resource modified.",
	*	  "_id":"4e26e7e866be7c0000000126"}]
	*	Example:
	*	curl --header "X-BugSwarmApiKey:810f2d5cab511f979242a6134e26a4c7e43f5a4b" \
	*	http://api.bugswarm.net/resources/00:1e:c2:0a:55:fd
	 * 
	 * @param resourceId id of resource
	 * @return specific instance or null if no match found.
	 * @throws IOException on I/O or authentication error.
	 */
	ResourceModel get(String resourceId) throws IOException;
	
	/**
	*	Remove a resource.
	*	URL: http://api.bugswarm.net/resources/**<resource_id>**
	*	Method: DELETE
	*	Parameters: none
	*	Returns: HTTP Response Codes
	*	Example:
	*	curl -X DELETE --header "X-BugSwarmApiKey:810f2d5cab511f979242a6134e26a4c7e43f5a4b" \
	*	http://api.bugswarm.net/resources/00:1e:c2:0a:55:fd
	*
	 * @param resourceId id of resource
	 * @return WS response code
	 * @throws IOException on I/O or authentication error.
	 */
	SwarmWSResponse remove(String resourceId) throws IOException;
	
	/**
	 * 
	 *	Get the list of swarms where <resource_id> is a resource.
	*	URL: http://api.bugswarm.net/resources/**<resource_id>* swarms
	*	Method: GET
	*	Parameters: none
	*	Returns: An array of objects in JSON format
	*	[{"public":false,
	*	  "resources":[{ "created_at":"2011-04-19T17:25:27.931Z",
	*	               "_id":"4dadc587dff78f0000000089",
	*	               "swarm_id":"aff17a1894bac95613600fb87d43b94d6ba82b1a",
	*	               "id":"Psi",
	*	               "user_id":"test",
	*	               "type":"producer"},
	*	             { "created_at":"2011-04-19T17:25:28.005Z",
	*	               "_id":"4dadc588dff78f000000008e",
	*	               "swarm_id":"aff17a1894bac95613600fb87d43b94d6ba82b1a",
	*	               "id":"Psi2",
	*	               "user_id":"test2",
	*	               "type":"consumer"}],
	*	  "created_at":"2011-04-19T17:25:27.819Z",
	*	  "_id":"4dadc587dff78f0000000086",
	*	  "description":"My Test Swarm Description",
	*	  "id":"aff17a1894bac95613600fb87d43b94d6ba82b1a",
	*	  "modified_at":"2011-04-19T17:25:28.006Z",
	*	  "name":"My Swarm",
	*	  "user_id":"test"}]
	*	Example:
	*	curl --header "X-BugSwarmApiKey:db07b2ac1130fc14d5ba0c170bf13cb109fd3304" http://api.bugswarm.net/resources/psi/swarms
	*
	 * @param resourceId id of resource
	 * @return List of SwarmResourceModel or empty list if no matches are found
	 * @throws IOException on I/O or authentication error.
	 */
	List<SwarmResourceModel> getMemberSwarms(String resourceId) throws IOException;

}
