package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.Activator;

/**
 * Centralized class for handing unsolicited and async messages from Swarm server.  Both at the XMPP level and the application level.
 * 
 * @author kgilmer
 * 
 */
public class PublicMessageHandler extends AbstractMessageHandler implements PacketListener {


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
	protected PublicMessageHandler(final Jid jid, final String swarmId, 
			final List<ISwarmServerRequestListener> requestListeners) {
		super(jid, swarmId, requestListeners);		
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
				
				if (m.getError() != null || m.getBody().equals("This room is not anonymous")) 
					handleError(m, m.getFrom());
				 else 
					handleSwarmRequest(m.getBody(), m.getFrom(), m.getTo());
			
			} else {
				Activator.getLog().log(LogService.LOG_WARNING, "Unhandled packet received from swarm " + swarmId);
			}
		} catch (ParseException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Unable to parse JID.", e);
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
}
