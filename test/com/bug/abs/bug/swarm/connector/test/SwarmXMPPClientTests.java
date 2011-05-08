package com.bug.abs.bug.swarm.connector.test;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.connector.osgi.OSGiHelper;
import com.buglabs.bug.swarm.connector.xmpp.XMLDocCreator;
import com.buglabs.util.XmlNode;

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
		//This class requires an OSGi context to run correctly but will try to mock the service registry when running in a plain JVM context.
		OSGiHelper osgi = OSGiHelper.getRef();
		
		assertNotNull(osgi);
		
		assertNotNull(osgi.getBUGFeeds());
		assertNotNull(osgi.getBUGModules());
		assertNotNull(osgi.getBUGServices());
	}
	
	
	/**
	 * Test the XMLDocCreator class that creates all XML documents destined for the swarm server.
	 * 
	 * @throws Exception
	 */
	public void testXmlDocCreator() throws Exception {
		OSGiHelper osgi = OSGiHelper.getRef();
		
		//Create the document.
		XmlNode xd = XMLDocCreator.createServiceModuleFeedDocument(
				osgi.getBUGServices(), 
				osgi.getBUGModules(), 
				osgi.getBUGFeeds());
		
		//Verify it's what we expect
		assertTrue(xd != null);
		assertTrue(xd.childExists("services"));
		assertTrue(xd.childExists("modules"));
		assertTrue(xd.childExists("feeds"));
		
	}
}
