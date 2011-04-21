package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.buglabs.util.simplerestclient.HTTPResponse;

/**
 * A Swarm WS Client implementation using json.simple and simplerestclient.
 * 
 * @author kgilmer
 *
 */
public class SwarmWSClient extends AbstractSwarmWSClient implements ISwarmWSClient {
	private SwarmMembersWSClient membersClient;

	public SwarmWSClient(String swarmHostUrl, String apiKey) {
		super(swarmHostUrl, apiKey);
	}
	
	/**
	 * @return Swarm Members API
	 */
	public IMembersClient getMembers() {
		if (membersClient == null)
			membersClient = new SwarmMembersWSClient(swarmHostUrl, apiKey, httpClient);
		
		return membersClient;
	}
	
	@Override
	public String create(String name, boolean isPublic, String description) throws IOException {
		if (name == null || description == null)
			throw new IllegalArgumentException("An input parameter is null.");
		
		if (!checkAndValidate(false))			
			throw new IOException(INVALID_SWARM_CONNECTION_ERROR_MESSAGE);
		
		Map<String, String> props = new HashMap<String, String>();
		props.put("name", name);
		props.put("public", Boolean.toString(isPublic));
		props.put("description", description);
		
		HTTPResponse response = httpClient.post(swarmHostUrl + "swarms", props);
		
		JSONObject json = (JSONObject) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		return json.get("swarm").toString();
	}

	@Override
	public int update(String swarmId, boolean isPublic, String description) throws IOException {
		if (swarmId == null || description == null)
			throw new IllegalArgumentException("An input parameter is null.");
		
		if (!checkAndValidate(false))			
			throw new IOException(INVALID_SWARM_CONNECTION_ERROR_MESSAGE);
		
		Map<String, String> props = new HashMap<String, String>();

		props.put("public", Boolean.toString(isPublic));
		props.put("description", description);
		
		HTTPResponse response = httpClient.put(swarmHostUrl + "swarms/" + swarmId, props);
		
		return response.getResponseCode();
	}

	@Override
	public int destroy(String swarmId) throws IOException {
		if (swarmId == null)
			throw new IllegalArgumentException("An input parameter is null.");
		
		if (!checkAndValidate(false))			
			throw new IOException(INVALID_SWARM_CONNECTION_ERROR_MESSAGE);
		
		HTTPResponse response = httpClient.delete(swarmHostUrl + "swarms/" + swarmId);
		
		return response.getResponseCode();
	}

	@Override
	public List<SwarmModel> list() throws IOException {		
		if (!checkAndValidate(false))			
			throw new IOException(INVALID_SWARM_CONNECTION_ERROR_MESSAGE);
		
		HTTPResponse response = httpClient.get(swarmHostUrl + "swarms");
		
		JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		return SwarmModel.createListFromJson(json);
	}

	@Override
	public SwarmModel get(String swarmId) throws IOException {
		if (swarmId == null)
			throw new IllegalArgumentException("An input parameter is null.");
		
		if (!checkAndValidate(false))			
			throw new IOException(INVALID_SWARM_CONNECTION_ERROR_MESSAGE);
		
		HTTPResponse response = httpClient.get(swarmHostUrl + "swarms/" + swarmId);
		JSONObject jo = (JSONObject) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		if (jo != null)
			return SwarmModel.createFromJson(jo);
		
		return null;
	}

	@Override
	public boolean isValid() throws IOException {
		return super.checkAndValidate(false);
	}
}
