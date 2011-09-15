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
import com.buglabs.util.http.RestClient;

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
	protected static final RestClient.ResponseDeserializer<JSONObject> JSONObjectDeserializer = 
		new RestClient.ResponseDeserializer<JSONObject>() {
	
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
	protected static final RestClient.ResponseDeserializer<ResourceModel> ResourceModelDeserializer = 
		new RestClient.ResponseDeserializer<ResourceModel>() {
	
		@Override
		public ResourceModel deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) 
			throws IOException {
			if (responseCode == 404)
				return null;
			
			JSONObject jsonObject = (JSONObject) JSONValue.parse(new InputStreamReader(input));
			
			return ResourceModel.createFromJson(jsonObject);
		}
	};
	
	/**
	 * Deserialize content into a List of ResourceModel.
	 */
	protected static final RestClient.ResponseDeserializer<List<ResourceModel>> ResourceModelListDeserializer = 
		new RestClient.ResponseDeserializer<List<ResourceModel>>() {
	
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
	protected static final RestClient.ResponseDeserializer<List<SwarmModel>> SwarmModelListDeserializer = 
		new RestClient.ResponseDeserializer<List<SwarmModel>>() {
	
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
	protected static final RestClient.ResponseDeserializer<List<SwarmResourceModel>> SwarmResourceModelListDeserializer = 
		new RestClient.ResponseDeserializer<List<SwarmResourceModel>>() {
	
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
	protected static final RestClient.ResponseDeserializer<SwarmWSResponse> SwarmWSResponseDeserializer = 
		new RestClient.ResponseDeserializer<SwarmWSResponse>() {
		@Override
		public SwarmWSResponse deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			return SwarmWSResponse.fromCode(responseCode);
		}
	};

}
