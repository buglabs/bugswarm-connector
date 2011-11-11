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
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.model.FeedRequest;
import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.Activator;

/**
 * Centralized class for handing unsolicited and async messages from XMPP
 * server.
 * 
 * @author kgilmer
 * 
 */
public class GroupChatMessageRequestHandler implements PacketListener, MessageListener, ChatManagerListener, ParticipantStatusListener {

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
	 * Process a message from the XMPP server.
	 * 
	 * @param rawMessage message as a string
	 * @param sender JID of originator of message
	 */
	private void processServerMessage(String rawMessage, String sender) {				
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
		} 
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
			} else {
				Activator.getLog().log(LogService.LOG_WARNING, "Participant " + p.getFrom() + " left " + swarmId + "  Cleaning up.");
				
				for (ISwarmServerRequestListener listener : requestListeners) {
					try {
						listener.cancelFeedRequests(new Jid(p.getFrom()), swarmId);
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
		processServerMessage(message.getBody(), chat.getParticipant());
	}

	@Override
	public void chatCreated(final Chat chat, final boolean createdLocally) {
		if (!createdLocally) {
			Activator.getLog().log(LogService.LOG_DEBUG, "Private chat created with " + chat.getParticipant());
	
			chat.addMessageListener(this);
		}

		// TODO: figure out how to handle when clients close chat connections
		// for proper cleanup.
	}

	@Override
	public void joined(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void left(String participant) {
		Activator.getLog().log(LogService.LOG_WARNING, "Participant " + participant + " left " + swarmId);
		
		for (ISwarmServerRequestListener listener : requestListeners) {
			try {
				listener.cancelFeedRequests(new Jid(participant), swarmId);
			} catch (ParseException e) {
				Activator.getLog().log(LogService.LOG_ERROR, "Parse error with JID.", e);
			}
		}
	}

	@Override
	public void kicked(String participant, String actor, String reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void voiceGranted(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void voiceRevoked(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void banned(String participant, String actor, String reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void membershipGranted(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void membershipRevoked(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moderatorGranted(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moderatorRevoked(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ownershipGranted(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ownershipRevoked(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void adminGranted(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void adminRevoked(String participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nicknameChanged(String participant, String newNickname) {
		// TODO Auto-generated method stub
		
	}
}
