package com.buglabs.bug.swarm.connector.xmpp.parse;

/**
 * A class that will attempt to parse incoming plaintext from an XMPP server and
 * return classes thought to represent the intent of the messages.
 * 
 * @author kgilmer
 * 
 */
public class XMPPPlainTextMessageParser {

	/**
	 * Enumeration for all known message types.
	 * 
	 * @author kgilmer
	 * 
	 */
	public enum XMPPMessageType {
		SWARM_INVITE
	};

	/**
	 * Interface for types that represent intents with data from the server.
	 * 
	 * @author kgilmer
	 * 
	 */
	public interface XMPPMessage {
		/**
		 * @return type of message.
		 */
		XMPPMessageType getType();
	}

	/**
	 * 
	 */
	private XMPPPlainTextMessageParser() {
		// Static class cannot be instantiated.
	}

	/**
	 * @param message
	 *            Text content of message from server.
	 * @return A message or null if the message did not have identifiable
	 *         characteristics.
	 * @throws XMPPPlainTextMessageParseException
	 *             if a parsing error occurs.
	 */
	public static XMPPMessage parseServerMessage(final String message) {
		// Make some guesses based on the contents of the server message
		if (InviteMessageImpl.isMessageOfType(message))
			return new InviteMessageImpl(message);

		return null;
	}
}
