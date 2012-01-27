package com.buglabs.bug.swarm.connector.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * A type of feed that represents a single binary resource.
 * 
 * This feed works just as a normal feed at the OSGi service level, except that
 * only defined keys are expected and read. Namely, one key:
 * <code>BinaryFeed.FEED_PAYLOAD_KEY</code>
 * 
 * The value of this key is expected to be an input stream representing a binary
 * resource that will be uploaded to the swarm server upon request.
 * 
 * @author kgilmer
 * 
 */
public class BinaryFeed extends Feed {

	/**
	 * A special key that denotes that the value field contains an InputStream
	 * of a binary resource.
	 */
	public static final String FEED_PAYLOAD_KEY = "SWARM.FEED.BINARY.PAYLOAD";

	/**
	 * Input stream of payload.
	 */
	private InputStream payload;

	/**
	 * Constructs the Binary Feed with the name and a map of necessary
	 * properties.
	 * 
	 * @param name
	 *            name of feed
	 * @param properties
	 *            Must contain at least BinaryFeed.FEED_PAYLOAD_KEY
	 */
	public BinaryFeed(String name, Map<?, ?> properties) {
		super(name, null);
		System.out.println("created binary feed object for"+name);
		try{
		payload = (InputStream) properties.get(BinaryFeed.FEED_PAYLOAD_KEY);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * @return The payload as a byte array.
	 * @throws IOException
	 *             on I/O error
	 */
	public byte[] getPayload() throws IOException {
		System.out.println("payload is null? "+ payload == null);
		byte[] array = IOUtils.toByteArray(payload);
		System.out.println("array length" +array.length);
		return IOUtils.toByteArray(payload);
	}

}
