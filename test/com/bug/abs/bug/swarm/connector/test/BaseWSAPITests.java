package com.bug.abs.bug.swarm.connector.test;

import java.util.Random;

import junit.framework.TestCase;

/**
 * Unit tests for ISwarmWSClient implementation
 * 
 * @author kgilmer
 *
 */
public abstract class BaseWSAPITests extends TestCase {
	public static final String API_KEY = "a0fc6588f11db4a1f024445e950ae6ae33bc0313";
	public static final String SWARM_HOST = "http://api.bugswarm.net";
	
	protected String testSwarmName;
	protected String testSwarmId;
	// helper methods

	protected String getTestSwarmName() {		
		if (testSwarmName == null) {
			Random r = new Random();
			testSwarmName = "TestSwarm" + this.getClass().getSimpleName() + r.nextFloat();		
		}
		
		return testSwarmName;		
	}
	
	/**
	 * @return
	 */
	protected String generateRandomSwarmName() {		

		Random r = new Random();
		return "TestSwarm" + this.getClass().getSimpleName() + r.nextFloat();					
	}

	protected String getTestSwarmDescription() {
		return "TestSwarmDescription" + this.getClass().getSimpleName();
	}
}