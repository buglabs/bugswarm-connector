package com.buglabs.bug.swarm.connector.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.util.http.ReSTClient;

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
	private final String resource;
	private final String swarmId;
	
	/**
	 * Deserialize to List of SwarmResourceModel.
	 */
	public static final ReSTClient.ResponseDeserializer<SwarmResourceModel> Deserializer = 
		new ReSTClient.ResponseDeserializer<SwarmResourceModel>() {
	
		@Override
		public SwarmResourceModel deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			if (responseCode == 404)
				return null;
			
			return SwarmResourceModel.deserialize(objectMapper.readTree(input));			
		}
	};
	
	/**
	 * Deserialize to List of SwarmResourceModel.
	 */
	public static final ReSTClient.ResponseDeserializer<List<SwarmResourceModel>> ListDeserializer = 
		new ReSTClient.ResponseDeserializer<List<SwarmResourceModel>>() {
	
		@Override
		public List<SwarmResourceModel> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();
			
			List<SwarmResourceModel> srml= new ArrayList<SwarmResourceModel>();
			
			for (JsonNode jn : objectMapper.readTree(input))
				srml.add(SwarmResourceModel.deserialize(jn));
			
			return srml;			
		}
	};
	
	//public static List<?> jsonListOf(Input)

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

	public static SwarmResourceModel deserialize(JsonNode jo) throws IOException {		
		return new SwarmResourceModel(
				jo.get("created_at").getTextValue(), 
				MemberType.valueOf(jo.get("type").getTextValue()), 
				jo.get("user_id").getTextValue(),
				jo.get("id").getTextValue(), 
				jo.get("swarm_id").getTextValue());		
	}

	public static String serialize(SwarmResourceModel object) throws IOException {
		if (objectMapper == null)
			objectMapper = new ObjectMapper();
		
		return objectMapper.writeValueAsString(object);
	}
}
