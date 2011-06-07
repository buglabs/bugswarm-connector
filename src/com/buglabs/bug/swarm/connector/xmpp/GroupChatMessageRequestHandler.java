package com.buglabs.bug.swarm.connector.xmpp;

import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class GroupChatMessageRequestHandler implements PacketListener {
	
	private final Jid jid;
	private final String swarmId;
	private final List<ISwarmServerRequestListener> requestListeners;

	protected GroupChatMessageRequestHandler(Jid jid, String swarmId, List<ISwarmServerRequestListener> requestListeners) {
		if (jid == null || swarmId == null || requestListeners == null)
			throw new IllegalArgumentException("Input parameter to constructor is null.");
		
		this.jid = jid;
		this.swarmId = swarmId;
		this.requestListeners = requestListeners;	
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
			
			if (isFeedListRequest(m))
				for (ISwarmServerRequestListener listener : requestListeners)
					listener.feedListRequest(new Jid(packet.getFrom()), swarmId);						
		} else {
			//TODO: log warning here, as is unexpected condition.
		}
	}

	private boolean isFeedListRequest(Message m) {
		//TODO: make work
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
}
