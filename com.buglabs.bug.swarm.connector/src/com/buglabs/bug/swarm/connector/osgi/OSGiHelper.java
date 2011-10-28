package com.buglabs.bug.swarm.connector.osgi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;

import com.buglabs.bug.dragonfly.module.IModuleControl;
import com.buglabs.bug.swarm.connector.test.OSGiHelperTester;
import com.buglabs.bug.swarm.connector.ui.SwarmConfigKeys;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.osgi.FilterUtil;
import com.buglabs.util.osgi.OSGiServiceLoader;

/**
 * Class to encapsulate OSGi service access for bugswarm-connector.
 * 
 * @author kgilmer
 * 
 */
public final class OSGiHelper implements ServiceListener {

	/**
	 * A listener for events that occur in the OSGi context for any
	 * swarm-enabled activity.
	 * 
	 * @author kgilmer
	 * 
	 */
	public interface EntityChangeListener {
		/**
		 * @param eventType
		 *            event type
		 * @param source
		 *            object source
		 */
		void change(int eventType, Object source);
	}

	private static BundleContext context;
	private static OSGiHelper ref;
	private Map<Object, Feed> feedServiceMap;
	private Map<String, Feed> feedNameMap;

	private List<EntityChangeListener> listeners;
	private ModulesFeed modulesFeed;
	private CapabilitiesFeed capabilitiesFeed;
	private ConfigurationAdmin configAdmin;

	/**
	 * @throws Exception
	 *             should not be thrown
	 */
	private OSGiHelper() throws Exception {
		context = Activator.getContext();
		
		if (context == null)
			throw new IllegalStateException("Unable to get OSGi BundleContext.");
		
		feedServiceMap = new HashMap<Object, Feed>();
		feedNameMap = new HashMap<String, Feed>();

		modulesFeed = createModuleFeed();
		capabilitiesFeed = createCapabiltiesFeed(modulesFeed);
		initializeWSProviders();
		initializeFeedProviders();

		if (context != null)
			context.addServiceListener(this);

		listeners = new CopyOnWriteArrayList<OSGiHelper.EntityChangeListener>();
	}

	/**
	 * Create the capabilities feed.
	 * 
	 * @param moduleFeed Modules Feed
	 * @return CapabilitiesFeed
	 */
	private CapabilitiesFeed createCapabiltiesFeed(ModulesFeed moduleFeed) {
		CapabilitiesFeed cf = new CapabilitiesFeed(context, moduleFeed);
		
		cf.register(context);
		
		return cf;
	}

	/**
	 * Refer to https://github.com/buglabs/bugswarm-connector/issues/20.
	 * @return ModulesFeed
	 * @throws InvalidSyntaxException 
	 */
	private ModulesFeed createModuleFeed() throws InvalidSyntaxException {
			
		ModulesFeed feed = new ModulesFeed(context);
		
		feed.register(context);
		context.addServiceListener(
				feed, 
				FilterUtil.generateServiceFilter(IModuleControl.class.getName()));
		
		return feed;
	}

	/**
	 * @return reference to OSGiHelper singleton
	 * @throws Exception
	 *             should not be thrown unless defect in code
	 */
	public static OSGiHelper getRef() throws Exception {
		if (ref == null)
			ref = new OSGiHelper();		

		return ref;
	}

