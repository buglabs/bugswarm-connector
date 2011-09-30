package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.Activator;
import com.buglabs.bug.swarm.connector.xmpp.parse.InviteMessageImpl;
import com.buglabs.bug.swarm.connector.xmpp.parse.XMPPPlainTextMessageParser;
import com.buglabs.bug.swarm.connector.xmpp.parse.XMPPPlainTextMessageParser.XMPPMessage;

/**
 * Centralized class for handing unsolicited and async messages from XMPP
 * server.
 * 
 * @author kgilmer
 * 
 */
public class RootMessageRequestHandler implements PacketListener, ChatManagerListener, MessageListener {

	
	private final Jid jid;

	private final List<ISwarmServerRequestListener> requestListeners;
	

	/**
	 * Construct the handler with a jid, swarmid, and list of local listeners.
	 * 
	 * @param jid
	 *            local jid	
	 * @param requestListeners
	 *            list of ISwarmServerRequestListeners
	 * @throws Exception
	 *             thrown if OSGi service binding fails
	 */
	protected RootMessageRequestHandler(final Jid jid, final List<ISwarmServerRequestListener> requestListeners) throws Exception {
		if (jid == null || requestListeners == null)
			throw new IllegalArgumentException("Input parameter to constructor is null.");

		this.jid = jid;
		this.requestListeners = requestListeners;
	}
	
	/**
	 * Process a message from the XMPP server.
	 * 
	 * @param rawMessage message as a string
	 * @param sender JID of originator of message
	 */
	private void processServerMessage(String rawMessage, String sender) {
			XMPPMessage im = XMPPPlainTextMessageParser.parseServerMessage(rawMessage);

			if (im != null) {
				for (ISwarmServerRequestListener listener : requestListeners) {
					try {
						if (handlePlainTextMessage(im, listener))
							return;
					} catch (ParseException e) {
						Activator.getLog().log(LogService.LOG_ERROR, "Parse error with JID.", e);
					}
				}

			} 
			
			Activator.getLog().log(LogService.LOG_ERROR, 
					"Unhandled request from : " + rawMessage + " in " + this.getClass().getCanonicalName());
	}

	@Override
	public void processPacket(final Packet packet) {
		if (isFromSelf(packet)) {			
			return;
		}

		if (packet instanceof Message) {
			Message m = (Message) packet;
			
			processServerMessage(m.getBody(), m.getFrom());
		}
	}

	/**
	 * @param packet
	 *            XMPP packet
	 * @return true if XMPP packet came from self.
	 */
	private boolean isFromSelf(final Packet packet) {
		// TODO: determine better way of determining if packet is from self
		return packet.getFrom().endsWith(jid.getResource());
	}

	@Override
	public void chatCreated(final Chat chat, final boolean createdLocally) {
		Activator.getLog().log(LogService.LOG_DEBUG, "Private chat created with " + chat.getParticipant());

		chat.addMessageListener(this);

		// TODO: figure out how to handle when clients close chat connections
		// for proper cleanup.
	}

	@Override
	public void processMessage(final Chat chat, final Message message) {	
		processServerMessage(message.getBody(), chat.getParticipant());
	}

	/**
	 * @param im
	 *            message
	 * @param listener
	 *            listener to send event to
	 * @return true if message was successfully handled, false otherwise.
	 */
	private boolean handlePlainTextMessage(final XMPPMessage im, final ISwarmServerRequestListener listener) throws ParseException {
		switch (im.getType()) {
		case SWARM_INVITE:
			InviteMessageImpl imi = (InviteMessageImpl) im;
			listener.swarmInviteRequest(new Jid(imi.getSenderJID()), imi.getRoomID());
			return true;
		default:
			return false;
		}
	}
}
