package com.buglabs.bug.swarm.devicestats;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.devicestats.providers.RAMStatProvider;
import com.buglabs.bug.swarm.devicestats.providers.StorageStatProvider;
import com.buglabs.bug.swarm.devicestats.providers.WifiStatProvider;
import com.buglabs.bug.swarm.devicestats.pub.DeviceStatProviderService;
import com.buglabs.util.osgi.FilterUtil;
import com.buglabs.util.osgi.LogServiceUtil;
import com.buglabs.util.shell.pub.ShellSession;


/**
 * Activator for bugswarm-devicestats: application to send device data to swarm.
 * 
 * @author kgilmer
 *
 */
public class Activator implements BundleActivator, ServiceListener {
	/**
	 * OSGi service property key for the feed name.
	 */
	public static final String FEED_SERVICE_NAME_PROPERTY = "SWARM.FEED.NAME";
	
	/**
	 * OSGi service property key for the feed update time.
	 */
	public static final String FEED_SERVICE_TIMESTAMP_PROPERTY = "SWARM.FEED.TIMESTAMP";

	/**
	 * Name of feed
	 */
	private static final Serializable FEED_NAME = "bugswarm-devicestats";
	
	/**
	 * Default interval to update device statistics.
	 */
	private static final int DEFAULT_UPDATE_INTERVAL = 30 * 1000;
	/**
	 * System property key used to define the update interval.
	 */
	private static final String UPDATE_INTERVAL_KEY = "bugswarm.devicestats.updateinterval";
	private Timer timer;
	private BundleContext context;
	private StatsUpdateTask updateTask;

	private static LogService log;

	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		this.log = LogServiceUtil.getLogService(context);
		int updateInterval = getUpdateInterval(context);
		
		registerInternalProviders(context);
		
		final List<DeviceStatProviderService> providers = getExistingDeviceStatProviders(context);
		
		context.addServiceListener(this, FilterUtil.generateServiceFilter(DeviceStatProviderService.class.getName()));
		
		Map<String, Serializable> map = new HashMap<String, Serializable>();
		ServiceRegistration registration = context.registerService(Map.class.getName(), map, createServiceProperties());
		
		timer = new Timer();	
		updateTask = new StatsUpdateTask(map, registration);
		
		for (DeviceStatProviderService provider : providers)
			updateTask.addProvider(provider);
		
		timer.schedule(updateTask, 0, updateInterval);
	}	

	@Override
	public void stop(BundleContext context) throws Exception {
		if (timer != null)
			timer.cancel();
	}
	
	public static Dictionary createServiceProperties() {
		Dictionary d = new Hashtable();		

		d.put(FEED_SERVICE_NAME_PROPERTY, FEED_NAME);
		d.put(FEED_SERVICE_TIMESTAMP_PROPERTY, System.currentTimeMillis());
		
		return d;
	}

	/**
	 * @param context
	 * @throws IOException 
	 */
	private void registerInternalProviders(BundleContext context) throws IOException {
		Dictionary sp = new Hashtable();
		
		sp.put("origin", getClass().getSimpleName());
		
		ShellSession session = new ShellSession(new File("/tmp"));
		
		context.registerService(DeviceStatProviderService.class.getName(), new StorageStatProvider(session), sp);
		context.registerService(DeviceStatProviderService.class.getName(), new RAMStatProvider(session), sp);
		context.registerService(DeviceStatProviderService.class.getName(), new WifiStatProvider(), sp);
	}

	/**
	 * Get a list of currently registered DeviceStatProviders.
	 * 
	 * @param context
	 * @return
	 */
	private List<DeviceStatProviderService> getExistingDeviceStatProviders(BundleContext context) {
		List<DeviceStatProviderService> d = new ArrayList<DeviceStatProviderService>();
		
		try {
			for (ServiceReference sr : context.getServiceReferences(DeviceStatProviderService.class.getName(), null))
				d.add((DeviceStatProviderService) context.getService(sr));
			
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		
		return d;
	}
	
	/**
	 * @param context BundleContext
	 * @return user defined or default update interval for device stats update.
	 */
	private int getUpdateInterval(BundleContext context) {
		if (context.getProperty(UPDATE_INTERVAL_KEY) != null) {
			try {
				return Integer.parseInt(context.getProperty(UPDATE_INTERVAL_KEY));
			} catch (Exception e) {
				//Ignore
			}
		}
		
		return DEFAULT_UPDATE_INTERVAL;
	}

	/**
	 * @return instance of LogService
	 */
	public static LogService getLog() {
		return log;
	}
	
	@Override
	public void serviceChanged(ServiceEvent event) {
		Object svc = context.getService(event.getServiceReference());
		
		if (! (svc instanceof DeviceStatProviderService))
			throw new IllegalArgumentException("OSGi service binding error, service is not a DeviceStatProvider.");
		
		DeviceStatProviderService provider = (DeviceStatProviderService) svc;
		
		if (event.getType() == ServiceEvent.REGISTERED)
			updateTask.addProvider(provider);
		else if (event.getType() == ServiceEvent.UNREGISTERING)
			updateTask.removeProvider(provider);
	}
}