package com.bug.abs.bug.swarm.connector.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.buglabs.bug.swarm.connector.BUGSwarmConnector;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;
import com.buglabs.util.simplerestclient.HTTPException;
import com.buglabs.util.simplerestclient.HTTPRequest;
import com.buglabs.util.simplerestclient.HTTPResponse;

import junit.framework.TestCase;

/**
 * Tests the high-level BUGSwarmConnector class
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnectorTests extends TestCase {

	@Override
	protected void setUp() throws Exception {
		System.out.println("setUp()");
		SwarmWSClient c = new SwarmWSClient(AccountConfig.getConfiguration());

		assertTrue(c.isValid() == null);
		
		//Delete all pre-existing swarms owned by test user.
		try {
			List<SwarmModel> swarms = c.list();
			
			for (SwarmModel sm : swarms) {
				if (sm.getUserId().equals(AccountConfig.getConfiguration().getUsername())) {
					c.destroy(sm.getId());
				}
			}
		} catch (HTTPException e) {
			//Ignore 404s.  They are not errors.  But unfortunately they have to be handled as errors since this is the REST way according to Camilo.
			if (e.getErrorCode() != 404)
				throw e;
		}

		AccountConfig.testSwarmId = c.create(AccountConfig.generateRandomSwarmName(), false, "A test swarm.");

		SwarmWSResponse response = c.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.PRODUCER,
				AccountConfig.XMPP_USERNAME, AccountConfig.getConfiguration().getResource());
		assertFalse(response.isError());
		// Resource 'web' is required so that http streaming will work
		response = c.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.CONSUMER, AccountConfig.XMPP_USERNAME, "web");
		assertFalse(response.isError());
	}

	@Override
	protected void tearDown() throws Exception {
		System.out.println("tearDown()");
		SwarmWSClient c = new SwarmWSClient(AccountConfig.getConfiguration());

		assertTrue(c.isValid() == null);
		
		SwarmWSResponse response = c.destroy(AccountConfig.testSwarmId);
		assertFalse(response.isError());
	}

	public void testInitializeConnector() throws InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		connector.start();
		Thread.sleep(15000);

		assertTrue(connector.isInitialized());

		connector.interrupt();
		connector.shutdown();

		Thread.sleep(5000);
	}

	/**
	 * https://github.com/buglabs/bugswarm/wiki/Swarm-Feeds-API
	 * 
	 * Test 'Querying available Feeds in a Swarm'
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void testGetFeedsGlobal() throws IOException, InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(15000);

		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getAPIKey());

		HTTPResponse response = request.get("http://api.bugswarm.net/swarms/" + AccountConfig.testSwarmId + "/feeds?stream=true", headers);

		StreamScanner scanner = new StreamScanner(response.getStream());
		scanner.start();

		Thread.sleep(2000);

		assertTrue(scanner.hasInputBeenRecieved());
		
		for (String r : scanner.getResponses()) {
			System.out.println("Testing r is a JSON object: " + r);
			Object o = JSONValue.parse(r);
			
			assertNotNull(o);
			assertTrue(o instanceof JSONObject);
			
			JSONObject ja = (JSONObject) o;
			
			assertFalse(ja.isEmpty());			
		}
		
		scanner.interrupt();
		connector.interrupt();
		connector.shutdown();
	}
	
	/**
	 * https://github.com/buglabs/bugswarm/wiki/Swarm-Feeds-API
	 * 
	 * Test 'Querying available Feeds in a Swarm Resource'
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void testGetFeedsResource() throws IOException, InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(15000);

		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getAPIKey());

		HTTPResponse response = request.get("http://api.bugswarm.net/swarms/" + AccountConfig.testSwarmId + "/resources/" + AccountConfig.getConfiguration().getResource() + "/feeds?stream=true", headers);

		StreamScanner scanner = new StreamScanner(response.getStream());
		scanner.start();

		Thread.sleep(2000);

		assertTrue(scanner.hasInputBeenRecieved());
		
		for (String r : scanner.getResponses()) {
			System.out.println("Testing r is a JSON object: " + r);
			Object o = JSONValue.parse(r);
			
			assertNotNull(o);
			assertTrue(o instanceof JSONObject);
			
			JSONObject ja = (JSONObject) o;
			
			assertFalse(ja.isEmpty());			
		}
		
		scanner.interrupt();
		connector.interrupt();
		connector.shutdown();
	}
	
	/**
	 * https://github.com/buglabs/bugswarm/wiki/Swarm-Feeds-API
	 * 
	 * Test 'Querying available Feeds in a Swarm Resource'
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void testGetSpecificFeed() throws IOException, InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(15000);

		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getAPIKey());

		HTTPResponse response = request.get("http://api.bugswarm.net/swarms/" + AccountConfig.testSwarmId + "/resources/" + AccountConfig.getConfiguration().getResource() + "/feeds?stream=true", headers);

		StreamScanner scanner = new StreamScanner(response.getStream());
		scanner.start();

		Thread.sleep(2000);

		assertTrue(scanner.hasInputBeenRecieved());
		
		for (String r : scanner.getResponses()) {
			System.out.println("Testing r is a JSON object: " + r);
			Object o = JSONValue.parse(r);
			
			assertNotNull(o);
			assertTrue(o instanceof JSONObject);
			
			JSONObject ja = (JSONObject) o;
			
			assertFalse(ja.isEmpty());			
			
			//For each key, which is a feed, query the actual feed.
			for (Object key : ja.keySet()) {
				HTTPResponse response2 = 
					request.get("http://api.bugswarm.net/swarms/"
							+ AccountConfig.testSwarmId + "/feeds/" + key, headers);

				StreamScanner scanner2 = new StreamScanner(response2.getStream());
				scanner.start();
				
				Thread.sleep(2000);

				assertTrue(scanner.hasInputBeenRecieved());
			}
		}
		
		scanner.interrupt();
		connector.interrupt();
		connector.shutdown();
	}

	private class StreamScanner extends Thread {

		private final InputStream istream;
		private int inputLineCount = 0;
		private List<String> responses = new CopyOnWriteArrayList<String>();

		public StreamScanner(InputStream istream) {
			this.istream = istream;
		}

		@Override
		public void run() {
			BufferedReader br = new BufferedReader(new InputStreamReader(istream));

			String line = null;

			try {
				while (!Thread.interrupted() && (line = br.readLine()) != null) {
					System.out.println("OUTPUT: " + line);

					if (line.trim().length() > 0) {
						inputLineCount++;
						responses.add(line.trim());
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}

		public boolean hasInputBeenRecieved() {
			return inputLineCount > 0;
		}

		public int getInputLineCount() {
			return inputLineCount;
		}
		
		public Iterable<String> getResponses() {
			return new Iterable<String>() {
				
				@Override
				public Iterator<String> iterator() {
					// TODO Auto-generated method stub
					return responses.iterator();
				}
			};
		}
	}
}
