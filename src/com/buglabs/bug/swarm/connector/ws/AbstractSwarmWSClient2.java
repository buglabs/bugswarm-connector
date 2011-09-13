package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.buglabs.bug.swarm.connector.model.ResourceModel;
import com.buglabs.bug.swarm.connector.model.SwarmModel;
import com.buglabs.bug.swarm.connector.model.SwarmResourceModel;
import com.buglabs.util.http.RestClient;
import com.buglabs.util.http.RestClient.Response;

/**
 * Base WSClient class with general common functionality.
 * 
 * @author kgilmer
 * 
 */
public abstract class AbstractSwarmWSClient2 {

	private static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";

	private static final String SWARM_APIKEY_HEADER_KEY = "X-BugSwarmApiKey";

	protected static final String INVALID_SWARM_CONNECTION_ERROR_MESSAGE = "API_KEY is invalid.";

	private static final boolean DEBUG_MODE = true;
	
	protected static final RestClient.ResponseDeserializer<SwarmWSResponse> WSRESPONSE_DESERIALIZER = 
		new RestClient.ResponseDeserializer<SwarmWSResponse>() {
		@Override
		public SwarmWSResponse deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			return SwarmWSResponse.fromCode(responseCode);
		}
	};
	
	protected static final RestClient.ResponseDeserializer<List<ResourceModel>> ResourceModelListDeserializer = 
		new RestClient.ResponseDeserializer<List<ResourceModel>>() {

		@Override
		public List<ResourceModel> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();
			
			JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(input));

			return ResourceModel.createListFromJson(json);
		}
	};
	
	protected static final RestClient.ResponseDeserializer<ResourceModel> ResourceModelDeserializer = 
		new RestClient.ResponseDeserializer<ResourceModel>() {

		@Override
		public ResourceModel deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return null;
			
			JSONObject jsonObject = (JSONObject) JSONValue.parse(new InputStreamReader(input));
			
			return ResourceModel.createFromJson(jsonObject);
		}
	};
	
	protected static final RestClient.ResponseDeserializer<List<SwarmResourceModel>> SwarmResourceModelListDeserializer = 
		new RestClient.ResponseDeserializer<List<SwarmResourceModel>>() {

		@Override
		public List<SwarmResourceModel> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();
			
			JSONArray jsonObject = (JSONArray) JSONValue.parse(new InputStreamReader(input));

			return SwarmResourceModel.createListFromJson(jsonObject);
		}
	};
	
	protected static final RestClient.ResponseDeserializer<List<SwarmModel>> SwarmModelListDeserializer = 
		new RestClient.ResponseDeserializer<List<SwarmModel>>() {

		@Override
		public List<SwarmModel> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();
			
			JSONArray json = (JSONArray) JSONValue.parse(new InputStreamReader(input));

			return SwarmModel.createListFromJson(json);
		}
	};
	
	protected static final RestClient.ResponseDeserializer<JSONObject> JSONObjectDeserializer = 
		new RestClient.ResponseDeserializer<JSONObject>() {

		@Override
		public JSONObject deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return null;
			
			return (JSONObject) JSONValue.parse(new InputStreamReader(input));
		}
	};
	
	protected final String swarmHostUrl;
	protected final String apiKey;
	protected final RestClient httpClient;
	protected boolean isValidated = false;

	private Map<String, String> staticHeaders;

	/**
	 * @param swarmHostUrl
	 *            url of swarm host, with scheme.
	 * @param apiKey
	 *            api key as provided by server
	 */
	public AbstractSwarmWSClient2(String swarmHostUrl, final String apiKey) {
		if (swarmHostUrl == null || apiKey == null)
			throw new IllegalArgumentException("An input parameter is null.");

		if (!swarmHostUrl.endsWith("/"))
			swarmHostUrl = swarmHostUrl + "/";

		this.swarmHostUrl = swarmHostUrl;
		this.apiKey = apiKey;
		this.httpClient = new RestClient();
		
		httpClient.addConnectionInitializer(new RestClient.ConnectionInitializer() {
			
			@Override
			public void initialize(HttpURLConnection connection) {
				for (Map.Entry<String, String> e : getSwarmHeaders().entrySet())
					connection.setRequestProperty(e.getKey(), e.getValue().toString());
			}
		});		
	}

	/**
	 * @param swarmHostUrl
	 *            url of swarm host, with scheme.
	 * @param apiKey
	 *            api key as provided by server
	 * @param httpClient
	 *            client-provided client
	 */
	protected AbstractSwarmWSClient2(String swarmHostUrl, final String apiKey, final RestClient httpClient) {
		if (swarmHostUrl == null || apiKey == null || httpClient == null)
			throw new IllegalArgumentException("An input parameter is null.");

		if (!swarmHostUrl.endsWith("/"))
			swarmHostUrl = swarmHostUrl + "/";

		this.swarmHostUrl = swarmHostUrl;
		this.apiKey = apiKey;
		this.httpClient = httpClient;
	}

	/**
	 * Validate connection to server.
	 * 
	 * @param force
	 *            ignores any previous successful validation.
	 * @return true if response is 200, false if otherwise
	 * @throws IOException
	 */
	public Throwable checkAndValidate(final boolean force) {
		if (isValidated && !force)
			return null;

		try {
			Response<Integer> response = httpClient.get(swarmHostUrl + "keys/" + apiKey + "/verify", RestClient.HTTP_CODE_DESERIALIZER);
			SwarmWSResponse wsr = SwarmWSResponse.fromCode(response.getContent());

			if (!wsr.isError()) {
				isValidated = true;
				return null;
			}

			return new IOException("Validation failed: " + wsr.toString());
		} catch (IOException e) {
			return e;
		}
	}

	/**
	 * @return required swarm headers
	 */
	protected Map<String, String> getSwarmHeaders() {
		if (staticHeaders == null) {
			staticHeaders = toMap(SWARM_APIKEY_HEADER_KEY, apiKey, 
									CONTENT_TYPE_HEADER_KEY, "application/json");
		}

		return staticHeaders;
	}

	/**
	 * @throws IOException
	 *             thrown when connection error occurs
	 */
	protected void validateAPIKey() throws IOException {
		Throwable err = checkAndValidate(false);
		if (err != null)
			throw new IOException(INVALID_SWARM_CONNECTION_ERROR_MESSAGE, err);
	}

	/**
	 * Given a variable number of <String, Object> pairs, construct a Map and
	 * return it with values loaded.
	 * 
	 * @param elements
	 *            name1, value1, name2, value2...
	 * @return a Map and return it with values loaded.
	 */
	public static Map<String, Object> toMap(Object... elements) {
		if (elements.length % 2 != 0) {
			throw new IllegalStateException("Input parameters must be even.");
		}

		Iterator<Object> i = Arrays.asList(elements).iterator();
		Map<String, Object> m = new HashMap<String, Object>();

		while (i.hasNext()) {
			m.put(i.next().toString(), i.next());
		}

		return m;
	}
	
	/**
	 * Given a variable number of <String, String> pairs, construct a Map and
	 * return it with values loaded.
	 * 
	 * @param elements
	 *            name1, value1, name2, value2...
	 * @return a Map and return it with values loaded.
	 */
	public static Map<String, String> toMap(String... elements) {
		if (elements.length % 2 != 0) {
			throw new IllegalStateException("Input parameters must be even.");
		}

		Iterator<String> i = Arrays.asList(elements).iterator();
		Map<String, String> m = new HashMap<String, String>();

		while (i.hasNext()) {
			m.put(i.next().toString(), i.next());
		}

		return m;
	}
	
	/**
	 * Throws IllegalArgumentException if any of the input parameters are null.
	 * @param params Object array of parameters that should not be null.
	 */
	protected static void validateParams(Object ... params) {
		for (int i = 0; i < params.length; ++i)
			if (params[i] == null)
				throw new IllegalArgumentException("An input parameter is null.");		
	}	
}
