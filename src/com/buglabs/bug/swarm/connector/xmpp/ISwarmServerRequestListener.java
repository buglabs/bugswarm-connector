package com.buglabs.bug.swarm.connector.xmpp;

public interface ISwarmServerRequestListener {

	void feedListRequest(Jid requestJid, String swarmId);
}
