package com.buglabs.bug.swarm.connector.xmpp;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.buglabs.bug.swarm.connector.osgi.BUGSwarmFeed;
import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleProperty;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.XmlNode;

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
	public static JSONArray createFeedArray(List<BUGSwarmFeed> feeds) {
		JSONArray array = new JSONArray();
		
		for (BUGSwarmFeed feed : feeds)
			array.add(createFeedElement(feed));
			
		return array;
	}

	public static JSONObject createFeedElement(BUGSwarmFeed feed) {
		JSONObject jobj = new JSONObject();
		jobj.put(feed.getName(), feed.getFeed());
		
		return jobj;
	}
}
