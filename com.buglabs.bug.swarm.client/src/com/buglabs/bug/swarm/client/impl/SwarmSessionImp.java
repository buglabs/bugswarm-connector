package com.buglabs.bug.swarm.client.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.buglabs.bug.swarm.client.ISwarmMessageListener;
import com.buglabs.bug.swarm.client.ISwarmSession;

/**
 * Implementation of ISwarmSession.
 * 
 * @author kgilmer
 *
 */
public class SwarmSessionImp implements ISwarmSession, ISwarmMessageListener {
	private Socket socket;
	private final String apiKey;
	private static final String CRLF = "\r\n";
	private final String hostname;
	private OutputStream soutput;
	private final List<ISwarmMessageListener> listeners;
	private SwarmParticipationReader readerThread;
	private final static ObjectMapper mapper = new ObjectMapper();
	private final String[] swarmIds;
	private final String resourceId;
	private final int port;
	private final SessionType type;	

	/**
	 * @param hostname host of server
	 * @param port port on server 
	 * @param apiKey api key
	 * @param resourceId resource id
	 * @param swarmIds list of swarms to join
	 * @throws UnknownHostException on host resolution error
	 * @throws IOException on I/O error
	 */
	public SwarmSessionImp(String hostname, ISwarmSession.SessionType type, int port, String apiKey, String resourceId, String ... swarmIds) throws UnknownHostException, IOException {		
		this.hostname = hostname;
		this.type = type;
		this.port = port;
		this.apiKey = apiKey;
		this.resourceId = resourceId;
		this.swarmIds = swarmIds;		
		this.listeners = new CopyOnWriteArrayList<ISwarmMessageListener>();
		this.listeners.add(this);
		this.socket = createSocket(hostname, port);

		sendHeader();	
	}


	private Socket createSocket(String hostname, int port) throws UnknownHostException, IOException {
		Socket socket = new Socket(hostname, port);
		this.soutput = socket.getOutputStream();		
		
		if (readerThread != null)
			readerThread.interrupt();
		
		this.readerThread = new SwarmParticipationReader(socket.getInputStream(), apiKey, listeners);
		this.readerThread.start();
		
		return socket;
	}


	/**
	 * Send the session initialization header to the server.  This is copied from other example code.
	 * 
	 * See https://github.com/buglabs/bugswarm-api/blob/master/java/Swarm/src/test/com/buglabs/swarm/NIOSockets.java
	 * 
	 * @throws IOException
	 */
	private void sendHeader() throws IOException {
		StringBuilder header = new StringBuilder();
		
		if (type == SessionType.PRODUCTION)
			header.append("POST ");
		else
			header.append("GET ");
		
		header.append(createStreamUrl());
		header.append(" HTTP/1.1").append(CRLF);
		header.append("Host: ");
		header.append(hostname).append(CRLF);
		header.append("Accept: application/json").append(CRLF);
		header.append("X-BugSwarmApiKey: ").append(apiKey).append(CRLF);
		
		// switch to keep-alive once nodejs fixes https://github.com/joyent/node/issues/940
		header.append("Connection: close").append(CRLF); 
		header.append("User-Agent: ");
		header.append(this.getClass().getSimpleName()).append(CRLF);
		header.append("Transfer-Encoding: chunked").append(CRLF);
		header.append("Content-Type: application/json ;charset=UTF-8").append(CRLF);
		header.append(CRLF);
		
		soutput.write(header.toString().getBytes());
		soutput.flush();
		//need to write one chunk into the stream before the platform will send data do us
		//I chose \ because it's the keepalive as well
		//see https://github.com/buglabs/bugswarm-connector/issues/30
		writeOut("\n");
	}

	/**
	 * @return the host-less url of the server with swarm and resource ids.
	 */
	private String createStreamUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append("/stream?resource_id=");
		sb.append(resourceId);
		
		if (swarmIds != null && swarmIds.length > 0)
			sb.append('&');
		
		for (Iterator<String> i = Arrays.asList(swarmIds).iterator(); i.hasNext();) {
			sb.append("swarm_id=");
			sb.append(i.next());
			if (i.hasNext())
				sb.append('&');
		}
		
