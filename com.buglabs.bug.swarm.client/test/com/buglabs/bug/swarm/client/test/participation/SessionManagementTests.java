package com.buglabs.bug.swarm.client.test.participation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.ISwarmSession;
import com.buglabs.bug.swarm.client.ISwarmStringMessageListener;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.SwarmWSResponse;
import com.buglabs.bug.swarm.client.model.Configuration;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;
import com.buglabs.bug.swarm.client.test.AccountConfig;

/**
 * Tests for session management between swarm.client and the server.
 *
 */
public class SessionManagementTests extends TestCase {

	@Override
	protected void setUp() throws Exception {
		/*
		 * Create a swarm, a resource, and associate them.
		 */
		assertNotNull(AccountConfig.getConfiguration());
		assertNotNull(AccountConfig.getConfiguration2());
		
		
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Configuration.Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		//Delete all pre-existing swarms owned by test user.
		List<SwarmModel> swarms = client.list();
		
		for (SwarmModel sm : swarms) {
			if (sm.getUserId().equals(AccountConfig.getConfiguration().getUsername())) {
				client.destroy(sm.getId());
			}
		}		
		
		String id = client.create(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());
		AccountConfig.testSwarmId = id;
		
		UserResourceModel urc = client.getUserResourceClient().add(AccountConfig.generateRandomResourceName(), "user resource desc", "pc", 0, 0);
		AccountConfig.testUserResource = urc;
		
		SwarmWSResponse response = client.getSwarmResourceClient().add(AccountConfig.testSwarmId, MemberType.PRODUCER, urc.getResourceId());
		
		assertTrue(!response.isError());
	}
	
	/**
	 * Open a session, wait 30 seconds, confirm that it is still active.  Then close the session.
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void test30SecondSession() throws UnknownHostException, IOException, InterruptedException {
		assertNotNull(AccountConfig.getConfiguration().getHostname(Configuration.Protocol.HTTP));
		assertNotNull(AccountConfig.getConfiguration().getParticipationAPIKey());
		assertNotNull(AccountConfig.testUserResource.getResourceId());
		assertNotNull(AccountConfig.testSwarmId);
		
		ISwarmSession session = SwarmClientFactory.createProductionSession(
				AccountConfig.getConfiguration().getHostname(Configuration.Protocol.HTTP),
				AccountConfig.getConfiguration().getParticipationAPIKey(),
				AccountConfig.testUserResource.getResourceId(), 
				AccountConfig.testSwarmId);
		
		assertNotNull(session);
		
		assertTrue(session.isConnected());
		SessionTestListener tl = new SessionTestListener();
		session.addListener(tl);
		session.join(AccountConfig.testSwarmId, AccountConfig.testUserResource.getResourceId());
		
		Thread.sleep(30000);
		
		assertTrue(session.isConnected());
		assertTrue(tl.isPresenceReceived());
		
		session.close();
		
		assertFalse(session.isConnected());
	}
	
	/**
	 * Open a session, wait 3 minutes, confirm that it is still active.  Then close the session.
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 * //Disabling for now to speed up build.
	 */
	public void test3MinuteSession() throws UnknownHostException, IOException, InterruptedException {
		assertNotNull(AccountConfig.getConfiguration().getHostname(Configuration.Protocol.HTTP));
		assertNotNull(AccountConfig.getConfiguration().getParticipationAPIKey());
		assertNotNull(AccountConfig.testUserResource.getResourceId());
		assertNotNull(AccountConfig.testSwarmId);
		
		ISwarmSession session = SwarmClientFactory.createProductionSession(
				AccountConfig.getConfiguration().getHostname(Configuration.Protocol.HTTP),
				AccountConfig.getConfiguration().getParticipationAPIKey(),
				AccountConfig.testUserResource.getResourceId(), 
				AccountConfig.testSwarmId);
		
		assertNotNull(session);
		
		assertTrue(session.isConnected());
		SessionTestListener tl = new SessionTestListener();
		session.addListener(tl);
		session.join(AccountConfig.testSwarmId, AccountConfig.testUserResource.getResourceId());
		
		Thread.sleep(60000 * 3);
		
		assertTrue(session.isConnected());
		assertTrue(tl.isPresenceReceived());
		
		session.close();
		
		assertFalse(session.isConnected());
	}
	
	/**
	 * 
	 * A test listener that captures which messages have been sent from session.
	 *
	 */
	class SessionTestListener implements ISwarmStringMessageListener {

		private boolean presenceReceived;
		private boolean messageRecieved;
		private boolean exceptionReceived;
		
		public SessionTestListener() {
			messageRecieved = false;
			presenceReceived = false;
			exceptionReceived = false;
		}
		@Override
		public void messageRecieved(String payload, String fromSwarm, String fromResource, boolean isPublic) {
			messageRecieved = true;
		}

		@Override
		public void presenceEvent(String fromSwarm, String fromResource, boolean isAvailable) {
			presenceReceived = true;
		}

		@Override
		public void exceptionOccurred(ExceptionType type, String message) {
			exceptionReceived = true;
		}
		
		public boolean isPresenceReceived() {
			return presenceReceived;
		}
		public boolean isMessageRecieved() {
			return messageRecieved;
		}
		public boolean isExceptionReceived() {
			return exceptionReceived;
		}
	}
}
