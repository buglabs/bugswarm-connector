package com.buglabs.bug.swarm.restclient.impl;

import java.io.IOException;
import java.net.UnknownHostException;

import org.touge.restclient.ReSTClient;
import org.touge.restclient.ReSTClient.URLBuilder;

import com.buglabs.bug.swarm.restclient.ISwarmParticipation;
import com.buglabs.bug.swarm.restclient.ISwarmSession;

public class SwarmParticipationClient implements ISwarmParticipation {

	private final String hostname;
	private final String apiKey;
	private static final ReSTClient restClient = new ReSTClient();	

	public SwarmParticipationClient(String hostname, String apiKey) {
		if (hostname.toLowerCase().startsWith("http://"))
			this.hostname = hostname.substring("http://".length());
		else 
			this.hostname = hostname;
		
		this.apiKey = apiKey;	 
	}

	@Override
	public ISwarmSession createSession(String resourceId, String... swarmIds) throws UnknownHostException, IOException {
		URLBuilder swarmUrl = restClient.buildURL(hostname)
					.append("stream")
					.addParameter("resource_id", resourceId)
					.emitScheme(false).emitDomain(false);
		
		for (String swarmId : swarmIds)
			swarmUrl.addParameter("swarm_id", swarmId);
		
		return new SwarmSessionImp(hostname, apiKey, swarmUrl.toString());
	}

}
