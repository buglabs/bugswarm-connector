package com.buglabs.bug.swarm.restclient.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.touge.restclient.ReSTClient.Response;

import com.buglabs.bug.swarm.restclient.ISwarmBinaryUploadClient;
import com.buglabs.bug.swarm.restclient.ISwarmClient;
import com.buglabs.bug.swarm.restclient.ISwarmInviteClient;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient;
import com.buglabs.bug.swarm.restclient.IUserResourceClient;
import com.buglabs.bug.swarm.restclient.SwarmWSResponse;
import com.buglabs.bug.swarm.restclient.model.ModelBase;
import com.buglabs.bug.swarm.restclient.model.SwarmModel;

/**
 * A Swarm WS Client implementation using json.simple and simplerestclient.
 * 
 * @author kgilmer
 * 
 */
public class SwarmWSClient extends AbstractSwarmWSClient implements ISwarmClient {
	private SwarmResourceWSClient membersClient;
	private SwarmBinaryUploadWSClient uploadClient;
	private UserResourceWSClient resourceClient;
	private SwarmInviteWSClient inviteClient;

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
	 * @return Swarm Members API
	 */
	public ISwarmResourcesClient getSwarmResourceClient() {
		if (membersClient == null)
			membersClient = new SwarmResourceWSClient(swarmHostUrl.toString(), apiKey, httpClient);

		return membersClient;
	}
	
	@Override
	public IUserResourceClient getUserResourceClient() {
		if (resourceClient == null) 
			resourceClient = new UserResourceWSClient(swarmHostUrl.toString(), apiKey, httpClient);
		
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
				
		InputStream body = createJsonStream(toMap(
				"name", name,
				"description", description,
				"public", new Boolean(isPublic)));
		
		Response<JsonNode> response = httpClient.callPost(
				swarmHostUrl.copy("swarms"), 
				body,
				ModelBase.JSON_DESERIALIZER);
		
		if (!response.isError())
			return response.getContent().get("id").getTextValue();
		
		throw new IOException("Unable to create swarm: " + response.getErrorMessage());
	}

	@Override
	public SwarmWSResponse update(final String swarmId, final boolean isPublic, final String description) throws IOException {
		validateParams(swarmId, description);
		
		InputStream body = createJsonStream(toMap(
				"public", isPublic,
				"description", description));
		
		Response<SwarmWSResponse> response = httpClient.callPut(
				swarmHostUrl.copy("swarms/", swarmId), 
				body,
				SwarmWSResponse.Deserializer);
		
		return response.getContent();
	}

	@Override
	public SwarmWSResponse destroy(final String swarmId) throws IOException {
		validateParams(swarmId);

		Response<SwarmWSResponse> response = httpClient.callDelete(
				swarmHostUrl.copy("swarms/", swarmId), 
				SwarmWSResponse.Deserializer);
		
		return response.getContent();
	}

	@Override
	public List<SwarmModel> list() throws IOException {
		Response<List<SwarmModel>> response = httpClient.callGet(
				swarmHostUrl.copy("swarms"), 
				SwarmModel.LIST_DESERIALIZER);
		
		return response.getContent();
	}

	@Override
	public SwarmModel get(final String swarmId) throws IOException {
		validateParams(swarmId);
		
		Response<SwarmModel> response = httpClient.callGet(
				swarmHostUrl.copy("swarms", swarmId),
				SwarmModel.DESERIALIZER);
		
		return response.getContent();
	}

	@Override
	public ISwarmInviteClient getSwarmInviteClient() {
		if (inviteClient == null)
			inviteClient = new SwarmInviteWSClient(swarmHostUrl.toString(), apiKey, httpClient);

		return inviteClient;		
	}
}
