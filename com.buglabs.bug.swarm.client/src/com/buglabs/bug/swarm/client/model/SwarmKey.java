package com.buglabs.bug.swarm.client.model;

import org.codehaus.jackson.JsonNode;

import com.buglabs.bug.swarm.client.ISwarmKeysClient;
import com.buglabs.bug.swarm.client.ISwarmKeysClient.KeyType;

/**
 * Model class for the Swarm API Key.
 * 
 * @author kgilmer
 *
 */
public class SwarmKey extends ModelBase {

	private final String createdAt;
	private final String key;
	private boolean active = false;
	private final ISwarmKeysClient.KeyType type;
	private final String userId;
	
	@Override
	public String toString() {		
		return key + " (" + type.toString() + ", " + userId + ", " + createdAt + ")";
	}
	
	/**
	 * @param createdAt creation date
	 * @param key key
	 * @param active if active
	 * @param type type of key
	 * @param userId user id
	 */
	public SwarmKey(String createdAt, String key, boolean active, KeyType type, String userId) {
		super();
		this.createdAt = createdAt;
		this.key = key;
		this.type = type;
		this.active = active;
		this.userId = userId;
	}

	/**
	 * @return date created at
	 */
	public String getCreatedAt() {
		return createdAt;
	}

	/**
	 * @return key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return true if active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return type of key
	 */
	public ISwarmKeysClient.KeyType getType() {
		return type;
	}

	/**
	 * @return user id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * An example response from server:
	 * 
	 * {
	 *	"created_at": "2011-10-21T15:37:35.636Z", 
	 *	"key": "b8c2382d0fd9123f3d0ccc760afa03854c652fdb", 
	 *	"status": "active", 
	 *	"type": "configuration", 
	 *	"user_id": "username"
  	 * }
  	 * 
	 * @param node json node 
	 * @return instance of SwarmKey based on JSON message.
	 */
	public static SwarmKey deserialize(JsonNode node) {
		
		boolean active = false;
		
		if (node.has("status") && node.get("status").getTextValue().equals("active"))
			active = true;
		
		KeyType kt = null;
		
		if (node.has("type") && node.get("type").getTextValue().equals(KeyType.PARTICIPATION.toString()))
			kt = KeyType.PARTICIPATION;
		else if (node.has("type") && node.get("type").getTextValue().equals(KeyType.CONFIGURATION.toString()))
			kt = KeyType.CONFIGURATION;
		
		if (kt == null)
			throw new IllegalStateException("Server did not specify field 'type'.");

		return new SwarmKey(
				node.get("created_at").getTextValue(), 
				node.get("key").getTextValue(), 
				active,
				kt, 
				node.get("user_id").getTextValue());
	}
}
