package com.buglabs.bug.swarm.restclient;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.restclient.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.restclient.model.Invitation;

/**
 * Contract for the API defined at:
 * 
 * http://developer.bugswarm.net/restful_invitations.html
 * 
 * @author kgilmer
 *
 */
public interface ISwarmInviteClient {
	
	/**
	 * "to": "other username",
	 * "resource_id": "16f101010e80dd123b87363af759cc22cf49ff5f",
	 * "resource_type": "consumer",
	 * "description": "Hey. Come join my awesome swarm!"
	 * @return fully poluted invitation.
	 * @throws IOException on I/O or application error.
	 */
	Invitation send(String user, String resourceId, MemberType resourceType) throws IOException;
	
	/**
	 * Use this method to monitor the status of invitations sent to resources to join a given swarm.
	 * 
	 * @return List of Invitations
	 * @throws IOException on I/O or application error.
	 */
	List<Invitation> getSentInvitations() throws IOException;
	
	/**
	 * Use this method to keep track of all invitations you have received.
	 * 
	 * @return List of all invitations for user.
	 * @throws IOException on I/O error
	 */
	List<Invitation> getRecievedInvitations() throws IOException;
	
	/**
	 * Use this method to keep track of what invitations you have received for a specific resource.
	 * 
	 * @return List of received invitations for specific resource.
	 * @throws IOException on I/O error
	 */
	List<Invitation> getRecievedInvitations(String resourceId) throws IOException;
	
	/**
	 * At any point in time, users may choose to respond to pending invitations for a given resource. Use this method to either accept or reject these invitations.
	 * 
	 * @param acceptInvitation if true, will attempt to accept invitation, otherwise will reject.
	 * @return an Invitation type with response populated.
	 * @throws IOException on I/O error
	 */
	Invitation respond(boolean acceptInvitation) throws IOException;
}
