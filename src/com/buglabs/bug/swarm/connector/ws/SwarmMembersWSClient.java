package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import com.buglabs.util.simplerestclient.HTTPRequest;
import com.buglabs.util.simplerestclient.HTTPResponse;

public class SwarmMembersWSClient extends AbstractSwarmWSClient implements IMembersClient {

	public SwarmMembersWSClient(String swarmHostUrl, String apiKey, HTTPRequest httpClient) {
		super(swarmHostUrl, apiKey, httpClient);		
	}

	@Override
	public List<SwarmMemberModel> list(String swarmId, MemberType type) throws IOException {
		if (!checkAndValidate(false))			
			throw new IOException(INVALID_SWARM_CONNECTION_ERROR_MESSAGE);
		
		HTTPResponse response = httpClient.get(swarmHostUrl + "swarms/" + swarmId + "/members");
		
		JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		return SwarmMemberModel.createListFromJson(json);
	}

	@Override
	public int add(String swarmId, MemberType type, String userId, String resource) throws IOException {
		if (!checkAndValidate(false))			
			throw new IOException("API_KEY is invalid.");
		
		Map<String, String> props = new HashMap<String, String>();

		props.put("type", type.toString());
		props.put("user_id", userId);
		props.put("resource", resource);
		
		HTTPResponse response = httpClient.post(swarmHostUrl + "swarms/" + swarmId + "/members", props);
		
		return response.getResponseCode();
	}

	@Override
	public int remove(String swarmId, String userId) throws IOException {
		if (!checkAndValidate(false))			
			throw new IOException(INVALID_SWARM_CONNECTION_ERROR_MESSAGE);
		
		Map<String, String> props = new HashMap<String, String>();
		
		props.put("user_id", userId);		
		
		return httpClient.delete(swarmHostUrl + "swarms/" + swarmId + "/members", props).getResponseCode();
	}

	@Override
	public List<SwarmModel> getSwarmsByMember(String userId, MemberType type) throws IOException {
		if (!checkAndValidate(false))			
			throw new IOException(INVALID_SWARM_CONNECTION_ERROR_MESSAGE);
		
		HTTPResponse response = httpClient.get(swarmHostUrl + "members/" + userId + "/swarms");
		
		JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		return SwarmModel.createListFromJson(json);
	}
}
