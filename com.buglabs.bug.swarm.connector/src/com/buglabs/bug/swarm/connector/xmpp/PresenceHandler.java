package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import com.buglabs.bug.swarm.connector.model.Jid;

/**
 * A handler for swarm presence events.
 * @author kgilmer
 *
 */
public class PresenceHandler extends AbstractMessageHandler implements PacketListener {

	protected PresenceHandler(Jid jid, String swarmId, List<ISwarmServerRequestListener> requestListeners) throws Exception {
		super(jid, swarmId, requestListeners);
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

}
