package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.SwarmWSResponse;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;
import com.buglabs.bug.swarm.connector.BUGSwarmConnector;
import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.util.simplerestclient.HTTPException;

/**
 * Tests the high-level BUGSwarmConnector notification functionality, or, any logic in which unplanned events come from the
 * server and the connector has to change in some way.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnectorNotificationT3sts extends TestCase {

	@Override
	protected void setUp() throws Exception {
		System.out.println("setUp()");
		ISwarmClient c = SwarmClientFactory.getSwarmClient(
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
	 * https://github.com/buglabs/bugswarm/wiki/Swarm-Feeds-API
	 * 
	 * Test 'Querying available Feeds in a Swarm'
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void testGetNotificationAfterFirstSwarmJoin() throws IOException, InterruptedException {
		ISwarmClient c =  SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP), 
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		BUGSwarmConnector connector = new BUGSwarmConnector(AccountConfig.getConfiguration());

		// Start the connector
		connector.start();

		// Wait for the feeds to initialize
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		//At this point the connector is initialized with no swarms.  We will now create a swarm and associate it with this user.
		//The server should notify connector that a new swarm should be joined.
		assertTrue(connector.getMemberSwarms().size() == 0);
		
		AccountConfig.testSwarmId = c.create(AccountConfig.generateRandomSwarmName(), false, "A test swarm for user " + AccountConfig.getConfiguration().getUsername());
		
		UserResourceModel userResource = c.getUserResourceClient().add(AccountConfig.generateRandomResourceName(), "test resource", "pc", 0, 0);
		assertNotNull(userResource);
		AccountConfig.testUserResource = userResource;
		
		SwarmWSResponse response = c.getSwarmResourceClient().add(
				AccountConfig.testSwarmId, 
				MemberType.PRODUCER,
				userResource.getResourceId());
		
		assertFalse(response.isError());		
		
		Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);
		
		//We should now have gotten a notification from the server and belong to the member swarm we just created.
		assertTrue(connector.getMemberSwarms().size() == 1);
		
		connector.interrupt();
		connector.shutdown();
	}
}
