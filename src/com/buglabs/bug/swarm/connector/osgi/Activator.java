package com.buglabs.bug.swarm.connector.osgi;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.buglabs.application.ServiceTrackerHelper;
import com.buglabs.bug.swarm.connector.BUGSwarmConnector;
import com.buglabs.bug.swarm.connector.Configuration;
import com.buglabs.bug.swarm.connector.ui.ConfigInitRunnable;
import com.buglabs.bug.swarm.connector.ui.SwarmConfigKeys;
import com.buglabs.osgi.sewing.pub.ISewingService;
import com.buglabs.util.LogServiceUtil;

public class Activator implements BundleActivator, ManagedService {
	public static final String CONFIG_PID_BUGSWARM = "BUGSWARM";

	/**
	 * Services required for BUGswarm to be configured.
	 */
	private String[] configurationServices = new String[] { ConfigurationAdmin.class.getName(), ISewingService.class.getName() };
	
	private static BundleContext context;
	private static LogService log;
	
	private Dictionary cachedConfig;
	private ServiceRegistration cmSr;
	
	private ServiceTracker sewingST;

	private BUGSwarmConnector connector;

	/**
	 * @return BundleContext
	 */
	static BundleContext getContext() {
		return context;
	}
	
	/**
	 * @return LogService
	 */
	public static LogService getLog() {
		return log;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		log = LogServiceUtil.getLogService(context);
		cmSr = context.registerService(ManagedService.class.getName(), this, getCMDictionary());
		sewingST = ServiceTrackerHelper.openServiceTracker(context, configurationServices , new ConfigInitRunnable(context, log));
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		log = null;
		cmSr.unregister();
	}
	
	/**
	 * @return A dictionary defining the configurations we're interested in.
	 */
	private Dictionary getCMDictionary() {
		Dictionary hm = new Hashtable();
		
		hm.put("service.pid", CONFIG_PID_BUGSWARM);
		
		return hm;
	}

	@Override
	public void updated(Dictionary config) throws ConfigurationException {
		if (config == null || (cachedConfig != null && configsIdentical(cachedConfig, config))) {
			//Nothing has changed, ignore event.
			return;
		}
		
		//Handle the case of swarm client being initialized.
		if (Configuration.isValid(config) && connector == null) {
			connector = new BUGSwarmConnector(new Configuration(config));
			connector.start();
		}
		
		//TODO: Handle case of swarm client being shutdown.
		if (!Configuration.isValid(config) && connector != null) {
			//TODO Implement me
		}
		
		//TODO: Handle case of swarm client being restarted.
		if (Configuration.isValid(config) && connector != null) {
			//TODO Implement me
		}
	}

	/**
	 * Calling .equals on these with identical kvps returns false.  Doing it the hard way.
	 * @param c1
	 * @param c2
	 * @return
	 */
	private boolean configsIdentical(Dictionary c1, Dictionary c2) {
		if (c1.size() != c2.size()) {
			return false;
		}
		
		for (Enumeration e = c1.keys(); e.hasMoreElements();) {
			Object c1k = e.nextElement();
			Object c1v = c1.get(c1k);
			
			if (c2.get(c1k) != c1v) {
				return false;
			}
		}
		
		return true;
	}

}
