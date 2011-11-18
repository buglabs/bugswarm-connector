package com.buglabs.bug.swarm.client.model;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.buglabs.bug.swarm.client.ISwarmResourcesClient;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;

/**
 * Swarm model class for SwarmMember.
 * 
 * See https://github.com/buglabs/bugswarm/wiki/Swarms-API (List method)
 * 
 * @author kgilmer
 * 
 */
public class SwarmResourceModel extends ModelBase {
	private final String createdAt;
	private final ISwarmResourcesClient.MemberType type;
	private final String userId;
	private final String resourceId;
	private final String swarmId;

	/**
	 * @param createdAt
	 *            date created at
	 * @param type
	 *            consumer or producer
	 * @param userId
	 *            user id
	 * @param resourceId
	 *            resource (XMPP)
	 * @param swarmId
	 *            id of swarm
	 */
	public SwarmResourceModel(final String createdAt, final ISwarmResourcesClient.MemberType type, final String userId,
			final String resourceId, final String swarmId) {
		this.createdAt = createdAt;
		this.type = type;
		this.userId = userId;
		this.resourceId = resourceId;
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
	 * @return resource id
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * @return swarm id as defined by the server
	 */
	public String getSwarmId() {
		return swarmId;
	}

	@Override
	public String toString() {

		return swarmId + "[" + userId + " (" + type + ")/" + resourceId + "]";
	}

	public static SwarmResourceModel deserialize(String swarmId, JsonNode jo) throws IOException {		
		return new SwarmResourceModel(
				toStringSafely(jo.get("created_at")), 
				MemberType.valueOf(jo.get("resource_type").getTextValue().toUpperCase()), 
				jo.get("user_id").getTextValue(),
				jo.get("resource_id").getTextValue(), 
				swarmId);		
	}

	public static String serialize(SwarmResourceModel object) throws IOException {
		if (objectMapper == null)
			objectMapper = new ObjectMapper();
		
		return objectMapper.writeValueAsString(object);
	}
}
