package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.util.Map;

import com.buglabs.util.simplerestclient.HTTPRequest;
import com.buglabs.util.simplerestclient.HTTPResponse;
import com.buglabs.util.simplerestclient.IFormFile;

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
	public SwarmBinaryUploadWSClient(String swarmHostUrl, String apiKey, HTTPRequest httpClient) {
		super(swarmHostUrl, apiKey, httpClient);
	}

	@Override
	public SwarmWSResponse upload(String userId, String resourceId, String swarmId, String filename, final byte[] payload) throws IOException {
		validate();

		final String[] elems = filename.split("\\.");

		if (elems.length != 2) {
			// TODO: support better filenames than "name.ext".
			throw new IOException("Invalid filename specified: " + filename);
		}

		IFormFile ffile = new IFormFile() {

			@Override
			public String getFilename() {
				return elems[0];
			}

			@Override
			public String getContentType() {
				return elems[1];
			}

			@Override
			public byte[] getBytes() {
				return payload;
			}
		};

		Map<String, Object> params = toMap(	"user_id", userId,
											"resource_id", resourceId);		
		params.put("file", ffile);

		HTTPResponse response = httpClient.postMultipart(swarmHostUrl + "upload", params);

		return SwarmWSResponse.fromCode(response.getResponseCode());
	}
}
