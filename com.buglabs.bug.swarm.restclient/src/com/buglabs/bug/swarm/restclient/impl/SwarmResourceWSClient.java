package com.buglabs.bug.swarm.restclient.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.touge.restclient.ReSTClient;
import org.touge.restclient.ReSTClient.Response;
import org.touge.restclient.ReSTClient.ResponseDeserializer;

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

		Response<List<SwarmResourceModel>> response = httpClient.callGet(
				swarmHostUrl.copy(
						"swarms/",
						swarmId, 
						"/resources?type=" + type), 
				new ResponseDeserializer<List<SwarmResourceModel>>() {

					@Override
					public List<SwarmResourceModel> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
							throws IOException {
						if (responseCode == 404)
							return Collections.emptyList();
						
						List<SwarmResourceModel> srml= new ArrayList<SwarmResourceModel>();
						ObjectMapper objectMapper = new ObjectMapper();
						JsonNode jtree = objectMapper.readTree(input);
						
						for (JsonNode jn : jtree)
							srml.add(SwarmResourceModel.deserialize(swarmId, jn));
						
						return srml;		
					}
				});
		
		return response.getContent();
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
	public List<SwarmModel> getSwarmsByMember(final String resourceId) throws IOException {
		validateParams(resourceId);

		//TODO, handle case when swarmHostUrl has slash or not has slash.
		return httpClient.callGet(swarmHostUrl.copy("resources/", resourceId, "/swarms"), 
				SwarmModel.LIST_DESERIALIZER).getContent();
	}

	@Override
	public SwarmWSResponse remove(final String swarmId, final MemberType type, final String userId, final String resourceId)
			throws IOException {

		validateParams(swarmId, type, userId, resourceId);

		Map<String, String> props = toMap(
				"type", type.toString(),
				"user_id", userId,
				"resource", resourceId,
				"X-HTTP-Method-Override", "DELETE");

		return httpClient.callPost(swarmHostUrl.copy("swarms/", swarmId, "/resources"), props, 
				SwarmWSResponse.Deserializer).getContent();
	}
}
