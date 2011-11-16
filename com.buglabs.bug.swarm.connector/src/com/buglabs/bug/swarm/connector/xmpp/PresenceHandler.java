package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.Activator;

/**
 * A handler for swarm presence events.
 * @author kgilmer
 *
 */
public class PresenceHandler extends AbstractMessageHandler implements PacketListener {

	private final SwarmXMPPClient swarmXMPPClient;

	protected PresenceHandler(Jid jid, String swarmId, List<ISwarmServerRequestListener> requestListeners, SwarmXMPPClient swarmXMPPClient) throws Exception {
		super(jid, swarmId, requestListeners);
		this.swarmXMPPClient = swarmXMPPClient;
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
	protected void handleMemberJoin(Presence p) throws ParseException {
		for (ISwarmServerRequestListener listener : requestListeners) {
				Activator.getLog().log(
						LogService.LOG_INFO, "On presence event, sending feed list to new swarm member " + p.getFrom());
				listener.addMemberSwarm(swarmId);
				
				listener.feedListRequest(new Jid(p.getFrom()), swarmId);		
		}		
	}
	
	/**
	 * Handle case that swarm peer has left swarm.  This is triggered upon XMPP presence events.
	 * @param p
	 * @throws ParseException
	 */
	private void handleMemberLeave(Presence p) throws ParseException {	
		Jid memberJid = new Jid(p.getFrom());
		if (memberJid.getResource().equals(jid.getResource())) {
			//This means we have left a swarm
			Activator.getLog().log(LogService.LOG_DEBUG, "We have left " + swarmId + ",  Cleaning up.");
			swarmXMPPClient.leaveSwarm(swarmId);
		} else {
			Activator.getLog().log(LogService.LOG_DEBUG, "Participant " + p.getFrom() + " left " + swarmId + ",  Cleaning up.");
			
			for (ISwarmServerRequestListener listener : requestListeners) {
					listener.cancelFeedRequests(new Jid(p.getFrom()), swarmId);				
			}		
		}
	}
}
