package com.buglabs.bug.swarm.client;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.client.model.SwarmKey;

/**
 * A client for the BUGswarm "API Keys" API.
 * 
 * See http://developer.bugswarm.net/restful_api_keys.html.
 * 
 * @author kgilmer
 *
 */
public interface ISwarmKeysClient {
	
	/**
	 * Type of swarm key.
	 */
	public enum KeyType {		
		/**
		 * A key type to generate a configuration key.
		 */
		CONFIGURATION("configuration"),
		/**
		 * A key type to generate a participation key.
		 */
		PARTICIPATION("participation");
		
		/**
		 * Name of member.
		 */
		private final String name;

		/**
		 * @param name
		 *            of member
		 */
		private KeyType(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * This method is used to create API keys. If a key already exists, this method will replace that key with a new one. To create a specific key, users must specify the key type in the URL.
	 * @param userName user name
	 * @param password password
	 * @param type type of key to create or <code>null</code> to create both keys.
	 * @return List of keys that were created from request
	 * @throws IOException on server or application error
	 */
	List<SwarmKey> create(String userName, String password, KeyType type) throws IOException;
	
	/**
	 * List the available keys for a given user.
	 * 
	 * @param userName user name
	 * @param password password
	 * @param type type of key to request, or <code>null</code> to request both configuration and participation keys.
	 * @return List of keys that are available from server.
	 * @throws IOException on server or application error
	 */
	List<SwarmKey> list(String userName, String password, KeyType type) throws IOException;
}
