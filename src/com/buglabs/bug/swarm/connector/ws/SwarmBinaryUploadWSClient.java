package com.buglabs.bug.swarm.connector.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import com.buglabs.util.http.RestClient;


/**
 * The implementation of the BUG Swarm Binary Upload WS API. See the interface
 * for details.
 * 
 * @author kgilmer
 * 
 */
public class SwarmBinaryUploadWSClient extends AbstractSwarmWSClient implements ISwarmBinaryUploadClient {

	/**
	 * @param swarmHostUrl
	 *            URL of swarm host
	 * @param apiKey
	 *            API KEY for user
	 * @param httpClient
	 *            HTTP client instance
	 */
	public SwarmBinaryUploadWSClient(String swarmHostUrl, String apiKey, RestClient httpClient) {
		super(swarmHostUrl, apiKey, httpClient);
	}

	@Override
	public SwarmWSResponse upload(String userId, String resourceId, String filename, final byte[] payload) throws IOException {
		validateParams(userId, resourceId, filename, payload);
	
		validateAPIKey();

		final String[] elems = filename.split("\\.");

		if (elems.length != 2) {
			// TODO: support better filenames than "name.ext".
			throw new IOException("Invalid filename specified: " + filename);
		}

		Map<String, Object> params = toMap(	(Object) "user_id", userId,
											"resource_id", resourceId);		
		params.put("file", new RestClient.FormInputStream(new ByteArrayInputStream(payload), elems[0], elems[1]));

		return httpClient.postMultipart(swarmHostUrl + "upload", params, ModelDeserializers.SwarmWSResponseDeserializer).getContent();
	}
}
