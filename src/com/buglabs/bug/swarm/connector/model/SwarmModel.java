package com.buglabs.bug.swarm.connector.model;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.codehaus.jackson.type.TypeReference;

import com.buglabs.bug.swarm.connector.ws.AbstractSwarmWSClient;

/**
 * Model class to represent a Swarm.
 * 
 * @author kgilmer
 * 
 */
public class SwarmModel {
	private final boolean isPublic;
	private final List<SwarmResourceModel> members;
	private final String createdAt;
	private final String id;
	private final String description;
	private final String modifiedAt;
	private final String name;
	private final String userId;

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

	/**
	 * From a json array, create a List of SwarmModel.
	 * 
	 * @param json
	 *            input object
	 * @return List of SwarmModel
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static List<SwarmModel> createListFromJson(final String json) throws JsonParseException, JsonMappingException, IOException {
		if (json == null || json.length() == 0)
			return Collections.emptyList();
		
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		List<SwarmModel>  list = mapper.readValue(json,  new TypeReference<List<SwarmModel>>() { });		
		
		return list;
	}

	/**
	 * Create a SwarmModel from a JSON object.
	 * 
	 * @param jsonObject
	 *            input object
	 * @return SwarmModel instance of SwarmModel
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static SwarmModel createFromJson(final String jsonObject) throws JsonParseException, JsonMappingException, IOException {
		// server is currently not returning modified_at
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		return mapper.readValue(jsonObject, SwarmModel.class);		
		/*
		return new SwarmModel(
				Boolean.parseBoolean(jsonObject.get("public").toString()),
				SwarmResourceModel.createListFromJson((JSONArray) jsonObject.get("resources")), 
				jsonObject.get("created_at").toString(),
				jsonObject.get("id").toString(), 
				jsonObject.get("description").toString(), 
				null, 
				jsonObject.get("name").toString(),
				jsonObject.get("user_id").toString());*/
	}
	
	public static class JSONDeserializer extends StdDeserializer {

		public JSONDeserializer() {
			super(SwarmModel.class);			
		}

		@Override
		public Object deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException, JsonProcessingException {
			
			JsonNode jsonObject = AbstractSwarmWSClient.getMapper().readTree(arg0);
			
			return new SwarmModel(
					Boolean.parseBoolean(jsonObject.get("public").toString()),
					SwarmResourceModel.createListFromJson((JsonNode) jsonObject.get("resources")), 
					jsonObject.get("created_at").toString(),
					jsonObject.get("id").toString(), 
					jsonObject.get("description").toString(), 
					null, 
					jsonObject.get("name").toString(),
					jsonObject.get("user_id").toString());
		}
		
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SwarmModel))
			return super.equals(obj);

		// The swarmID should be globally unique.

		SwarmModel tsm = (SwarmModel) obj;

		return tsm.getId().equals(this.getId());
	}
}
