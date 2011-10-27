package com.buglabs.bug.swarm.connector.test;

import junit.framework.TestCase;

/**
 * Tests the high-level BUGSwarmConnector class in regards to feeds.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnectorFeedT3sts extends TestCase {
	
	/**
	 * As of 0.3 (?) the feed api has been removed, and there is
	 * nothing currently to replace it.  Since unable to query 
	 * feeds am removing these tests.
	 * 
	 * TODO: When/if there is a way to access feeds via an API the tests
	 * should be changed to use that new API.
	 */
/*
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
				AccountConfig.testSwarmId, 
				MemberType.CONSUMER, 
				"web");
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

	*//**
	 * https://github.com/buglabs/bugswarm/wiki/Swarm-Feeds-API.
	 * 
	 * Test 'Querying available Feeds in a Swarm'
	 * 
	 * @throws IOException on I/O error
	 * @throws InterruptedException if interrupted
	 *//*
	public void testGetFeedsGlobal() throws IOException, InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getConfingurationAPIKey());

		HTTPResponse response = request.get(AccountConfig.getConfiguration().getHostname(Protocol.HTTP) 
				+ "/swarms/" + AccountConfig.testSwarmId + "/feeds?stream=true", headers);
		
		StreamScanner scanner = new StreamScanner(response.getStream());
		scanner.start();

		Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);

		assertTrue(scanner.hasInputBeenRecieved());

		for (String r : scanner.getResponses()) {
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

	*//**
	 * https://github.com/buglabs/bugswarm/wiki/Swarm-Feeds-API.
	 * 
	 * Test 'Querying available Feeds in a Swarm Resource'
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 *//*
	public void testGetFeedsResource() throws IOException, InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getConfingurationAPIKey());

		HTTPResponse response = request.get(AccountConfig.getConfiguration().getHostname(Protocol.HTTP) + "/swarms/" + AccountConfig.testSwarmId + "/resources/"
				+ AccountConfig.getConfiguration().getResource() + "/feeds?stream=true", headers);

		StreamScanner scanner = new StreamScanner(response.getStream());
		scanner.start();

		Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);

		assertTrue(scanner.hasInputBeenRecieved());

		for (String r : scanner.getResponses()) {
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

	*//**
	 * https://github.com/buglabs/bugswarm/wiki/Swarm-Feeds-API
	 * 
	 * Test 'Querying a Swarm Feed'
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 *//*
	public void testGetSpecificFeed() throws IOException, InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getConfingurationAPIKey());

		HTTPResponse response = request.get(AccountConfig.getConfiguration().getHostname(Protocol.HTTP) + 
				"/swarms/" + AccountConfig.testSwarmId + "/resources/"
				+ AccountConfig.getConfiguration().getResource() + "/feeds?stream=true", headers);

		StreamScanner scanner = new StreamScanner(response.getStream());
		scanner.start();

		Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);

		assertTrue(scanner.hasInputBeenRecieved());
		int responseCount = 0;
		for (String r : scanner.getResponses()) {
			responseCount++;
			Object o = JSONValue.parse(r);

			assertNotNull(o);
			assertTrue(o instanceof JSONObject);

			JSONObject ja = (JSONObject) o;

			assertFalse(ja.isEmpty());
			assertTrue(ja.containsKey("payload"));

			Object payloadObject = ja.get("payload");

			assertTrue(payloadObject instanceof JSONArray);

			JSONArray payloadArray = (JSONArray) payloadObject;

			// For each key, which is a feed, query the actual feed.
			for (Object key : payloadArray) {
				assertTrue(key instanceof JSONObject);
				JSONObject jo = (JSONObject) key;

				for (Object rk : jo.keySet()) {
					String url = AccountConfig.getConfiguration().getHostname(Protocol.HTTP) 
					+ "/swarms/" + AccountConfig.testSwarmId + "/feeds/" + rk;
					
					System.out.println("Get data for feed " + rk + " to " + url);

					HTTPResponse response2 = request.get(url, headers);
					
					StreamScanner scanner2 = new StreamScanner(response2.getStream());
					scanner2.start();

					Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);

					assertTrue(scanner2.hasInputBeenRecieved());		
					scanner2.interrupt();
				}
			}
		}

		assertTrue(responseCount > 0);
		scanner.interrupt();
		connector.interrupt();
		connector.shutdown();
	}

	*//**
	 * https://github.com/buglabs/bugswarm/wiki/Swarm-Feeds-API
	 * 
	 * Test 'Querying a feed from a specific Swarm Resource of type producer'
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 *//*
	public void testGetSpecificFeedFromResource() throws IOException, InterruptedException {
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		HTTPRequest request = new HTTPRequest();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-BugSwarmApiKey", AccountConfig.getConfiguration().getConfingurationAPIKey());

		HTTPResponse response = request.get(AccountConfig.getConfiguration().getHostname(Protocol.HTTP) + "/swarms/" + AccountConfig.testSwarmId + "/resources/"
				+ AccountConfig.getConfiguration().getResource() + "/feeds?stream=true", headers);

		StreamScanner scanner = new StreamScanner(response.getStream());
		scanner.start();

		Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);

		assertTrue(scanner.hasInputBeenRecieved());

		for (String r : scanner.getResponses()) {
			Object o = JSONValue.parse(r);

			assertNotNull(o);
			assertTrue(o instanceof JSONObject);

			JSONObject ja = (JSONObject) o;

			assertFalse(ja.isEmpty());
			assertTrue(ja.containsKey("payload"));

			Object payloadObject = ja.get("payload");

			assertTrue(payloadObject instanceof JSONArray);

			JSONArray payloadArray = (JSONArray) payloadObject;

			// For each key, which is a feed, query the actual feed.
			for (Object key : payloadArray) {
				assertTrue(key instanceof JSONObject);
				JSONObject jo = (JSONObject) key;

				for (Object rk : jo.keySet()) {
					String url = 
						AccountConfig.getConfiguration().getHostname(Protocol.HTTP)
						+ "/swarms/" + AccountConfig.testSwarmId + "/resources/" 
						+ AccountConfig.getConfiguration().getResource() +  "/feeds/" + rk;
					
					System.out.println("Get data for feed " + rk + " sending to " + url);

					HTTPResponse response2 = request.get(url,
							headers);

					StreamScanner scanner2 = new StreamScanner(response2.getStream());
					scanner2.start();

					Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);

					assertTrue(scanner2.hasInputBeenRecieved());
					scanner2.interrupt();
				}
			}
		}

		scanner.interrupt();
		connector.interrupt();
		connector.shutdown();
	}
*/}
