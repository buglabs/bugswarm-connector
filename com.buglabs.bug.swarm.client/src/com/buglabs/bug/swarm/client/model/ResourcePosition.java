package com.buglabs.bug.swarm.client.model;

/**
 * Represents the position of a Resource-Device.
 * 
 * @author kgilmer
 *
 */
public class ResourcePosition extends ModelBase {
	private double longitude;	
	private double latitude;
	
	/**
	 * @param longitude Longitude of device
	 * @param latitude Latitude of device
	 */
	public ResourcePosition(final double longitude, final double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	/**
	 * @return latitude
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * @return longitude
	 */
	public double getLongitude() {
		return longitude;
	}
}
