package com.buglabs.bug.swarm.client.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.touge.restclient.ReSTClient.ResponseDeserializer;

import com.buglabs.bug.swarm.client.ISwarmInviteClient.InvitationState;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient;
import com.buglabs.bug.swarm.client.ISwarmResourcesClient.MemberType;

/**
 * A Swarm Invitation is the mechanism by which users can advertise and associate with 3rd party swarms.
 * 
 * See http://developer.bugswarm.net/restful_invitations.html
 * 
 * @author kgilmer
 *
 */
public class Invitation extends ModelBase {

	/*
	 * This appears to be the complete set of fields for Invitation in the server:
	 * 
	 *  "accepted_at": "2011-10-10T03:28:56.083Z",
     *  "id": "b773a3c03b413d0f8398913aff719a00b7ef2d5"
     *  "from": "username",
     *  "swarm_id": "3a147bd1a79a7d075b8cd2f1d48db9719c1f6739",
     *  "description": "Hey. Come join my awesome swarm!",
     *  "resource_type": "producer",
     *  "resource_id": "052be4babe6efa128fa9a09997d4562250156aae"
     *  "to": "other username",
     *  "sent_at": "2011-10-10T03:12:11.773Z",
     *  "status": "accepted"
     *  
     *  This appears to be the minimal required set to send an invite:
     *  
	 *  "id": "fba0ca4bc9c63a964d9781c5487617e5f17dffd9",
	 *  "from": "username",
	 *  "swarm_id": "3a147bd1a79a7d075b8cd2f1d48db9719c1f6739",
	 *  "description": "Hey. Come join my awesome swarm!",
	 *  "resource_type": "consumer",
	 *  "resource_id": "16f101010e80dd123b87363af759cc22cf49ff5f",
	 *  "to": "other username",
	 *  "sent_at": "2011-10-10T03:12:11.773Z",
	 *  "status": "new"
	 */
	
	/**
	 * Deserializer for Invitation
	 */
	public static final ResponseDeserializer<Invitation> DESERIALIZER = new ResponseDeserializer<Invitation>() {

		@Override
		public Invitation deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return null;		
			
			JsonNode jn = objectMapper.readTree(input);
			
			return Invitation.deserialize(jn);
		}
		
	};
	
	/**
	 * Deserializer for lists of Invitation
	 */
	public static final ResponseDeserializer<List<Invitation>> LIST_DESERIALIZER = new ResponseDeserializer<List<Invitation>>() {

		@Override
		public List<Invitation> deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
			if (responseCode == 404)
				return Collections.emptyList();	
			
			List<Invitation> srml= new ArrayList<Invitation>();
			
			for (JsonNode jn : objectMapper.readTree(input))
				srml.add(Invitation.deserialize(jn));
			
			return srml;			
		}
		
	};
	
	private final String id;
	private final String description;
	private final ISwarmResourcesClient.MemberType type;
	private final String resourceId;
	private final String fromUser;
	private final String toUser;
	private final InvitationState status;
	private final String acceptedAt;
	private final String sentAt;
	
	/**
	 * @param id id of invitation
	 * @param description description of invitation
	 * @param type type of invitation
	 * @param resourceId resource id associated with invitation
	 * @param fromUser user originating invitation
	 * @param toUser target user of invitation
	 * @param status status of invitation
	 * @param sentAt date at which invitation was sent
	 */
	public Invitation(String id, String description, MemberType type, String resourceId, String fromUser, String toUser, InvitationState status,
			String sentAt) {
		this.id = id;
		this.description = description;
		this.type = type;
		this.resourceId = resourceId;
		this.fromUser = fromUser;
		this.toUser = toUser;
		this.status = status;
		this.acceptedAt = null;
		this.sentAt = sentAt;
	}
	
	
	protected static Invitation deserialize(JsonNode jn) {
		return new Invitation(
				jn.get("id").getTextValue(), 
				ModelBase.toStringSafely(jn.get("description")), 
				MemberType.valueOf(jn.get("resource_type").getTextValue().toUpperCase()), 
				jn.get("resource_id").getTextValue(), 
				jn.get("from").getTextValue(), 
				jn.get("to").getTextValue(), 
				InvitationState.valueOf(jn.get("status").getTextValue().toUpperCase()), 
				ModelBase.toStringSafely(jn.get("accepted_at")),
				jn.get("sent_at").getTextValue());
	}


	/**
	 * @param id id of invitation
	 * @param description description of invitation
	 * @param type type of invitation
	 * @param resourceId resource id associated with invitation
	 * @param fromUser user originating invitation
	 * @param toUser target user of invitation
	 * @param status status of invitation
	 * @param acceptedAt date when inviation was accepted
	 * @param sentAt date at which invitation was sent
	 */
	public Invitation(String id, String description, MemberType type, String resourceId, String fromUser, String toUser, InvitationState status,
			String acceptedAt, String sentAt) {
		this.id = id;
		this.description = description;
		this.type = type;
		this.resourceId = resourceId;
		this.fromUser = fromUser;
		this.toUser = toUser;
		this.status = status;
		this.acceptedAt = acceptedAt;
		this.sentAt = sentAt;
	}
	
	/**
	 * @return id of invitation.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @return description of invitation.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return type of membership associated with the invitation.
	 */
	public ISwarmResourcesClient.MemberType getType() {
		return type;
	}
	/**
	 * @return resource id of the invitation.
	 */
	public String getResourceId() {
		return resourceId;
	}
	/**
	 * @return originator of invitation
	 */
	public String getFromUser() {
		return fromUser;
	}
	/**
	 * @return target of invitation
	 */
	public String getToUser() {
		return toUser;
	}
	/**
	 * @return if invitation has been accepted or not.
	 */
	public InvitationState getStatus() {
		return status;
	}
	/**
	 * @return if invitation accepted, date which occurred.
	 */
	public String getAcceptedAt() {
		return acceptedAt;
	}
	/**
	 * @return date that invitation was sent.
	 */
	public String getSentAt() {
		return sentAt;
	}
}
