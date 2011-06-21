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
import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.ws.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.connector.ws.SwarmModel;
import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;
import com.buglabs.bug.swarm.connector.ws.SwarmWSResponse;
import com.buglabs.util.simplerestclient.HTTPException;
import com.buglabs.util.simplerestclient.HTTPRequest;
import com.buglabs.util.simplerestclient.HTTPResponse;

import junit.framework.TestCase;

/**
 * Tests the high-level BUGSwarmConnector notification functionality, or, any logic in which unplanned events come from the
 * server and the connector has to change in some way.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnectorNotificationTests extends TestCase {

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
	}

	@Override
	protected void tearDown() throws Exception {
		System.out.println("tearDown()");
		SwarmWSClient c = new SwarmWSClient(AccountConfig.getConfiguration());

		assertTrue(c.isValid() == null);

		SwarmWSResponse response = c.destroy(AccountConfig.testSwarmId);
		assertFalse(response.isError());
	}


	/**
	 * https://github.com/buglabs/bugswarm/wiki/Swarm-Feeds-API
	 * 
	 * Test 'Querying available Feeds in a Swarm'
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void testGetNotificationAfterFirstSwarmJoin() throws IOException, InterruptedException {
		SwarmWSClient c = new SwarmWSClient(AccountConfig.getConfiguration());
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		//At this point the connector is initialized with no swarms.  We will now create a swarm and associate it with this user.
		//The server should notify connector that a new swarm should be joined.
		assertTrue(connector.getMemberSwarms().size() == 0);
		
		AccountConfig.testSwarmId = c.create(AccountConfig.generateRandomSwarmName(), false, "A test swarm for user " + AccountConfig.getConfiguration().getUsername());

		SwarmWSResponse response = c.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.PRODUCER,
				AccountConfig.XMPP_USERNAME, AccountConfig.getConfiguration().getResource());
		assertFalse(response.isError());
		// Resource 'web' is required so that http streaming will work
		response = c.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.CONSUMER, AccountConfig.XMPP_USERNAME, "web");
		assertFalse(response.isError());
		
		Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);
		
		//We should now have gotten a notification from the server and belong to the member swarm we just created.
		assertTrue(connector.getMemberSwarms().size() == 1);
		
		connector.interrupt();
		connector.shutdown();
	}
}
