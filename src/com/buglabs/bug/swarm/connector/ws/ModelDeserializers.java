package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;

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
	protected static final ReSTClient.ResponseDeserializer<ObjectNode> JSONObjectDeserializer = 
		new ReSTClient.ResponseDeserializer<ObjectNode>() {
	
		@Override
		public ObjectNode deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return null;
			ObjectMapper mapper = new ObjectMapper();
			
			return (ObjectNode) mapper.readTree(input);
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
			
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(input, ResourceModel.class);			
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

			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(input, new TypeReference<List<ResourceModel>>() { });					
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
			
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(input, new TypeReference<List<SwarmModel>>() { });			
		}
	};
	
	/**
	 * Deserialize into List of SwarmModel.
	 */
	protected static final ReSTClient.ResponseDeserializer<SwarmModel> SwarmModelDeserializer = 
		new ReSTClient.ResponseDeserializer<SwarmModel>() {
	
		@Override
		public SwarmModel deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			if (responseCode == 404)
				return null;
			
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(input, SwarmModel.class);			
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
			
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(input, new TypeReference<List<SwarmResourceModel>>() { });					
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
