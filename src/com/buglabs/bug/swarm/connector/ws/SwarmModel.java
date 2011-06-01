package com.buglabs.bug.swarm.connector.ws;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
	 * @param isPublic swarm public?
	 * @param members list of existing members
	 * @param createdAt String datetime of creation
	 * @param id swarmId as defined by server
	 * @param description Textual description of swarm
	 * @param modifiedAt last modified
	 * @param name name of swarm
	 * @param userId userId of owner
	 */
	public SwarmModel(final boolean isPublic, final List<SwarmResourceModel> members, final String createdAt, 
			final String id, final String description, final String modifiedAt, final String name, final String userId) {
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
	 * @param json input object
	 * @return List of SwarmModel
	 */
	public static List<SwarmModel> createListFromJson(final JSONArray json) {
		List<SwarmModel> l = new ArrayList<SwarmModel>();

		for (Object o : json)
			l.add(createFromJson((JSONObject) o));

		return l;
	}

	/**
	 * Create a SwarmModel from a JSON object.
	 * 
	 * @param jsonObject input object
	 * @return SwarmModel instance of SwarmModel
	 */
	public static SwarmModel createFromJson(final JSONObject jsonObject) {
		// server is currently not returning modified_at
		return new SwarmModel(
				Boolean.parseBoolean(jsonObject.get("public").toString()), 
				SwarmResourceModel.createListFromJson((JSONArray) jsonObject.get("resources")), 
				jsonObject.get("created_at").toString(), 
				jsonObject.get("id").toString(), 
				jsonObject.get("description").toString(), 
				null, 
				jsonObject.get("name").toString(), 
				jsonObject.get("user_id").toString());
	}
}
