package com.buglabs.bug.swarm.client.test.participation;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmInviteClient.InvitationResponse;
import com.buglabs.bug.swarm.client.ISwarmInviteClient.InvitationState;
import com.buglabs.bug.swarm.client.ISwarmJsonMessageListener;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.ISwarmSession;
import com.buglabs.bug.swarm.client.ISwarmStringMessageListener;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.SwarmWSResponse;
import com.buglabs.bug.swarm.client.model.Configuration;
import com.buglabs.bug.swarm.client.model.Invitation;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;
import com.buglabs.bug.swarm.client.test.AccountConfig;

/**
 * Unit tests for ISwarmParticipationClient implementation.
 * 
 * @author kgilmer
 *
 */
public class ParticipationAPITests extends TestCase {
	
	private static final String description = "invite test description";
	
	volatile boolean psession1MessageRecieved = false;
	volatile boolean psession2MessageRecieved = false;
	volatile boolean psession1PresenceMessageRecieved = false;
	volatile boolean psession2PresenceMessageRecieved = false;
	volatile boolean psession1ExceptionRecieved = false;
	volatile boolean psession2ExceptionRecieved = false;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 * 
	 * For user 1:
	 * - Delete all existing swarms
	 * - Delete all existing resources
	 * - Create swarm, store id
	 * - Create resource, store id
	 * 
	 * For user 2:
	 * - Delete all existing swarms
	 * - Delete all existing resources
	 * - Create new resource, store id
	 * 
	 */
	@Override
	protected void setUp() throws Exception {
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
		
		//Determine that 2nd user can connect and delete any existing swarms.
		ISwarmClient client2 = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration2().getHostname(Configuration.Protocol.HTTP),
				AccountConfig.getConfiguration2().getConfingurationAPIKey());
		
		//Delete all pre-existing swarms owned by test user.	
		for (SwarmModel sm : client2.list()) {
			if (sm.getUserId().equals(AccountConfig.getConfiguration2().getUsername())) {
				client2.destroy(sm.getId());
			}
		}
		
		for (UserResourceModel ur : client2.getUserResourceClient().list())
			client2.getUserResourceClient().destroy(ur.getResourceId());
		
		urc = client2.getUserResourceClient().add(AccountConfig.generateRandomResourceName(), "user resource desc", "pc", 0, 0);
		AccountConfig.testUserResource2 = urc;
		
