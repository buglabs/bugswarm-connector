package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmConfiguration;
import com.buglabs.bug.swarm.client.ISwarmSession;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;
import com.buglabs.bug.swarm.connector.Configuration;
import com.buglabs.bug.swarm.connector.Configuration.Protocol;

/**
 * Tests the high-level BUGSwarmConnector test environment.
 * 
 * @author kgilmer
 * 
 */
public class T3stEnvironmentValidationT3sts extends TestCase {

	
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
		assertNotNull(System.getProperty("report.misc"));		
	}
	
	/**
	 * Create configuration client.
	 * @throws IOException 
	 */
	public void testCreateConfigurationClient() throws IOException {		
		ISwarmClient wsClient = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP), 
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		assertNotNull(wsClient);
		
		List<SwarmModel> swarms = wsClient.list();
		
		assertNotNull(swarms);
		
		ISwarmConfiguration client2 = SwarmClientFactory.getSwarmConfigurationClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP), 
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		assertNotNull(client2);
		
		List<UserResourceModel> resources = client2.listResource();
		
		assertNotNull(resources);
	}
	
	/**
	 * Can create the participation client.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void testCreateParticipationClient() throws UnknownHostException, IOException {
		Configuration c = AccountConfig.getConfiguration();
		ISwarmConfiguration cclient = SwarmClientFactory.getSwarmConfigurationClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP), 
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		List<UserResourceModel> existingResources = cclient.getResources();
		
		assertNotNull(existingResources);
		
		UserResourceModel urm = null;
		if (existingResources.size() == 0) {
			urm = cclient.createResource(
					AccountConfig.generateRandomResourceName(), 
					AccountConfig.getTestSwarmDescription(), 
					"pc", 0, 0);
		} else {
			urm = existingResources.get(0);
		}
		
		List<SwarmModel> swarms = cclient.listSwarms();
		
		assertNotNull(swarms);
		
		String testSwarmId = null;
		if (swarms.size() == 0) {
			testSwarmId = cclient.createSwarm(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());
		} else {
			testSwarmId = swarms.get(0).getId();
		}
			
		
		ISwarmSession psession = SwarmClientFactory.createSwarmSession(
				c.getHostname(Protocol.HTTP), 
				c.getParticipationAPIKey(), 
				urm.getResourceId(), testSwarmId);
		
		assertNotNull(psession);
		assertTrue(psession.isConnected());
	}
}