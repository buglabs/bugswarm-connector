package com.buglabs.bug.swarm.connector.xmpp;

import java.util.List;

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
	public static String createFeedArray(final List<Feed> feeds) {
		JSONObject jobj = new JSONObject();

		for (Feed feed : feeds)
			jobj.put(feed.getName(), feed.getFeed());

		return jobj.toJSONString();
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
}
