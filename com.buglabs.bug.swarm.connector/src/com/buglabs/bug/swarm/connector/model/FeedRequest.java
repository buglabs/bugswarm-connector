package com.buglabs.bug.swarm.connector.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * A swarm-server based Feed request.
 * 
 * @author kgilmer
 *
 */
public class FeedRequest {

	/**
	 * Type of feed.  Based on HTTP operations.
	 *
	 */
	public enum FeedType { 
		get, put, post, delete;		
	}
	private static ObjectMapper mapper;
	private final FeedType type;
	private final String name;
	private final Map<String, Object> params;
	
	/**
	 * @param type type of request as String
	 * @param name name of feed
	 * @param params parameters associated with feed request
	 */
	public FeedRequest(String type, String name, Map<String, Object> params) {
		this.type = FeedType.valueOf(type);
		this.name = name;
		if (params == null)
			this.params = Collections.EMPTY_MAP;
		else
			this.params = params;
	}
	
	/**
	 * @param type type of request
	 * @param name name of feed
	 * @param params parameters associated with feed request
	 */
	public FeedRequest(FeedType type, String name, Map<String, Object> params) {
		this.type = type;
		this.name = name;
		if (params == null)
			this.params = Collections.EMPTY_MAP;
		else
			this.params = params;
	}
	
	/**
	 * @param type type of request
	 * @param name name of feed
	 */
	public FeedRequest(String type, String name) {
		this(type, name, Collections.EMPTY_MAP);
	}	
	
	/**
	 * @return type (ex 'get')
	 */
	public FeedType getType() {
		return type;
	}

	/**
	 * @return name of feed
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return parameters of feed request or an empty map.
	 */
	public Map<String, Object> getParams() {
		return params;
	}
	
	/**
	 * @return true if feed request is for all feeds.
	 */
	public boolean isFeedListRequest() {
		return name.equalsIgnoreCase("feeds");
	}
	
	/**
	 * @return true if the feed request is for a specific feed.
	 */
	public boolean isFeedRequest() {	
		return !isFeedListRequest() && name != null && type == FeedType.get;
	}
	
	/**
	 * See https://www.pivotaltracker.com/story/show/14457689.
	 * @return true if feed request is a meta request.
	 */
	public boolean isFeedMetaRequest() {
		/*{    'type': 'put', 
		      'feed': 'location', 
		      'body': { 'status': 'off'} 
		 }*/
		//{ "type": "put", "feed": "feed1", "params": { "status": "off"}}
		
		return !isFeedListRequest() && !isFeedRequest() && type == FeedType.put && params.containsKey("status");
	}	

	/**
	 * @param jsonString json document from server as a String
	 * @return FeedRequest object or null if invalid or incomplete message.
	 */
	public static FeedRequest parseJSON(String jsonString) {
		if (mapper == null)
			mapper = new ObjectMapper();
		
		
		JsonNode jn;
		try {
			jn = mapper.readTree(jsonString);
			
			if (jn.has("type") && jn.has("feed")) {
				String type = jn.get("type").getTextValue();
				String name = jn.get("feed").getTextValue();
				Map<String, Object> frp = new HashMap<String, Object>();
				if (jn.has("params")) {
					for (Iterator<Entry<String, JsonNode>> jni = jn.get("params").getFields(); jni.hasNext();) {
						Entry<String, JsonNode> pn = jni.next();
						if (pn.getKey().equals("frequency"))
							frp.put(pn.getKey(), pn.getValue().asInt());
						else if (pn.getValue().isArray())
							frp.put(pn.getKey(), mapper.readValue(pn.getValue(), List.class));
						else
							frp.put(pn.getKey(), pn.getValue().asText());
					}
				}
				
				return new FeedRequest(type, name, frp);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			// Squelch parsing error messages.		
		} 
				
		return null;
	}

	/**
	 * @return true if a stream request is made, false otherwise.
	 */
	public boolean hasFrequency() {
		if (params == null || !params.containsKey("frequency"))
			return false;
		
		try {
			int f = Integer.parseInt(params.get("frequency").toString());
			
			if (f > 0)
				return true;
		} catch (NumberFormatException e) {			
		}
		
		return false;
	}

	/**
	 * @return the requested frequency, or 0 if unspecified.
	 */
	public long getFrequency() {
		if (!hasFrequency())
			return 0;
		
		return Integer.parseInt(params.get("frequency").toString());
	}
}