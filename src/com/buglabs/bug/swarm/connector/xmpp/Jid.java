package com.buglabs.bug.swarm.connector.xmpp;

public class Jid {

	private final String username;
	private final String hostname;
	private final String resource;

	public Jid(String username, String hostname, String resource) {
		this.username = username;
		this.hostname = hostname;
		this.resource = resource;
	
	}

	public String getUsername() {
		return username;
	}

	public String getHostname() {
		return hostname;
	}

	public String getResource() {
		return resource;
	}

	@Override
	public String toString() {
		return username + "@" + hostname + "/" + resource;
	}
}
