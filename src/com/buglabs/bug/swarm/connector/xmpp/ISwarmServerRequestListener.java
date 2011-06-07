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
	void feedListRequest(Jid requestJid, String swarmId);

	/**A swarm-server based client is directly requesting a list of all available feeds.
	 * @param chat Smack XMPP object that can be used to respond to the client directly.
	 * @param swarmId ID of swarm that binds self and requester
	 */
	void feedListRequest(final Chat chat, final String swarmId);
}
