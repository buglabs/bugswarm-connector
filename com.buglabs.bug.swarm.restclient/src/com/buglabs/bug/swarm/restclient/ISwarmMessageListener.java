package com.buglabs.bug.swarm.restclient;

import java.util.Map;

/**
 * Listener interface to handle messages from swarm server.
 * @author kgilmer
 *
 */
public interface ISwarmMessageListener {

	void messageRecieved(Map<String, ?> payload, String fromSwarm, String fromResource, boolean isPublic);
}
