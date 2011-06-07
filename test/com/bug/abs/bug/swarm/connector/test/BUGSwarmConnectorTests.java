package com.bug.abs.bug.swarm.connector.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.buglabs.bug.swarm.connector.BUGSwarmConnector;
import com.buglabs.util.simplerestclient.HTTPRequest;
import com.buglabs.util.simplerestclient.HTTPResponse;

import junit.framework.TestCase;

/**
 * Tests the high-level BUGSwarmConnector class
 * 
 * @author kgilmer
 *
 */
public class BUGSwarmConnectorTests extends TestCase  {

	
	public void testInitializeConnector() throws InterruptedException {
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
		
		connector.start();			
		
		Thread.sleep(15000);
		
		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getAPIKey());
		
		HTTPResponse response = request.get("http://api.bugswarm.net/swarms/5af1efdd4e995a17a9b0846670bb6f4634cf7eb3/feeds?stream=true", headers);
		
		StreamScanner scanner = new StreamScanner(response.getStream());
		
		Thread.sleep(5000);
		
		assertTrue(scanner.hasInputBeenRecieved());
		connector.interrupt();
		connector.shutdown();
		
	}
	
	private class StreamScanner extends Thread {

		private final InputStream istream;
		private boolean inputReceived = false;

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
						inputReceived = true;
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
			return inputReceived;
		}
	}
}
