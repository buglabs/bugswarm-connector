package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.Activator;

/**
 * A handler for private messages from swarm peers.
 * 
 * @author kgilmer
 *
 */
public class PrivateMessageHandler extends AbstractMessageHandler implements MessageListener {

	protected PrivateMessageHandler(Jid jid, String swarmId, List<ISwarmServerRequestListener> requestListeners) {
		super(jid, swarmId, requestListeners);
	}

	@Override
	public void processMessage(Chat chat, Message message) {
		try {
			if (message.getError() == null)
				handleSwarmRequest(message.getBody(), chat.getParticipant(), message.getTo());
			else 
				handleError(message, chat.getParticipant());
		} catch (ParseException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Error parsing message.", e);
		}
	}
}
