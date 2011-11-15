package com.buglabs.bug.swarm.client;


/**
 * Listener interface to handle messages from swarm server.  This is a base listener and does not 
 * provide clients a way to get message payload.  Refer to subclassing interfaces that provide
 * access to the message payload in specific types, such as String or Map.
 * 
 * @see ISwarmJsonMessageListener
 * @see ISwarmStringMessageListener
 * 
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
