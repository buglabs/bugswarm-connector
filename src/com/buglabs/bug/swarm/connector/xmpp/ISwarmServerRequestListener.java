package com.buglabs.bug.swarm.connector.xmpp;

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
}
