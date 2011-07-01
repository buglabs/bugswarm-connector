package com.buglabs.bug.swarm.connector.test;

import java.util.Arrays;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.buglabs.bug.swarm.connector.ws.SwarmWSClient;

/**
 * Tests the high-level BUGSwarmConnector class in regards to feeds.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnectorNativeT3sts extends TestCase {

	
	/**
	 * Test that the bugswarm-connector bundle is installed and running in the OSGi framework instance.
	 * 
	 * @throws InterruptedException
	 */
	public void testConnectorAvailable() throws InterruptedException {
		
		assertNotNull(Activator.getDefault());
		assertNotNull(Activator.getDefault().getContext());
		
		BundleContext context = Activator.getDefault().getContext();
		
		
		Bundle swarmBundle = null;
		for (Bundle bundle : Arrays.asList(context.getBundles()))
			if (bundle.getHeaders().get("Bundle-SymbolicName") != null && bundle.getHeaders().get("Bundle-SymbolicName").equals("com.buglabs.bug.swarm.connector"))
				swarmBundle = bundle;
				
		assertNotNull(swarmBundle);
				
		assertTrue(swarmBundle.getState() == Bundle.ACTIVE);
	}
	
	/**
	 * Determine if system properties have enough information to run tests.
	 */
	public void testSystemBUGSwarmPropertiesAvailable() {
		assertNotNull(System.getProperty("com.buglabs.bugswarm.hostname"));
		assertNotNull(System.getProperty("com.buglabs.bugswarm.apikey"));
		assertNotNull(System.getProperty("com.buglabs.bugswarm.username"));
	}
	
	/**
	 * Create a swarm, associate this device to the swarm.
	 */
	public void testCreateAssociateSwarmToBUG() {
		String host = System.getProperty("com.buglabs.bugswarm.hostname");
		String apikey = System.getProperty("com.buglabs.bugswarm.apikey");
		SwarmWSClient wsClient = new SwarmWSClient("http://ws." + host, apikey);
		
		assertTrue(wsClient.isValid() == null);
	}
}