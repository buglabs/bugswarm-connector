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
 * Represents the device-centric aspects of a Resource.
 * 
 * @author kgilmer
 *
 */
public class UserResourceModel extends ModelBase {
	
	/**
	 * Deserialize content into a List of ResourceModel.
	 */
	public static final ReSTClient.ResponseDeserializer<List<UserResourceModel>> LIST_DESERIALIZER = 
		new ReSTClient.ResponseDeserializer<List<UserResourceModel>>() {
	
		@Override
		public List<UserResourceModel> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();
			
			List<UserResourceModel> rml = new ArrayList<UserResourceModel>();
			
			JsonNode jn = objectMapper.readTree(input);
			
			for (JsonNode rm : jn)
				rml.add(UserResourceModel.deserialize(rm));
			
			return rml;
		}
	};
	
	/**
	 * Deserialize content into a ResourceModel.
	 */
	public static final ReSTClient.ResponseDeserializer<UserResourceModel> DESERIALIZER = 
		new ReSTClient.ResponseDeserializer<UserResourceModel>() {
	
		@Override
		public UserResourceModel deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) 
			throws IOException {
			if (responseCode == 404)
				return null;
			
			return UserResourceModel.deserialize(objectMapper.readTree(input));			
		}
	};

	/*
	 * * [{"position":{ "longitude":0, "latitude":0 },
	 * "created_at":"2011-07-20T14:36:24.518Z", 
	 * "user_id":"test",
	 * "type":"producer",
	 * "name":"bug20_modified",
	 * "modified_at":"2011-07-20T14:36:24.574Z", 
	 * "machine_type":"bug",
	 * "id":"00:1e:c2:0a:55:fd", 
	 * "description":"my first resource modified.",
	 * "_id":"4e26e7e866be7c0000000126"}]
	 */
	
	private ResourcePosition position;
	private String createDate;
	private String userId;
	private String name;
	private String modifyDate;
	private String machineType;
	private String resourceId;
	private String description;
	
	/**
	 * @param position ResourcePosition
	 * @param createDate Creation date
	 * @param userId User id
	 * @param name resource name (label)
	 * @param modifyDate modification date
	 * @param machineType machine type
	 * @param resourceId id of resource
	 * @param description description of resource
	 */
	public UserResourceModel(ResourcePosition position, String createDate, String userId, String name
			, String modifyDate, String machineType, String resourceId, String description) {
		this.position = position;
		this.createDate = createDate;
		this.userId = userId;
		this.name = name;
		this.modifyDate = modifyDate;
		this.machineType = machineType;
		this.resourceId = resourceId;
		this.description = description;
	}
	
	/**
	 * @return position
	 */
	public ResourcePosition getPosition() {
		return position;
	}
	
	/**
	 * @return creation date
	 */
	public String getCreateDate() {
		return createDate;
	}
	
	/**
	 * @return user id
	 */
	public String getUserId() {
		return userId;
	}
	
	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return modification date
	 */
	public String getModifyDate() {
		return modifyDate;
	}
	
	/**
	 * @return machine type
	 */
	public String getMachineType() {
		return machineType;
	}
	
	/**
	 * @return resource id
	 */
	public String getResourceId() {
		return resourceId;
	}
	
	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}
	
	public static UserResourceModel deserialize(final JsonNode jsonObject) throws IOException {
		//TODO: Implement the ResourcePosition
		return new UserResourceModel(null,
				toStringSafely(jsonObject.get("created_at")),
				toStringSafely(jsonObject.get("user_id")),
				toStringSafely(jsonObject.get("name")),
				toStringSafely(jsonObject.get("modified_at")),
				toStringSafely(jsonObject.get("machine_type")),
				toStringSafely(jsonObject.get("id")),
				toStringSafely(jsonObject.get("description")));
	}

	public static String serialize(UserResourceModel object) throws IOException {
		if (objectMapper == null)
			objectMapper = new ObjectMapper();
		
		return objectMapper.writeValueAsString(object);
	}
}
