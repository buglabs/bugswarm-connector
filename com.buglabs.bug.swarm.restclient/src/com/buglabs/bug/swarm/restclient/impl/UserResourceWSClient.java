package com.buglabs.bug.swarm.restclient.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.touge.restclient.ReSTClient;

import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient;
import com.buglabs.bug.swarm.restclient.IUserResourceClient;
import com.buglabs.bug.swarm.restclient.SwarmWSResponse;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.restclient.model.SwarmResourceModel;
import com.buglabs.bug.swarm.restclient.model.UserResourceModel;

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

		// TODO: allow for position coordinates
		Map<String, Object> props = toMap(				
				"name", resourceName, 
				"description", description, 
				"machine_type", machineType,
				"position", toMap("longitude", longitude, "latitude", latitude));

		return httpClient.callPost(
				swarmHostUrl.copy("resources"), 
				createJsonStream(props), 
				UserResourceModel.Deserializer).getContent();
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
				UserResourceModel.ListDeserializer)
				.getContent();
		
	}

	@Override
	public UserResourceModel get(String resourceId) throws IOException {
		validateParams(resourceId);
		
		return httpClient.callGet(swarmHostUrl.copy("resources/", resourceId), 
				UserResourceModel.Deserializer).getContent();				
	}

	@Override
	public SwarmWSResponse remove(String resourceId) throws IOException {
		validateParams(resourceId);

		return httpClient.callDelete(swarmHostUrl.copy("resources/", resourceId), 
				SwarmWSResponse.Deserializer).getContent();
	}

	@Override
	public List<SwarmResourceModel> getMemberSwarms(String resourceId) throws IOException {
		validateParams(resourceId);

		return httpClient.callGet(swarmHostUrl.copy("resources/", resourceId, "/swarms"), 
				SwarmResourceModel.ListDeserializer).getContent();
	}

	@Override
	public List<UserResourceModel> list() throws IOException {		
		return httpClient.callGet(swarmHostUrl.copy("resources"), 
				UserResourceModel.ListDeserializer).getContent();
	}
}
