package com.buglabs.bug.swarm.connector.test;

import java.util.Arrays;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Tests the high-level BUGSwarmConnector class in regards to feeds.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmConnectorNativeT3sts extends TestCase {

	
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
}