package com.buglabs.bug.swarm.connector.xmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * A MessageListener that does nothing.  Used for "fire and forget" messages to other peers.
 * 
 * @author kgilmer
 *
 */
public class DeadMessageListener implements MessageListener {

	@Override
	public void processMessage(Chat chat, Message message) {		
	}
}
