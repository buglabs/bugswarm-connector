package com.buglabs.bug.swarm.connector.xmpp;

import org.jivesoftware.smack.Chat;

import com.buglabs.bug.swarm.connector.model.FeedRequest;
import com.buglabs.bug.swarm.connector.model.Jid;

/**
 * Listener for all non-solicited messages that can be received from Swarm XMPP
 * server.
 * 
 * @author kgilmer
 * 
 */
public interface ISwarmServerRequestListener {

	/**
	 * A swarm-server based client is requesting a list of all available feeds.
	 * 
	 * @param requestJid
	 *            JID of requester
	 * @param swarmId
	 *            Swarm ID that binds self and requester
	 */
	void feedListRequest(final Jid requestJid, final String swarmId);

	/**
	 * A swarm-server based client is directly requesting a list of all
	 * available feeds.
	 * 
	 * @param chat
	 *            Smack XMPP object that can be used to respond to the client
	 *            directly.
	 * @param swarmId
	 *            ID of swarm that binds self and requester
	 */
	void feedListRequest(final Chat chat, final String swarmId);

	/**
	 * A swarm-server based client is requesting data from a specific feed.
	 * 
	 * @param jid
	 *            JID of requester
	 * @param swarmId
	 *            swarmID that binds
	 * @param feedRequest
	 *            name of feed being requested
	 */
	void feedRequest(final Jid jid, final String swarmId, final FeedRequest feedRequest);

	/**
	 * @param jid
	 *            JID of requester
	 * @param roomId
	 *            room to be invited to.
	 */
	void swarmInviteRequest(final Jid jid, String roomId);

	/**
	 * The server has sent a message to turn on/off a feed or other unspecified behavior.
	 * @param request FeedRequest
	 * @param swarmId swarm associated with request
	 */
	void feedMetaRequest(FeedRequest request, String swarmId);
	
	void addMemberSwarm(String swarmId);

	/**
	 * Cancel any/all feed requests going to specific user.
	 * 
	 * @param jid user that is receiving feeds
	 * @param swarmId id of swarm 
	 */
	void cancelFeedRequests(Jid jid, String swarmId);
}
