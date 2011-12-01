package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.buglabs.bug.swarm.client.ISwarmSession;
import com.buglabs.bug.swarm.client.ISwarmStringMessageListener;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.model.Configuration;
import com.buglabs.bug.swarm.client.model.Configuration.Protocol;
import com.buglabs.bug.swarm.connector.osgi.Activator;


/**
 * Tests the high-level BUGSwarmConnector class in regards to feeds.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnectorFeedTests extends TwoParticipantsOneSwarmTestCase {
	
	/**
	 * Test that connector/producer can create a feed and the consumer receives notification of the new feed.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public void testConsumerReceivesNewFeedAnnouncement() throws UnknownHostException, IOException {
		Configuration c1 = AccountConfig.getConfiguration();
		Configuration c2 = AccountConfig.getConfiguration2();
		
		//Listen to swarm
		ISwarmSession session = SwarmClientFactory.createConsumptionSession(
				c1.getHostname(Protocol.HTTP), c1.getParticipationAPIKey(), AccountConfig.testUserResource2.getResourceId(), AccountConfig.testSwarmId);
		
		assertNotNull(session);
		session.addListener(new TestListener());
		
		//Create new feed
		Map<String, Object> feed = new HashMap<String, Object>();
		loadRandomValues(feed);
				
		//Register feed
		BundleContext context = Activator.getContext();
		assertNotNull(context);
		ServiceRegistration sr = context.registerService(Map.class.getName(), feed, getServiceProperties());
		
		//Request feed
		//session.
		
		//Cleanup
		sr.unregister();
	}
	
	private Dictionary getServiceProperties() {
		Dictionary d = new Hashtable();
		
		d.put("SWARM.FEED.NAME", "test-feed");
		d.put("SWARM.FEED.TIMESTAMP", System.currentTimeMillis());
		
		return d;
	}

	private void loadRandomValues(Map<String, Object> feed) {
		Random r = new Random();
		
		int l = r.nextInt(50) + 50;
		
		for (int i = 0; i < l; ++i)
			feed.put("key-" + i, r.nextDouble());
	}

	private class TestListener implements ISwarmStringMessageListener {
		private boolean presence = false;
		private boolean exception = false;
		
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

		@Override
		public void messageRecieved(String payload, String fromSwarm, String fromResource, boolean isPublic) {
			
		}
	}
}
