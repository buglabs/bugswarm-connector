package com.bug.abs.bug.swarm.connector.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.buglabs.bug.swarm.connector.BUGSwarmConnector;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
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
		SwarmWSClient c = new SwarmWSClient(AccountConfig.getConfiguration());

		assertTrue(c.isValid() == null);

		AccountConfig.testSwarmId = c.create(AccountConfig.generateRandomSwarmName(), false, "A test swarm.");

		SwarmWSResponse response = c.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.PRODUCER,
				AccountConfig.testSwarmId, AccountConfig.getConfiguration().getResource());
		assertFalse(response.isError());
		// Resource 'web' is required so that http streaming will work
		response = c.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.CONSUMER, AccountConfig.testSwarmId, "web");
		assertFalse(response.isError());
	}

	@Override
	protected void tearDown() throws Exception {
		SwarmWSClient c = new SwarmWSClient(AccountConfig.getConfiguration());

		assertTrue(c.isValid() == null);
		
		SwarmWSResponse response = c.destroy(AccountConfig.testSwarmId);
		assertFalse(response.isError());
	}

	public void donttestInitializeConnector() throws InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		connector.start();
		Thread.sleep(15000);

		assertTrue(connector.isInitialized());

		Thread.sleep(10000);
		connector.interrupt();
		connector.shutdown();

		Thread.sleep(10000);
	}

	public void testGetFeedsGlobal() throws IOException, InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(20000);

		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getAPIKey());

		HTTPResponse response = request.get("http://api.bugswarm.net/swarms/" + AccountConfig.testSwarmId + "/feeds?stream=true", headers);

		StreamScanner scanner = new StreamScanner(response.getStream());
		scanner.start();

		Thread.sleep(5000);

		assertTrue(scanner.hasInputBeenRecieved());
		connector.interrupt();
		connector.shutdown();

	}

	private class StreamScanner extends Thread {

		private final InputStream istream;
		private int inputLineCount = 0;

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

					if (line.trim().length() > 0)
						inputLineCount++;
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
	}
}
