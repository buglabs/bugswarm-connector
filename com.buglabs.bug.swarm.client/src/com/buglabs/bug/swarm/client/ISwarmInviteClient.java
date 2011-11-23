package com.buglabs.bug.swarm.client;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;
import com.buglabs.bug.swarm.client.model.Invitation;

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
	 * An invitation sent to a resource can be accepted or rejected by the owner (user) of that resource.
	 */
	public enum InvitationResponse {
		/**
		 * A response to accept an invitation.
		 */
		ACCEPT("accept"), 
		/**
		 * A response to reject an invitation.
		 */
		REJECT("reject");

		/**
		 * Name of member.
		 */
		private final String name;

		/**
		 * @param name
		 *            of member
		 */
		private InvitationResponse(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * An invitation sent to a resource can be accepted or rejected by the owner (user) of that resource.
	 */
	public enum InvitationState {
		/**
		 * Accepted state.
		 */
		ACCEPTED("accepted"), 
		/**
		 * Rejected state.
		 */
		REJECTED("rejected"), 
		/**
		 * New or undecided state.
		 */
		NEW("new");

		/**
		 * Name of member.
		 */
		private final String name;

		/**
		 * @param name
		 *            of member
		 */
		private InvitationState(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * "to": "other username",
	 * "resource_id": "16f101010e80dd123b87363af759cc22cf49ff5f",
	 * "resource_type": "consumer",
	 * "description": "Hey. Come join my awesome swarm!"
	 * @param swarmId swarm id
	 * @param user user id
	 * @param resourceId resource id
	 * @param resourceType resource type
	 * @param description description or null for no description
	 * @return fully populated invitation.
	 * @throws IOException on I/O or application error.
	 */	
	Invitation send(String swarmId, String user, String resourceId, MemberType resourceType, String description) throws IOException;
	
	/**
	 * Use this method to monitor the status of invitations sent to resources to join a given swarm.
	 * 
	 * @param swarmId swarm id
	 * @return List of Invitations
	 * @throws IOException on I/O or application error.
	 */
	List<Invitation> getSentInvitations(String swarmId) throws IOException;
	
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
	 * @param resourceId resource id
	 * @return List of received invitations for specific resource.
	 * @throws IOException on I/O error
	 */
	List<Invitation> getRecievedInvitations(String resourceId) throws IOException;
	
	/**
	 * At any point in time, users may choose to respond to pending invitations for a given resource. Use this method to either accept or reject these invitations.
	 * 
	 * @param resourceId resource id
	 * @param invitationId invitation id
	 * @param action InvitationResponse of intended action
	 * @return an Invitation type with response populated.
	 * @throws IOException on I/O error
	 */
	Invitation respond(String resourceId, String invitationId, InvitationResponse action) throws IOException;

}
