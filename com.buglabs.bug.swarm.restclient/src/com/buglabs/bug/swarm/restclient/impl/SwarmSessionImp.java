package com.buglabs.bug.swarm.restclient.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

	private volatile boolean running = false;
	private final Socket socket;
	private final String apiKey;
	private static final String CRLF = "\r\n";
	private final String hostname;
	private final OutputStream soutput;
	private List<ISwarmMessageListener> listeners;
	private final SocketReader readerThread;
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
		initialize();
		
		this.readerThread = new SocketReader(socket.getInputStream());
		this.readerThread.start();
		
	}

	private void initialize() throws IOException {
		StringBuilder header = new StringBuilder();
		header.append("POST ");
		header.append("/stream");
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
		
		String ps = generatePresence(true, true, swarmIds, hostname, resourceId);
		
		header.append(Integer.toHexString(ps.getBytes().length)).append(CRLF);
		header.append(ps).append(CRLF);
		
		System.out.println(header.toString());
		running = true;
		writeOut(header.toString().getBytes());		
	}

	private static String generatePresence(boolean outgoing, boolean available, String[] swarmIds2, String hostname, String resourceId) throws JsonGenerationException, JsonMappingException, IOException {
		String direction = "to";
		if (!outgoing)
			direction = "from";
		
		List<String> destinationSwarms = new ArrayList<String>();
		
		for (String swarmId : swarmIds2)
			destinationSwarms.add(swarmId + "@" + hostname + "/" + resourceId);
		
		Map<String, Object> m = toMap("presence", 
				toMap(direction, destinationSwarms));
		
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
		
		Map<String, Object> message = new HashMap<String, Object>();
		
		message.put("message", toMap("payload", payload));
		
		String ms = mapper.writeValueAsString(message);
		String ml = Integer.toHexString(ms.length());
		
		StringBuilder sb = new StringBuilder();
		sb.append(ml).append(CRLF);
		sb.append(ms).append(CRLF);
		
		System.out.println(sb.toString());
		writeOut(sb.toString().getBytes());		
	}
	
	private void writeOut(byte[] bytes) throws IOException {
		if (!running  || bytes == null || soutput == null)
			throw new IOException("Connection has closed");
		
		soutput.write(bytes);
		soutput.flush();
	}

	@Override
	public void send(Map<String, ?> payload, String... swarmIds) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(Map<String, ?> payload, String swarmId, String resourceId) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	synchronized public void addListener(ISwarmMessageListener listener) {
		if (listeners == null)
			listeners = new CopyOnWriteArrayList<ISwarmMessageListener>();
		
		listeners.add(listener);
	}

	@Override
	public void removeListener(ISwarmMessageListener listener) {
		if (listeners != null)
			listeners.remove(listener);
	}

	@Override
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (readerThread != null) {
			readerThread.interrupt();
		}
	}
	
	private class SocketReader extends Thread {
		
		private final BufferedReader reader;
		public SocketReader(InputStream is) throws UnsupportedEncodingException {
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		}
		@Override
		public void run() {
			String line = null;
			try {
				//Here rather than reading the line as a string, 
				//Create (via jsonfactory) a jsonparser and call nextToken() for same effect
				while ((line = reader.readLine()) != null) {
					System.out.println(apiKey + ": " + line);
					if (listeners != null) {
						//Here we parse the json message, extract payload, fromSwarm, fromResource, isPublic
						Map<String, ?> payload = null;
						String fromSwarm = null;
						String fromResource = null;
						boolean isPublic = true;
						/*for (ISwarmMessageListener listener : listeners)
							listener.messageRecieved(payload, fromSwarm, fromResource, isPublic);*/
					}
					
					Thread.sleep(100);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				return;
			} finally {
				running = false;
				System.out.println(apiKey + " Reader exited");
			}
		}
	}

	@Override
	public void join(String swarmId, String resourceId) throws IOException {
		StringBuilder buffer = new StringBuilder();
		String ps = generatePresence(true, true, swarmIds, hostname, resourceId);
		
		buffer.append(Integer.toHexString(ps.getBytes().length)).append(CRLF);
		buffer.append(ps).append(CRLF);
		
		System.out.println(buffer.toString());
		soutput.write(buffer.toString().getBytes());
		soutput.flush();
	}
}
