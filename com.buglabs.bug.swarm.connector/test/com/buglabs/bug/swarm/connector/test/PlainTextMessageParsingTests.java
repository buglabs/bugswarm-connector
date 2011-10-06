package com.buglabs.bug.swarm.connector.test;

import com.buglabs.bug.swarm.connector.xmpp.parse.InviteMessageImpl;
import com.buglabs.bug.swarm.connector.xmpp.parse.XMPPPlainTextMessageParser;
import com.buglabs.bug.swarm.connector.xmpp.parse.XMPPPlainTextMessageParser.XMPPMessage;

import junit.framework.TestCase;

/**
 * Tests for the parsing of plain-text messages originating from the XMPP server intended for (human) chat recipients that need to
 * be handled by connector.
 * 
 * @author kgilmer
 *
 */
public class PlainTextMessageParsingTests extends TestCase {

	
	/**
	 * Test message type "invite" from server.
	 */
	public void testInviteMessageParsing() {
		String msg = 
			"admin@xmpp.bugswarm-test/9313381421316809306990551 invites you to the room 13066b7ac390d25d4817c021d8f531083e37737d@swarms.xmpp.bugswarm-test (User barberdt wants you use his Swarm services)";
		
		XMPPMessage parsedMessage = XMPPPlainTextMessageParser.parseServerMessage(msg);
		
		assertNotNull(parsedMessage);
		assertEquals(parsedMessage.getType(), XMPPPlainTextMessageParser.XMPPMessageType.SWARM_INVITE);
		
		assertTrue(parsedMessage instanceof InviteMessageImpl);
		
		InviteMessageImpl mi = (InviteMessageImpl) parsedMessage;
		
		assertTrue(mi.getRoomID().equals("13066b7ac390d25d4817c021d8f531083e37737d"));
		assertTrue(mi.getSenderJID().equals("admin@xmpp.bugswarm-test/9313381421316809306990551"));		
	}
}
