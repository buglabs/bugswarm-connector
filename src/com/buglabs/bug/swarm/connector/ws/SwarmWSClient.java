package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.buglabs.util.simplerestclient.HTTPException;
import com.buglabs.util.simplerestclient.HTTPRequest;
import com.buglabs.util.simplerestclient.HTTPResponse;

/**
 * A Swarm WS Client implementation using json.simple and simplerestclient.
 * 
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
		httpClient.addConfigurator(new com.buglabs.util.simplerestclient.HTTPRequest.HTTPConnectionInitializer() {
			
			@Override
			public void initialize(HttpURLConnection connection) {
				for (Map.Entry<String, String> e: getSwarmHeaders().entrySet())
					connection.setRequestProperty(e.getKey(), e.getValue());
			}
		});
	}
	
	@Override
	public String create(String name, boolean isPublic, String description) throws IOException {
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
		Map<String, String> props = new HashMap<String, String>();

		props.put("public", Boolean.toString(isPublic));
		props.put("description", description);
		
		HTTPResponse response = httpClient.put(swarmHostUrl + "swarms/" + swarmId, props);
		
		return response.getResponseCode();
	}

	@Override
	public int destroy(String swarmId) throws IOException {
		HTTPResponse response = httpClient.delete(swarmHostUrl + "swarms/" + swarmId);
		
		return response.getResponseCode();
	}

	@Override
	public List<SwarmModel> list() throws IOException {		
		HTTPResponse response = httpClient.get(swarmHostUrl + "swarms");
		
		JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		return SwarmModel.createListFromJson(json);
	}

	@Override
	public SwarmModel get(String swarmId) throws IOException {
		HTTPResponse response = httpClient.get(swarmHostUrl + "swarms/" + swarmId);
		JSONObject jo = (JSONObject) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		if (jo != null)
			return SwarmModel.createFromJson(jo);
		
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

	@Override
	public boolean isValid() throws IOException {
		try {
			HTTPResponse response = httpClient.get(swarmHostUrl + "keys/" + apiKey + "/verify");
			return response.getResponseCode() == 200;
		} catch (HTTPException e) {
			//Only catch HTTP exceptions so that connection errors are passed back to client.			
		}
		
		return false;
	}
}
