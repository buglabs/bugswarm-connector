package com.buglabs.bug.swarm.connector.ws;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author kgilmer
 * 
 */
public class SwarmModel {
	private final boolean isPublic;
	private final List<SwarmMemberModel> members;
	private final String createdAt;
	private final String id;
	private final String description;
	private final String modifiedAt;
	private final String name;
	private final String userId;

	public SwarmModel(final boolean isPublic, final List<SwarmMemberModel> members, final String createdAt, final String id,
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
	 * @return
	 */
	public boolean isPublic() {
		return isPublic;
	}

	/**
	 * @return List of SwarmMemberModel of group members.
	 */
	public List<SwarmMemberModel> getMembers() {
		return members;
	}

	/**
	 * @return
	 */
	public String getCreatedAt() {
		return createdAt;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return
	 */
	public String getModifiedAt() {
		return modifiedAt;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
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
	 * @return
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
	 * @param jo
	 * @return
	 */
	public static SwarmModel createFromJson(final JSONObject jo) {
		// server is currently not returning modified_at
		return new SwarmModel(Boolean.parseBoolean(jo.get("public").toString()), SwarmMemberModel.createListFromJson((JSONArray) jo
				.get("resources")), jo.get("created_at").toString(), jo.get("id").toString(), jo.get("description").toString(), null, jo
				.get("name").toString(), jo.get("user_id").toString());
	}
}
