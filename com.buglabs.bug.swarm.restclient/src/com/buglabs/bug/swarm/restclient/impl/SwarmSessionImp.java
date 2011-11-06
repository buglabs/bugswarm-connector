package com.buglabs.bug.swarm.restclient.impl;

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

import com.buglabs.bug.swarm.restclient.ISwarmMessageListener;
import com.buglabs.bug.swarm.restclient.ISwarmSession;

public class SwarmSessionImp implements ISwarmSession {
	private final Socket socket;
	private final String apiKey;
	private static final String CRLF = "\r\n";
	private final String hostname;
	private final OutputStream soutput;
	private final List<ISwarmMessageListener> listeners;
	private final SwarmParticipationReader readerThread;
	private final static ObjectMapper mapper = new ObjectMapper();
	private final String[] swarmIds;
	private final String resourceId;

	public SwarmSessionImp(String hostname, String apiKey, String resourceId, String ... swarmIds) throws UnknownHostException, IOException {		
		this.hostname = hostname;
		this.apiKey = apiKey;
		this.resourceId = resourceId;
		this.swarmIds = swarmIds;		
		this.socket = new Socket(hostname, 80);
		this.soutput = socket.getOutputStream();
		this.listeners = new CopyOnWriteArrayList<ISwarmMessageListener>();
		this.readerThread = new SwarmParticipationReader(socket.getInputStream(), apiKey, listeners);
		this.readerThread.start();	

		sendHeader();	
	}


	private void sendHeader() throws IOException {
		StringBuilder header = new StringBuilder();
		header.append("POST ");
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
	}

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

	private static String generatePresence(boolean outgoing, boolean available, String[] swarmIds2, String hostname, String resourceId) throws JsonGenerationException, JsonMappingException, IOException {
		String direction = "to";
		if (!outgoing)
			direction = "from";
		
		Map<String, Object> m = toMap("presence", 
				toMap(direction, swarmIds2));
		
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
		writeOut(mapper.writeValueAsString(createPayloadMap(payload)));		
	}


	@Override
	public void send(Map<String, ?> payload, String... swarmIds) throws IOException {
		Map<String, Object> map = createPayloadMap(payload);
		map.put("to", Arrays.asList(swarmIds));

		writeOut(mapper.writeValueAsString(map));
	}

	@Override
	public void send(Map<String, ?> payload, List<Map.Entry<String, String>> swarmAndResource) throws IOException {
		
	}
	
	@Override
	public void join(String swarmId, String resourceId) throws IOException {
		StringBuilder buffer = new StringBuilder();
		String ps = generatePresence(true, true, swarmIds, hostname, resourceId);
		
		buffer.append(Integer.toHexString(ps.getBytes().length)).append(CRLF);
		buffer.append(ps).append(CRLF);
		
		debugOut(buffer.toString(), true);
		soutput.write(buffer.toString().getBytes());
		soutput.flush();
	}
	
	private void debugOut(String message, boolean out) {
		System.out.print(apiKey.substring(0, 4));
		if (out)
			System.out.print(" --> ");
		else
			System.out.print(" <-- ");
		
		System.out.println(message);
	}
	
	private Map<String, Object> createPayloadMap(Map<String, ?> payload) {
		Map<String, Object> map =new HashMap<String, Object>();
		
		map.put("message", toMap("payload", payload));
		
		return map;
	}
	
	private void writeOut(String message) throws IOException {
		if (!isConnected())
			throw new IOException("Connection has closed");
		
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
		try {
			StringBuilder buffer = new StringBuilder(); // StringBuilder(staticHeader.toString());
			String ps = generatePresence(true, false, swarmIds, hostname, resourceId);
			
			buffer.append(Integer.toHexString(ps.getBytes().length)).append(CRLF);
			buffer.append(ps).append(CRLF);
			
			debugOut(buffer.toString(), true);
			soutput.write(buffer.toString().getBytes());
			soutput.flush();
		} catch (IOException e) {			
		}
		
		try {
			socket.close();
		} catch (IOException e) {			
		}
		
		if (readerThread != null) {
			readerThread.interrupt();
		}
	}
	
	@Override
	public boolean isConnected() {
		return readerThread.isRunning() && socket.isConnected();
	}
}
