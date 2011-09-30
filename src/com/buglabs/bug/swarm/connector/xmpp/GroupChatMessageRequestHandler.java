package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.model.FeedRequest;
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
public class GroupChatMessageRequestHandler implements PacketListener, ChatManagerListener, MessageListener {

	/**
	 * If same message comes from server within this time window, it will be ignored.
	 */
	private static final long DUPLICATE_DELAY = 1000 * 3;
	/**
	 * Stores a window of messages recently received from the server.
	 */
	private static final Map<String, Long> MESSAGE_HISTORY = new HashMap<String, Long>();
	/**
	 * If the number of messages in messageHistory exceeds value, scan for messages that have passed DUPLICATE_DELAY and remove them.
	 */
	private static final int MESSAGE_HISTORY_GC_COUNT = 10;
	
	private final Jid jid;
	private final String swarmId;
	private final List<ISwarmServerRequestListener> requestListeners;
	

	/**
	 * Construct the handler with a jid, swarmid, and list of local listeners.
	 * 
	 * @param jid
	 *            local jid
	 * @param swarmId
	 *            swarm associated with handler
	 * @param requestListeners
	 *            list of ISwarmServerRequestListeners
	 * @throws Exception
	 *             thrown if OSGi service binding fails
	 */
	protected GroupChatMessageRequestHandler(final Jid jid, final String swarmId, 
			final List<ISwarmServerRequestListener> requestListeners) throws Exception {
		if (jid == null || swarmId == null || requestListeners == null)
			throw new IllegalArgumentException("Input parameter to constructor is null.");

		this.jid = jid;
		this.swarmId = swarmId;
		this.requestListeners = requestListeners;
	}

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
	protected GroupChatMessageRequestHandler(final Jid jid, final List<ISwarmServerRequestListener> requestListeners) throws Exception {
		if (jid == null || requestListeners == null)
			throw new IllegalArgumentException("Input parameter to constructor is null.");

		this.jid = jid;
		this.swarmId = null;
		this.requestListeners = requestListeners;
	}
	
	/**
	 * Process a message from the XMPP server.
	 * 
	 * @param rawMessage message as a string
	 * @param sender JID of originator of message
	 */
	private void processServerMessage(String rawMessage, String sender) {
		if (isDuplicateMessage(rawMessage, sender, DUPLICATE_DELAY)) {
			Activator.getLog().log(LogService.LOG_ERROR, 
					"Ignoring duplicate message from " + sender);
			return;
		}
		
		FeedRequest freq = FeedRequest.parseJSON(rawMessage);
		
		if (freq == null) {
			Activator.getLog().log(LogService.LOG_ERROR, 
					"Unhandled message received from swarm " + swarmId + " message: " + rawMessage);
			return;
		}

		if (freq.isFeedListRequest()) {
			for (ISwarmServerRequestListener listener : requestListeners) {
				try {
					listener.feedListRequest(new Jid(sender), swarmId);
				} catch (ParseException e) {
					Activator.getLog().log(LogService.LOG_ERROR, "Parse error with JID.", e);
				}
			}
		} else if (freq.isFeedRequest()) {
			for (ISwarmServerRequestListener listener : requestListeners) {
				try {
					listener.feedRequest(new Jid(sender), swarmId, freq);
				} catch (ParseException e) {
					Activator.getLog().log(LogService.LOG_ERROR, "Parse error with JID.", e);
				}
			}
		} else if (freq.isFeedMetaRequest()) {
			for (ISwarmServerRequestListener listener : requestListeners) {
				listener.feedMetaRequest(freq, swarmId);					
			}
		} else {
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
					"Unhandled feed request from swarm " + swarmId + " message: " + rawMessage);
		}
	}

	/**
	 * Check if a given message from the server is a dup.
	 * 
	 * @param rawMessage message from server
	 * @param sender sender of message
	 * @param timeWindowMillis time that message is to be considered duplicate
	 * @return true if the request message
	 */
	private synchronized boolean isDuplicateMessage(String rawMessage, String sender, long timeWindowMillis) {
		StringBuilder sb = new StringBuilder();
		boolean retval = false;
		
		String key = sb.append(rawMessage).append(sender).toString();
		
		if (MESSAGE_HISTORY.containsKey(key)) {
			long time = System.currentTimeMillis() - MESSAGE_HISTORY.get(key);
			
			if (time <= timeWindowMillis)
				retval = true;
		} 
		
		MESSAGE_HISTORY.put(key, System.currentTimeMillis());
		
		if (MESSAGE_HISTORY.size() > MESSAGE_HISTORY_GC_COUNT) {
			//Garbage collect old messages
			List<String> garbage = new ArrayList<String>();
			
			for (Map.Entry<String, Long> entry : MESSAGE_HISTORY.entrySet())
				if ((System.currentTimeMillis() - entry.getValue()) > DUPLICATE_DELAY)
					garbage.add(entry.getKey());
			
			for (String mkey : garbage)
				MESSAGE_HISTORY.remove(mkey);
			
			if (garbage.size() > 0)
				Activator.getLog().log(LogService.LOG_DEBUG, "Garbage collected " + garbage.size() + " messages.");
		}
		
		return retval;
	}

	@Override
	public void processPacket(final Packet packet) {
		if (isFromSelf(packet)) {			
			return;
		}

		Activator.getLog().log(
				LogService.LOG_INFO,
				"Swarm " + swarmId + " received new public message " + packet.getPacketID() 
				+ " from " + packet.getFrom() + " to: "	+ packet.getTo());

		if (packet instanceof Message) {
			Message m = (Message) packet;
			
			processServerMessage(m.getBody(), m.getFrom());
		} else if (packet instanceof Presence) {
			Presence p = (Presence) packet;
			
			if (p.isAvailable()) {
				for (ISwarmServerRequestListener listener : requestListeners) {
					try {
						Activator.getLog().log(
								LogService.LOG_INFO, "On presence event, sending feed list to new swarm member " + p.getFrom());
						listener.feedListRequest(new Jid(p.getFrom()), swarmId);
					} catch (ParseException e) {
						Activator.getLog().log(LogService.LOG_ERROR, "Parse error with JID.", e);
					}
				}
			} else if (p.isAway()) {
				Activator.getLog().log(LogService.LOG_INFO, "TODO: cleanup any existing feed requests to user " + p.getFrom());
			}
		} else {
			Activator.getLog().log(LogService.LOG_WARNING, "Unhandled packet received from swarm " + swarmId);
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
