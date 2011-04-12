package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.buglabs.util.simplerestclient.HTTPRequest;
import com.buglabs.util.simplerestclient.HTTPResponse;

/**
 * A Swarm WS Client implementation using json.simple and simplerestclient.
 * @author kgilmer
 *
 */
public class SwarmWSClient implements ISwarmWSClient {

	private final String swarmHostUrl;
	private final String apiKey;
	private HTTPRequest httpClient;
	private HashMap<String, String> staticHeaders;

	public SwarmWSClient(String swarmHostUrl, String apiKey) {
		if (!swarmHostUrl.endsWith("/"))
			swarmHostUrl = swarmHostUrl + "/";
		
		this.swarmHostUrl = swarmHostUrl;
		this.apiKey = apiKey;
		this.httpClient = new HTTPRequest();
	}
	
	@Override
	public String create(String name, boolean isPublic, String description) throws IOException {
		Map<String, String> props = new HashMap<String, String>();
		props.put("name", name);
		props.put("public", Boolean.toString(isPublic));
		props.put("description", description);
		
		HTTPResponse response = httpClient.post(swarmHostUrl + "swarms", props, getSwarmHeaders());
		
		JSONObject json = (JSONObject) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		return json.get("swarm").toString();
	}

	@Override
	public int update(boolean isPublic, String description) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SwarmModel> list() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SwarmModel get(String swarmId) {
		// TODO Auto-generated method stub
		return null;
	}

	private Map<String, String> getSwarmHeaders() {
		if (staticHeaders == null) {
			staticHeaders = new HashMap<String, String>();
			
			staticHeaders.put("X-BugSwarmApiKey", apiKey);
			staticHeaders.put("Content-Type", "application/json");
		}
		
		return staticHeaders;
	}
}
