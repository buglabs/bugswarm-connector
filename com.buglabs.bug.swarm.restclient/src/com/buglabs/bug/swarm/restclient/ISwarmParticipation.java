package com.buglabs.bug.swarm.restclient;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Top-level interface for SwarmParticipation API.  
 * @author kgilmer
 *
 */
public interface ISwarmParticipation {

	ISwarmSession createSession(String resourceId, String ... swarmIds) throws UnknownHostException, IOException;
}
