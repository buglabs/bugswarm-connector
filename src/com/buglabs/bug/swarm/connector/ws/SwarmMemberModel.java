package com.buglabs.bug.swarm.connector.ws;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.buglabs.bug.swarm.connector.ws.IMembersClient.MemberType;

/**
 * Swarm model class for SwarmMember.  
 * 
 * See https://github.com/buglabs/bugswarm/wiki/Swarms-API (List method)
 * 
 * @author kgilmer
 *
 */
public class SwarmMemberModel {
	private final String createdAt;
	private final IMembersClient.MemberType type;
	private final String userId;
	private final String resource;
	private final String swarmId;

	public SwarmMemberModel(String createdAt, IMembersClient.MemberType type, String userId, String resource, String swarmId) {
		this.createdAt = createdAt;
		this.type = type;
		this.userId = userId;
		this.resource = resource;
		this.swarmId = swarmId;		
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public IMembersClient.MemberType getType() {
		return type;
	}

	public String getUserId() {
		return userId;
	}

	public String getResource() {
		return resource;
	}

	public String getSwarmId() {
		return swarmId;
	}

	/**
	 * From a json array, create a List of SwarmMemberModel.
	 * 
	 * @param json
	 * @return
	 */
	public static List<SwarmMemberModel> createListFromJson(JSONArray json) {
		List<SwarmMemberModel> l = new ArrayList<SwarmMemberModel>();
		
		for (Object o: json) {
			JSONObject jo = (JSONObject) o;
			
			l.add(new SwarmMemberModel(
					jo.get("created_at").toString(), 
					MemberType.valueOf(jo.get("type").toString().toUpperCase()), 
					jo.get("user_id").toString(), 
					jo.get("resource").toString(), 
					jo.get("swarm_id").toString()));
		}
		
		return l;
	}
}
