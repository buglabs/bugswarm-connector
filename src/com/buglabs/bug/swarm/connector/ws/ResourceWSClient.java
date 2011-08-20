package com.buglabs.bug.swarm.connector.ws;

import java.util.List;

import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.util.simplerestclient.HTTPRequest;

/**
 * Implementation of IResourceClient
 * @author kgilmer
 *
 */
public class ResourceWSClient extends AbstractSwarmWSClient implements IResourceClient {

	/**
	 * @param swarmHostUrl
	 * @param apiKey
	 * @param httpClient
	 */
	public ResourceWSClient(String swarmHostUrl, String apiKey, HTTPRequest httpClient) {
		super(swarmHostUrl, apiKey, httpClient);
	}

	@Override
	public SwarmWSResponse add(String resourceId, String userId, String resourceName, String description, MemberType type,
			String machineType) {
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public SwarmWSResponse update(String resourceName, String resourceDescription, MemberType type, String machineType) {
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public List<ResourceModel> get(MemberType type) {
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public ResourceModel get(String resourceId) {
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public SwarmWSResponse remove(String resourceId) {
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public List<SwarmResourceModel> getMemberSwarms(String resourceId) {
		throw new RuntimeException("Unimplemented");
	}
}