		return sb.toString();
	}

	/**
	 * Generate the Json message as string for presence to server.
	 * 
	 * @param available if false, add type:unavailable to message
	 * @param swarmIds array of swarm ids that are being joined.
	 * @return String of json message for outgoing presence.
	 * @throws JsonGenerationException on JSon error
	 * @throws JsonMappingException on JSon error
	 * @throws IOException on I/O error
	 */
	private static String generateOutgoingPresenceMessage(boolean available, String ... swarmIds) throws JsonGenerationException, JsonMappingException, IOException {
		Map<String, Object> m = toMap(
				"presence", 
					toMap("to", swarmIds));
		
		if (!available)
			m.put("type", "unavailable");
		
		return mapper.writeValueAsString(m);		
	}
	
	/**
	 * Given a variable number of <String, String> pairs, construct a Map and
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

	@Override
	public void send(Map<String, ?> payload) throws IOException {					
		writeOut(mapper.writeValueAsString(createPayloadMap(resourceId, payload)));		
	}


	@Override
	public void send(Map<String, ?> payload, String... swarmIds) throws IOException {
		Map<String, Object> map = createPayloadMap(resourceId, payload);
		map.put("to", Arrays.asList(swarmIds));

		writeOut(mapper.writeValueAsString(map));
	}

	@Override
	public void send(Map<String, ?> payload, List<Map.Entry<String, String>> swarmAndResource) throws IOException {
		//TODO: implement this.
		throw new RuntimeException("Unimplemented.");
	}
	
	@Override
	public void join(String swarmId, String resourceId) throws IOException {
		StringBuilder buffer = new StringBuilder();
		String ps = generateOutgoingPresenceMessage(true, swarmId);
		
		buffer.append(Integer.toHexString(ps.getBytes().length)).append(CRLF);
		buffer.append(ps).append(CRLF);
		
		debugOut(buffer.toString(), true);
		soutput.write(buffer.toString().getBytes());
		soutput.flush();
	}
	
	/**
	 * Print debug messages to system console.
	 * TODO: remove once code is stable.
	 * @param message
	 * @param out
	 */
	private void debugOut(String message, boolean out) {
		System.out.print(apiKey.substring(0, 4));
		if (out)
			System.out.print(" --> ");
		else
			System.out.print(" <-- ");
		
		System.out.println(message);
	}
	
	/**
	 * Create a Map of payload.
	 * @param payload
	 * @return
	 */
	private Map<String, Object> createPayloadMap(String resourceId, Map<String, ?> payload) {
		Map<String, Object> map =new HashMap<String, Object>();
		
		map.put("message", toMap("payload", payload));
		map.put("from", toMap("resource", resourceId));
		
		return map;
	}
	
	/**
	 * Send message to server.  Handles calculating the message length.
	 * 
	 * @param message input message
	 * @throws IOException on socket I/O error
	 */
	private void writeOut(String message) throws IOException {
		if (!isConnected()) {
			this.socket = createSocket(hostname, port);
		}			
		
		debugOut(message, true);
		
		soutput.write(Integer.toHexString(message.length()).getBytes());
		soutput.write(CRLF.getBytes());
		soutput.write(message.getBytes());
		soutput.write(CRLF.getBytes());
		soutput.flush();
	}

	@Override
	public void addListener(ISwarmMessageListener listener) {		
		listeners.add(listener);
	}

	@Override
	public void removeListener(ISwarmMessageListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void close() {		
		if (readerThread != null)
			readerThread.shuttingDown();
		
		//Attempt to send presence message.
		try {
			StringBuilder buffer = new StringBuilder(); // StringBuilder(staticHeader.toString());
			String ps = generateOutgoingPresenceMessage(false, swarmIds);
			
			buffer.append(Integer.toHexString(ps.getBytes().length)).append(CRLF);
			buffer.append(ps).append(CRLF);
			
			debugOut(buffer.toString(), true);
			soutput.write(buffer.toString().getBytes());
			soutput.flush();
		} catch (IOException e) {			
		}
		
		//Attempt to close socket.
		try {
			socket.close();
		} catch (IOException e) {			
		}
		
		//Attempt to interrupt reader.
		if (readerThread != null) {
			readerThread.interrupt();
			readerThread = null;
		}
	}
	
	@Override
	public boolean isConnected() {
		//TODO: Fix the connected state such that if the reader disconnects, it is rebound until the client explicitly closes the connection.
		return readerThread != null && socket != null && readerThread.isRunning() && socket.isConnected();
	}


	@Override
	public void presenceEvent(String fromSwarm, String fromResource, boolean isAvailable) {		
	}


	@Override
	public void exceptionOccurred(ExceptionType type, String message) {
		if (type == ExceptionType.SERVER_UNEXPECTED_DISCONNECT) {
			try {
				this.socket = createSocket(hostname, port);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}


	@Override
	public void request(String feedName) throws IOException {
		// TODO Implement
		throw new RuntimeException("Unimplemented");
	}
}
