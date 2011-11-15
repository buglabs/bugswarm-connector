package com.buglabs.bug.swarm.connector.osgi;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.buglabs.bug.swarm.connector.BUGSwarmConnector;
import com.buglabs.bug.swarm.connector.Configuration;
import com.buglabs.bug.swarm.connector.ui.ConfigInitRunnable;
import com.buglabs.bug.swarm.connector.ui.SwarmConfigKeys;
import com.buglabs.osgi.sewing.pub.ISewingService;
import com.buglabs.util.osgi.LogServiceUtil;
import com.buglabs.util.osgi.ServiceTrackerUtil;

/**
 * Activator for bugswarm-connector. Entry point for OSGi.
 * 
 * @author kgilmer
 * 
 */
public class Activator implements BundleActivator, ManagedService {
	// public static final String CONFIG_PID_BUGSWARM = "BUGSWARM";

	/**
	 * Services required for BUGswarm to be configured.
	 */
	private final String[] configurationServices = new String[] { 
			ConfigurationAdmin.class.getName(), 
			ISewingService.class.getName() };

	private static BundleContext context;
	private static LogService log;

	private static ConfigurationAdmin configAdmin;

	private Dictionary<String, String> cachedConfig;
	private ServiceRegistration cmSr;

	private BUGSwarmConnector connector;

	private ServiceTracker sewingST;

	/**
	 * @return BundleContext
	 */
	public static BundleContext getContext() {
		return context;
	}

	/**
	 * @return LogService
	 */
	public static LogService getLog() {
		if (log == null)
			log = new FakeLogService();

		return log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		log = LogServiceUtil.getLogService(context);
		cmSr = context.registerService(ManagedService.class.getName(), this, getCMDictionary());
		sewingST = ServiceTrackerUtil.openServiceTracker(context, new ConfigInitRunnable(context, log), configurationServices);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		sewingST.close();
		Activator.context = null;
		log = null;
		cmSr.unregister();
	}

	/**
	 * @return A dictionary defining the configurations we're interested in.
	 */
	private Dictionary<String, String> getCMDictionary() {
		Dictionary<String, String> hm = new Hashtable<String, String>();

		hm.put("service.pid", SwarmConfigKeys.CONFIG_PID_BUGSWARM);

		return hm;
	}

	@Override
	public void updated(final Dictionary config) throws ConfigurationException {
		if (config == null || (cachedConfig != null && configsIdentical(cachedConfig, config))) {
			log.log(LogService.LOG_DEBUG, this.getClass().getSimpleName() + " configuration updated.");
			return;
		}

		// Swarm client being started.
		if (Configuration.isValid(config) && connector == null) {
			log.log(LogService.LOG_DEBUG, this.getClass().getSimpleName() + " starting connector.");
			Configuration nc = new Configuration(config);
			log.log(LogService.LOG_DEBUG, this.getClass().getSimpleName() + " connector configuration: " + nc);
			connector = new BUGSwarmConnector(context, nc);
			connector.start();
			return;
		}

		// Swarm client being shutdown.
		if (!Configuration.isValid(config) && connector != null) {
			log.log(LogService.LOG_DEBUG, this.getClass().getSimpleName() + " stopping connector.");
			connector.shutdown();
			connector = null;
			return;
		}

		String state = "inactive";
		if (connector != null)
			state = "active";		
		log.log(LogService.LOG_WARNING, this.getClass().getSimpleName() + " configuration changed but no action performed.  Connector is " + state);
		log.log(LogService.LOG_DEBUG, "Config: " + config.toString());
	}

	/**
	 * @param defaultValue Default value if undefined.
	 * @return Hostname property as defined as a OSGi property, or defaultValue if
	 *         undefined.
	 *         
	 */
	public static Object getBundleContextProperty(String key, Object defaultValue) {
		Object property = context.getProperty(key);
		return property == null ? defaultValue : property;
	}

	/**
	 * Calling .equals on these with identical kvps returns false. Doing it the
	 * hard way.
	 * 
	 * @param c1
	 *            Dictionary
	 * @param c2
	 *            Dictionary
	 * @return true if they are identical, false otherwise.
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
	

	/**
	 * Set the resource id that's created by the server for the device.
	 * @param resourceId resource id or null to unset the resource id.
	 * @throws IOException on ConfigAdmin error
	 */
	public static void setResourceId(String resourceId) throws IOException {
		if (configAdmin == null) {
				configAdmin = (ConfigurationAdmin) OSGiUtil.getServiceInstance(context, ConfigurationAdmin.class.getName());
			
				if (configAdmin == null)
					throw new IOException("Unable to get configAdmin.");
		}
		
		org.osgi.service.cm.Configuration config = configAdmin.getConfiguration(SwarmConfigKeys.CONFIG_PID_BUGSWARM);
		
		if (config == null) //This should not happen, since the webui will create the configuration for us.
			throw new IllegalStateException("Configuration for connector does not exist.");
		
		Dictionary properties = config.getProperties();
		if (resourceId != null)
			properties.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_RESOURCE_ID, resourceId);
		else
			properties.remove(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_RESOURCE_ID);
		
		config.update(properties);
	}	

	/**
	 * A Logger for cases when a framework logger is unavailable.
	 * 
	 * @author kgilmer
	 * 
	 */
	private static class FakeLogService implements LogService {

		@Override
		public void log(int level, String message) {
			System.out.println(level + ": " + message);
		}

		@Override
		public void log(int level, String message, Throwable exception) {
			System.out.println(level + ": " + message);
			exception.printStackTrace();
		}

		@Override
		public void log(ServiceReference sr, int level, String message) {
			System.out.println(level + ": " + message);
		}

		@Override
		public void log(ServiceReference sr, int level, String message, Throwable exception) {
			System.out.println(level + ": " + message);
			exception.printStackTrace();
		}

	}
}
