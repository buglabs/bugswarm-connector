package com.buglabs.bug.swarm.client.test.configuration;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.client.ISwarmKeysClient;
import com.buglabs.bug.swarm.client.ISwarmKeysClient.KeyType;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.model.Configuration.Protocol;
import com.buglabs.bug.swarm.client.model.SwarmKey;
import com.buglabs.bug.swarm.client.test.AccountConfig;


/**
 * Tests for the Swarm API Keys WS API.
 * 
 * @author kgilmer
 *
 */
public class SwarmAPIKeyWSAPITests extends TestCase {
		
	@Override
	protected void setUp() throws Exception {
	
	}
	
	@Override
	protected void tearDown() throws Exception {
		
	}
	
	/**
	 * Test that client can add keys.
	 * @throws IOException 
	 */
	public void testAddKeys() throws IOException {
		assertNotNull(AccountConfig.getConfiguration().getHostname(Protocol.HTTP));
		
		String hostname = AccountConfig.getConfiguration().getHostname(Protocol.HTTP);
		String username = "connector_test";
		String password = "connector_test";
		
		ISwarmKeysClient client = SwarmClientFactory.getAPIKeyClient(hostname);
		
		assertNotNull(client);
		
		List<SwarmKey> keys = client.create(username, password, null);
		
		assertNotNull(keys);
		assertTrue(keys.size() == 2);
		
		for (SwarmKey key : keys) {
			System.out.println(key.toString());
			assertNotNull(key.getCreatedAt());
			assertNotNull(key.getKey());
			assertNotNull(key.getUserId());
			assertNotNull(key.getType());
			assertTrue(key.isActive());
		}		
		
		keys = client.create(username, password, KeyType.CONFIGURATION);
		
		assertNotNull(keys);
		assertTrue(keys.size() == 1);
		
		for (SwarmKey key : keys) {
			System.out.println(key.toString());
			assertNotNull(key.getCreatedAt());
			assertNotNull(key.getKey());
			assertNotNull(key.getUserId());
			assertNotNull(key.getType() == KeyType.CONFIGURATION);
			assertTrue(key.isActive());
		}		
	
		keys = client.create(username, password, KeyType.PARTICIPATION);
		
		assertNotNull(keys);
		assertTrue(keys.size() == 1);
		
		for (SwarmKey key : keys) {
			System.out.println(key.toString());
			assertNotNull(key.getCreatedAt());
			assertNotNull(key.getKey());
			assertNotNull(key.getUserId());
			assertNotNull(key.getType() == KeyType.PARTICIPATION);
			assertTrue(key.isActive());
		}	
	}
	
	/**
	 * Test that client can list keys.
	 * @throws IOException 
	 */
	public void testListKeys() throws IOException {		
		assertNotNull(AccountConfig.getConfiguration().getHostname(Protocol.HTTP));
		
		String hostname = AccountConfig.getConfiguration().getHostname(Protocol.HTTP);
		String username = "connector_test";
		String password = "connector_test";
		
		ISwarmKeysClient client = SwarmClientFactory.getAPIKeyClient(hostname);
		
		assertNotNull(client);
		
		List<SwarmKey> keys = client.list(username, password, null);
		
		assertNotNull(keys);
		assertTrue(keys.size() > 1);
		
		for (SwarmKey key : keys) {
			System.out.println(key.toString());
			assertNotNull(key.getCreatedAt());
			assertNotNull(key.getKey());
			assertNotNull(key.getUserId());
			assertNotNull(key.getType());
			assertTrue(key.isActive());
		}		
	
	}
}
