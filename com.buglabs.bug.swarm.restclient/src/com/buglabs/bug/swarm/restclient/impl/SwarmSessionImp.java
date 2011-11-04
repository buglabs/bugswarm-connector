package com.buglabs.bug.swarm.restclient.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
	private final String swarmUrl;
	private static final String CRLF = "\r\n";
	private final String hostname;
	private final OutputStream soutput;
	private List<ISwarmMessageListener> listeners;
	private final SocketReader readerThread;
	private final ObjectMapper mapper = new ObjectMapper();
	private final String[] swarmIds;

	public SwarmSessionImp(String hostname, String apiKey, String swarmUrl, String ... swarmIds) throws UnknownHostException, IOException {		
		this.hostname = hostname;
		this.apiKey = apiKey;
		this.swarmUrl = swarmUrl;
		this.swarmIds = swarmIds;		
		this.socket = new Socket(hostname, 80);
		this.soutput = socket.getOutputStream();
		initialize();
		
		this.readerThread = new SocketReader(socket.getInputStream());
		this.readerThread.start();
		
	}

	private void initialize() throws IOException {
		StringBuilder header = new StringBuilder();
		header.append("GET ");
		header.append(swarmUrl);
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
		
		System.out.println(header.toString());
		
		soutput.write(header.toString().getBytes());
		
	//	mapper.writeValue(soutput, generatePresence(true, true, swarmIds));
		
		soutput.flush();
	}

	private static Map<String, Object> generatePresence(boolean outgoing, boolean available, String[] swarmIds2) {
		String direction = "to";
		if (!outgoing)
			direction = "from";
		
		Map<String, Object> m = toMap("presence", 
				toMap(direction, Arrays.asList(swarmIds2)));
		
		if (!available)
			m.put("type", "unavailable");
		
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
		
		
		//TODO: add envelope to json so it's valid. 
		try {
			//mapper.writeValue(soutput, payload);
			soutput.write("{\"message\": {\"payload\": {\"x\":1}}}".getBytes());
			soutput.flush();
		} catch (JsonGenerationException e) {
			throw new IOException(e);
		} catch (JsonMappingException e) {
			throw new IOException(e);
		} 
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
					if (listeners != null) {
						//Here we parse the json message, extract payload, fromSwarm, fromResource, isPublic
						Map<String, ?> payload = null;
						String fromSwarm = null;
						String fromResource = null;
						boolean isPublic = true;
						for (ISwarmMessageListener listener : listeners)
							listener.messageRecieved(payload, fromSwarm, fromResource, isPublic);
					}
					
					Thread.sleep(500);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				return;
			}
		}
	}

}
