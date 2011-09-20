package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.buglabs.bug.swarm.connector.Configuration;
import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.model.SwarmModel;
import com.buglabs.util.http.RestClient;

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
			membersClient = new SwarmResourceWSClient(swarmHostUrl.toString(), apiKey, httpClient);

		return membersClient;
	}
	
	@Override
	public IResourceClient getResourceClient() {
		if (resourceClient == null) 
			resourceClient = new ResourceWSClient(swarmHostUrl.toString(), apiKey, httpClient);
		
		return resourceClient;
	}

	/**
	 * @return Swarm Members API
	 */
	public ISwarmBinaryUploadClient getSwarmBinaryUploadClient() {
		if (uploadClient == null)
			uploadClient = new SwarmBinaryUploadWSClient(swarmHostUrl.toString(), apiKey, httpClient);

		return uploadClient;
	}

	@Override
	public String create(final String name, final boolean isPublic, final String description) throws IOException {
		validateParams(name, description);
		
		validateAPIKey();

		return httpClient.callPost(swarmHostUrl.copy("swarms"), 
				toMap("name", name, 
					"public", Boolean.toString(isPublic),
					"description", description)
					, ModelDeserializers.JSONObjectDeserializer)
			.getContent().get("swarm").toString();
	}

	@Override
	public SwarmWSResponse update(final String swarmId, final boolean isPublic, final String description) throws IOException {
		validateParams(swarmId, description);
		
		validateAPIKey();

		return httpClient.callPut(swarmHostUrl.copy("swarms/", swarmId), 
				toMap(
						"public", Boolean.toString(isPublic),
						"description", description),
				ModelDeserializers.SwarmWSResponseDeserializer).getContent();
	}

	@Override
	public SwarmWSResponse destroy(final String swarmId) throws IOException {
		validateParams(swarmId);

		validateAPIKey();

		return httpClient.callDelete(swarmHostUrl.copy("swarms/", swarmId), ModelDeserializers.SwarmWSResponseDeserializer).getContent();
	}

	@Override
	public List<SwarmModel> list() throws IOException {
		validateAPIKey();

		return httpClient.callGet(swarmHostUrl.copy("swarms"), ModelDeserializers.SwarmModelListDeserializer).getContent();
	}

	@Override
	public SwarmModel get(final String swarmId) throws IOException {
		validateParams(swarmId);

		validateAPIKey();

		Object o = JSONValue.parse(new InputStreamReader(
				httpClient.callGet(swarmHostUrl.copy("swarms", swarmId), RestClient.INPUTSTREAM_DESERIALIZER).getContent()));
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
