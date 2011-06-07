package com.buglabs.bug.swarm.connector.xmpp;

import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.json.simple.JSONArray;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.osgi.Activator;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;

/**
 * Centralized class for handing unsolicited messages from XMPP server.
 * 
 * @author kgilmer
 *
 */
public class GroupChatMessageRequestHandler implements PacketListener, ChatManagerListener {
	
	private final Jid jid;
	private final String swarmId;
	private final List<ISwarmServerRequestListener> requestListeners;
	private OSGiHelper osgiHelper;

	/**
	 * Construct the handler with a jid, swarmid, and list of local listeners.
	 * 
	 * @param jid local jid
	 * @param swarmId swarm associated with handler
	 * @param requestListeners list of ISwarmServerRequestListeners
	 * @throws Exception thrown if OSGi service binding fails
	 */
	protected GroupChatMessageRequestHandler(
			final Jid jid, final String swarmId, final List<ISwarmServerRequestListener> requestListeners) throws Exception {
		if (jid == null || swarmId == null || requestListeners == null)
			throw new IllegalArgumentException("Input parameter to constructor is null.");
		
		this.jid = jid;
		this.swarmId = swarmId;
		this.requestListeners = requestListeners;	
		this.osgiHelper = OSGiHelper.getRef();
	}
	
	@Override
	public void processPacket(final Packet packet) {
		if (isFromSelf(packet)) {
			System.out.println("Ignoring message from self: " + packet.getPacketID() + "  " + jid);
			return;
		}
		
		Activator.getLog().log(LogService.LOG_INFO, "Swarm " + swarmId + " received new public message " + packet.getPacketID() + " from "
				+ packet.getFrom() + " to: " + packet.getTo()); 
		
		if (packet instanceof Message) {
			Message m = (Message) packet;
			
			if (isFeedListRequest(m)) {
				for (ISwarmServerRequestListener listener : requestListeners) {
					try {
						listener.feedListRequest(new Jid(packet.getFrom()), swarmId);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
			}
		} else {
			Activator.getLog().log(LogService.LOG_WARNING, "Unhandled packet received from swarm " + swarmId);
		}
	}

	/**
	 * @param m message
	 * @return true if message is a feed list request
	 */
	private boolean isFeedListRequest(final Message m) {
		//TODO: make work
		Activator.getLog().log(LogService.LOG_DEBUG, "Checking if " + m.getBody() + " is a Feed List Request");
		return true;
	}

	/**
	 * @param packet XMPP packet
	 * @return true if XMPP packet came from self.
	 */
	private boolean isFromSelf(final Packet packet) {
		//TODO: determine better way of determining if packet is from self
		return packet.getFrom().endsWith(jid.getResource());
	}

	@Override
	public void chatCreated(final Chat chat, final boolean createdLocally) {
		Activator.getLog().log(LogService.LOG_DEBUG, "Private chat created with " + chat.getParticipant());
		
		JSONArray document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds());
		
		try {
			chat.sendMessage(document.toJSONString());
		} catch (XMPPException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Failed to send private message to " + chat.getParticipant(), e);
		}
		
		Activator.getLog().log(LogService.LOG_DEBUG, "Sent " + document.toJSONString() + " to " + chat.getParticipant());
	}
}
