package com.buglabs.bug.swarm.connector.ws;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SwarmModel {
	private final boolean isPublic;
	private final List<SwarmMemberModel> members;
	private final String createdAt;
	private final String id;
	private final String description;
	private final String modifiedAt;
	private final String name;
	private final String userId;

	public SwarmModel(boolean isPublic, List<SwarmMemberModel> members, String createdAt, String id, String description, String modifiedAt, String name, String userId) {
		this.isPublic = isPublic;
		this.members = members;
		this.createdAt = createdAt;
		this.id = id;
		this.description = description;
		this.modifiedAt = modifiedAt;
		this.name = name;
		this.userId = userId;		
	}

	public boolean isPublic() {
		return isPublic;
	}

	public List<SwarmMemberModel> getMembers() {
		return members;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getModifiedAt() {
		return modifiedAt;
	}

	public String getName() {
		return name;
	}

	public String getUserId() {
		return userId;
	}

	@Override
	public String toString() {		
		return name + "[" + userId + "]";
	}
	/**
	 * From a json array, create a List of SwarmModel.
	 * @param json
	 * @return
	 */
	public static List<SwarmModel> createListFromJson(JSONArray json) {
		List<SwarmModel> l = new ArrayList<SwarmModel>();
		
		for (Object o: json) 
			l.add(createFromJson((JSONObject) o));
		
		return l;
	}

	public static SwarmModel createFromJson(JSONObject jo) {
		
		return new SwarmModel(
				Boolean.parseBoolean(jo.get("public").toString()), 
				SwarmMemberModel.createListFromJson((JSONArray) jo.get("members")), 
				jo.get("created_at").toString(), 
				jo.get("id").toString(), 
				jo.get("description").toString(), 
				null,  // server is currently not returning modified_at
				jo.get("name").toString(), 
				jo.get("user_id").toString());
	}
}
