package com.buglabs.bug.swarm.connector.model;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient;

/**
 * Swarm model class for SwarmMember.
 * 
 * See https://github.com/buglabs/bugswarm/wiki/Swarms-API (List method)
 * 
 * @author kgilmer
 * 
 */
public class SwarmResourceModel {
	private final String createdAt;
	private final ISwarmResourcesClient.MemberType type;
	private final String userId;
	private final String resource;
	private final String swarmId;

	/**
	 * @param createdAt
	 *            date created at
	 * @param type
	 *            consumer or producer
	 * @param userId
	 *            user id
	 * @param resource
	 *            resource (XMPP)
	 * @param swarmId
	 *            id of swarm
	 */
	public SwarmResourceModel(final String createdAt, final ISwarmResourcesClient.MemberType type, final String userId,
			final String resource, final String swarmId) {
		this.createdAt = createdAt;
		this.type = type;
		this.userId = userId;
		this.resource = resource;
		this.swarmId = swarmId;
	}

	/**
	 * @return date as a String for CreatedAt
	 */
	public String getCreatedAt() {
		return createdAt;
	}

	/**
	 * @return type
	 */
	public ISwarmResourcesClient.MemberType getType() {
		return type;
	}

	/**
	 * @return user id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @return resource (XMPP)
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * @return swarm id as defined by the server
	 */
	public String getSwarmId() {
		return swarmId;
	}

	@Override
	public String toString() {

		return swarmId + "[" + userId + " (" + type + ")/" + resource + "]";
	}

	/**
	 * From a json array, create a List of SwarmMemberModel.
	 * 
	 * @param json
	 *            input object
	 * @return a List of SwarmMemberModel
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static List<SwarmResourceModel> createListFromJson(final JsonNode json) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<SwarmResourceModel> result = mapper.readValue(json, new TypeReference<List<SwarmResourceModel>>() { });
		return result;
	}
}
