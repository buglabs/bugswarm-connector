package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStream;
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
import com.buglabs.util.http.RestClient;
import com.buglabs.util.http.RestClient.Response;
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
	public ResourceWSClient(String swarmHostUrl, String apiKey, RestClient httpClient) {
		super(swarmHostUrl, apiKey, httpClient);
	}

	@Override
	public SwarmWSResponse add(String resourceId, String userId, String resourceName, String description, MemberType type,
			String machineType) throws IOException {
		validateParams(resourceId, type, userId, resourceName, description);

		validateAPIKey();

		// TODO: allow for position coordinates
		Map<String, String> props = toMap("id", resourceId, "user_id", userId, "name", resourceName, "description", description, "type",
				type.toString(), "machine_type", machineType, "position", "{\"Longitude\": 0, \"latitude\": 0}");

		return httpClient.post(swarmHostUrl + "resources", props, ModelDeserializers.SwarmWSResponseDeserializer).getContent();
	}

	@Override
	public SwarmWSResponse update(String resourceId, String resourceName, String resourceDescription, MemberType type, String machineType)
			throws IOException {
		validateParams(resourceId, resourceName, resourceDescription, type, machineType);

		validateAPIKey();

		Map<String, String> props = toMap("name", resourceName, "description", resourceDescription, "type", type.toString(),
				"machine_type", machineType);

		return httpClient.put(swarmHostUrl + "resources/" + resourceId, props, ModelDeserializers.SwarmWSResponseDeserializer).getContent();
	}

	@Override
	public List<ResourceModel> get(MemberType type) throws IOException {
		validateAPIKey();

		if (type == null)
			return httpClient.get(swarmHostUrl + "resources", ModelDeserializers.ResourceModelListDeserializer).getContent();
		else
			return httpClient.get(swarmHostUrl + "resources", toMap("type", type.toString()), ModelDeserializers.ResourceModelListDeserializer).getContent();
	}

	@Override
	public ResourceModel get(String resourceId) throws IOException {
		validateParams(resourceId);
		validateAPIKey();

		return httpClient.get(swarmHostUrl + "resources/" + resourceId, ModelDeserializers.ResourceModelDeserializer).getContent();
	}

	@Override
	public SwarmWSResponse remove(String resourceId) throws IOException {
		validateParams(resourceId);

		validateAPIKey();

		return httpClient.delete(swarmHostUrl + "resources/" + resourceId, ModelDeserializers.SwarmWSResponseDeserializer).getContent();
	}

	@Override
	public List<SwarmResourceModel> getMemberSwarms(String resourceId) throws IOException {
		validateParams(resourceId);

		validateAPIKey();

		return httpClient.get(swarmHostUrl + "resources/" + resourceId + "/swarms", ModelDeserializers.SwarmResourceModelListDeserializer).getContent();
	}
}
