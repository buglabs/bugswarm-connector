package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.List;

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
public abstract class AbstractMessageHandler {

	protected final Jid jid;
	protected final String swarmId;
	protected final List<ISwarmServerRequestListener> requestListeners;
	

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
	protected AbstractMessageHandler(final Jid jid, final String swarmId, 
			final List<ISwarmServerRequestListener> requestListeners) {
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
	protected void handleSwarmRequest(String rawMessage, String sender) throws ParseException {				
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
	
	/**
	 * Handle case that swarm peer has joined swarm.  This is triggered upon XMPP presence events.
	 * @param p
	 * @throws ParseException
	 */
	protected void handleMemberJoin(Presence p) throws ParseException {
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
	protected void handleMemberLeave(Presence p) throws ParseException {		
		Activator.getLog().log(LogService.LOG_DEBUG, "Participant " + p.getFrom() + " left " + swarmId + "  Cleaning up.");
		
		for (ISwarmServerRequestListener listener : requestListeners) {
				listener.cancelFeedRequests(new Jid(p.getFrom()), swarmId);				
		}		
	}
}
