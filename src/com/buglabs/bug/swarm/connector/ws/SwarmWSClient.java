package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.buglabs.bug.swarm.connector.Configuration;
import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.model.SwarmModel;
import com.buglabs.util.simplerestclient.HTTPResponse;

/**
 * A Swarm WS Client implementation using json.simple and simplerestclient.
 * 
 * @author kgilmer
 * 
 */
public class SwarmWSClient extends AbstractSwarmWSClient implements ISwarmClient {
	private SwarmResourceWSClient membersClient;
	private SwarmBinaryUploadWSClient uploadClient;
	private ResourceWSClient resourceClient;

	/**
	 * Create a client from a url and apikey.
	 * 
	 * @param swarmHostUrl
	 *            URL of swarm server WS API
	 * @param apiKey
	 *            API_KEY provided by server
	 */
	public SwarmWSClient(final String swarmHostUrl, final String apiKey) {
		super(swarmHostUrl, apiKey);
	}

	/**
	 * Create a client from a Configuration.
	 * 
	 * @param config
	 *            client configuration
	 */
	public SwarmWSClient(final Configuration config) {
		super(config.getHostname(Protocol.HTTP), config.getAPIKey());
	}

	/**
	 * @return Swarm Members API
	 */
	public ISwarmResourcesClient getSwarmResourceClient() {
		if (membersClient == null)
			membersClient = new SwarmResourceWSClient(swarmHostUrl, apiKey, httpClient);

		return membersClient;
	}
	
	@Override
	public IResourceClient getResourceClient() {
		if (resourceClient == null) 
			resourceClient = new ResourceWSClient(swarmHostUrl, apiKey, httpClient);
		
		return resourceClient;
	}

	/**
	 * @return Swarm Members API
	 */
	public ISwarmBinaryUploadClient getSwarmBinaryUploadClient() {
		if (uploadClient == null)
			uploadClient = new SwarmBinaryUploadWSClient(swarmHostUrl, apiKey, httpClient);

		return uploadClient;
	}

	@Override
	public String create(final String name, final boolean isPublic, final String description) throws IOException {
		validateParams(name, description);
		
		validateAPIKey();

		Map<String, String> props = new HashMap<String, String>();
		props.put("name", name);
		props.put("public", Boolean.toString(isPublic));
		props.put("description", description);

		HTTPResponse response = httpClient.post(swarmHostUrl + "swarms", props);

		JSONObject json = (JSONObject) JSONValue.parse(new InputStreamReader(response.getStream()));

		return json.get("swarm").toString();
	}

	@Override
	public SwarmWSResponse update(final String swarmId, final boolean isPublic, final String description) throws IOException {
		validateParams(swarmId, description);
		
		validateAPIKey();

		Map<String, String> props = new HashMap<String, String>();

		props.put("public", Boolean.toString(isPublic));
		props.put("description", description);

		HTTPResponse response = httpClient.put(swarmHostUrl + "swarms/" + swarmId, props);

		return SwarmWSResponse.fromCode(response.getResponseCode());
	}

	@Override
	public SwarmWSResponse destroy(final String swarmId) throws IOException {
		validateParams(swarmId);

		validateAPIKey();

		HTTPResponse response = httpClient.delete(swarmHostUrl + "swarms/" + swarmId);

		return SwarmWSResponse.fromCode(response.getResponseCode());
	}

	@Override
	public List<SwarmModel> list() throws IOException {
		validateAPIKey();

		HTTPResponse response = httpClient.get(swarmHostUrl + "swarms");

		JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(response.getStream()));

		return SwarmModel.createListFromJson(json);
	}

	@Override
	public SwarmModel get(final String swarmId) throws IOException {
		validateParams(swarmId);

		validateAPIKey();

		HTTPResponse response = httpClient.get(swarmHostUrl + "swarms/" + swarmId);

		Object o = JSONValue.parse(new InputStreamReader(response.getStream()));
		JSONObject jo = null;

		if (o instanceof JSONArray)
			jo = (JSONObject) ((JSONArray) o).get(0);
		else if (o instanceof JSONObject)
			jo = (JSONObject) o;

		if (jo != null)
			return SwarmModel.createFromJson(jo);

		return null;
	}

	@Override
	public Throwable isValid() {
		return super.checkAndValidate(false);
	}
}
