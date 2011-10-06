package com.buglabs.bug.swarm.connector.xmpp;

import java.util.List;

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
	public static JSONArray createFeedArray(final List<Feed> feeds) {
		JSONArray array = new JSONArray();

		for (Feed feed : feeds)
			array.add(createFeedElement(feed));

		return array;
	}

	/**
	 * Create a JSON object for a feed.
	 * 
	 * @param feed
	 *            feed to be converted
	 * @return JSON representation of feed
	 */
	public static JSONObject createFeedElement(final Feed feed) {
		JSONObject jobj = new JSONObject();
		jobj.put(feed.getName(), feed.getFeed());

		return jobj;
	}
}
