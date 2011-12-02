package com.buglabs.bug.swarm.client.test.participation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.buglabs.bug.swarm.client.ISwarmSession;
import com.buglabs.bug.swarm.client.ISwarmStringMessageListener;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.model.Configuration;
import com.buglabs.bug.swarm.client.model.Configuration.Protocol;
import com.buglabs.bug.swarm.client.test.AccountConfig;
import com.buglabs.bug.swarm.client.test.TwoParticipantsOneSwarmTestCase;

/**
 * Various tests for swarm feeds using Participation client.
 * @author kgilmer
 *
 */
public class FeedTests extends TwoParticipantsOneSwarmTestCase {

	private static final Random rnd = new Random();
	/**
	 * Basic test for producer client sending a feed that a consumer client receives.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void testFeedSendReceive() throws UnknownHostException, IOException, InterruptedException {
		Configuration ac1 = AccountConfig.getConfiguration();
		Configuration ac2 = AccountConfig.getConfiguration2();
		
		ISwarmSession produceClient = SwarmClientFactory.createConsumptionSession(
				ac1.getHostname(Protocol.HTTP), ac1.getParticipationAPIKey(), AccountConfig.testUserResource.getResourceId(), AccountConfig.testSwarmId);
		
		ISwarmSession consumeClient = SwarmClientFactory.createConsumptionSession(
				ac2.getHostname(Protocol.HTTP), ac2.getParticipationAPIKey(), AccountConfig.testUserResource2.getResourceId(), AccountConfig.testSwarmId);
		
		assertNotNull(produceClient);
		assertNotNull(consumeClient);
		
		TestListener consumerListener = new TestListener();
		consumeClient.addListener(consumerListener);
		
		TestListener producerListener = new TestListener();
		produceClient.addListener(producerListener);
		
		produceClient.send(generateRandomPayload());
		
		Thread.sleep(1000);
		
		assertTrue(consumerListener.getMessage());
		
		produceClient.close();
		consumeClient.close();
	}
	
	private Map<String, ?> generateRandomPayload() {
		Map<String, Object> m = new HashMap<String, Object>();
		
		int l = rnd.nextInt(5) + 5;
		
		for (int i = 0; i < l; ++i)
			m.put("key-" + i, rnd.nextDouble());
		
		return m;
	}

	private class TestListener implements ISwarmStringMessageListener {
		private boolean presence = false;
		private boolean exception = false;
		private boolean message = false;
		
		@Override
		public void presenceEvent(String fromSwarm, String fromResource, boolean isAvailable) {
			presence = true;
		}

		@Override
		public void exceptionOccurred(ExceptionType type, String message) {
			exception = true;
		}
		
		public boolean getPresence() {
			return presence;
		}
		
		public boolean getException() {
			return exception;
		}
		
		public boolean getMessage() {
			return message;
		}

		@Override
		public void messageRecieved(String payload, String fromSwarm, String fromResource, boolean isPublic) {
			message  = true;
		}
	}
}
