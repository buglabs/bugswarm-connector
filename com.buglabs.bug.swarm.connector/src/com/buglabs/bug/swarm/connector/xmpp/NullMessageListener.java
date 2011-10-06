package com.buglabs.bug.swarm.connector.xmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * A MessageListener that does nothing. Used for "fire and forget" messages to
 * other peers.
 * 
 * @author kgilmer
 * 
 */
public class NullMessageListener implements MessageListener {

	@Override
	public void processMessage(final Chat chat, final Message message) {
	}
}
