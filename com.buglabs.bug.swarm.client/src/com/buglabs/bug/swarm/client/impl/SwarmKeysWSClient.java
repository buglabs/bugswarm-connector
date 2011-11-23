package com.buglabs.bug.swarm.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.touge.restclient.ReSTClient;
import org.touge.restclient.ReSTClient.Response;
import org.touge.restclient.ReSTClient.ResponseDeserializer;
import org.touge.restclient.ReSTClient.URLBuilder;

import com.buglabs.bug.swarm.client.ISwarmKeysClient;
import com.buglabs.bug.swarm.client.model.SwarmKey;

/**
 * Implementation of ISwarmKeysClient.
 * 
 * @author kgilmer
 *
 */
public class SwarmKeysWSClient implements ISwarmKeysClient {

	private static ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * Deserializer for list of api keys from server in json format.
	 */
	private static final ResponseDeserializer<List<SwarmKey>> KEYS_LIST_DESERIALIZER = new ResponseDeserializer<List<SwarmKey>>() {		
		
		@Override
		public List<SwarmKey> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();
			
			List<SwarmKey> rml = new ArrayList<SwarmKey>();
						
			JsonNode jn = objectMapper.readTree(input);
			
			if (jn.isArray())
				for (JsonNode rm : jn)
					rml.add(SwarmKey.deserialize(rm));
			else 
				rml.add(SwarmKey.deserialize(jn));
			
			return rml;
		}
	};
	
	private final String hostname;

	/**
	 * @param hostname hostname of swarm server 
	 */
	public SwarmKeysWSClient(String hostname) {
		this.hostname = hostname;		
	}

	@Override
	public List<SwarmKey> create(String userName, String password, KeyType type) throws IOException {
		//Creating a new restclient instance to work with basic authentication.  This is unique to the api keys client.
		ReSTClient restClient = new ReSTClient(userName, password);
		
		//Handle all errors by throwing them.
		restClient.setErrorHandler(new ReSTClient.ErrorHandler() {
			
			@Override
			public void handleError(int code, String message) throws IOException {
				throw new IOException(message);
			}
		});
		
		URLBuilder url = restClient.buildURL(hostname).append("keys");
		
		if (type != null)
			url.append(type.toString());
		
		Response<List<SwarmKey>> response = restClient.callPost(url, (InputStream) null, KEYS_LIST_DESERIALIZER);
						
		return response.getContent();
	}

	@Override
	public List<SwarmKey> list(String userName, String password, KeyType type) throws IOException {
		//Creating a new restclient instance to work with basic authentication.  This is unique to the api keys client.
		ReSTClient restClient = new ReSTClient(userName, password);
		
		//Handle all errors by throwing them.
		restClient.setErrorHandler(new ReSTClient.ErrorHandler() {
			
			@Override
			public void handleError(int code, String message) throws IOException {
				throw new IOException(message);
			}
		});
		
		URLBuilder url = restClient.buildURL(hostname).append("keys");
		
		if (type != null)
			url.append(type.toString());
		
		Response<List<SwarmKey>> response = restClient.callGet(url, KEYS_LIST_DESERIALIZER);
		
		return response.getContent();
	}
}
