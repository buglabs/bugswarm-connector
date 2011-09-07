package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.buglabs.bug.swarm.connector.model.ResourceModel;
import com.buglabs.bug.swarm.connector.model.SwarmResourceModel;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.util.simplerestclient.HTTPRequest;
import com.buglabs.util.simplerestclient.HTTPResponse;

/**
 * Implementation of IResourceClient.
 * 
 * @author kgilmer
 * 
 */
public class ResourceWSClient extends AbstractSwarmWSClient implements IResourceClient {

	/**
	 * @param swarmHostUrl
	 *            URL of Swarm server
	 * @param apiKey
	 *            API key for client
	 * @param httpClient
	 *            instance of HTTP client
	 */
	public ResourceWSClient(String swarmHostUrl, String apiKey, HTTPRequest httpClient) {
		super(swarmHostUrl, apiKey, httpClient);
	}

	@Override
	public SwarmWSResponse add(String resourceId, String userId, String resourceName, String description, MemberType type,
			String machineType) throws IOException {
		validateParams(resourceId, type, userId, resourceName, description);

		validateAPIKey();

		// TODO: allow for position coordinates
		Map<String, String> props = toMap(
				"id", resourceId, 
				"user_id", userId, 
				"name", resourceName, 
				"description", description, 
				"type", type.toString(), 
				"machine_type", machineType, 
				"position", "{\"Longitude\": 0, \"latitude\": 0}");

		HTTPResponse response = httpClient.post(swarmHostUrl + "resources", props);

		return SwarmWSResponse.fromCode(response.getResponseCode());
	}

	@Override
	public SwarmWSResponse update(String resourceId, String resourceName, String resourceDescription
			, MemberType type, String machineType)
			throws IOException {
		validateParams(resourceId, resourceName, resourceDescription, type, machineType);

		validateAPIKey();

		Map<String, String> props = toMap(
				"name", resourceName, 
				"description", resourceDescription, 
				"type", type.toString(),	
				"machine_type", machineType);

		HTTPResponse response = httpClient.put(swarmHostUrl + "resources/" + resourceId, props);

		return SwarmWSResponse.fromCode(response.getResponseCode());
	}

	@Override
	public List<ResourceModel> get(MemberType type) throws IOException {
		validateAPIKey();
		
		Map<String, String> params;
		
		if (type == null)
			params = Collections.emptyMap();
		else
			params = toMap("type", type.toString());
		
		HTTPResponse response = httpClient.get(swarmHostUrl + "resources", params);

		JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(response.getStream()));

		return ResourceModel.createListFromJson(json);		
	}

	@Override
	public ResourceModel get(String resourceId) throws IOException {
		validateParams(resourceId);
		validateAPIKey();
		
		HTTPResponse response = httpClient.get(swarmHostUrl + "resources/" + resourceId);
		
		if (response.getResponseCode() == HTTPResponse.HTTP_CODE_NOT_FOUND)
			return null;
		
		response.checkStatus();
		
		JSONObject jsonObject = (JSONObject) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		return ResourceModel.createFromJson(jsonObject);		
	}

	@Override
	public SwarmWSResponse remove(String resourceId) throws IOException {
		validateParams(resourceId);
		
		validateAPIKey();
		
		HTTPResponse response = httpClient.delete(swarmHostUrl + "resources/" + resourceId);
		
		return SwarmWSResponse.fromCode(response.getResponseCode());
	}

	@Override
	public List<SwarmResourceModel> getMemberSwarms(String resourceId) throws IOException {
		validateParams(resourceId);
		
		validateAPIKey();
		
		HTTPResponse response = httpClient.get(swarmHostUrl + "resources/" + resourceId + "/swarms");
		
		if (response.getResponseCode() == HTTPResponse.HTTP_CODE_NOT_FOUND)
			return Collections.emptyList();
		
		response.checkStatus();
		
		JSONArray jsonObject = (JSONArray) JSONValue.parse(new InputStreamReader(response.getStream()));
		
		return SwarmResourceModel.createListFromJson(jsonObject);			
	}
}
