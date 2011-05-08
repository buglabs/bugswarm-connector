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
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.buglabs.bug.swarm.connector.Configuration;
import com.buglabs.util.XmlNode;

/**
 * Default implementation of ISwarmConnector
 * 
 * @author kgilmer
 * 
 */
public class SwarmXMPPClient  {
	private volatile boolean disposed = false;
	private XMPPConnection connection;
	
	/**
	 * Swarm name as key, MUC client as value.
	 */
	private Map<String, MultiUserChat> swarmMap = new HashMap<String, MultiUserChat>();
	private final Configuration config;
	
	/**
	 * @param config
	 * @param userKey
	 */
	public SwarmXMPPClient(final Configuration config) {
		this.config = config;
	}
		
	public void connect() throws IOException, XMPPException {				
		// Get a unique ID for the device software is running on.
		//String clientId = ClientIdentity.getRef().getId();
		if (connection == null) {				
			connection = createConnection(config.getHostname());
			login(connection, config.getUsername(), config.getUsername());
			disposed = false;
		}		
	}
	
	/**
	 * @return true of the XMPP connection is active.
	 */
	public boolean isConnected() {
		//TODO maybe inspect the connection in some way to make sure its valid.
		return connection != null;
	}
	
	/**
	 * @return
	 */
	public String getUsername() {
		return config.getUsername();
	}
	
	public String getResource() {
		return config.getResource();
	}
	
	/**
	 * @return
	 */
	public String getHostname() {
		return config.getHostname();
	}
	
	/**
	 * @return XMPP connection
	 */
	public Connection getConnection() {	
		return connection;
	}

	/**
	 * Send presence to the swarm to notify that client is online.
	 * @param id
	 * @throws XMPPException 
	 */
	public void joinSwarm(String swarmId) throws XMPPException {
		MultiUserChat muc = getMUC(swarmId);
		
		if (!muc.isJoined())
			muc.join(getResource());
	}

	/**
	 * Determines if a given user in the given swarm is currently online.
	 * 
	 * @param swarmId
	 * @param userId
	 * @return
	 */
	public boolean isPresent(String swarmId, String userId) {
		MultiUserChat muc = getMUC(swarmId);
		
		Presence presence = muc.getOccupantPresence(userId);
		
		if (presence == null)
			return false;
		
		return presence.isAvailable();
	}

	/**
	 * Advertise local services to another swarm member.
	 * 
	 * @param id
	 * @param userId
	 * @param createServiceModuleFeedDocument
	 * @throws XMPPException 
	 */
	public void advertise(String swarmId, String userId, XmlNode serviceModuleFeedDocument) throws XMPPException {
		MultiUserChat muc = getMUC(swarmId);
		
		Chat pchat = muc.createPrivateChat(userId, new NullMessageListener());
		pchat.sendMessage(serviceModuleFeedDocument.toString());
	}

	private static void login(XMPPConnection connection, String user, String pass) throws XMPPException {
		connection.connect();
		//TODO break out resource into property.
		connection.login(user, pass, "Home");
	}

	/**
	 * Creates a new XMPPConnection using the connection preferences. This is
	 * useful when not using a connection from the connection pool in a test
	 * case.
	 * 
	 * @return a new XMPP connection.
	 */
	private static XMPPConnection createConnection(String host) {
		// Create the configuration for this new connection
		ConnectionConfiguration config = new ConnectionConfiguration(host, 5222);
		// TODO breakout port and other config options into properties
		config.setCompressionEnabled(Boolean.getBoolean("test.compressionEnabled"));
		config.setSendPresence(true);

		return new XMPPConnection(config);
	}
	
	/**
	 * Get or create if necessary the MUC for a swarm.
	 * @param roomId
	 * @return
	 */
	private MultiUserChat getMUC(String roomId) {
		if (!swarmMap.containsKey(roomId))
			swarmMap.put(roomId, new MultiUserChat(connection, getMUCRoomName(roomId, connection.getHost())));
		
		return swarmMap.get(roomId);
	}
	
	/**
	 * Create the identifier for a MUC Room based on name and host.
	 * @param roomId
	 * @param host
	 * @return
	 */
	private String getMUCRoomName(String roomId, String host) {
		//This name is generated by convention of bugswarm
		return roomId + "@" + "swarms." + host;
	}

	/**
	 * Shutdown all connectors and cleanup.  Once called, this service cannot be used again.
	 */
	protected void dispose() {
		if (disposed) {
			return;
		}
		
		connection.disconnect();
		connection = null;
		disposed  = true;		
	}
}
