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
import java.util.Dictionary;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.osgi.framework.BundleContext;

import com.buglabs.bug.swarm.connector.Activator;

/**
 * Default implementation of ISwarmConnector
 * 
 * @author kgilmer
 * 
 */
public class SwarmXMPPClient  {
	private final BundleContext context;
	private final Dictionary config;
	private volatile boolean disposed = false;
	private XMPPConnection connection;
	
	/**
	 * @param config
	 * @param userKey
	 */
	public SwarmXMPPClient(BundleContext context, final Dictionary config) {
		if (! isConfigValid(config)) {
			throw new IllegalArgumentException("Configuration is invalid.");
		}
		
		this.context = context;
		this.config = config;	
	}

	/**
	 * @param dict
	 * @return true if the nvp's in the dictionary contain necessary information to connect to a swarm server.
	 */
	private boolean isConfigValid(Dictionary dict) {
	
		return hasValue(dict.get(Activator.CONFIG_KEY_BUGSWARM_SERVER).toString());
	}
	
	/**
	 * @param s
	 * @return true if String value contains something.
	 */
	private boolean hasValue(String s) {
		if (s == null) {
			return false;
		}
		
		if (s.trim().length() == 0) {
			return false;
		}
		
		return true;
	}

	public void connect() throws IOException, XMPPException {				
		// Get a unique ID for the device software is running on.
		String clientId = ClientIdentity.getRef().getId();
		
		
		if (connection == null) {				
			connection = createConnection((String) config.get(Activator.CONFIG_KEY_BUGSWARM_SERVER));
			login(connection, (String) config.get(Activator.CONFIG_KEY_BUGSWARM_USERKEY), (String) config.get(Activator.CONFIG_KEY_BUGSWARM_NICKNAME));
			disposed = false;
		}		
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
