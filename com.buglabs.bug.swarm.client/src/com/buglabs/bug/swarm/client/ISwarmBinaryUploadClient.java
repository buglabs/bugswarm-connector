package com.buglabs.bug.swarm.client;

import java.io.IOException;

/**
 * Represents the client-side of the Swarm Binary Uploads API as defined here:
 * https://github.com/buglabs/bugswarm/wiki/Binary-Uploads-API.
 * 
 * @author kgilmer
 * 
 */
public interface ISwarmBinaryUploadClient {

	/**
	 * Upload binary data to swarm.
	 * 	
	 * @param userId user id
	 * @param resourceId resource id
	 * @param filename
	 *            Abstract name of file
	 * @param payload
	 *            byte array of binary data
	 * @return WS response
	 * @throws IOException
	 */
	SwarmWSResponse upload(String userId, String resourceId, String filename, byte[] payload) throws IOException;
}
