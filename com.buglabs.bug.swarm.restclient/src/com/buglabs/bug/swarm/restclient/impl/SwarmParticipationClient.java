package com.buglabs.bug.swarm.restclient.impl;

import java.io.IOException;
import java.net.UnknownHostException;

import com.buglabs.bug.swarm.restclient.ISwarmParticipation;
import com.buglabs.bug.swarm.restclient.ISwarmSession;

public class SwarmParticipationClient implements ISwarmParticipation {

	private final String hostname;
	private final String apiKey;

	public SwarmParticipationClient(String hostname, String apiKey) {
		if (hostname.toLowerCase().startsWith("http://"))
			this.hostname = hostname.substring("http://".length());
		else 
			this.hostname = hostname;
		
		this.apiKey = apiKey;	 
	}

	@Override
	public ISwarmSession createSession(String resourceId, String... swarmIds) throws UnknownHostException, IOException {					
		return new SwarmSessionImp(hostname, apiKey, resourceId, swarmIds);
	}

}
