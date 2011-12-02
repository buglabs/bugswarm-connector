package com.buglabs.bug.swarm.client.test;

import java.io.IOException;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmConfiguration;
import com.buglabs.bug.swarm.client.ISwarmInviteClient.InvitationResponse;
import com.buglabs.bug.swarm.client.ISwarmInviteClient.InvitationState;
import com.buglabs.bug.swarm.client.ISwarmKeysClient;
import com.buglabs.bug.swarm.client.ISwarmKeysClient.KeyType;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.model.Configuration;
import com.buglabs.bug.swarm.client.model.Configuration.Protocol;
import com.buglabs.bug.swarm.client.model.Invitation;
import com.buglabs.bug.swarm.client.model.SwarmKey;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;

/**
 * Abstract base class that sets up a swarm test environment with:
 * 
 * connector_test user
 * connector_test2 user
 * 
 * generated API Keys for connector_test
 * generated API Keys for connector_test2
 * 
 * generated resource for connector_test
 * generated resource for connector_test2
 * 
 * swarm created by connector_test
 * invite created and sent to connector_test2
 * invite accepted by connector_test2 as consumer
 * 
 * After setUp() has been run, the configuration will be loaded with the following values:
 * 
 * AccountConfig.testSwarmId
 * AccountConfig.testUserResource
 * AccountConfig.testUserResource2
 * 
 * @author kgilmer
 *
 */
public abstract class TwoParticipantsOneSwarmTestCase extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		//Create configuration and APIKeys
		Configuration c1 = AccountConfig.getConfiguration();
		Configuration c2 = AccountConfig.getConfiguration2();
		
		if (!validKeys(c1, c2)) {
			createAPIKeys(c1, c2);
		}
		
		assertTrue(validKeys(c1, c2));		
		
		//Clear any existing state for test users				
		deleteExistingSwarms(c1, c2);
		deleteExistingResources(c1, c2);
		
		//Create new test state
		ISwarmConfiguration cclient1 = SwarmClientFactory.getSwarmConfigurationClient(c1.getHostname(Protocol.HTTP), c1.getConfingurationAPIKey());
		ISwarmConfiguration cclient2 = SwarmClientFactory.getSwarmConfigurationClient(c2.getHostname(Protocol.HTTP), c2.getConfingurationAPIKey());
		
		UserResourceModel user1resource = cclient1.createResource(AccountConfig.generateRandomResourceName(), "Test Resource", "pc", 0, 0);
		UserResourceModel user2resource = cclient2.createResource(AccountConfig.generateRandomResourceName(), "Test Resource 2", "pc", 0, 0);		
				
		String testSwarm = cclient1.createSwarm(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());		
		
		cclient1.addResource(testSwarm, MemberType.PRODUCER, user1resource.getResourceId());
		AccountConfig.testSwarmId = testSwarm;
		AccountConfig.testUserResource = user1resource;
		AccountConfig.testUserResource2 = user2resource;
		
		//Send and accept invitation so user 2 joins test swarm.		
		Invitation invite = cclient1.send(testSwarm, user2resource.getUserId(), user2resource.getResourceId(), MemberType.CONSUMER, "Test Invite");
		assertTrue(invite.getStatus() == InvitationState.NEW);
		
		for (Invitation i : cclient2.getRecievedInvitations(user2resource.getResourceId()))
			cclient2.respond(i.getResourceId(), i.getId(), InvitationResponse.ACCEPT);
		
		//Validate that both users are members of test swarm.		
		boolean creatorInSwarm = false;
		for (SwarmModel sm : cclient1.listSwarms())
			if (sm.getId().equals(testSwarm))
				creatorInSwarm = true;
		
		boolean consumerInSwarm = false;
		for (SwarmModel sm : cclient2.listSwarms())
			if (sm.getId().equals(testSwarm))
				consumerInSwarm = true;
		
		assertTrue(creatorInSwarm);
		assertTrue(consumerInSwarm);
	}

	/**
	 * Delete all resources for given configurations.
	 * @param configs
	 * @throws IOException
	 */
	protected void deleteExistingResources(Configuration ... configs) throws IOException {
		for (Configuration c : configs) {
			ISwarmConfiguration cclient1 = SwarmClientFactory.getSwarmConfigurationClient(c.getHostname(Protocol.HTTP), c.getConfingurationAPIKey());
			
			for (UserResourceModel urm : cclient1.listResource())
				cclient1.removeResource(urm.getResourceId());
		}
		
	}

	/**
	 * Verify a set of configurations has valid keys.
	 * @param configs
	 */
	protected boolean validKeys(Configuration ... configs) {
		for (Configuration c : configs) {
			if (c.getConfingurationAPIKey() == null)
				return false;
			if (c.getConfingurationAPIKey().trim().length() == 0)
				return false;
			if (c.getParticipationAPIKey() == null)
				return false;
			if (c.getParticipationAPIKey().trim().length() == 0)
				return false;
		}
		
		return true;
	}

	/**
	 * Delete all swarms for a given user.
	 * @param c
	 * @throws IOException
	 */
	private void deleteExistingSwarms(Configuration ... configs) throws IOException {
		for (Configuration c : configs) {
			ISwarmClient client = SwarmClientFactory.getSwarmClient(
					c.getHostname(Protocol.HTTP), 
					c.getConfingurationAPIKey());
			
			for (SwarmModel sm : client.list())
				client.destroy(sm.getId());
		}
	}


	/**
	 * @param username
	 * @return configuration key, participation key
	 * @throws IOException 
	 */
	protected void createAPIKeys(Configuration ... configs) throws IOException {
		
		for (Configuration conf : configs) {
			ISwarmKeysClient keyClient = 
					SwarmClientFactory.getAPIKeyClient(conf.getHostname(Protocol.HTTP));
	
			SwarmKey pkey = keyClient.create(conf.getUsername(), conf.getUsername(), KeyType.PARTICIPATION).get(0);
			SwarmKey ckey = keyClient.create(conf.getUsername(), conf.getUsername(), KeyType.CONFIGURATION).get(0);
	
			conf.setConfingurationAPIKey(ckey.getKey());
			conf.setParticipationAPIKey(pkey.getKey());
		}
	}
}
