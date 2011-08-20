package com.buglabs.bug.swarm.connector.ws;

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
	public ResourceModel(ResourcePosition position, String createDate, String userId, String name
			, String modifyDate, String machineType, String resourceId, String description) {
		super();
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

	
}
