package com.buglabs.bug.swarm.connector.xmpp;

/**
 * Represents an XMPP JID (user, host, resource).
 * 
 * @author kgilmer
 *
 */
public class Jid {

	private final String username;
	private final String hostname;
	private final String resource;

	/**
	 * Construct a JID with the specific elements.
	 * 
	 * @param username user
	 * @param hostname host
	 * @param resource resource
	 */
	public Jid(final String username, final String hostname, final String resource) {
		this.username = username;
		this.hostname = hostname;
		this.resource = resource;
	
	}

	/**
	 * Construct a JID from a String.
	 * 
	 * @param rawJid JID in format of 'username@hostname/resource'
	 * @throws Exception thrown on parse exception
	 */
	public Jid(final String rawJid) throws Exception {
		String [] elems = rawJid.split("@");
		
		if (elems.length < 2)
			throw new Exception("Invalid raw Jid: " + rawJid);
		
		this.username = elems[0];
		
		//String may contain multiple "@" chars, so only parse from the first occurrance.
		elems = rawJid.substring(username.length() + 1).split("/");
		
		if (elems.length != 2)
			throw new Exception("Invalid raw Jid: " + rawJid);
		
		this.hostname = elems[0];
		this.resource = elems[1];
	}

	/**
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return host name
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @return resource
	 */
	public String getResource() {
		return resource;
	}

	@Override
	public String toString() {
		return username + "@" + hostname + "/" + resource;
	}
}
