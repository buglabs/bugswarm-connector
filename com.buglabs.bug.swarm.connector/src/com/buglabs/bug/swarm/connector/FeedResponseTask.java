package com.buglabs.bug.swarm.connector;

import java.util.TimerTask;

import org.jivesoftware.smack.XMPPException;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.Feed;
import com.buglabs.bug.swarm.connector.xmpp.JSONElementCreator;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;

/**
 * A TimerTask that handles responding to a Feed request.
 * 
 * @author kgilmer
 *
 */
public class FeedResponseTask extends TimerTask {
	
	private final SwarmXMPPClient xmppClient;
	private final Jid jid;
	private final String swarmId;
	private final Feed feed;
	private final LogService log;
	
	/**
	 * @param xmppClient instance of XMPP client that will be used to send the response.
	 * @param jid jid of recipient
	 * @param swarmId id of associated swarm
	 * @param feed instance of requested feed
	 * @param log instance of LogService
	 */
	public FeedResponseTask(SwarmXMPPClient xmppClient, Jid jid, String swarmId, Feed feed, LogService log) {
		this.xmppClient = xmppClient;
		this.jid = jid;
		this.swarmId = swarmId;
		this.feed = feed;
		this.log = log;		
	}	
	
	@Override
	public void run() {
		String document = JSONElementCreator.createFeedElement(feed);
		
		try {
			xmppClient.sendFeedToUser(jid, swarmId, document);
		} catch (XMPPException e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending feeds to " + jid, e);
		}
	}

}
