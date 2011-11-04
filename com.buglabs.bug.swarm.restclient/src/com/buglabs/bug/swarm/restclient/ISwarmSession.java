package com.buglabs.bug.swarm.restclient;

import java.io.IOException;
import java.util.Map;

/**
 * Session interface for active connection to swarm server.
 * @author kgilmer
 *
 */
public interface ISwarmSession {

	void send(Map<String, ?> payload) throws IOException;
	
	void send(Map<String, ?> payload, String ... swarmIds) throws IOException;
	
	void send(Map<String, ?> payload, String swarmId, String resourceId) throws IOException;
	
	void addListener(ISwarmMessageListener listener);
	
	void removeListener(ISwarmMessageListener listener);
	
	void close();
}
