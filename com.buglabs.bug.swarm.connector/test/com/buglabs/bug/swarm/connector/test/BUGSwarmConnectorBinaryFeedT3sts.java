package com.buglabs.bug.swarm.connector.test;

import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.SwarmWSResponse;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.connector.BUGSwarmConnector;
import com.buglabs.bug.swarm.connector.Configuration.Protocol;
import com.buglabs.util.simplerestclient.HTTPException;

/**
 * Tests the BUGSwarmConnector class in regards to binary feeds.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnectorBinaryFeedT3sts extends TestCase {

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
		BUGSwarmConnector connector = new BUGSwarmConnector(Activator.getDefault().getContext(), AccountConfig.getConfiguration());

		connector.start();
		Thread.sleep(AccountConfig.CONNECTOR_INIT_SLEEP_MILLIS);

		assertTrue(connector.isInitialized());

		connector.interrupt();
		connector.shutdown();

		Thread.sleep(AccountConfig.CONNECTOR_FEED_CHANGE_SLEEP_MILLIS);
	}
}
