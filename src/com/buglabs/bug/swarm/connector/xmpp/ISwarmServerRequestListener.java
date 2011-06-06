package com.buglabs.bug.swarm.connector.xmpp;

public interface ISwarmServerRequestListener {

	void feedListRequest(String requestJid, String swarmId);
}
