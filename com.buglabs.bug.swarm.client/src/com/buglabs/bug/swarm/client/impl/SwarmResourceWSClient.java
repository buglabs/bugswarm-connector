package com.buglabs.bug.swarm.client.impl;

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
import org.touge.restclient.ReSTClient.URLBuilder;

import com.buglabs.bug.swarm.client.ISwarmResourcesClient;
import com.buglabs.bug.swarm.client.SwarmWSResponse;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.SwarmResourceModel;

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
		
		URLBuilder url = swarmHostUrl.copy(
				"swarms/",
				swarmId, 
				"/resources?type=" + type);

		//We cannot use a static deserializer here because the json scope of the deserializer does not contain the swarmid.
		Response<List<SwarmResourceModel>> response = httpClient.callGet(
				url, 
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
	public List<SwarmResourceModel> list(final String swarmId) throws IOException {
		validateParams(swarmId);
		
		URLBuilder url = swarmHostUrl.copy(
				"swarms/",
				swarmId, 
				"/resources");

		//We cannot use a static deserializer here because the json scope of the deserializer does not contain the swarmid.
		Response<List<SwarmResourceModel>> response = httpClient.callGet(
				url, 
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

		// URL: http://api.bugswarm.net/swarms/SWARM_ID/resources
		URLBuilder url = swarmHostUrl.copy()
			.append("swarms")
			.append(swarmId)
			.append("resources");
		
		Map<String, String> props = toMap(
				"resource_type", type.toString(),
				"resource_id", resource);

		return httpClient.callPost(
				url, 
				createJsonStream(props), 
				SwarmWSResponse.Deserializer).getContent();
	}

	@Override
	public List<SwarmModel> getSwarmsByMember(final String resourceId) throws IOException {
		validateParams(resourceId);
		
		URLBuilder url = swarmHostUrl.copy("resources/", resourceId, "/swarms");

		Response<List<SwarmModel>> response = httpClient.callGet(url, 
				SwarmModel.LIST_DESERIALIZER);
		
		return response.getContent();
	}

	@Override
	public SwarmWSResponse remove(final String swarmId, final MemberType type, final String userId, final String resourceId)
			throws IOException {

		validateParams(swarmId, type, userId, resourceId);

		URLBuilder url = swarmHostUrl.copy("swarms/", swarmId, "/resources");
		
		Map<String, String> props = toMap(
				"resource_type", type.toString(),			
				"resource_id", resourceId);
		
		Map<String, String> headers = toMap(
				"X-HTTP-Method-Override", "DELETE");

		Response<SwarmWSResponse> response = httpClient.callPost(
				url, 
				createJsonStream(props), 
				headers,
				SwarmWSResponse.Deserializer);
		
		return response.getContent();
	}
}
