package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.buglabs.bug.swarm.connector.model.ResourceModel;
import com.buglabs.bug.swarm.connector.model.SwarmModel;
import com.buglabs.bug.swarm.connector.model.SwarmResourceModel;
import com.buglabs.util.http.ReSTClient;

/**
 * A set of static objects that can deserialize model object from remote input streams.
 * 
 * @author kgilmer
 *
 */
public final class ModelDeserializers {
	
	/**
	 * Utility class.
	 */
	private ModelDeserializers() {
		
	}

	/**
	 * Deserialize server content into a JSONObject.
	 */
	protected static final ReSTClient.ResponseDeserializer<JSONObject> JSONObjectDeserializer = 
		new ReSTClient.ResponseDeserializer<JSONObject>() {
	
		@Override
		public JSONObject deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return null;
			
			return (JSONObject) JSONValue.parse(new InputStreamReader(input));
		}
	};
	
	/**
	 * Deserialize content into a ResourceModel.
	 */
	protected static final ReSTClient.ResponseDeserializer<ResourceModel> ResourceModelDeserializer = 
		new ReSTClient.ResponseDeserializer<ResourceModel>() {
	
		@Override
		public ResourceModel deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) 
			throws IOException {
			if (responseCode == 404)
				return null;
			
			//Temporary hack to work for both Object and Array responses 
			//from server until documentation matches actual behavior.
			//TODO: remove temp hack
			Object response = JSONValue.parse(new InputStreamReader(input));
			
			if (response instanceof JSONArray) {
				return ResourceModel.createFromJson((JSONObject) ((JSONArray) response).get(0));
			} else if (response instanceof JSONObject) {
				return ResourceModel.createFromJson((JSONObject) response); 
			}
			
			throw new IOException("Unknown type returned from JSON parser.");
		}
	};
	
	/**
	 * Deserialize content into a List of ResourceModel.
	 */
	protected static final ReSTClient.ResponseDeserializer<List<ResourceModel>> ResourceModelListDeserializer = 
		new ReSTClient.ResponseDeserializer<List<ResourceModel>>() {
	
		@Override
		public List<ResourceModel> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();
			
			JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(input));
	
			return ResourceModel.createListFromJson(json);
		}
	};
	
	/**
	 * Deserialize into List of SwarmModel.
	 */
	protected static final ReSTClient.ResponseDeserializer<List<SwarmModel>> SwarmModelListDeserializer = 
		new ReSTClient.ResponseDeserializer<List<SwarmModel>>() {
	
		@Override
		public List<SwarmModel> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();
			
			JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(input));
	
			return SwarmModel.createListFromJson(json);
		}
	};
	
	/**
	 * Deserialize to List of SwarmResourceModel.
	 */
	protected static final ReSTClient.ResponseDeserializer<List<SwarmResourceModel>> SwarmResourceModelListDeserializer = 
		new ReSTClient.ResponseDeserializer<List<SwarmResourceModel>>() {
	
		@Override
		public List<SwarmResourceModel> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();
			
			JSONArray jsonObject = (JSONArray) JSONValue.parse(new InputStreamReader(input));
	
			return SwarmResourceModel.createListFromJson(jsonObject);
		}
	};
	
	/**
	 * Deserialize to SwarmWSResponse.
	 */
	protected static final ReSTClient.ResponseDeserializer<SwarmWSResponse> SwarmWSResponseDeserializer = 
		new ReSTClient.ResponseDeserializer<SwarmWSResponse>() {
		@Override
		public SwarmWSResponse deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			return SwarmWSResponse.fromCode(responseCode);
		}
	};

}
