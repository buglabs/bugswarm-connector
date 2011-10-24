package com.buglabs.bug.swarm.restclient.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.touge.restclient.ReSTClient;

import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient;
import com.buglabs.bug.swarm.restclient.SwarmWSResponse;
import com.buglabs.bug.swarm.restclient.model.SwarmModel;
import com.buglabs.bug.swarm.restclient.model.SwarmResourceModel;

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

		return httpClient.callGet(swarmHostUrl.copy("swarms/", swarmId, "/resources?type=" + type), 
				SwarmResourceModel.LIST_DESERIALIZER).getContent();
	}

	@Override
	public SwarmWSResponse add(final String swarmId, final MemberType type
			, final String resource) throws IOException {
		validateParams(swarmId, type, resource);

		Map<String, String> props = toMap(
				"resource_type", type.toString(),
				"resource_id", resource);

		return httpClient.callPost(
				swarmHostUrl.copy("swarms/", swarmId), 
				createJsonStream(props), 
				SwarmWSResponse.Deserializer).getContent();
	}

	@Override
	public List<SwarmModel> getSwarmsByMember(final String resource) throws IOException {
		validateParams(resource);

		//TODO, handle case when swarmHostUrl has slash or not has slash.
		return httpClient.callGet(swarmHostUrl.copy("resources/", resource, "/swarms"), 
				SwarmModel.LIST_DESERIALIZER).getContent();
	}

	@Override
	public SwarmWSResponse remove(final String swarmId, final MemberType type, final String userId, final String resource)
			throws IOException {

		validateParams(swarmId, type, userId, resource);

		Map<String, String> props = toMap(
				"type", type.toString(),
				"user_id", userId,
				"resource", resource,
				"X-HTTP-Method-Override", "DELETE");

		return httpClient.callPost(swarmHostUrl.copy("swarms/", swarmId, "/resources"), props, 
				SwarmWSResponse.Deserializer).getContent();
	}
}
