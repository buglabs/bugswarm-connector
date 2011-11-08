package com.buglabs.bug.swarm.client.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.touge.restclient.ReSTClient;
import org.touge.restclient.ReSTClient.Response;
import org.touge.restclient.ReSTClient.URLBuilder;

import com.buglabs.bug.swarm.client.ISwarmInviteClient;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.model.Invitation;

public class SwarmInviteWSClient extends AbstractSwarmWSClient implements ISwarmInviteClient {

	public SwarmInviteWSClient(String host, String apiKey, ReSTClient httpClient) {
		super(host, apiKey, httpClient);
	}

	@Override
	public Invitation send(String swarmId, String user, String resourceId, MemberType resourceType, String description) throws IOException {
		//URL: http://api.bugswarm.net/swarms/SWARM_ID/invitations
		URLBuilder url = swarmHostUrl.copy()
			.append("swarms")
			.append(swarmId)
			.append("invitations");
		
		Map<String, String> body = toMap(
				"to", user,
				"resource_id", resourceId,
				"resource_type", resourceType.toString());
		
		if (description != null && description.length() > 0)
			body.put("description", description);
		
		
		Response<Invitation> resp = httpClient.callPost(url, super.createJsonStream(body), Invitation.DESERIALIZER);
		
		return resp.getContent();
	}

	@Override
	public List<Invitation> getSentInvitations(String swarmId) throws IOException {
		//URL: http://api.bugswarm.net/swarms/SWARM_ID/invitations
		URLBuilder url = swarmHostUrl.copy()
			.append("swarms")
			.append(swarmId)
			.append("invitations");
		
		Response<List<Invitation>> resp = httpClient.callGet(url, Invitation.LIST_DESERIALIZER);
		
		return resp.getContent();
	}

	@Override
	public List<Invitation> getRecievedInvitations() throws IOException {
		// URL: http://api.bugswarm.net/invitations
		URLBuilder url = swarmHostUrl.copy()
			.append("invitations");	
		
		Response<List<Invitation>> resp = httpClient.callGet(url, Invitation.LIST_DESERIALIZER);
		
		return resp.getContent();
	}

	@Override
	public List<Invitation> getRecievedInvitations(String resourceId) throws IOException {
		// URL: http://api.bugswarm.net/resources/RESOURCE_ID/invitations
		URLBuilder url = swarmHostUrl.copy()
			.append("resources")
			.append(resourceId)
			.append("invitations");	
		
		Response<List<Invitation>> resp = httpClient.callGet(url, Invitation.LIST_DESERIALIZER);
		
		return resp.getContent();		
	}

	@Override
	public Invitation respond(String resourceId, String invitationId, InvitationResponse resourceType) throws IOException {
		// URL: http://api.bugswarm.net/resources/RESOURCE_ID/invitations/INVITATION_ID
		URLBuilder url = swarmHostUrl.copy()
			.append("resources")
			.append(resourceId)
			.append("invitations")
			.append(invitationId);	
		
		Map<String, String> body = toMap("status", resourceType.toString());
		
		Response<Invitation> response = httpClient.callPut(url, super.createJsonStream(body), Invitation.DESERIALIZER);
		
		return response.getContent();
	}
}
