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
	
	protected static final String INVALID_SWARM_CONNECTION_ERROR_MESSAGE = "API_KEY is invalid.";

	protected final String swarmHostUrl;
	protected final String apiKey;
	protected HTTPRequest httpClient;
	protected boolean isValidated = false;
	
	private HashMap<String, String> staticHeaders;

	public AbstractSwarmWSClient(String swarmHostUrl, String apiKey) {
		if (!swarmHostUrl.endsWith("/"))
			swarmHostUrl = swarmHostUrl + "/";
		
		this.swarmHostUrl = swarmHostUrl;
		this.apiKey = apiKey;
		this.httpClient = new HTTPRequest();
		httpClient.addConfigurator(new com.buglabs.util.simplerestclient.HTTPRequest.HTTPConnectionInitializer() {
			
			@Override
			public void initialize(HttpURLConnection connection) {
				for (Map.Entry<String, String> e: getSwarmHeaders().entrySet())
					connection.setRequestProperty(e.getKey(), e.getValue());
			}
		});
	}

	protected AbstractSwarmWSClient(String swarmHostUrl, String apiKey, HTTPRequest httpClient) {
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
	 * @return
	 * @throws IOException
	 */
	public boolean checkAndValidate(boolean force) throws IOException {
		if (isValidated && !force)
			return true;
		
		try {
			HTTPResponse response = httpClient.get(swarmHostUrl + "keys/" + apiKey + "/verify");
			int rval = response.getResponseCode();
			
			if (rval == 200) {
				isValidated = true;
				return true;
			}			
		} catch (HTTPException e) {
			//Only catch HTTP exceptions so that connection errors are passed back to client.			
		}
		
		return false;
	}
	
	/**
	 * @return required swarm headers
	 */
	protected Map<String, String> getSwarmHeaders() {
		if (staticHeaders == null) {
			staticHeaders = new HashMap<String, String>();
			
			staticHeaders.put("X-BugSwarmApiKey", apiKey);
			staticHeaders.put("Content-Type", "application/json");
		}
		
		return staticHeaders;
	}
}
