package com.buglabs.bug.swarm.restclient.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import com.buglabs.bug.swarm.restclient.ISwarmMessageListener;

public class SwarmParticipationReader extends Thread {
	
	private final BufferedReader reader;
	private volatile boolean running = false;
	private final String apiKey;
	private final List<ISwarmMessageListener> listeners;
	
	public SwarmParticipationReader(InputStream is, String apiKey, List<ISwarmMessageListener> listeners) throws UnsupportedEncodingException {
		this.apiKey = apiKey;
		this.listeners = listeners;
		this.reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	}
	
	@Override
	public void run() {
		running = true;
		String line = null;
		try {
			//Here rather than reading the line as a string, 
			//Create (via jsonfactory) a jsonparser and call nextToken() for same effect
			while ((line = reader.readLine().trim()) != null) {
				if (line.length() == 0 || isNumeric(line))
					continue;
				
				debugOut(line, false);
				if (listeners != null) {
					//Here we parse the json message, extract payload, fromSwarm, fromResource, isPublic
					Map<String, ?> payload = null;
					String fromSwarm = null;
					String fromResource = null;
					boolean isPublic = true;
					/*for (ISwarmMessageListener listener : listeners)
						listener.messageRecieved(payload, fromSwarm, fromResource, isPublic);*/
				}
				
				Thread.sleep(100);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			return;
		} finally {
			running = false;
			debugOut("Reader exited", false);
		}
	}
	
	private boolean isNumeric(String line) {
		try {
			Long.parseLong(line, 16);
			return true;
		} catch (NumberFormatException e) {				
		}
		return false;
	}
	
	private void debugOut(String message, boolean out) {
		System.out.print(apiKey.substring(0, 4));
		if (out)
			System.out.print(" --> ");
		else
			System.out.print(" <-- ");
		
		System.out.println(message);
	}

	public boolean isRunning() {
		return running;
	}
}