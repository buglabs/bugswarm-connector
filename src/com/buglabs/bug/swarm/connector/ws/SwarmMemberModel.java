package com.buglabs.bug.swarm.connector.ws;

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
	private final String type;
	private final String userId;
	private final String resource;
	private final String swarmId;

	public SwarmMemberModel(String createdAt, String type, String userId, String resource, String swarmId) {
		this.createdAt = createdAt;
		this.type = type;
		this.userId = userId;
		this.resource = resource;
		this.swarmId = swarmId;		
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getType() {
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
}
