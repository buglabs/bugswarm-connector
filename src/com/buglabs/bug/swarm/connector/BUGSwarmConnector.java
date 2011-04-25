package com.buglabs.bug.swarm.connector;

import java.io.IOException;
import java.util.Dictionary;

import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;

/**
 * The swarm connector client for BUGswarm system.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnector extends Thread {

	/**
	 * Configuration info for swarm server.
	 */
	private final Configuration config;
	/**
	 * Web service client to swarm server.
	 */
	private SwarmWSClient wsClient;
	/**
	 * True if the initalize() method has been called, false otherwise.
	 */
	private boolean initialized = false;
	private SwarmXMPPClient xmppClient;

	public BUGSwarmConnector(Configuration config) {
		this.config = config;
		if (!config.isValid())
			throw new IllegalArgumentException("Invalid configuration");
	}

	@Override
	public void run() {
		try {
			if (!initialized)
				initialize();
			
			
		} catch (Exception e) {
			//TODO handle errors
		}
	}

	/**
	 * Initialize the connection to the swarm server.
	 * @throws Exception 
	 */
	public boolean initialize() throws Exception {
		wsClient = new SwarmWSClient(config.getHostname(), config.getApi_key());
		if (wsClient.isValid()) {
			xmppClient = new SwarmXMPPClient(config);
			
			if (OSGiHelper.getRef() != null) {
				initialized = true;
				return true;
			}
		}
		
		return false;
	}
}
