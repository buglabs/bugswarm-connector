package com.buglabs.bug.swarm.devicestats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.osgi.framework.ServiceRegistration;

import com.buglabs.bug.swarm.devicestats.pub.DeviceStatProviderService;


/**
 * TimerTask that iterates through all DeviceStatProviders, collects the statistics,
 * then notifies bugswarm-connector that the feed has changed.  connector, if configured 
 * to do so, will then export the feed to interested swarm members.
 * 
 * @author kgilmer
 *
 */
public class StatsUpdateTask extends TimerTask {

	private List<DeviceStatProviderService> providers;
	private final Map<String, Serializable> map;
	private final ServiceRegistration registration;

	public StatsUpdateTask(Map<String, Serializable> map, ServiceRegistration registration) {
		this.map = map;
		this.registration = registration;		
		providers = new ArrayList<DeviceStatProviderService>();		
	}

	@Override
	public void run() {
		map.clear();				
		
		synchronized (providers) {
			for (DeviceStatProviderService provider : providers)
				provider.addStats(map);
		}
		
		registration.setProperties(Activator.createServiceProperties());
	}

	public void addProvider(DeviceStatProviderService provider) {
		synchronized (providers) {
			providers.add(provider);
		}		
	}

	public void removeProvider(DeviceStatProviderService provider) {
		synchronized (providers) {
			providers.remove(provider);
		}
	}

}
