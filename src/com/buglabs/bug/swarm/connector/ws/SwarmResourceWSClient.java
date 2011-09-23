package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buglabs.bug.swarm.connector.model.SwarmModel;
import com.buglabs.bug.swarm.connector.model.SwarmResourceModel;
import com.buglabs.util.http.ReSTClient;

/**
 * Client implementation for Swarm Members API.
 * 
 * @author kgilmer
 * 
 */
public class SwarmResourceWSClient extends AbstractSwarmWSClient implements ISwarmResourcesClient {

	/**
	 * @param swarmHostUrl
	 *            URL of swarm WS server
	 * @param apiKey
	 *            API_KEY
	 * @param httpClient
	 *            base HTTP client
	 */
	public SwarmResourceWSClient(final String swarmHostUrl, final String apiKey, final ReSTClient httpClient) {
		super(swarmHostUrl, apiKey, httpClient);
	}

	@Override
	public List<SwarmResourceModel> list(final String swarmId, final MemberType type) throws IOException {
		validateParams(swarmId, type);

		validateAPIKey();

		return httpClient.callGet(swarmHostUrl.copy("swarms/", swarmId, "/resources?type=" + type), 
				ModelDeserializers.SwarmResourceModelListDeserializer).getContent();
	}

	@Override
	public SwarmWSResponse add(final String swarmId, final MemberType type
			, final String userId, final String resource) throws IOException {
		validateParams(swarmId, type, userId, resource);

		validateAPIKey();

		Map<String, String> props = toMap(
				"type", type.toString(),
				"user_id", userId,
				"resource", resource);

		return httpClient.callPost(swarmHostUrl.copy("swarms/", swarmId, "/resources"), props, 
				ModelDeserializers.SwarmWSResponseDeserializer).getContent();
	}

	@Override
	public List<SwarmModel> getSwarmsByMember(final String resource) throws IOException {
		validateParams(resource);

		validateAPIKey();

		//TODO, handle case when swarmHostUrl has slash or not has slash.
		return httpClient.callGet(swarmHostUrl.copy("resources/", resource, "/swarms"), 
				ModelDeserializers.SwarmModelListDeserializer).getContent();
	}

	@Override
	public SwarmWSResponse remove(final String swarmId, final MemberType type, final String userId, final String resource)
			throws IOException {

		validateParams(swarmId, type, userId, resource);
		validateAPIKey();

		Map<String, String> props = toMap(
				"type", type.toString(),
				"user_id", userId,
				"resource", resource,
				"X-HTTP-Method-Override", "DELETE");

		return httpClient.callPost(swarmHostUrl.copy("swarms/", swarmId, "/resources"), props, 
				ModelDeserializers.SwarmWSResponseDeserializer).getContent();
	}
}
