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

public class GroupChatMessageRequestHandler implements PacketListener, ChatManagerListener {
	
	private final Jid jid;
	private final String swarmId;
	private final List<ISwarmServerRequestListener> requestListeners;
	private OSGiHelper osgiHelper;

	protected GroupChatMessageRequestHandler(Jid jid, String swarmId, List<ISwarmServerRequestListener> requestListeners) throws Exception {
		if (jid == null || swarmId == null || requestListeners == null)
			throw new IllegalArgumentException("Input parameter to constructor is null.");
		
		this.jid = jid;
		this.swarmId = swarmId;
		this.requestListeners = requestListeners;	
		this.osgiHelper = OSGiHelper.getRef();
	}
	
	@Override
	public void processPacket(Packet packet) {
		if (isFromSelf(packet)) {
			System.out.println("Ignoring message from self: " + packet.getPacketID() + "  " + jid);
			return;
		}
		
		System.out.println("Swarm " + swarmId + " received new public message " + packet.getPacketID() + " from "
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

	private boolean isFeedListRequest(Message m) {
		//TODO: make work
		System.out.println("Checking if " + m.getBody() + " is a Feed List Request");
		return true;
	}

	/**
	 * @param packet
	 * @return true if XMPP packet came from self.
	 */
	private boolean isFromSelf(Packet packet) {
		//TODO: determine better way of determining if packet is from self
		return packet.getFrom().endsWith(jid.getResource());
	}

	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		System.out.println("Private chat created with " + chat.getParticipant());
		
		JSONArray document = JSONElementCreator.createFeedArray(osgiHelper.getBUGFeeds());
		
		try {
			chat.sendMessage(document.toJSONString());
		} catch (XMPPException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Failed to send private message to " + chat.getParticipant(), e);
		}
		
		System.out.println("Sent " + document.toJSONString() + " to " + chat.getParticipant());
	}
}
