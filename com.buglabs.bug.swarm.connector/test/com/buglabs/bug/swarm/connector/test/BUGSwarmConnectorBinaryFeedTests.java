package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.json.simple.JSONValue;

import com.buglabs.bug.swarm.connector.BUGSwarmConnector;
import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.restclient.ISwarmClient;
import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.restclient.SwarmClientFactory;
import com.buglabs.bug.swarm.restclient.SwarmWSResponse;
import com.buglabs.bug.swarm.restclient.model.SwarmModel;
import com.buglabs.util.simplerestclient.HTTPException;
import com.buglabs.util.simplerestclient.HTTPRequest;
import com.buglabs.util.simplerestclient.HTTPResponse;

/**
 * Tests the BUGSwarmConnector class in regards to binary feeds.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnectorBinaryFeedTests extends TestCase {

	/**
	 * Test input parameters:
	 * 
	 * -Dreport.misc=bugswarm-test,3077514aa9aa5a5826cfd9d04ee059db1a18057d,ddef1fa815d8549fa184e2716405f2cc553b5316
	 * 
	 */
	
	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		System.out.println("setUp()");
		ISwarmClient c =  SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP), 
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		// Delete all pre-existing swarms owned by test user.
		try {
			List<SwarmModel> swarms = c.list();

			for (SwarmModel sm : swarms) {
				if (sm.getUserId().equals(AccountConfig.getConfiguration().getUsername())) {
					c.destroy(sm.getId());
				}
			}
		} catch (HTTPException e) {
			// Ignore 404s. They are not errors. But unfortunately they have to
			// be handled as errors since this is the REST way according to
			// Camilo.
			if (e.getErrorCode() != 404)
				throw e;
		}

		AccountConfig.testSwarmId = c.create(AccountConfig.generateRandomSwarmName(), false, "A test swarm.");

		SwarmWSResponse response = c.getSwarmResourceClient().add(
				AccountConfig.testSwarmId, 
				MemberType.PRODUCER,
				AccountConfig.getConfiguration().getResource());
		assertFalse(response.isError());
		// Resource 'web' is required so that http streaming will work
		response = c.getSwarmResourceClient().add(
				AccountConfig.testSwarmId
				, MemberType.CONSUMER, "web");
		assertFalse(response.isError());
	}

	@Override
	protected void tearDown() throws Exception {
		System.out.println("tearDown()");
		ISwarmClient c =  SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP), 
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		SwarmWSResponse response = c.destroy(AccountConfig.testSwarmId);
		assertFalse(response.isError());
	}

	/**
	 * Test initializing the connector.  
	 * @throws InterruptedException
	 */
	public void testInitializeConnector() throws InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		connector.start();
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		assertTrue(connector.isInitialized());

		connector.interrupt();
		connector.shutdown();

		Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);
	}

	public void testGetPictureFeed() throws IOException, InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		String url = 
			AccountConfig.getConfiguration().getHostname(Protocol.HTTP)
			+ "/swarms/" + AccountConfig.testSwarmId + "/resources/" 
			+ AccountConfig.getConfiguration().getResource() +  "/feeds/" 
			+ OSGiHelperTester.TEST_BINARY_FEED_NAME;
		
		System.out.println("Getting binary data for feed " + OSGiHelperTester.TEST_BINARY_FEED_NAME + " sending to " + url);

		try {
			HTTPResponse response = request.get(url,
					headers);
			System.out.println(response.getString());
		
		
			System.out.println("boo");
			
			StreamScanner scanner = new StreamScanner(response.getStream());
			scanner.start();
	
			Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);
	
			assertTrue(scanner.hasInputBeenRecieved());
	
			for (String r : scanner.getResponses()) {
				Object o = JSONValue.parse(r);
	
				assertNotNull(o);
				System.out.println(o);
			}
	
			scanner.interrupt();
			connector.interrupt();
			connector.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
