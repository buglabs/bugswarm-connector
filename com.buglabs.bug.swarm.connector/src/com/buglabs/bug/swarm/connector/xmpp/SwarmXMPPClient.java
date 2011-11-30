/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package com.buglabs.bug.swarm.connector.xmpp;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.client.model.Configuration;
import com.buglabs.bug.swarm.client.model.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.Activator;

/**
 * Default implementation of the bugswarm-specific XMPP client.
 * 
 * @author kgilmer
 * 
 */
public class SwarmXMPPClient {
	private volatile boolean disposed = false;
	private XMPPConnection connection;

	/**
	 * Swarm name as key, MUC client as value.
	 */
	private final Map<String, MultiUserChat> swarmMap = new HashMap<String, MultiUserChat>();
	private final Configuration config;
	private final Jid jid;
	private final List<ISwarmServerRequestListener> requestListeners;
	private final Map<String, List<PacketListener>> requestHandlers;
	private final Map<String, Chat> chatCache;
	private SwarmAssociationHandler swarmAssociationHandler;

	/**
	 * @param config
	 *            Configuration to be used to create connection.
	 */
	public SwarmXMPPClient(final Configuration config) {
		this.config = config;
		this.jid = new Jid(config.getUsername(), config.getHostname(Protocol.XMPP), config.getResource());
		this.requestListeners = new ArrayList<ISwarmServerRequestListener>();
		this.requestHandlers = new HashMap<String, List<PacketListener>>();
		this.chatCache = new HashMap<String, Chat>();
	}

	/**
	 * Connect to XMPP server using the configuration passed in the constructor.
	 * 
	 * @param listener ISwarmServerRequestListener
	 * @throws IOException
	 *             on connection failure
	 * @throws XMPPException
	 *             on XMPP protocol failure
	 */
	public void connect(ISwarmServerRequestListener listener) throws IOException, XMPPException {
		if (connection == null) {
			connection = createConnection(config.getHostname(Protocol.XMPP), config.getXMPPPort());
			login(connection, config.getUsername(), config.getParticipationAPIKey(), config.getResource());
			disposed = false;
		}

		if (!requestListeners.contains(listener))
			requestListeners.add(listener);
		
		swarmAssociationHandler = new SwarmAssociationHandler(this, listener);
		connection.getChatManager().addChatListener(swarmAssociationHandler);	
	}

	/**
	 * @return true of the XMPP connection is active.
	 */
	public boolean isConnected() {
		// TODO maybe inspect the connection in some way to make sure its valid.
		return connection != null;
	}

	/**
	 * @return Username
	 */
	public String getUsername() {
		return config.getUsername();
	}

	/**
	 * @return XMPP Resource
	 */
	public String getResource() {
		return config.getResource();
	}

	/**
	 * @return XMPP server hostname
	 */
	public String getHostname() {
		return config.getHostname(Protocol.XMPP);
	}

	/**
	 * @return XMPP connection
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Send presence to the swarm to notify that client is online.
	 * 
	 * @param swarmId
	 *            swarm id
	 * @param listener
	 *            listener for server-based messages, null is ok if no listener
	 *            required.
	 * @throws Exception
	 *             On connection error
	 */
	public void joinSwarm(final String swarmId, final ISwarmServerRequestListener listener) throws Exception {
		MultiUserChat muc = getMUC(swarmId);
		
		if (listener != null && !requestListeners.contains(listener))
			requestListeners.add(listener);

		muc.join(getResource());
		
		if (!requestHandlers.containsKey(swarmId)) {
			PublicMessageHandler requestHandler = new PublicMessageHandler(jid, swarmId, requestListeners);
			muc.addMessageListener(requestHandler);
			
			PresenceHandler presenceHandler = new PresenceHandler(jid, swarmId, requestListeners, this);
			muc.addParticipantListener(presenceHandler);			
			
			if (!requestHandlers.containsKey(swarmId)) 
				requestHandlers.put(swarmId, new ArrayList<PacketListener>());
			
			requestHandlers.get(swarmId).add(requestHandler);	
			requestHandlers.get(swarmId).add(presenceHandler);
		} 		
	}

	/**
	 * Send unpresense to the swarm to notify that the client is offline.
	 * 
	 * @param swarmId
	 *            swarm id to leave
	 */
	public void leaveSwarm(final String swarmId) {
		MultiUserChat muc = swarmMap.get(swarmId);

		if (muc != null && muc.isJoined()) {
			if (requestHandlers.containsKey(swarmId)) {
				for (PacketListener listener : requestHandlers.get(swarmId)) {
					muc.removeMessageListener(listener);
					muc.removeParticipantListener(listener);
				}
									
				requestHandlers.remove(swarmId);
			}
			muc.leave();			
			clearChatCache(swarmId);
		} else {
			Activator.getLog().log(
					LogService.LOG_WARNING, "leaveSwarm() called with a swarm not currently joined: " + swarmId);
		}
	}

	

