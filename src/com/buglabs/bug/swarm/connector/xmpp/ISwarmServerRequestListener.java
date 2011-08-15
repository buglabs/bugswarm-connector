package com.buglabs.bug.swarm.connector.xmpp;

import org.jivesoftware.smack.Chat;

/**
 * Listener for all non-solicited messages that can be received from Swarm XMPP server.
 * 
 * @author kgilmer
 *
 */
public interface ISwarmServerRequestListener {

	/**
	 * A swarm-server based client is requesting a list of all available feeds.
	 * 
	 * @param requestJid JID of requester 
	 * @param swarmId Swarm ID that binds self and requester
	 */
	void feedListRequest(final Jid requestJid, final String swarmId);

	/**A swarm-server based client is directly requesting a list of all available feeds.
	 * @param chat Smack XMPP object that can be used to respond to the client directly.
	 * @param swarmId ID of swarm that binds self and requester
	 */
	void feedListRequest(final Chat chat, final String swarmId);

	/**A swarm-server based client is requesting data from a specific feed.
	 * 
	 * @param jid JID of requester
	 * @param swarmId swarmID that binds 
	 * @param feedRequestName name of feed being requested
	 */
	void feedRequest(final Jid jid, final String swarmId, final String feedRequestName);
	
	/**
	 * @param jid JID of requester
	 * @param roomId room to be invited to.
	 */
	void swarmInviteRequest(final Jid jid, String roomId);
}
