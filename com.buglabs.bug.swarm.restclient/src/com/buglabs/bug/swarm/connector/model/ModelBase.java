package com.buglabs.bug.swarm.connector.model;

import org.codehaus.jackson.map.ObjectMapper;

import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;

public abstract class ModelBase {

	protected static ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * @param in Input, null ok.
	 * @return null if input is null, MemberType.valueOf() eval otherwise.
	 */
	protected static MemberType toMemberTypeSafely(Object in) {
		if (in == null)
			return null;
		return MemberType.valueOf(in.toString());
	}
	
	/**
	 * @param in String input, null ok.
	 * @return null if input is null, or toString() on object.
	 */
	protected static String toStringSafely(Object in) {
		if (in == null)
			return null;
		return in.toString();
	}
}
