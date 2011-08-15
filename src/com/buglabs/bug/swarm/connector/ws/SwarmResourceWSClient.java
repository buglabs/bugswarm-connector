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

/**
 * Client implementation for Swarm Members API.
 * 
 * @author kgilmer
 *
 */
public class SwarmResourceWSClient extends AbstractSwarmWSClient implements ISwarmResourcesClient {

	/**
	 * @param swarmHostUrl URL of swarm WS server
	 * @param apiKey API_KEY
	 * @param httpClient base HTTP client
	 */
	public SwarmResourceWSClient(final String swarmHostUrl, final String apiKey, final HTTPRequest httpClient) {
		super(swarmHostUrl, apiKey, httpClient);		
	}

	@Override
	public List<SwarmResourceModel> list(final String swarmId, final MemberType type) throws IOException {
		if (swarmId == null || type == null)
			throw new IllegalArgumentException("An input parameter is null.");
		
		validate();		
		
		HTTPResponse response = httpClient.get(swarmHostUrl + "swarms/" + swarmId + "/resources?type=" + type);
		
		JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		return SwarmResourceModel.createListFromJson(json);
	}

	@Override
	public SwarmWSResponse add(final String swarmId, 
			final MemberType type, final String userId, final String resource) throws IOException {
		
		if (swarmId == null || type == null || userId == null || resource == null)
			throw new IllegalArgumentException("An input parameter is null.");
		
		validate();
		
		Map<String, String> props = new HashMap<String, String>();

		props.put("type", type.toString());
		props.put("user_id", userId);
		props.put("resource", resource);
		
		HTTPResponse response = httpClient.post(swarmHostUrl + "swarms/" + swarmId + "/resources", props);
		
		
		return SwarmWSResponse.fromCode(response.getResponseCode());
	}

	@Override
	public List<SwarmModel> getSwarmsByMember(final String resource) throws IOException {
		if (resource == null)
			throw new IllegalArgumentException("An input parameter is null.");
		
		validate();
		
		HTTPResponse response = httpClient.get(swarmHostUrl + "resources/" + resource + "/swarms");
		
		JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		return SwarmModel.createListFromJson(json);
	}

	@Override
	public SwarmWSResponse remove(final String swarmId, final MemberType type, 
			final String userId, final String resource) throws IOException {
		
		if (swarmId == null || type == null || userId == null || resource == null)
			throw new IllegalArgumentException("An input parameter is null.");
		
		validate();
		
		Map<String, String> props = new HashMap<String, String>();
		
		props.put("type", type.toString());
		props.put("user_id", userId);
		props.put("resource", resource);
		props.put("X-HTTP-Method-Override", "DELETE");
		
		return SwarmWSResponse.fromCode(
				httpClient.post(swarmHostUrl + "swarms/" + swarmId + "/resources", props).getResponseCode());
	}
}
