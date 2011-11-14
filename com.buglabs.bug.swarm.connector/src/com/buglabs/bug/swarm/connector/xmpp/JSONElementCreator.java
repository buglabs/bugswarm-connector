package com.buglabs.bug.swarm.connector.xmpp;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.buglabs.bug.swarm.connector.osgi.Feed;
import com.buglabs.bug.swarm.connector.osgi.ModulesFeed;

/**
 * This stateless class handles all JSON message creation for
 * bugswarm-connector.
 * 
 * @author kgilmer
 * 
 */
public final class JSONElementCreator {

	private static ObjectMapper mapper = new ObjectMapper();

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
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public static String createCapabilitiesJson(final List<Feed> feeds) throws JsonGenerationException, JsonMappingException, IOException {
		Map<String, Feed> feedMap = new HashMap<String, Feed>();
		
		for (Feed f : feeds)
			feedMap.put(f.getName(), f);
		
		if (!feedMap.containsKey("capabilities"))
			throw new IllegalStateException("Feeds do not contain minimal set for management web ui.");
		
		//TODO: try just serializing feedMap.get("capabilities").getFeed()
		ModulesFeed modules = (ModulesFeed) feedMap.get("capabilities").getFeed().get("modules");
		List<String> feedNames = (List<String>) feedMap.get("capabilities").getFeed().get("feeds");
		
		Map<String, Object> capabilities = new HashMap<String, Object>();
		capabilities.put("feeds", feedNames);
		capabilities.put("modules", modules.getFeed());
		
		return mapper.writeValueAsString(capabilities);
		
//		JSONObject capabilities = new JSONObject();
//		capabilities.put("feeds", createFeedListJson(feeds));
//		capabilities.put("modules", modules.getFeed());
//		JSONObject root = new JSONObject();
//		root.put("capabilities", capabilities);
//
//		return root.toJSONString();
	}

	/**
	 * Create a JSON object for a feed.
	 * 
	 * @param feed
	 *            feed to be converted
	 * @return JSON representation of feed
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public static String createFeedElement(final Feed feed) throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(feed);
		
		/*JSONObject jobj = new JSONObject();
		jobj.put(feed.getName(), feed.getFeed());

		return jobj.toJSONString();*/
	}

	/**
	 * Create a list of the names of available feeds.
	 * @param bugFeeds List of feeds.
	 * @return String of json of feed names.
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public static String createFeedListJson(List<Feed> bugFeeds) throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(bugFeeds);
		
		/*JSONArray ja = new JSONArray();
		
		for (Feed f : bugFeeds)
			if (!f.getName().equals("modules") && !f.getName().equals("capabilities") && !f.getName().equals("feeds"))
				ja.add(f.getName());
		
		return ja;*/
	}
}
