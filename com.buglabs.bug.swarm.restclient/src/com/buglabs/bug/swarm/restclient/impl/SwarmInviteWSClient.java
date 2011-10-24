package com.buglabs.bug.swarm.restclient.impl;

import java.io.IOException;
import java.util.List;

import org.touge.restclient.ReSTClient;

import com.buglabs.bug.swarm.restclient.ISwarmInviteClient;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.restclient.model.Invitation;

public class SwarmInviteWSClient extends AbstractSwarmWSClient implements ISwarmInviteClient {

	public SwarmInviteWSClient(String host, String apiKey, ReSTClient httpClient) {
		super(host, apiKey, httpClient);
	}

	@Override
	public Invitation send(String user, String resourceId, MemberType resourceType) throws IOException {
		// TODO Auto-generated method stub
		return null;
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
