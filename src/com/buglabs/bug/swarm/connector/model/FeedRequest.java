package com.buglabs.bug.swarm.connector.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * A swarm-server based Feed request.
 * 
 * @author kgilmer
 *
 */
public class FeedRequest {

	private final String type;
	private final String name;
	private final Map<String, Object> params;
	
	/**
	 * @param type type of request
	 * @param name name of feed
	 * @param params parameters associated with feed request
	 */
	public FeedRequest(String type, String name, Map<String, Object> params) {
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
	public String getType() {
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
	 * @param jsonString json document from server as a String
	 * @return FeedRequest object or null if invalid or incomplete message.
	 */
	public static FeedRequest parseJSON(String jsonString) {
		Object o = JSONValue.parse(jsonString);

		if (o != null && o instanceof JSONObject) {
			JSONObject jo = (JSONObject) o;
			Map<String, Object> frp = null;
			
			String type = jo.get("type").toString();
			String name = jo.get("feed").toString();
			JSONObject params = (JSONObject) jo.get("params");
			
			if (params != null) {
				frp = new HashMap<String, Object>();
				for (Object e : params.entrySet()) {
					Object key = ((Map.Entry) e).getKey();
					Object val = ((Map.Entry) e).getValue();
					
					frp.put(key.toString(), val);					
				}
			}
			
			return new FeedRequest(type, name, frp);
		}
		
		return null;
	}
}
