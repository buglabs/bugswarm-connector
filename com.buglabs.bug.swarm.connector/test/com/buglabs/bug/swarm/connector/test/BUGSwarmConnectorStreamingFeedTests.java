package com.buglabs.bug.swarm.connector.test;

import junit.framework.TestCase;

/**
 * Tests the BUGSwarmConnector class in regards to binary feeds.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnectorStreamingFeedTests extends TestCase {
/*
	
	@Override
	protected void setUp() throws Exception {
		System.out.println("setUp()");
		SwarmWSClient c = new SwarmWSClient(AccountConfig.getConfiguration());

		assertTrue(c.isValid() == null);

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

		SwarmWSResponse response = c.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.PRODUCER,
				AccountConfig.XMPP_USERNAME, AccountConfig.getConfiguration().getResource());
		assertFalse(response.isError());
		// Resource 'web' is required so that http streaming will work
		response = c.getSwarmResourceClient().add(AccountConfig.testSwarmId
				, MemberType.CONSUMER, AccountConfig.XMPP_USERNAME, "web");
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

	*//**
	 * Test initializing the connector.  
	 * @throws InterruptedException
	 *//*
	public void testInitializeConnector() throws InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		connector.start();
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		assertTrue(connector.isInitialized());

		connector.interrupt();
		connector.shutdown();

		Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);
	}

	public void testGetStreamingFeed() throws IOException, InterruptedException {
		
		
		 * curl -vvv --header 'X-BugSwarmApiKey:3077514aa9aa5a5826cfd9d04ee059db1a18057d' http://192.168.20.120:8002/swarms/c78defee941e061b9f1550497cb5a34d84c1982c/feeds/feed1?frequency=5\&stream=true
		 
		
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getAPIKey());
		
		String url = 
			AccountConfig.getConfiguration().getHostname(Protocol.HTTP)
			+ "/swarms/" + AccountConfig.testSwarmId + "/feeds/feed1?frequency=5&stream=true" ;
		
		System.out.println("Getting streaming data for feed " + OSGiHelperTester.TEST_BINARY_FEED_NAME + " sending to " + url);

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
*/}
