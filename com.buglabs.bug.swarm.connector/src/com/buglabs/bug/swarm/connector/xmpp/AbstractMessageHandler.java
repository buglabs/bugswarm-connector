package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;
import java.util.List;

import org.jivesoftware.smack.packet.Message;
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
		//This occurs when multiple connectors are in a swarm.  The message is ignored.
		if (rawMessage.startsWith("{\"capabilities"))
			return;
		
		FeedRequest freq = FeedRequest.parseJSON(rawMessage);
		
		if (freq == null) {
			Activator.getLog().log(LogService.LOG_WARNING, 
					"Unhandled message received from " + sender + " swarm " + swarmId + " message: " + rawMessage);
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
	
	protected void handleError(Message message, String participant) {
		System.out.println("Error message: " + message.getError() + " from: " + participant);
	}
}
