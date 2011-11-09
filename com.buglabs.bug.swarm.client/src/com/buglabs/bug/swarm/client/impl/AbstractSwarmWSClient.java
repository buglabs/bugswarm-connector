package com.buglabs.bug.swarm.client.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.touge.restclient.ReSTClient;

/**
 * Base WSClient class with general common functionality.
 * 
 * @author kgilmer
 * 
 */
public abstract class AbstractSwarmWSClient {
	private static final String SWARM_APIKEY_HEADER_KEY = "X-BugSwarmApiKey";

	protected static final String INVALID_SWARM_CONNECTION_ERROR_MESSAGE = "API_KEY is invalid.";

	private static final boolean DEBUG_MODE = true;

	private static ObjectMapper mapper;
	
	protected final ReSTClient.URLBuilder swarmHostUrl;
	protected final String apiKey;
	protected final ReSTClient httpClient;	

	private Map<String, String> staticHeaders;

	/**
	 * @param swarmHostUrl
	 *            url of swarm host, with scheme.
	 * @param apiKey
	 *            api key as provided by server
	 */
	public AbstractSwarmWSClient(String swarmHostUrl, final String apiKey) {
		if (swarmHostUrl == null || apiKey == null)
			throw new IllegalArgumentException("An input parameter is null.");		

		this.apiKey = apiKey;
		this.httpClient = new ReSTClient();
		this.swarmHostUrl = httpClient.buildURL(swarmHostUrl);
		
		if (DEBUG_MODE)
			httpClient.setDebugWriter(new PrintWriter(System.out));
		
		//Add a connection initializer that automatically appends the swarm-server
		//authentication headers to the request.
		httpClient.addConnectionInitializer(new ReSTClient.ConnectionInitializer() {
			
			@Override
			public void initialize(HttpURLConnection connection) {
				for (Map.Entry<String, String> e : getSwarmHeaders().entrySet())
					connection.setRequestProperty(e.getKey(), e.getValue().toString());
			}
		});		
		
		//Set up an error handler for the rest client that throws exceptions in some situations, 
		//based on the swarm-server behavior.
		httpClient.setErrorHandler(new ReSTClient.ErrorHandler() {
			
			@Override
			public void handleError(int code, String message) throws IOException {
				//Return 5xx errors, and 4xx errors except for 404 (resource does not exist) and 409 (resource conflict)
				//as these errors typically require something to be handled in the code that makes the initial request.
				
				if (code >= 400 && code < 600 && code != 409)
					throw new IOException("Server returned HTTP error " + code + ": " + message);
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
	protected AbstractSwarmWSClient(String swarmHostUrl, final String apiKey, final ReSTClient httpClient) {
		if (swarmHostUrl == null || apiKey == null || httpClient == null)
			throw new IllegalArgumentException("An input parameter is null.");

		this.apiKey = apiKey;
		this.httpClient = httpClient;
		this.swarmHostUrl = httpClient.buildURL(swarmHostUrl);
	}

	/**
	 * @return required swarm headers
	 */
	protected Map<String, String> getSwarmHeaders() {
		if (staticHeaders == null) {
			staticHeaders = toMap(SWARM_APIKEY_HEADER_KEY, apiKey, 
								"content-type", "application/json");
		}

		return staticHeaders;
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
	
	/**
	 * Create an input stream of a String-representation of a JSon object.
	 * Uses Jackson's serialization logic to change a Map to JSon.
	 * @param input A map of serializable types 
	 * @return A JSon document as an input stream.
	 * @throws JsonGenerationException on serialization errors
	 * @throws JsonMappingException on serialization errors
	 * @throws IOException on serialization errors
	 */
	protected static InputStream createJsonStream(Map<?, ?> input) throws JsonGenerationException, JsonMappingException, IOException {
		if (mapper == null)
			mapper = new ObjectMapper();
		
		return new ByteArrayInputStream(mapper.writeValueAsBytes(input));		
	}
}
