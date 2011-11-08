package com.buglabs.bug.swarm.restclient;

import java.util.Map;

/**
 * Listener interface to handle messages from swarm server.
 * @author kgilmer
 *
 */
public interface ISwarmMessageListener {

	/**
	 * Classification of errors and unexpected events that can occur within the ISwarmParticipation client.
	 */
	public enum ExceptionType {
		/**
		 * A server-based message was received that was in an unknown format or otherwise unparsable.
		 */
		SERVER_MESSAGE_PARSE_ERROR,
		/**
		 * The server connection failed unexpectedly.
		 */
		SERVER_UNEXPECTED_DISCONNECT, 
		/**
		 * The server sent a valid message but without fields that were expected and required.
		 */
		INVALID_MESSAGE,
		
		/**
		 * The server has sent an error message
		 */
		SERVER_ERROR;
	}
	/**
	 * A server-based message was received.
	 * 
	 * @param payload of the message or null if no payload.
	 * @param fromSwarm id of swarm or null of no swarm id was provided.
	 * @param fromResource id of resource or null if no resource id was provided.
	 * @param isPublic true by default or false if field set to false by server.
	 */
	void messageRecieved(Map<String, ?> payload, String fromSwarm, String fromResource, boolean isPublic);
	
	/**
	 * A server-based presence event was received.
	 * 
	 * @param fromSwarm id of swarm in which presence change occurred, or null if unspecified.
	 * @param fromResource id of resource that generated presence event, or null if unspecified.
	 * @param isAvailable true if server specified type:available in presence event, false otherwise.
	 */
	void presenceEvent(String fromSwarm, String fromResource, boolean isAvailable);
	
	/**
	 * An exception in the swarm client has occurred.  This method is provided for clients to do error
	 * logging or respond to 
	 * @param type
	 * @param message
	 */
	void exceptionOccurred(ExceptionType type, String message);
}
