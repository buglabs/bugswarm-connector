package com.buglabs.bug.swarm.devicestats.pub;

import java.io.Serializable;
import java.util.Map;

/**
 * A service that provide device statistics for publishing to swarm as part of the bugswarm-devicestats application.
 * 
 * Clients can implement this service and register with the Service Registry.  Then, bugswarm-devicestats will
 * automatically export the client statistics along with the others to member swarms.
 * 
 * @author kgilmer
 *
 */
public interface DeviceStatProviderService {
	/**
	 * Given the passed Map, add one or more properties to the device-global map.
	 * 
	 * @param propertyMap
	 */
	void addStats(Map<String, Serializable> propertyMap);
}
