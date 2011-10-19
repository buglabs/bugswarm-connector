package com.buglabs.bug.swarm.connector.model;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;

/**
 * Represents the device-centric aspects of a Resource.
 * 
 * @author kgilmer
 *
 */
public class ResourceModel extends ModelBase {

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
	private final MemberType type;
	
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
	public ResourceModel(ResourcePosition position, MemberType type, String createDate, String userId, String name
			, String modifyDate, String machineType, String resourceId, String description) {
		this.position = position;
		this.type = type;
		this.createDate = createDate;
		this.userId = userId;
		this.name = name;
		this.modifyDate = modifyDate;
		this.machineType = machineType;
		this.resourceId = resourceId;
		this.description = description;
	}
	
	/**
	 * @return MemberType
	 */
	public MemberType getType() {
		return type;
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
	
	public static ResourceModel deserialize(final String json) throws IOException {
		if (objectMapper == null)
			objectMapper = new ObjectMapper();
		
		JsonNode jsonObject = objectMapper.readTree(json);
						
		return new ResourceModel(null,
				toMemberTypeSafely(jsonObject.get("type")),
				toStringSafely(jsonObject.get("created_at")),
				toStringSafely(jsonObject.get("user_id")),
				toStringSafely(jsonObject.get("name")),
				toStringSafely(jsonObject.get("modified_at")),
				toStringSafely(jsonObject.get("machine_type")),
				toStringSafely(jsonObject.get("id")),
				toStringSafely(jsonObject.get("description")));
	}

	public static String serialize(ResourceModel object) throws IOException {
		if (objectMapper == null)
			objectMapper = new ObjectMapper();
		
		return objectMapper.writeValueAsString(object);
	}
}
