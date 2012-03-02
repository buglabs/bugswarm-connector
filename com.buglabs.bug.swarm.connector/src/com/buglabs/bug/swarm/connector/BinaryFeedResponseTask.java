package com.buglabs.bug.swarm.connector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TimerTask;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.jivesoftware.smack.XMPPException;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.connector.model.BinaryFeed;
import com.buglabs.bug.swarm.connector.model.Feed;
import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;

/**
 * A TimerTask that handles responding to a feed request for binary data.
 * 
 * @author kgilmer, jconnolly
 *
 */
public class BinaryFeedResponseTask extends TimerTask {

	private static final String LOCATION = "Location";
	private final ISwarmClient wsClient;
	private final Jid feedRequester;
	private final String swarmId;
	private Feed feed;
	private final LogService log;
	private SwarmXMPPClient xmppClient;
	private Jid thisJid;
	private DefaultHttpClient client;
	private String configurationAPIKey;
	private static ObjectMapper mapper = new ObjectMapper();


	/**
	 * @param xmppClient instance of ws client
	 * @param recipient 
	 * @param wsClient2 jid of destination
	 * @param swarmId id of associated swarm
	 * @param feed binary feed that has been requested
	 * @param log instance of log service
	 */
	public BinaryFeedResponseTask(SwarmXMPPClient xmppClient, String configurationAPIKey, ISwarmClient wsClient, Jid feedRequester, Jid thisJid, String swarmId, Feed feed, LogService log) {
		this.xmppClient = xmppClient;
		this.configurationAPIKey = configurationAPIKey;
		this.wsClient = wsClient;
		this.feedRequester = feedRequester;
		this.thisJid = thisJid;
		this.swarmId = swarmId;
		this.feed = feed;
		this.log = log;
		client = new DefaultHttpClient();		
	}	

	@Override
	public void run() {
		try {
			String uploadUrl = upload();
			if(uploadUrl==null)
				throw new IOException();
			else{			
				xmppClient.sendFeedToUser(feedRequester, swarmId, "{\"Camera\": {\"location\": \""+uploadUrl+"\"}}");
			}
		} catch (XMPPException e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending feeds to " + feedRequester, e);
		} catch (IOException e) {
			log.log(LogService.LOG_ERROR, "Error occurred while sending binary feed to " + feedRequester, e);
		}
	}

	/**
	 * 
	 * @return the url location returned after a successful uload
	 */

	protected String upload() {
		String location = null;
		try {
			HttpPost httppost = new HttpPost(
					"http://api.test.bugswarm.net/upload");
			// not InputStream!
			// https://issues.apache.org/jira/browse/HTTPCLIENT-1014
			log.log(LogService.LOG_DEBUG, "length: "+((BinaryFeed) feed).getPayload().length);
			ByteArrayBody file = new ByteArrayBody(
					((BinaryFeed) feed).getPayload(), "image/jpeg",
					"Picture.jpeg");
			
			// TODO: fix below, user_id
			// StringBody user_id = new StringBody(thisJid.getResource());
			System.out.println(thisJid.getResource().split("-")[0]);
			StringBody resource_id = new StringBody(thisJid.getResource().split("-")[0]);
			
			MultipartEntity reqEntity = new MultipartEntity();
			// reqEntity.addPart("user_id", user_id);
			System.out.println("feed requester: "+feedRequester);
			reqEntity.addPart("resource_id", resource_id);
			reqEntity.addPart("file", file);
			httppost.setHeader("x-bugswarmapikey", configurationAPIKey);
			httppost.setEntity(reqEntity);

			HttpResponse response = client.execute(httppost);
			HttpEntity resEntity = response.getEntity();

			for (Header header : response.getAllHeaders()) {
				System.out.println(header.getName());
				if (header.getName().equals(LOCATION)) {
					location = header.getValue();
					System.out.println(location);
					break;
				}
			}

			EntityUtils.consume(resEntity);
			//client.getConnectionManager().shutdown();

			return location;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "null";
	}

}