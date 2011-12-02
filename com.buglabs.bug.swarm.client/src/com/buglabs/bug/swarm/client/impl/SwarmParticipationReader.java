package com.buglabs.bug.swarm.client.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmJsonMessageListener;
import com.buglabs.bug.swarm.client.ISwarmMessageListener;
import com.buglabs.bug.swarm.client.ISwarmMessageListener.ExceptionType;
import com.buglabs.bug.swarm.client.ISwarmStringMessageListener;

/**
 * Reads from the HTTP-like input stream for a swarm participation session and sends events to listeners when they occur.
 * 
 * @author kgilmer
 *
 */
public class SwarmParticipationReader extends Thread {
	
	private static final String MESSAGE_KEY = "message";
	private static final String RESOURCE_KEY = "resource";
	private static final String SWARM_KEY = "swarm";
	private static final String TYPE_VALUE_AVAILABLE = "available";
	private static final String TYPE_KEY = "type";
	private static final String FROM_KEY = "from";
	private static final String PRESENCE_KEY = "presence";
	private static final String CODE_KEY = "code";
	
	private final BufferedReader reader;
	private volatile boolean running = false;
	private volatile boolean shuttingDown = false;
	private final String apiKey;
	private final List<ISwarmMessageListener> listeners;
	private final static ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * @param is inputstream, must not be null.
	 * @param apiKey api key, must not be null.
	 * @param listeners List of ISwarmMessageListener.  Must not be null.
	 * @throws UnsupportedEncodingException
	 */
	protected SwarmParticipationReader(InputStream is, String apiKey, List<ISwarmMessageListener> listeners) throws UnsupportedEncodingException {
		AbstractSwarmWSClient.validateParams(is, apiKey, listeners);
		
		this.apiKey = apiKey;
		this.listeners = listeners;
		this.reader = new BufferedReader(new InputStreamReader(is, ISwarmClient.SWARM_CHARACTER_ENCODING));
	}
	
