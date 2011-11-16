package com.buglabs.bug.swarm.connector.xmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.osgi.Activator;

/**
 * Handles global swarm invitation requests.
 * 
 * @author kgilmer
 *
 */
public class SwarmAssociationHandler implements ChatManagerListener, MessageListener {

	private final SwarmXMPPClient swarmXMPPClient;
	private final ISwarmServerRequestListener listener;

	/**
	 * @param swarmXMPPClient
	 * @param listener
	 */
	public SwarmAssociationHandler(SwarmXMPPClient swarmXMPPClient, ISwarmServerRequestListener listener) {
		this.swarmXMPPClient = swarmXMPPClient;
		this.listener = listener;		
	}

	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {	
		chat.addMessageListener(this);
	}

	@Override
	public void processMessage(Chat chat, Message message) {		
		if (message.getBody().contains("invites you to the room")) {
			String swarmId = chat.getParticipant().split("@")[0];				
			
			try {
				swarmXMPPClient.joinSwarm(swarmId, listener);
			} catch (Exception e) {
				Activator.getLog().log(LogService.LOG_ERROR, "Failed to join swarm.", e);
			}
		}		
	}

}
