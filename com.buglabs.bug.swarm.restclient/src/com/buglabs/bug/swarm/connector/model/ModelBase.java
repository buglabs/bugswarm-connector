package com.buglabs.bug.swarm.connector.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.touge.restclient.ReSTClient;

import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;

public abstract class ModelBase {

	protected static ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * Deserialize server content into a JSONObject.
	 */
	public static final ReSTClient.ResponseDeserializer<JsonNode> JSONObjectDeserializer = 
		new ReSTClient.ResponseDeserializer<JsonNode>() {
	
		@Override
		public JsonNode deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return null;
			
			return (JsonNode) objectMapper.readTree(input);
		}
	};
	
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
