package com.buglabs.bug.swarm.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.touge.restclient.ReSTClient.Response;

import com.buglabs.bug.swarm.client.ISwarmBinaryUploadClient;
import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmConfiguration;
import com.buglabs.bug.swarm.client.ISwarmInviteClient;
import com.buglabs.bug.swarm.client.ISwarmInviteClient.InvitationResponse;
import com.buglabs.bug.swarm.client.ISwarmKeysClient;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.IUserResourceClient;
import com.buglabs.bug.swarm.client.SwarmWSResponse;
import com.buglabs.bug.swarm.client.model.Invitation;
import com.buglabs.bug.swarm.client.model.ModelBase;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.SwarmResourceModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;

/**
 * A Swarm WS Client implementation using json.simple and simplerestclient.
 * 
 * @author kgilmer
 * 
 */
public class SwarmWSClient extends AbstractSwarmWSClient implements ISwarmClient, ISwarmConfiguration {
	private SwarmResourceWSClient membersClient;
	private SwarmBinaryUploadWSClient uploadClient;
	private UserResourceWSClient resourceClient;
	private SwarmInviteWSClient inviteClient;
	private SwarmKeysWSClient keysClient;

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
	@Override
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
	@Override
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
	public String create(final String name, final String description) throws IOException {
		validateParams(name, description);
				
		InputStream body = createJsonStream(toMap(
				"name", name,
				"description", description));
		
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
	
	@Override
	public ISwarmKeysClient getSwarmKeysClient() {
		if (keysClient == null)
			keysClient = new SwarmKeysWSClient(swarmHostUrl.toString());

		return keysClient;		
	}

	@Override
	public SwarmWSResponse upload(String userId, String resourceId, String filename, byte[] payload) throws IOException {	
		return getSwarmBinaryUploadClient().upload(userId, resourceId, filename, payload);
	}

	@Override
	public String createSwarm(String name, boolean isPublic, String description) throws IOException {		
		return create(name, isPublic, description);
	}

	@Override
	public SwarmWSResponse updateSwarm(String swarmId, boolean isPublic, String description) throws IOException {		
		return update(swarmId, isPublic, description);
	}

	@Override
	public SwarmWSResponse destroySwarm(String swarmId) throws IOException {
		return destroy(swarmId);
	}

	@Override
	public List<SwarmModel> listSwarms() throws IOException {
		return list();
	}

	@Override
	public SwarmModel getSwarm(String swarmId) throws IOException {
		return get(swarmId);
	}

	@Override
	public Invitation send(String swarmId, String user, String resourceId, MemberType resourceType, String description) throws IOException {
		return getSwarmInviteClient().send(swarmId, user, resourceId, resourceType, description);
	}

	@Override
	public List<Invitation> getSentInvitations(String swarmId) throws IOException {
		return getSwarmInviteClient().getSentInvitations(swarmId);
	}

	@Override
	public List<Invitation> getRecievedInvitations() throws IOException {		
		return getSwarmInviteClient().getRecievedInvitations();
	}

	@Override
	public List<Invitation> getRecievedInvitations(String resourceId) throws IOException {	
		return getSwarmInviteClient().getRecievedInvitations(resourceId);
	}

	@Override
	public Invitation respond(String resourceId, String invitationId, InvitationResponse action) throws IOException {	
		return getSwarmInviteClient().respond(resourceId, invitationId, action);
	}

	@Override
	public List<SwarmResourceModel> listResources(String swarmId, ISwarmResourcesClient.MemberType type) throws IOException {
		return getSwarmResourceClient().list(swarmId, type);
	}

	@Override
	public SwarmWSResponse addResource(String swarmId, ISwarmResourcesClient.MemberType type, String resourceId) throws IOException {
		return getSwarmResourceClient().add(swarmId, type, resourceId);
	}

	@Override
	public SwarmWSResponse removeResource(String swarmId, ISwarmResourcesClient.MemberType type, String userId, String resourceId)
			throws IOException {
		return getSwarmResourceClient().remove(swarmId, type, userId, resourceId);
	}

	@Override
	public UserResourceModel createResource(String resourceName, String description, String machineType, float longitude, float latitude) throws IOException {
		return getUserResourceClient().add(resourceName, description, machineType, longitude, latitude);
	}

	@Override
	public SwarmWSResponse updateResource(String resourceId, String resourceName, String resourceDescription, MemberType type, String machineType) throws IOException {
		return getUserResourceClient().update(resourceId, resourceName, resourceDescription, type, machineType);
	}

	@Override
	public List<UserResourceModel> getResources() throws IOException {
		return getUserResourceClient().get();
	}

	@Override
	public UserResourceModel getResource(String resourceId) throws IOException {
		return getUserResourceClient().get(resourceId);
	}

	@Override
	public SwarmWSResponse removeResource(String resourceId) throws IOException {
		return getUserResourceClient().destroy(resourceId);
	}

	@Override
	public List<SwarmModel> getMemberSwarms(String resourceId) throws IOException {
		return getUserResourceClient().getMemberSwarms(resourceId);
	}

	@Override
	public List<UserResourceModel> listResource() throws IOException {
		return getUserResourceClient().list();
	}
}
