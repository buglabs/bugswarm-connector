package com.buglabs.bug.swarm.client;

import java.util.Map;


/**
 * Listener interface to handle Json messages from swarm server.
 * @author kgilmer
 *
 */
public interface ISwarmJsonMessageListener extends ISwarmMessageListener {

	/**
	 * A server-based message was received.
	 * 
	 * @param payload of the message or null if no payload.
	 * @param fromSwarm id of swarm or null of no swarm id was provided.
	 * @param fromResource id of resource or null if no resource id was provided.
	 * @param isPublic true by default or false if field set to false by server.
	 */
	void messageRecieved(Map<String, ?> payload, String fromSwarm, String fromResource, boolean isPublic);	
}
