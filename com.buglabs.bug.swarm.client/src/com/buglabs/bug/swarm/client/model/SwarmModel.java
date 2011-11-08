package com.buglabs.bug.swarm.client.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.touge.restclient.ReSTClient;

/**
 * Model class to represent a Swarm.
 * 
 * @author kgilmer
 * 
 */
public class SwarmModel extends ModelBase {
	private final boolean isPublic;
	private final List<SwarmResourceModel> members;
	private final String createdAt;
	private final String id;
	private final String description;
	private final String modifiedAt;
	private final String name;
	private final String userId;

	/**
	 * Deserialize into SwarmModel.
	 */
	public static final ReSTClient.ResponseDeserializer<SwarmModel> DESERIALIZER = 
		new ReSTClient.ResponseDeserializer<SwarmModel>() {
	
		@Override
		public SwarmModel deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			if (responseCode == 404)
				return null;
						
			JsonNode jn = objectMapper.readTree(input);
					
			return SwarmModel.deserialize(jn);			
		}
	};
	
	/**
	 * Deserialize into List of SwarmModel.
	 */
	public static final ReSTClient.ResponseDeserializer<List<SwarmModel>> LIST_DESERIALIZER = 
		new ReSTClient.ResponseDeserializer<List<SwarmModel>>() {
	
		@Override
		public List<SwarmModel> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();
			
			List<SwarmModel> rml = new ArrayList<SwarmModel>();
			
			JsonNode jn = objectMapper.readTree(input);
			
			for (JsonNode rm : jn)
				rml.add(SwarmModel.deserialize(rm));
			
			return rml;			
		}
	};
	
	/**
	 * @param isPublic
	 *            swarm public?
	 * @param members
	 *            list of existing members
	 * @param createdAt
	 *            String datetime of creation
	 * @param id
	 *            swarmId as defined by server
	 * @param description
	 *            Textual description of swarm
	 * @param modifiedAt
	 *            last modified
	 * @param name
	 *            name of swarm
	 * @param userId
	 *            userId of owner
	 */
	public SwarmModel(final boolean isPublic, final List<SwarmResourceModel> members, 
			final String createdAt, final String id,
			final String description, final String modifiedAt, final String name, final String userId) {
		this.isPublic = isPublic;
		this.members = members;
		this.createdAt = createdAt;
		this.id = id;
		this.description = description;
		this.modifiedAt = modifiedAt;
		this.name = name;
		this.userId = userId;
	}

	/**
	 * Returns true if the swarm is public.
	 * 
	 * @return true if public
	 */
	public boolean isPublic() {
		return isPublic;
	}

	/**
	 * @return List of SwarmMemberModel of group members.
	 */
	public List<SwarmResourceModel> getMembers() {
		return members;
	}

	/**
	 * @return date as String for creation.
	 */
	public String getCreatedAt() {
		return createdAt;
	}

	/**
	 * @return id of swarm
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return desc of swarm
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return modified date as String
	 */
	public String getModifiedAt() {
		return modifiedAt;
	}

	/**
	 * @return name of swarm
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return user id of creator
	 */
	public String getUserId() {
		return userId;
	}

	@Override
	public String toString() {
		return name + "[" + userId + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SwarmModel))
			return super.equals(obj);

		// The swarmID should be globally unique.

		SwarmModel tsm = (SwarmModel) obj;

		return tsm.getId().equals(this.getId());
	}

	public static SwarmModel deserialize(final JsonNode jsonObject) throws IOException {
		List<SwarmResourceModel> srml = new ArrayList<SwarmResourceModel>();
		
		for (JsonNode jn : jsonObject.get("resources"))
			srml.add(SwarmResourceModel.deserialize(
					jsonObject.get("id").getTextValue(), jn));
		
		return new SwarmModel(
				Boolean.parseBoolean(jsonObject.get("public").getTextValue()),
				srml, 
				jsonObject.get("created_at").getTextValue(),
				jsonObject.get("id").getTextValue(), 
				jsonObject.get("description").toString(), 
				null, 
				jsonObject.get("name").getTextValue(),
				jsonObject.get("user_id").getTextValue());
	}

	public static String serialize(SwarmModel object) throws IOException {
		if (objectMapper == null)
			objectMapper = new ObjectMapper();
		
		return objectMapper.writeValueAsString(object);
	}
}
