package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.List;

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

/**
 * Centralized class for handing unsolicited and async messages from Swarm server.  Both at the XMPP level and the application level.
 * 
 * @author kgilmer
 * 
 */
public class GroupChatMessageRequestHandler implements PacketListener, MessageListener, ChatManagerListener {

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
	 * Handle a swarm request from server.  This method is only for swarm-specific requests, not XMPP messages.
	 * 
	 * @param rawMessage message as a string
	 * @param sender JID of originator of message
	 * @throws ParseException 
	 */
	private void handleSwarmRequest(String rawMessage, String sender) throws ParseException {				
		FeedRequest freq = FeedRequest.parseJSON(rawMessage);
		
		if (freq == null) {
			Activator.getLog().log(LogService.LOG_ERROR, 
					"Unhandled private message received from user " + sender + " swarm " + swarmId + " message: " + rawMessage);
			return;
		}

		if (freq.isFeedListRequest()) {
			for (ISwarmServerRequestListener listener : requestListeners) {
				listener.feedListRequest(new Jid(sender), swarmId);
			}
		} else if (freq.isFeedRequest()) {
			for (ISwarmServerRequestListener listener : requestListeners) {				
				listener.feedRequest(new Jid(sender), swarmId, freq);				
			}
		} else if (freq.isFeedMetaRequest()) {
			for (ISwarmServerRequestListener listener : requestListeners) {
				listener.feedMetaRequest(freq, swarmId);					
			}
		} 
	}

	@Override
	public void processPacket(final Packet packet) {
		if (isFromSelf(packet)) {			
			return;
		}

		Activator.getLog().log(
				LogService.LOG_INFO,
				"Swarm " + swarmId + " received new public message from " + packet.getFrom() + " to: "	+ packet.getTo());

		try {
			if (packet instanceof Message) {
				Message m = (Message) packet;
				
				if (m.getError() != null) 
					handleMessageError(m);
				 else 
					handleSwarmRequest(m.getBody(), m.getFrom());
			
			} else if (packet instanceof Presence) {
				Presence p = (Presence) packet;
				
				if (p.isAvailable())
					handleMemberJoin(p);
				else 
					handleMemberLeave(p);
				
			} else {
				Activator.getLog().log(LogService.LOG_WARNING, "Unhandled packet received from swarm " + swarmId);
			}
		} catch (ParseException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Unable to parse JID.", e);
		}
	}
	
	/**
	 * Handle messages with errors sent in XMPP packet from server.
	 * 
	 * @param m
	 */
	private void handleMessageError(Message m) {
		Activator.getLog().log(LogService.LOG_ERROR, "Server sent error for message: " + m.getError().toXML());
	}

	/**
	 * Handle case that swarm peer has joined swarm.  This is triggered upon XMPP presence events.
	 * @param p
	 * @throws ParseException
	 */
	private void handleMemberJoin(Presence p) throws ParseException {
		for (ISwarmServerRequestListener listener : requestListeners) {
				Activator.getLog().log(
						LogService.LOG_INFO, "On presence event, sending feed list to new swarm member " + p.getFrom());
				listener.feedListRequest(new Jid(p.getFrom()), swarmId);
			
		}		
	}

	/**
	 * Handle case that swarm peer has left swarm.  This is triggered upon XMPP presence events.
	 * @param p
	 * @throws ParseException
	 */
	private void handleMemberLeave(Presence p) throws ParseException {		
		Activator.getLog().log(LogService.LOG_DEBUG, "Participant " + p.getFrom() + " left " + swarmId + "  Cleaning up.");
		
		for (ISwarmServerRequestListener listener : requestListeners) {
				listener.cancelFeedRequests(new Jid(p.getFrom()), swarmId);				
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
	public void processMessage(final Chat chat, final Message message) {
		try {
			handleSwarmRequest(message.getBody(), chat.getParticipant());
		} catch (ParseException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Unable to parse JID.", e);
		}
	}

	@Override
	public synchronized void chatCreated(final Chat chat, final boolean createdLocally) {
		if (!createdLocally && !chat.getListeners().contains(this)) {					
			chat.addMessageListener(this);
			Activator.getLog().log(LogService.LOG_DEBUG, "Private chat created with " + chat.getParticipant());			
		}
	}
}
