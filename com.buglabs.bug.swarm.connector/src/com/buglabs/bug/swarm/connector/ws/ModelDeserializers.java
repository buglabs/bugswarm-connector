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
			
			//Here we need to completely change the serialization code.  
			
			/*
			 * Example of what we get from 0.3 server:
			 * [
			    {
			        "id": "0d7e3064b9e0973691d69c2e55d634154c389c13",
			        "resources": [
			            {
			                "id": "t410s",
			                "member_since": "2011-10-18T21:38:25.767Z",
			                "_id": "4e9df1d17633ae6521000064",
			                "created_at": "2011-09-20T22:48:51.269Z",
			                "user_id": "connector_test",
			                "type": "producer"
			            },
			            {
			                "id": "web",
			                "member_since": "2011-10-18T21:38:25.767Z",
			                "_id": "4e9df1d17633ae6521000065",
			                "created_at": "2011-09-20T22:48:52.941Z",
			                "user_id": "connector_test",
			                "type": "consumer"
			            }
			        ],
			        "description": "A test swarm.",
			        "name": "TestSwarm-AccountConfig0.08131975",
			        "created_at": "2011-09-20T22:48:48.991Z",
			        "user_id": "kgilmer",
			        "public": true
			    }
			]
			 */
			JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(input));
			
			JSONObject jsonObj = (JSONObject) json.get(0);			
			String swarmId = (String) jsonObj.get("id");
			JSONArray resourceArray = (JSONArray) jsonObj.get("resources");
	
			return SwarmModel.createListFromJson(swarmId, resourceArray);
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
	
			return SwarmResourceModel.createListFromJson(null, jsonObject);
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
