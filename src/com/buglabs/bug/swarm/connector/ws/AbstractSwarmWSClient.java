package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.buglabs.util.simplerestclient.HTTPException;
import com.buglabs.util.simplerestclient.HTTPRequest;
import com.buglabs.util.simplerestclient.HTTPResponse;

/**
 * Base WSClient class with general common functionality.
 * 
 * @author kgilmer
 *
 */
public abstract class AbstractSwarmWSClient {
	
	private static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";

	private static final String SWARM_APIKEY_HEADER_KEY = "X-BugSwarmApiKey";

	protected static final String INVALID_SWARM_CONNECTION_ERROR_MESSAGE = "API_KEY is invalid.";

	private static final boolean DEBUG_MODE = true;

	protected final String swarmHostUrl;
	protected final String apiKey;
	protected HTTPRequest httpClient;
	protected boolean isValidated = false;
	
	private Map<String, String> staticHeaders;

	public AbstractSwarmWSClient(String swarmHostUrl, String apiKey) {
		if (swarmHostUrl == null || apiKey == null)
			throw new IllegalArgumentException("An input parameter is null.");
		
		if (!swarmHostUrl.endsWith("/"))
			swarmHostUrl = swarmHostUrl + "/";
		
		this.swarmHostUrl = swarmHostUrl;
		this.apiKey = apiKey;
		this.httpClient = new HTTPRequest(DEBUG_MODE);
		httpClient.addConfigurator(new com.buglabs.util.simplerestclient.HTTPRequest.HTTPConnectionInitializer() {
			
			@Override
			public void initialize(HttpURLConnection connection) {
				for (Map.Entry<String, String> e: getSwarmHeaders().entrySet())
					connection.setRequestProperty(e.getKey(), e.getValue());
			}
		});
	}

	protected AbstractSwarmWSClient(String swarmHostUrl, String apiKey, HTTPRequest httpClient) {
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
	 * @param force ignores any previous successful validation.
	 * @return true if response is 200, false if otherwise
	 * @throws IOException
	 */
	public Throwable checkAndValidate(boolean force) {
		if (isValidated && !force)
			return null;
		
		try {
			HTTPResponse response = httpClient.get(swarmHostUrl + "keys/" + apiKey + "/verify");
			int rval = response.getResponseCode();
			
			if (rval >= 200 && rval < 400) {
				isValidated = true;
				return null;
			}			
			
			return new HTTPException(rval, "Validation failed.");
		} catch (IOException e) {
			return e;					
		}
	}
	
	/**
	 * @return required swarm headers
	 */
	protected Map<String, String> getSwarmHeaders() {
		if (staticHeaders == null) {
			staticHeaders = toMap(SWARM_APIKEY_HEADER_KEY, apiKey, CONTENT_TYPE_HEADER_KEY, "application/json");			
		}
		
		return staticHeaders;
	}
	
	/**
	 * Create a map of strings, each even element being a key, each successive odd element being the value.
	 * @param elems
	 * @return
	 */
	protected Map<String, String> toMap(String... elems) {
		if (elems == null || elems.length % 2 != 0)
			throw new RuntimeException("Must specify name/value pair for each entry in map.");
		
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < elems.length / 2; i = i + 2) {
			map.put(elems[i], elems[i + 1]);
		}
		
		return map;
	}
}
