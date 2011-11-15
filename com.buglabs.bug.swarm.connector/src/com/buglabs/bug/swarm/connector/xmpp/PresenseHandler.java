package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.Activator;

public class PresenseHandler implements PacketListener {

	private final List<ISwarmServerRequestListener> requestListeners;
	private final String swarmId;

	public PresenseHandler(String swarmId, List<ISwarmServerRequestListener> requestListeners) {
		this.swarmId = swarmId;
		this.requestListeners = requestListeners;
	}

	@Override
	public void processPacket(Packet packet) {
		if (!(packet instanceof Presence)) 
			return;
			
		Presence p = (Presence) packet;
		
		try {
			if (p.isAvailable())
				handleMemberJoin(p);
			else 
				handleMemberLeave(p);		
		} catch (ParseException e) {
			e.printStackTrace();
		}
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

}