		//Confirm that original user still has swarm.
		assertTrue(client.list().size() == 1);
	}
	
	/**
	 * Test sending an invite.  Only confirms that server returns invite object as specified in documentation.
	 * 
	 * @throws IOException
	 */
	private void sendInvite(MemberType type) throws IOException {
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Configuration.Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		assertNotNull(client);
		assertNotNull(client.getSwarmInviteClient());
		assertNotNull(AccountConfig.testSwarmId);
		
		Invitation invite = client.getSwarmInviteClient().send(
				AccountConfig.testSwarmId, 
				AccountConfig.getConfiguration2().getUsername(), 
				AccountConfig.testUserResource2.getResourceId(), 
				type, 
				description);
		
		assertNotNull(invite);
		assertNotNull(invite.getId());
		assertNotNull(invite.getFromUser());
		assertNotNull(invite.getToUser());
		assertNotNull(invite.getResourceId());
		assertNotNull(invite.getStatus());
		assertTrue(invite.getStatus().equals(InvitationState.NEW));
		assertNotNull(invite.getDescription());
		assertTrue(invite.getDescription().equals(description));
		
		AccountConfig.testInviteId = invite.getId();
	}
	
	/**
	 * Test accepting an invitation.  
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void testSwarmParticipationAPI() throws IOException, InterruptedException {
		sendInvite(MemberType.PRODUCER);
		acceptInvitation();
		ISwarmClient client = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration().getHostname(Configuration.Protocol.HTTP),
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		UserResourceModel urc = client.getUserResourceClient().add("stream_resource", "stream resource desc", "pc", 0, 0);
		
		System.out.println("User 1 Key: " + AccountConfig.getConfiguration().getParticipationAPIKey().substring(0, 4));
 		ISwarmSession psession1 = SwarmClientFactory.createProductionSession(
				AccountConfig.getConfiguration().getHostname(Configuration.Protocol.HTTP),
				AccountConfig.getConfiguration().getParticipationAPIKey(),
				urc.getResourceId(), 
				AccountConfig.testSwarmId);
 		
 		psession1MessageRecieved = false;
		psession2MessageRecieved = false;
		psession1PresenceMessageRecieved = false;
		psession2PresenceMessageRecieved = false;
		psession1ExceptionRecieved = false;
		psession2ExceptionRecieved = false;
		
		psession1.addListener(new ISwarmJsonMessageListener() {
			
			@Override
			public void messageRecieved(Map<String, ?> payload, String fromSwarm, String fromResource, boolean isPublic) {
				System.out.print(fromSwarm);
				System.out.print(" ");
				System.out.print(fromResource);
				System.out.print(" ");
				System.out.println(payload);
				psession1MessageRecieved = true;
			}

			@Override
			public void exceptionOccurred(ExceptionType type, String message) {
				System.err.print(type.toString());
				System.err.print(" ");
				System.err.print(message);
				psession1ExceptionRecieved = true;
			}

			@Override
			public void presenceEvent(String fromSwarm, String fromResource, boolean isAvailable) {
				System.out.print(fromSwarm);
				System.out.print(" ");
				System.out.println(fromResource);
				psession1PresenceMessageRecieved = true;
			}
		});
		
		psession1.addListener(new ISwarmStringMessageListener() {
			
			@Override
			public void messageRecieved(String payload, String fromSwarm, String fromResource, boolean isPublic) {
				System.out.print(fromSwarm);
				System.out.print(" ");
				System.out.print(fromResource);
				System.out.print(" ");
				System.out.println(payload);
				psession1MessageRecieved = true;
			}

			@Override
			public void exceptionOccurred(ExceptionType type, String message) {
				System.err.print(type.toString());
				System.err.print(" ");
				System.err.print(message);
				psession1ExceptionRecieved = true;
			}

			@Override
			public void presenceEvent(String fromSwarm, String fromResource, boolean isAvailable) {
				System.out.print(fromSwarm);
				System.out.print(" ");
				System.out.println(fromResource);
				psession1PresenceMessageRecieved = true;
			}
		});
		
		psession1.join(AccountConfig.testSwarmId, urc.getResourceId());
		
		//Setup the second user.
		assertNotNull(AccountConfig.testUserResource2.getResourceId());
		System.out.println("User 2 Key: " + AccountConfig.getConfiguration2().getParticipationAPIKey().substring(0, 4));
		
		ISwarmSession psession2 = SwarmClientFactory.createProductionSession(
				AccountConfig.getConfiguration2().getHostname(Configuration.Protocol.HTTP),
				AccountConfig.getConfiguration2().getParticipationAPIKey(),
				AccountConfig.testUserResource2.getResourceId(), AccountConfig.testSwarmId);
		
		psession2.addListener(new ISwarmJsonMessageListener() {
			
			@Override
			public void messageRecieved(Map<String, ?> payload, String fromSwarm, String fromResource, boolean isPublic) {
				System.out.print(fromSwarm);
				System.out.print(" ");
				System.out.print(fromResource);
				System.out.print(" ");
				System.out.println(payload);
				psession2MessageRecieved = true;
			}

			@Override
			public void exceptionOccurred(ExceptionType type, String message) {
				System.err.print(type.toString());
				System.err.print(" ");
				System.err.print(message);
				
				psession2ExceptionRecieved = true;
			}

			@Override
			public void presenceEvent(String fromSwarm, String fromResource, boolean isAvailable) {
				System.out.print(fromSwarm);
				System.out.print(" ");
				System.out.println(fromResource);
				psession2PresenceMessageRecieved = true;
			}
		});
		
		psession2.join(AccountConfig.testSwarmId, AccountConfig.testUserResource2.getResourceId());
		psession2.send(AccountConfig.generateRandomPayload());
		
		Thread.sleep(10000);
		
		assertTrue(psession1PresenceMessageRecieved);
		//assertFalse(psession1MessageRecieved);
		assertFalse(psession1ExceptionRecieved);
		
		assertTrue(psession2PresenceMessageRecieved);
		//assertFalse(psession2MessageRecieved);
		assertFalse(psession2ExceptionRecieved);
		
		psession1.close();
		psession2.close();
		
		Thread.sleep(2000);
		assertFalse(psession1ExceptionRecieved);
		assertFalse(psession2ExceptionRecieved);
	}

	/**
	 * Test accepting an invitation.  
	 * @throws IOException
	 */
	private void acceptInvitation() throws IOException {
		ISwarmClient client2 = SwarmClientFactory.getSwarmClient(
				AccountConfig.getConfiguration2().getHostname(Configuration.Protocol.HTTP),
				AccountConfig.getConfiguration2().getConfingurationAPIKey());
		assertNotNull(client2);
		assertNotNull(client2.getSwarmInviteClient());
		assertNotNull(AccountConfig.testSwarmId);
		assertNotNull(AccountConfig.testInviteId);
		
		sendInvite(MemberType.PRODUCER);
		List<Invitation> receivedInvites = client2.getSwarmInviteClient().getRecievedInvitations(AccountConfig.testUserResource2.getResourceId());
		
		assertNotNull(receivedInvites);
		assertTrue(receivedInvites.isEmpty() == false);
		Invitation invite = null;
		
		for (Invitation i : receivedInvites)
			if (i.getId().equals(AccountConfig.testInviteId))
				invite = i;
		
		assertNotNull(invite);
		assertNotNull(invite.getId());
		assertNotNull(invite.getFromUser());
		assertNotNull(invite.getToUser());
		assertNotNull(invite.getResourceId());
		assertNotNull(invite.getStatus());
		assertTrue(invite.getStatus().equals(InvitationState.NEW));
		assertNotNull(invite.getDescription());
		assertTrue(invite.getDescription().equals(description));
		
		Invitation acceptInvite = client2.getSwarmInviteClient().respond(invite.getResourceId(), invite.getId(), InvitationResponse.ACCEPT);
		
		assertNotNull(acceptInvite);
		assertTrue(acceptInvite.getStatus().equals(InvitationState.ACCEPTED));
		assertNotNull(acceptInvite.getAcceptedAt());
	}
}
