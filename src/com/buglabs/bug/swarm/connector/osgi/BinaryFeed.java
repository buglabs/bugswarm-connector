package com.buglabs.bug.swarm.connector.osgi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * A type of feed that represents a single binary resource.
 * 
 * @author kgilmer
 *
 */
public class BinaryFeed extends Feed {

	/**
	 * A special key that denotes that the value field contains an InputStream of a binary resource.
	 */
	public static final String FEED_PAYLOAD_KEY = "SWARM.FEED.BINARY.PAYLOAD";
	
	private InputStream payload;

	/**
	 * Constructs the Binary Feed with the name and a map of necessary properties.
	 * 
	 * @param name name of feed
	 * @param properties Must contain at least BinaryFeed.FEED_PAYLOAD_KEY
	 */
	public BinaryFeed(String name, Map<?, ?> properties) {
		super(name, null);
		
		payload = (InputStream) properties.get(BinaryFeed.FEED_PAYLOAD_KEY);
	}

	/**
	 * @return The payload as a byte array.
	 * @throws IOException on I/O error
	 */
	public byte[] getPayload() throws IOException {
		return IOUtils.toByteArray(payload);
	}
	
}
