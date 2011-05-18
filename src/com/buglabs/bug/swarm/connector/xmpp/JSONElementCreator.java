package com.buglabs.bug.swarm.connector.xmpp;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.buglabs.bug.swarm.connector.osgi.Feed;

/**
 * This stateless class handles all xml message creation for bugswarm-connector.
 * @author kgilmer
 *
 */
public class JSONElementCreator {
	
	/**
	 * For a list of BUGSwarmFeed create a JSON array
	 * @param feeds
	 * @return
	 */
	public static JSONArray createFeedArray(List<Feed> feeds) {
		JSONArray array = new JSONArray();
		
		for (Feed feed : feeds)
			array.add(createFeedElement(feed));
			
		return array;
	}

	public static JSONObject createFeedElement(Feed feed) {
		JSONObject jobj = new JSONObject();
		jobj.put(feed.getName(), feed.getFeed());
		
		return jobj;
	}
}
