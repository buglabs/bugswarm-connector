package com.buglabs.bug.swarm.connector.test;

import junit.framework.TestCase;

import org.json.simple.JSONArray;

import com.buglabs.bug.swarm.connector.model.Jid;
import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.xmpp.JSONElementCreator;
import com.buglabs.bug.swarm.connector.xmpp.SwarmXMPPClient;

/**
 * Test the Swarm-XMPP client
 * @author kgilmer
 *
 */
public class SwarmXMPPClientTests extends TestCase {

	/**
	 * Test the OSGi helper class that encapsulates OSGi service registory operations.
	 * 
	 * @throws Exception
	 */
	public void testOSGiHelper() throws Exception {
		boolean illegalStateThrown = false;
		
		try {
			OSGiHelper osgi = OSGiHelper.getRef();
		} catch (IllegalStateException e) {
			illegalStateThrown = true;
		}
		
		assertTrue(illegalStateThrown);		
	}
	
	
	/**
	 * Test creating, connecting, and disconnecting to XMPP server with client.
	 * 
	 * @throws Exception on any error
	 */
	public void testCreateXMPPClient() throws Exception {
		SwarmXMPPClient xmppClient = new SwarmXMPPClient(AccountConfig.getConfiguration());
		xmppClient.connect(new SwarmRequestListener());
		
		Thread.sleep(5000);
		
		assertTrue(xmppClient.isConnected());
		
		xmppClient.disconnect();
		
		assertFalse(xmppClient.isConnected());
	}
	
	public void testJidParsing() throws Exception {
		Jid j = new Jid("username", "hostname", "resource");
		
		assertTrue(j.getHostname().equals("hostname"));
		assertTrue(j.getUsername().equals("username"));
		assertTrue(j.getResource().equals("resource"));
		assertTrue(j.toString().equals("username@hostname/resource"));
		
		j = new Jid("username@hostname/resource");
		
		assertTrue(j.getHostname().equals("hostname"));
		assertTrue(j.getUsername().equals("username"));
		assertTrue(j.getResource().equals("resource"));
		assertTrue(j.toString().equals("username@hostname/resource"));
		
		boolean failed = false;
		try {
			j = new Jid(null);
		} catch (Exception e) {
			failed = true;
		}
		assertTrue(failed);
		
		failed = false;
		try {
			j = new Jid("asdf");
		} catch (Exception e) {
			failed = true;
		}
		assertTrue(failed);
		
		failed = false;
		try {
			j = new Jid("username@hostname");
		} catch (Exception e) {
			failed = true;
		}
		assertTrue(failed);
		
		failed = false;
		try {
			j = new Jid("hostname/resource");
		} catch (Exception e) {
			failed = true;
		}
		assertTrue(failed);
	}
}
