package com.buglabs.bug.swarm.connector.xmpp.parse;

import com.buglabs.bug.swarm.connector.xmpp.parse.XMPPPlainTextMessageParser.XMPPMessage;
import com.buglabs.bug.swarm.connector.xmpp.parse.XMPPPlainTextMessageParser.XMPPMessageType;

/**
 * Class for swarm invites.
 * 
 * Handles messages like: //admin@xmpp.bugswarm-dev/514215491308973723669054
 * invites you to the room
 * a383895de7a351df9c0bbc4b26047915bdc7e53b@swarms.xmpp.bugswarm-dev (User
 * connector_test wants you use his Swarm services)
 * 
 * @author kgilmer
 * 
 */
public class InviteMessageImpl implements XMPPMessage {
	/**
	 * Position in string array for room id.
	 */
	private static final int ROOM_ID_INDEX = 6;

	/**
	 * Position in string array for sender jid.
	 */
	private static final int SENDER_JID_INDEX = 0;

	private final String senderJID;
	private final String roomID;

	/**
	 * @param rawMessage
	 *            raw text message
	 * @throws XMPPPlainTextMessageParseException
	 *             upon parse error
	 */
	protected InviteMessageImpl(final String rawMessage) {
		String[] elems = rawMessage.split(" ");
		if (elems.length < ROOM_ID_INDEX + 1)
			throw new RuntimeException("Message does not have enough words: " + rawMessage);
		this.senderJID = elems[SENDER_JID_INDEX];
		this.roomID = elems[ROOM_ID_INDEX].split("@")[0];
	}

	/**
	 * @return JID of sender of message.
	 */
	public String getSenderJID() {
		return senderJID;
	}

	/**
	 * @return id of room invited to.
	 */
	public String getRoomID() {
		return roomID;
	}

	@Override
	public XMPPMessageType getType() {
		return XMPPMessageType.SWARM_INVITE;
	}

	/**
	 * @param message
	 *            raw server message
	 * @return true if the input string contains characteristics that identify
	 *         it as this type.
	 */
	public static boolean isMessageOfType(final String message) {
		return message.contains("invites you to the room") && message.split(" ").length > (ROOM_ID_INDEX + 1);
	}
}
