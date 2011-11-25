package com.buglabs.bug.swarm.connector.osgi.pub;

/**
 * Exposes the current connector connection state to clients.
 * 
 * @author kgilmer
 *
 */
public interface IConnectorServiceStatus {

	/**
	 * Connector states.
	 *
	 */
	public enum Status {
		INACTIVE, ACTIVE;
	}
	
	/**
	 * @return the current state of connector.
	 */
	Status getStatus();
}
