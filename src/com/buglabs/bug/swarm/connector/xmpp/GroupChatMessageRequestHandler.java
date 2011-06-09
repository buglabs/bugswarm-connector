package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.osgi.Activator;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;

/**
 * Centralized class for handing unsolicited messages from XMPP server.
 * 
 * @author kgilmer
 *
 */
public class GroupChatMessageRequestHandler implements PacketListener, ChatManagerListener, MessageListener {
	
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
		
		Activator.getLog().log(LogService.LOG_INFO, "Swarm " + swarmId + " received new public message "
				+ packet.getPacketID() + " from "	+ packet.getFrom() + " to: " + packet.getTo()); 
		
		if (packet instanceof Message) {
			Message m = (Message) packet;
			
			if (isFeedListRequest(m)) {
				for (ISwarmServerRequestListener listener : requestListeners) {
					try {
						listener.feedListRequest(new Jid(packet.getFrom()), swarmId);
					} catch (ParseException e) {
						Activator.getLog().log(LogService.LOG_ERROR, "Parse error with JID.", e);
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
		//What we are looking for here is a JSON object that contains a key of "feed" and a value of "feeds".  This specific
		//combo means that the client is requesting the list of all client feeds.
		String message = m.getBody();
		
		Object o = JSONValue.parse(message);
		
		if (o != null && o instanceof JSONObject) {
			JSONObject jo = (JSONObject) o;
			
			if (jo.containsKey("feed"))
				return jo.get("feed").equals("feeds");
		}

		return false;
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
		
		chat.addMessageListener(this);
		
		//TODO: figure out how to handle when clients close chat connections for proper cleanup.
	}

	@Override
	public void processMessage(Chat chat, Message message) {
		if (isFeedListRequest(message)) {
			for (ISwarmServerRequestListener listener : requestListeners) {
				try {
					listener.feedListRequest(chat, swarmId);
				} catch (Exception e) {
					Activator.getLog().log(LogService.LOG_ERROR, "Parse error with JID.", e);
				}					
			}
		} else {
			Activator.getLog().log(LogService.LOG_WARNING, "Unhandled client message: " + message);
		}
			
	}
}
