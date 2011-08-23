package com.buglabs.bug.swarm.connector.test;

import org.jivesoftware.smack.Chat;

import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.xmpp.ISwarmServerRequestListener;

public class SwarmRequestListener implements ISwarmServerRequestListener {
	
	@Override
	public void feedListRequest(Jid jid, String swarmId) {
		System.out.println("feedListRequest() " + jid + " " + swarmId);
	}

	@Override
	public void feedListRequest(Chat chat, String swarmId) {
		System.out.println("feedListRequest() " + chat.getParticipant() + " " + swarmId);
	}

	@Override
	public void feedRequest(Jid jid, String swarmId, String feedRequestName) {
		System.out.println("feedRequest() " + jid + " " + swarmId);
	}

	@Override
	public void swarmInviteRequest(Jid sender, String roomId) {
		System.out.println("swarmInviteRequest() " + sender + " " + roomId);
	}
}
