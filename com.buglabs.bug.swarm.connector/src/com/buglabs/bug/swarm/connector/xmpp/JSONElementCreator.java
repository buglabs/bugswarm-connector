package com.buglabs.bug.swarm.connector.xmpp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.buglabs.bug.swarm.connector.osgi.Feed;

/**
 * This stateless class handles all JSON message creation for
 * bugswarm-connector.
 * 
 * @author kgilmer
 * 
 */
public final class JSONElementCreator {

	/**
	 * No instance allowed.
	 */
	private JSONElementCreator() {

	}

	/**
	 * For a list of BUGSwarmFeed create a JSON array.
	 * 
	 * @param feeds
	 *            list of feeds
	 * @return JSON array
	 */
	public static String createCapabilitiesJson(final List<Feed> feeds) {
		Map<String, Feed> feedMap = new HashMap<String, Feed>();
		
		for (Feed f : feeds)
			feedMap.put(f.getName(), f);
		
		if (!feedMap.containsKey("capabilities"))
			throw new IllegalStateException("Feeds do not contain minimal set for management web ui.");
		
		JSONObject root = new JSONObject();
		root.put("capabilities", feedMap.get("capabilities").getFeed());

		return root.toJSONString();
	}

	/**
	 * Create a JSON object for a feed.
	 * 
	 * @param feed
	 *            feed to be converted
	 * @return JSON representation of feed
	 */
	public static String createFeedElement(final Feed feed) {
		JSONObject jobj = new JSONObject();
		jobj.put(feed.getName(), feed.getFeed());

		return jobj.toJSONString();
	}

	/**
	 * Create a list of the names of available feeds.
	 * @param bugFeeds List of feeds.
	 * @return String of json of feed names.
	 */
	public static String createFeedListJson(List<Feed> bugFeeds) {
		JSONArray ja = new JSONArray();
		
		for (Feed f : bugFeeds)
			if (!f.getName().equals("modules") && !f.getName().equals("capabilities"))
				ja.add(f.getName());
		
		return ja.toJSONString();
	}
}