	/**
	 * Add listener for OSGi service change events that relate to bugswarm
	 * events.
	 * 
	 * @param listener
	 *            listener
	 */
	public void addListener(final EntityChangeListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Remove listener for OSGi service change events that relate to bugswarm
	 * events.
	 * 
	 * @param listener
	 *            listener
	 */
	public void removeListener(final EntityChangeListener listener) {
		if (listeners != null)
			listeners.remove(listener);
	}

	/**
	 * @return List of Feed services available at time of call from OSGi service
	 *         registry.
	 */
	public List<Feed> getBUGFeeds() {
		return new ArrayList<Feed>(feedServiceMap.values());
	}

	/**
	 * @param feedRequestName name of feed
	 * @return Feed from name
	 */
	public Feed getBUGFeed(String feedRequestName) {

		return feedNameMap.get(feedRequestName);
	}

	/**
	 * @throws Exception
	 *             should not be thrown
	 */
	private void initializeModuleProviders() throws Exception {
		if (context != null) {
			synchronized (feedServiceMap) {
				OSGiServiceLoader.loadServices(
						context, IModuleControl.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
					public void load(final Object service) throws Exception {
						if (!feedServiceMap.containsKey(service)) {
							feedServiceMap.put(service, Feed.createForType(service));
						}
					}
				});
			}
			synchronized (feedNameMap) {
				OSGiServiceLoader.loadServices(
						context, IModuleControl.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
					public void load(final Object service) throws Exception {
						Feed f = Feed.createForType(service);
						if (!feedNameMap.containsKey(f.getName())) {
							feedNameMap.put(f.getName(), f);
						}
					}
				});
			}
		} else {
			OSGiHelperTester.loadMockIModuleControls(feedServiceMap, feedNameMap);
		}
	}

	/**
	 * Scan OSGi service registry and return a list of all available
	 * PublicWSProvider instances.
	 * 
	 * @throws Exception
	 *             should not be thrown
	 */
	private void initializeWSProviders() throws Exception {
		if (context != null) {
			synchronized (feedServiceMap) {
				OSGiServiceLoader.loadServices(
						context, PublicWSProvider.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
					public void load(final Object service) throws Exception {
						if (!feedServiceMap.containsKey(service)) {
							feedServiceMap.put(service, Feed.createForType(service));
						}
					}
				});
			}
			synchronized (feedNameMap) {
				OSGiServiceLoader.loadServices(
						context, PublicWSProvider.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
					public void load(final Object service) throws Exception {
						Feed f = Feed.createForType(service);

						if (f != null && !feedNameMap.containsKey(f.getName()))
							feedNameMap.put(f.getName(), f);

					}
				});
			}
		} else {
			OSGiHelperTester.loadMockPublicWSProviders(feedServiceMap, feedNameMap);
		}
	}

	/**
	 * Scan OSGi service registry and return a list of all available
	 * java.util.Map instances with swarm properties.
	 * 
	 * @throws Exception
	 *             should not be thrown
	 */
	private void initializeFeedProviders() throws Exception {
		if (context != null) {
			synchronized (feedServiceMap) {
				// TODO: Optimize by specifying a proper filter rather than
				// filter in code.
				ServiceReference[] srs = context.getAllServiceReferences(Map.class.getName(), null);
				if (srs != null)
					for (ServiceReference sr : srs) {
						Feed feed = Feed.createForType(sr);

						if (feed != null && !feedServiceMap.entrySet().contains(feed)) {
							feedServiceMap.put(context.getService(sr), feed);
						} else {
							Activator.getLog().log(LogService.LOG_WARNING,
									Map.class.getName() + " ignored: " + Feed.FEED_SERVICE_NAME_PROPERTY + " is not a property.");
						}
					}
			}
			synchronized (feedNameMap) {
				// TODO: Optimize by specifying a proper filter rather than
				// filter in code.
				ServiceReference[] srs = context.getAllServiceReferences(Map.class.getName(), null);
				if (srs != null)
					for (ServiceReference sr : srs) {
						Feed feed = Feed.createForType(sr);

						if (feed != null && !feedNameMap.entrySet().contains(feed)) {
							feedNameMap.put(feed.getName(), feed);
						} else {
							Activator.getLog().log(LogService.LOG_WARNING,
									Map.class.getName() + " ignored: " + Feed.FEED_SERVICE_NAME_PROPERTY + " is not a property.");
						}
					}
			}
		} else {
			OSGiHelperTester.loadMockFeedProviders(feedServiceMap, feedNameMap);
		}
	}

	/**
	 * Clean up and stop listening to OSGi service registry.
	 */
	public void shutdown() {
		if (context != null) {
			context.removeServiceListener(this);
			
			if (modulesFeed != null)
				context.removeServiceListener(modulesFeed);
						
			if (capabilitiesFeed != null)
				capabilitiesFeed = null;
		}
		
		if (feedServiceMap != null) {
			feedServiceMap.clear();
		}

		if (feedNameMap != null) {
			feedNameMap.clear();
		}
	}

	@Override
	public void serviceChanged(final ServiceEvent event) {
		if (isValidEvent(event)) {
			Object svc = context.getService(event.getServiceReference());
			try {
				if (event.getType() == ServiceEvent.REGISTERED) {
					if (isFeedEvent(svc))
						initializeFeedProviders();
					else if (isServiceEvent(svc))
						initializeWSProviders();
					else if (isModuleEvent(svc))
						initializeModuleProviders();
				} else if (event.getType() == ServiceEvent.UNREGISTERING) {
					feedServiceMap.remove(svc);
					feedNameMap.remove(event.getServiceReference().getProperty("SWARM.FEED.NAME"));
				}
			} catch (Exception e) {
				Activator.getLog().log(LogService.LOG_ERROR, "Failed to update state from OSGi service event.", e);
			}

			// If we have event listeners, send notifications of the change.
			if (listeners != null && listeners.size() > 0) {
				for (EntityChangeListener listener : listeners)
					listener.change(event.getType(), event.getServiceReference());
			}
		}
	}

	/**
	 * Is ServiceEvent relevant for swarm?
	 * 
	 * @param event
	 *            service event
	 * @return true if the event pertains to bugswarm-connector
	 */
	private boolean isValidEvent(final ServiceEvent event) {
		boolean typeValid = event.getType() == ServiceEvent.REGISTERED || event.getType() == ServiceEvent.UNREGISTERING
				|| event.getType() == ServiceEvent.MODIFIED;
		Object service = context.getService(event.getServiceReference());
		boolean classValid = isModuleEvent(service) || isServiceEvent(service) || isFeedEvent(service);

		return typeValid && classValid;
	}

	/**
	 * @param service
	 *            service event
	 * @return true if event is from a feed.
	 */
	private boolean isFeedEvent(final Object service) {
		return service instanceof Map;
	}

	/**
	 * @param service
	 *            service event
	 * @return true if event is from a BUG service.
	 */
	private boolean isServiceEvent(final Object service) {
		return service instanceof PublicWSProvider;
	}

	/**
	 * @param service
	 *            service event
	 * @return true if event is from a BUG module.
	 */
	private boolean isModuleEvent(final Object service) {
		return service instanceof IModuleControl;
	}
	
	/**
	 * Convert an array of Strings into a Dictionary.
	 * 
	 * @param elements a list of elements of even size.  First element is key, second is value, and repeat.
	 * @return Dictionary of key value pairs of input elements
	 */
	public static Dictionary toDictionary(String ... elements) {
		if (elements.length % 2 != 0) {
			throw new IllegalStateException("Input parameters must be even.");
		}

		Iterator<String> i = Arrays.asList(elements).iterator();
		Hashtable<String, String> m = new Hashtable<String, String>();

		while (i.hasNext()) {
			m.put(i.next().toString(), i.next());
		}

		return m;
	}

	/**
	 * Set the resource id that's created by the server for the device.
	 * @param resourceId resource id or null to unset the resource id.
	 * @throws IOException on ConfigAdmin error
	 */
	public void setResourceId(String resourceId) throws IOException {
		if (configAdmin == null)
			configAdmin = getConfigAdmin();
		
		Configuration config = configAdmin.getConfiguration(SwarmConfigKeys.CONFIG_PID_BUGSWARM);
		
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
	 * @return reference to ConfigurationAdmin or throw IllegalStateException if unavailable.
	 */
	private ConfigurationAdmin getConfigAdmin() {
		if (context == null)
			throw new IllegalStateException("BundleContext is not available.");
		
		ServiceReference casr = context.getServiceReference(ConfigurationAdmin.class.getName());
		
		if (casr == null)
			throw new IllegalStateException(ConfigurationAdmin.class.getName() + " is not in the service registry.");
		
		ConfigurationAdmin ca = (ConfigurationAdmin) context.getService(casr);
		
		if (ca == null)
			throw new IllegalStateException(ConfigurationAdmin.class.getName() + " cannot be referenced.");
		
		return ca;
	}
}
