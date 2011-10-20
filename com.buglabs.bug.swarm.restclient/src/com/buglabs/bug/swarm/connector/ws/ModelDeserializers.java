package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

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
	
	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Utility class.
	 */
	private ModelDeserializers() {
	
	}

	/**
	 * Deserialize server content into a JSONObject.
	 */
	protected static final ReSTClient.ResponseDeserializer<JsonNode> JSONObjectDeserializer = 
		new ReSTClient.ResponseDeserializer<JsonNode>() {
	
		@Override
		public JsonNode deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return null;
			
			return (JsonNode) mapper.readTree(input);
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
			
			return ResourceModel.deserialize(IOUtils.toString(input));			
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
			
			List<ResourceModel> rml = new ArrayList<ResourceModel>();
			
			JsonNode jn = mapper.readTree(input);
			
			for (JsonNode rm : jn)
				rml.add(ResourceModel.deserialize(rm.toString()));
			
			return rml;
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
			
			List<SwarmModel> rml = new ArrayList<SwarmModel>();
			
			JsonNode jn = mapper.readTree(input);
			
			for (JsonNode rm : jn)
				rml.add(SwarmModel.deserialize(rm));
			
			return rml;			
		}
	};
	
	/**
	 * Deserialize into SwarmModel.
	 */
	protected static final ReSTClient.ResponseDeserializer<SwarmModel> SwarmModelDeserializer = 
		new ReSTClient.ResponseDeserializer<SwarmModel>() {
	
		@Override
		public SwarmModel deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			if (responseCode == 404)
				return null;
						
			JsonNode jn = mapper.readTree(input);
					
			return SwarmModel.deserialize(jn);			
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
			
			List<SwarmResourceModel> rml = new ArrayList<SwarmResourceModel>();
			
			JsonNode jn = mapper.readTree(input);
			
			for (JsonNode rm : jn)
				rml.add(SwarmResourceModel.deserialize(rm));
			
			return rml;			
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
