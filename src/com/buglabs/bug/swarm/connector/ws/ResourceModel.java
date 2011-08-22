package com.buglabs.bug.swarm.connector.ws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;

/**
 * Represents the device-centric aspects of a Resource.
 * 
 * @author kgilmer
 *
 */
public class ResourceModel {

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

	/**
	 * See https://github.com/buglabs/bugswarm/wiki/Resources.
	 * 
	 * @param json Array of json objects
	 * @return List of Resource Model
	 */
	public static List<ResourceModel> createListFromJson(JSONArray json) {
		if (json == null || json.size() == 0)
			return Collections.emptyList();
		
		List<ResourceModel> l = new ArrayList<ResourceModel>();

		for (Object o : json)
			l.add(createFromJson((JSONObject) o));

		return l;
	}

	/**
	 * See https://github.com/buglabs/bugswarm/wiki/Resources.
	 * 
	 * @param jsonObject ResourceModel Json object
	 * @return a ResourceModel from the json stanza.
	 */
	public static ResourceModel createFromJson(JSONObject jsonObject) {
		return new ResourceModel(null,
				MemberType.valueOf(jsonObject.get("type").toString()),
				jsonObject.get("created_at").toString(),
				jsonObject.get("user_id").toString(),
				jsonObject.get("name").toString(),
				jsonObject.get("modified_at").toString(),
				jsonObject.get("machine_type").toString(),
				jsonObject.get("id").toString(),
				jsonObject.get("description").toString());
	}

	
}
