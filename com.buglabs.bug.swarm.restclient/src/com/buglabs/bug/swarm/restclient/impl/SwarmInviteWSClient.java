package com.buglabs.bug.swarm.restclient.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.touge.restclient.ReSTClient;
import org.touge.restclient.ReSTClient.Response;
import org.touge.restclient.ReSTClient.URLBuilder;

import com.buglabs.bug.swarm.restclient.ISwarmInviteClient;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.restclient.model.Invitation;

public class SwarmInviteWSClient extends AbstractSwarmWSClient implements ISwarmInviteClient {

	public SwarmInviteWSClient(String host, String apiKey, ReSTClient httpClient) {
		super(host, apiKey, httpClient);
	}

	@Override
	public Invitation send(String swarmId, String user, String resourceId, MemberType resourceType, String description) throws IOException {
		URLBuilder url = swarmHostUrl.copy()
			.append("swarms")
			.append(swarmId)
			.append("invitations");
		
		Map<String, String> body = toMap(
				"to", user,
				"resource_id", resourceId,
				"resource_type", resourceType.toString());
		
		if (description != null)
			body.put("description", description);
		
		Response<Invitation> resp = httpClient.callPost(url, super.createJsonStream(body), Invitation.DESERIALIZER);
		
		return resp.getContent();
	}

	@Override
	public List<Invitation> getSentInvitations() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Invitation> getRecievedInvitations() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Invitation> getRecievedInvitations(String resourceId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Invitation respond(boolean acceptInvitation) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	
}