	@Override
	public void run() {
		running = true;
		String line = null;
		String disconnectMessage = "Server disconnect";
		
		readinput:
			
		try {			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				//Filter empty lines and line length lines.
				if (line.length() == 0 || isNumeric(line))
					continue;
				
				//Quick check on line to make sure it looks like json.
				if (!looksLikeJson(line)) {
					if (isHTTPHeader(line))
						continue;
					
					for (ISwarmMessageListener listener : listeners)
						listener.exceptionOccurred(ExceptionType.SERVER_MESSAGE_PARSE_ERROR, "Unparsable message: " + line);
					continue;
				}
				
				debugOut(line, false);
				
				//Parse the message string into a JsonNode.
				JsonNode jmessage = mapper.readTree(line);

				//TODO: optimize by moving message parsing code out of the listener loop.
				for (ISwarmMessageListener listener : listeners) {
					if (jmessage.has(PRESENCE_KEY)) {
						if (!isValidPresenceMessage(jmessage)) {
							listener.exceptionOccurred(ExceptionType.INVALID_MESSAGE, "Presense did not have expected values.");
						} else {
							boolean avail = pv(jmessage.get(PRESENCE_KEY).get(TYPE_KEY)) != null && pv(jmessage.get(PRESENCE_KEY).get(TYPE_KEY)).equals(TYPE_VALUE_AVAILABLE);
							listener.presenceEvent(
									pv(jmessage.get(PRESENCE_KEY).get(FROM_KEY).get(SWARM_KEY)), 
									pv(jmessage.get(PRESENCE_KEY).get(FROM_KEY).get(RESOURCE_KEY)), 
									avail);
						}
					} else if (jmessage.has(MESSAGE_KEY)) {
						if (!isValidMessageMessage(jmessage)) {
							listener.exceptionOccurred(ExceptionType.INVALID_MESSAGE, "Message did not have expected values.");
						} else {				
							String swarmId = null;
							String resourceId = null;
							boolean isPublic = false;
							
							if (jmessage.get("message").has("from")) {
								swarmId = pv(jmessage.get("message").get("from").get("swarm"));
								resourceId = pv(jmessage.get("message").get("from").get("resource"));
							}
							
							if (jmessage.get("message").has("public"))
								isPublic = jmessage.get("message").get("public").asBoolean();
							
							if (listener instanceof ISwarmJsonMessageListener) {
								JsonNode payloadJson = jmessage.get("message").get("payload");
								if (payloadJson.isArray()) {
									List<Map<String, Object>> nodes = mapper.readValue(payloadJson, List.class);
									for (Map<String, Object> n : nodes) {
										((ISwarmJsonMessageListener)listener).messageRecieved(n, swarmId, resourceId, isPublic);
									}
								} else {
									Map<String, Object> payload = mapper.readValue(payloadJson, Map.class);
									((ISwarmJsonMessageListener)listener).messageRecieved(payload, swarmId, resourceId, isPublic);
								}
							} else if (listener instanceof ISwarmStringMessageListener) {
								String payload = jmessage.get("message").get("payload").asText();
								((ISwarmStringMessageListener)listener).messageRecieved(payload, swarmId, resourceId, isPublic);
							} else if (listener instanceof SwarmSessionImp) {
								//The session impl only listens for exceptions so it can re-establish the socket connection.  
							} else {
								throw new IllegalArgumentException("Listener " + listener.getClass().getName() + " is abstract.  Use a concrete listener");
							}
						}
					} else if (jmessage.has(CODE_KEY)) {
						listener.exceptionOccurred(ExceptionType.SERVER_ERROR, jmessage.toString());
					} else {
						listener.exceptionOccurred(ExceptionType.INVALID_MESSAGE, "JSon did not have expected value ['presence' | 'message']");
					}						
				}
				
				Thread.sleep(100);
			}
		} catch (IOException e) {
			disconnectMessage = e.getMessage();
		} catch (InterruptedException e) {
			return;
		} finally {
			//Relying on first element of listeners being the SwarmSessionImpl.  See line 56.
			if (!shuttingDown)
				listeners.get(0).exceptionOccurred(ExceptionType.SERVER_UNEXPECTED_DISCONNECT, disconnectMessage);
			
			running = false;			
		}
	}
	
	/**
	 * @param line
	 * @return true if input line looks like an HTTP header
	 */
	private boolean isHTTPHeader(String line) {
		if (line.startsWith("HTTP"))
			return true;
		
		if (line.indexOf(':') > -1)
			return true;
		
		return false;
	}

	/**
	 * @param jmessage
	 * @return  if message has minimum structure required for a 'message' message
	 */
	private boolean isValidMessageMessage(JsonNode jmessage) {
		if (jmessage.has("message") && jmessage.get("message").has("payload"))
			return true;
		
		return false;
	}

	/**
	 * @param jmessage
	 * @return true if message has minimum structure required for a presence message
	 */
	private boolean isValidPresenceMessage(JsonNode jmessage) {
		if (jmessage.has(PRESENCE_KEY) && jmessage.get(PRESENCE_KEY).has(FROM_KEY))
			return true;
		
		return false;
	}

	/**
	 * Avoid NPEs by only calling getTextValue() on non-null references.
	 * 
	 * @param jsonNode
	 * @return text value of node or null if node is null.
	 */
	private String pv(JsonNode jsonNode) {
		if (jsonNode == null)
			return null;
		
		return jsonNode.getTextValue();
	}

	/**
	 * Scan first and last characters of string to see if they are json-like.
	 * @param line input string
	 * @return true if looks like json, false otherwise.
	 */
	private boolean looksLikeJson(String line) {
		if (line.startsWith("{") && line.endsWith("}"))
			return true;
		
		if (line.startsWith("[") && line.endsWith("]"))
			return true;
		
		return false;
	}

	/**
	 * @param in input string
	 * @return true if input line can be parsed as a Long
	 */
	private boolean isNumeric(String in) {
		try {
			Long.parseLong(in, 16);
			return true;
		} catch (NumberFormatException e) {				
		}
		return false;
	}
	
	private void debugOut(String message, boolean out) {
		System.out.print(apiKey.substring(0, 4));
		if (out)
			System.out.print(" --> ");
		else
			System.out.print(" <-- ");
		
		System.out.println(message);
	}

	/**
	 * @return true if socket is reading.
	 */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * This is set when client explicitly closes session.  Once set, socket I/O errors are not passed back to client in listener. 
	 */
	protected void shuttingDown() {
		shuttingDown = true;
	}
}