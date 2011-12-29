package com.buglabs.bug.swarm.connector.xmpp;

import java.text.ParseException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.osgi.service.log.LogService;
import org.w3c.dom.views.AbstractView;

import com.buglabs.bug.swarm.connector.model.Jid;
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
		Activator.getLog().log(LogService.LOG_INFO, "Chat Created in SwarmAssociationHandler, Participant: "+chat.getParticipant());
		chat.addMessageListener(this);
	}

	public void processMessage(Chat chat, Message message) {		
		if (message.getBody().contains("invites you to the room")) {
			String swarmId = chat.getParticipant().split("@")[0];				

			try {
				swarmXMPPClient.joinSwarm(swarmId, listener);
			} catch (Exception e) {
				Activator.getLog().log(LogService.LOG_ERROR, "Failed to join swarm.", e);
			}
		}
		//this is the proverbial "connector connects to a swarm where webui is already waiting
		//receives a PM from webui asking for device-stats
		else if (message.getBody().contains("feed")){
			Activator.getLog().log(LogService.LOG_DEBUG, "Received new Private Message from: "+chat.getParticipant()+" requesting "+message.getBody());
			String swarmId = chat.getParticipant().split("@")[0];				
			AbstractMessageHandler handler = new PrivateMessageHandler(swarmXMPPClient.getJid(), swarmId, swarmXMPPClient.getRequestListeners());
			try {
				handler.handleSwarmRequest(message.getBody(), chat.getParticipant());
			} catch (ParseException e) {
				e.printStackTrace();
			}


		}
	}
}
