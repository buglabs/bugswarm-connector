package com.buglabs.bug.swarm.connector.osgi;

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
	private String[] configurationServices = new String[] { ConfigurationAdmin.class.getName(), ISewingService.class.getName() };

	private static BundleContext context;
	private static LogService log;

	private Dictionary<String, String> cachedConfig;
	private ServiceRegistration cmSr;

	private BUGSwarmConnector connector;

	private ServiceTracker sewingST;

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
		log.log(LogService.LOG_DEBUG, this.getClass().getSimpleName() + " configuration updated.");

		if (config == null || (cachedConfig != null && configsIdentical(cachedConfig, config))) {
			log.log(LogService.LOG_DEBUG, this.getClass().getSimpleName() + " configuration updated.");
			return;
		}

		// Swarm client being started.
		if (Configuration.isValid(config) && connector == null) {
			log.log(LogService.LOG_DEBUG, this.getClass().getSimpleName() + " starting connector.");
			Configuration nc = new Configuration(config);
			log.log(LogService.LOG_DEBUG, this.getClass().getSimpleName() + " connector configuration: " + nc);
			connector = new BUGSwarmConnector(nc);
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

		log.log(LogService.LOG_WARNING, this.getClass().getSimpleName() + " configuration changed but no action performed.");
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