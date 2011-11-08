package com.buglabs.bug.swarm.client.example;
import java.io.IOException;
import java.util.Random;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmInviteClient;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient;
import com.buglabs.bug.swarm.client.IUserResourceClient;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.ISwarmInviteClient.InvitationResponse;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.model.Invitation;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.SwarmResourceModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;

//## This file illustrates how to use the swarm.restclient library for the configuration side of BUGswarm
public class Example {

	public static void main(String[] args) throws IOException {
		//## Get an instance of the root client using the factory:
		//A default error handler is set that will throw IOException on any non-application HTTP error.
		ISwarmClient client = SwarmClientFactory.getSwarmClient("api.test.bugswarm.net", "3077514aa9aa5a5826cfd9d04ee059db1a18057d");
		
		//## Create a new swarm:
		String id = client.create("my sweet swarm", true, "my swarm description");
		
		//## Get the details of my new swarm:
		SwarmModel swarm = client.get(id);
		System.out.println(swarm.getCreatedAt());
		
		//## Get a list of my member swarms:
		//By convention, the client will never return null to list operations but rather empty lists.  So, checking for null is not required.
		System.out.println("I belong to...");
		for (SwarmModel sm : client.list())
			System.out.println(sm.getId());
		
		//## Create a new resource:
		//Note that the clients are partitioned along the logical separations of the API specification.
		IUserResourceClient resourceClient = client.getUserResourceClient();
		UserResourceModel resource = resourceClient.add("my-resource", "a sweet swarm resource.", "pc", 0, 0);
		
		System.out.println("My new resource id is " + resource.getResourceId());
		
		//## List all of my resources
		System.out.println("I have these resources...");
		for (UserResourceModel urm : resourceClient.list())
			System.out.println(urm.getName() + " - " + urm.getDescription());
		
		//## Invite someone to my swarm:
		ISwarmInviteClient inviteClient = client.getSwarmInviteClient();
		//Note this will fail if run since the supplied user and resource are not valid.
		Invitation invite = inviteClient.send(swarm.getId(), "some-user", "some-resource", MemberType.CONSUMER, "hey man join my sweet swarm.");
		
		//## List my invitations:
		System.out.println("I have invited these users...");
		for (Invitation i : inviteClient.getSentInvitations(swarm.getId()))
			System.out.println(i.getToUser());
		
		//## Check to see if anyone wants me in their swarms:
		System.out.println("I will randomly accept and reject my invites...");
		Random r = new Random();
		
		for (Invitation i : inviteClient.getRecievedInvitations())
			if (r.nextBoolean())
				inviteClient.respond(i.getResourceId(), i.getId(), InvitationResponse.ACCEPT);
			else
				inviteClient.respond(i.getResourceId(), i.getId(), InvitationResponse.REJECT);
		
		//## Show what resources are in my swarm:
		ISwarmResourcesClient swarmResourceClient = client.getSwarmResourceClient();
		
		System.out.println("Here is who is in my swarms...");
		for (SwarmResourceModel srm : swarmResourceClient.list(swarm.getId(), MemberType.CONSUMER))
			System.out.println(srm.getUserId() + " - " + srm.getResourceId());
	}
}
