package com.buglabs.bug.swarm.client.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.touge.restclient.ReSTClient;
import org.touge.restclient.ReSTClient.Response;

import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.IUserResourceClient;
import com.buglabs.bug.swarm.client.SwarmWSResponse;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;

/**
 * Implementation of IResourceClient.
 * 
 * @author kgilmer
 * 
 */
public class UserResourceWSClient extends AbstractSwarmWSClient implements IUserResourceClient {

	/**
	 * @param swarmHostUrl
	 *            URL of Swarm server
	 * @param apiKey
	 *            API key for client
	 * @param httpClient
	 *            instance of HTTP client
	 */
	public UserResourceWSClient(String swarmHostUrl, String apiKey, ReSTClient httpClient) {
		super(swarmHostUrl, apiKey, httpClient);
	}

	@Override
	public UserResourceModel add(String resourceName, String description, String machineType, float longitude, float latitude) throws IOException {
		validateParams(resourceName, description, machineType);

		Map<String, Object> props = toMap(				
				"name", resourceName, 
				"description", description, 
				"machine_type", machineType,
				"position", toMap("longitude", longitude, "latitude", latitude));

		return httpClient.callPost(
				swarmHostUrl.copy("resources"), 
				createJsonStream(props), 
				UserResourceModel.DESERIALIZER).getContent();
	}

	@Override
	public SwarmWSResponse update(String resourceId, String resourceName, String resourceDescription, MemberType type, 
			String machineType)	throws IOException {
		validateParams(resourceId, resourceName, resourceDescription, type, machineType);

		Map<String, String> props = toMap(
				"name", resourceName, 
				"description", resourceDescription, 
				"type", type.toString(),
				"machine_type", machineType);

		return httpClient.callPut(swarmHostUrl.copy("resources/", resourceId), props, 
				SwarmWSResponse.Deserializer).getContent();
	}

	@Override
	public List<UserResourceModel> get() throws IOException {

		return httpClient.callGet(
				swarmHostUrl.copy("resources"), 
				UserResourceModel.LIST_DESERIALIZER)
				.getContent();
		
	}

	@Override
	public UserResourceModel get(String resourceId) throws IOException {
		validateParams(resourceId);
		
		return httpClient.callGet(swarmHostUrl.copy("resources/", resourceId), 
				UserResourceModel.DESERIALIZER).getContent();				
	}

	@Override
	public SwarmWSResponse destroy(String resourceId) throws IOException {
		validateParams(resourceId);

		return httpClient.callDelete(swarmHostUrl.copy("resources/", resourceId), 
				SwarmWSResponse.Deserializer).getContent();
	}

	@Override
	public List<SwarmModel> getMemberSwarms(String resourceId) throws IOException {
		validateParams(resourceId);

		Response<List<SwarmModel>> response = httpClient.callGet(
				swarmHostUrl.copy("resources/", resourceId, "/swarms"), 
				SwarmModel.LIST_DESERIALIZER);
		
		return response.getContent();
	}

	@Override
	public List<UserResourceModel> list() throws IOException {		
		return httpClient.callGet(swarmHostUrl.copy("resources"), 
				UserResourceModel.LIST_DESERIALIZER).getContent();
	}
}
