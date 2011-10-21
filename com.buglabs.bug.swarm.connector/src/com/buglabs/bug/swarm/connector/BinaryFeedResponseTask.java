package com.buglabs.bug.swarm.connector;

import java.io.IOException;
import java.util.TimerTask;

import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.BinaryFeed;
import com.buglabs.bug.swarm.restclient.ISwarmClient;

/**
 * A TimerTask that handles responding to a feed request for binary data.
 * 
 * @author kgilmer
 *
 */
public class BinaryFeedResponseTask extends TimerTask {
	
	private final ISwarmClient wsClient;
	private final Jid jid;
	private final String swarmId;
	private BinaryFeed feed;
	private final LogService log;
	
	/**
	 * @param wsClient instance of ws client
	 * @param jid jid of destination
	 * @param swarmId id of associated swarm
	 * @param feed binary feed that has been requested
	 * @param log instance of log service
	 */
	public BinaryFeedResponseTask(ISwarmClient wsClient, Jid jid, String swarmId, BinaryFeed feed, LogService log) {
		this.wsClient = wsClient;
		this.jid = jid;
		this.swarmId = swarmId;
		this.feed = feed;
		this.log = log;		
	}	
	
	@Override
	public void run() {
		try {
			wsClient.getSwarmBinaryUploadClient()
				.upload(jid.getUsername(), jid.getResource(), feed.getName(), feed.getPayload());
		} catch (IOException e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending binary feed to " + jid, e);
		}
	}

}