	/**
	 * Send a private message to a swarm peer.
	 * 
	 * @param swarmId
	 *            id of swarm
	 * @param userId
	 *            id of user
	 * @param message
	 *            document that should be sent as advertisement
	 * @throws XMPPException
	 *             on XMPP protocol error	
	 */
	public void sendPrivateMessage(final String swarmId, final String userId, final String message) throws XMPPException {
		MultiUserChat muc = getMUC(swarmId);
		
		if (muc == null)
			throw new IllegalStateException("Unable to access MUC " + swarmId);
		
		try {
			Chat pchat = chatCache.get(userId + swarmId);
			if (pchat == null) {
				pchat = muc.createPrivateChat(userId, new PrivateMessageHandler(new Jid(userId), swarmId, requestListeners));
				chatCache.put(userId + swarmId, pchat);
			}
		
			pchat.sendMessage(message);
		} catch (ParseException pe) {
			throw new XMPPException(pe);
		}
	}

	/**
	 * Send a public message to a swarm (MUC).
	 * 
	 * @param swarmId
	 *            id of swarm
	 * @param message
	 *            document that should be sent as advertisement
	 * @throws XMPPException
	 *             on XMPP protocol error
	 */
	public void sendPublicMessage(final String swarmId, final String message) throws XMPPException {
		MultiUserChat muc = getMUC(swarmId);
		
		if (muc == null)
			throw new IllegalStateException("Unable to access MUC " + swarmId);
		
		//Only send feed to swarms that have other members joined.
		if (muc != null && muc.getOccupantsCount() > 1) {			
				muc.sendMessage(message);
				Activator.getLog().log(LogService.LOG_DEBUG, "Sent " + message + " to swarm " + swarmId);
		} else {
			Activator.getLog().log(LogService.LOG_DEBUG, "No other participants, did not send message to " + swarmId);
		}
	}

	/**
	 * @param connection
	 *            XMPP connection
	 * @param user
	 *            username
	 * @param pass
	 *            password
	 * @throws XMPPException
	 *             on XMPP protocol error
	 */
	private static void login(final XMPPConnection connection, final String user, final String pass, final String resource)
			throws XMPPException {
		connection.connect();
		connection.login(user, pass, resource);
	}

	/**
	 * Creates a new XMPPConnection using the connection preferences. This is
	 * useful when not using a connection from the connection pool in a test
	 * case.
	 * 
	 * @param host
	 *            hostname of XMPP server
	 * @param port
	 *            port of XMPP server
	 * @return a new XMPP connection.
	 */
	private static XMPPConnection createConnection(final String host, final int port) {
		// Create the configuration for this new connection
		ConnectionConfiguration config = new ConnectionConfiguration(host, port);
		// TODO breakout port and other config options into properties
		config.setCompressionEnabled(Boolean.getBoolean("test.compressionEnabled"));
		config.setSendPresence(true);

		return new XMPPConnection(config);
	}

	/**
	 * Get or create if necessary the MUC for a swarm.
	 * 
	 * @param roomId
	 *            id of room
	 * @return chat client
	 */
	private MultiUserChat getMUC(final String roomId) {
		if (!swarmMap.containsKey(roomId))
			swarmMap.put(roomId, new MultiUserChat(connection, getMUCRoomName(roomId, connection.getHost())));

		return swarmMap.get(roomId);
	}

	/**
	 * Create the identifier for a MUC Room based on name and host.
	 * 
	 * @param roomId
	 *            id of room
	 * @param host
	 *            hostname
	 * @return String for full XMPP room name
	 */
	private String getMUCRoomName(final String roomId, final String host) {
		// This name is generated by convention of bugswarm
		return roomId + "@" + "swarms." + host;
	}

	/**
	 * Shutdown all connectors and cleanup. Once called, this service cannot be
	 * used again.
	 */
	protected void dispose() {
		if (disposed) {
			return;
		}

		if (connection != null) {
			if (swarmAssociationHandler != null) {
				connection.getChatManager().removeChatListener(swarmAssociationHandler);
				swarmAssociationHandler = null;
			}
			connection.disconnect();
		}
		connection = null;
		disposed = true;
	}

	/**
	 * Disconnect from server.
	 */
	public void disconnect() {
		if (!disposed)
			dispose();
	}

	/**
	 * @param requestJid
	 *            JID of requester
	 * @param swarmId
	 *            Swarm ID
	 * @param document
	 *            Feed as JSON document
	 * @throws XMPPException
	 *             on XMPP error
	 */
	public void sendAllFeedsToUser(Jid requestJid, String swarmId, String document) throws XMPPException {
		sendPrivateMessage(swarmId, requestJid.toString(), document);		
	}

	/**
	 * @return JID of connected client
	 */
	public Jid getJid() {
		return jid;
	}

	/**
	 * @param requestJid
	 *            JID of requester
	 * @param swarmId
	 *            Swarm ID
	 * @param document
	 *            Feed as JSON document
	 * @throws XMPPException
	 *             on XMPP error
	 */
	public void sendFeedToUser(Jid requestJid, String swarmId, String document) throws XMPPException {
		sendPrivateMessage(swarmId, requestJid.toString(), document);		
	}

	/**
	 * Clear the chat cache based on subset of key.  Intended to be user (resource) or swarm.
	 * @param identity
	 */
	public void clearChatCache(String identity) {
		List<String> caches = new ArrayList(chatCache.keySet());
		for (String name : caches)
			if (name.contains(identity))
				chatCache.remove(name);	
	}
}
