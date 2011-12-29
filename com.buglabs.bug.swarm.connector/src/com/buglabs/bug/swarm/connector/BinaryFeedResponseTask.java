package com.buglabs.bug.swarm.connector;

import java.io.IOException;
import java.util.TimerTask;

import org.codehaus.jackson.map.ObjectMapper;
import org.jivesoftware.smack.XMPPException;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.SwarmWSResponse;
import com.buglabs.bug.swarm.connector.model.BinaryFeed;
import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;

/**
 * A TimerTask that handles responding to a feed request for binary data.
 * 
 * @author kgilmer, jconnolly
 *
 */
public class BinaryFeedResponseTask extends TimerTask {
	
	private final ISwarmClient wsClient;
	private final Jid jid;
	private final String swarmId;
	private BinaryFeed feed;
	private final LogService log;
	private SwarmXMPPClient xmppClient;
	private static ObjectMapper mapper = new ObjectMapper();

	
	/**
	 * @param xmppClient instance of ws client
	 * @param wsClient2 jid of destination
	 * @param swarmId id of associated swarm
	 * @param feed binary feed that has been requested
	 * @param log instance of log service
	 */
	public BinaryFeedResponseTask(SwarmXMPPClient xmppClient, ISwarmClient wsClient, Jid jid, String swarmId, BinaryFeed feed, LogService log) {
		this.xmppClient = xmppClient;
		this.wsClient = wsClient;
		this.jid = jid;
		this.swarmId = swarmId;
		this.feed = feed;
		this.log = log;		
	}	
	
	@Override
	//IRL conversation with camilo, new workflow is:
	//1) WebUI requests a binary feed
	//2) connector responds by uploading binary file via MIME post to BASE_URL/upload
	//3) connector parses the header response for Location field for the url of the newly uploaded file
	//4) 
	public void run() {
		try {
			SwarmWSResponse resp = wsClient.getSwarmBinaryUploadClient()
				.upload(jid.getUsername(), jid.getResource(), feed.getName(), feed.getPayload());
			String uploadUrl = (String) resp.getHeaders().get("Location");
			if(uploadUrl==null)
				throw new IOException();
			else			
				xmppClient.sendFeedToUser(jid, swarmId, uploadUrl);
		} catch (XMPPException e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending feeds to " + jid, e);
		} catch (IOException e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending binary feed to " + jid, e);
		}
	}

}
