package com.buglabs.bug.swarm.connector.osgi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.test.OSGiHelperTester;
import com.buglabs.module.IModuleControl;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.OSGiServiceLoader;

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
		 * @param eventType event type
		 * @param source object source
		 */
		void change(int eventType, Object source);
	}

	private static BundleContext context;
	private static OSGiHelper ref;
	private Map<Object, Feed> feeds;

	private List<EntityChangeListener> listeners;

	/**
	 * @throws Exception should not be thrown
	 */
	private OSGiHelper() throws Exception {
		context = Activator.getContext();
		feeds = new HashMap<Object, Feed>();
		
		initializeModuleProviders();
		initializeWSProviders();
		initializeFeedProviders();

		if (context != null)
			context.addServiceListener(this);

		listeners = new CopyOnWriteArrayList<OSGiHelper.EntityChangeListener>();
	}

	/**
	 * @return reference to OSGiHelper singleton
	 * @throws Exception should not be thrown unless defect in code
	 */
	public static OSGiHelper getRef() throws Exception {
		if (ref == null)
			ref = new OSGiHelper();

		// Here we are checking to see if the context is null. If so we are not
		// running in OSGi and need to provide
		// some fake data for test execution. This should likely be disabled in
		// production code, or when
		// osgi-based junits are available.

		return ref;
	}

	/**
	 * Add listener for OSGi service change events that relate to bugswarm events.
	 * @param listener listener
	 */
	public void addListener(final EntityChangeListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Remove listener for OSGi service change events that relate to bugswarm events.
	 * @param listener listener
	 */
	public void removeListener(final EntityChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @return List of Feed services available at time of call from OSGi service registry.
	 */
	public List<Feed> getBUGFeeds() {
		return new ArrayList<Feed>(feeds.values());
	}

	/**
	 * @throws Exception should not be thrown
	 */
	private void initializeModuleProviders() throws Exception {	
		if (context != null) {
			synchronized (feeds) {
				OSGiServiceLoader.loadServices(
						context, IModuleControl.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
					public void load(final Object service) throws Exception {					
						if (!feeds.containsKey(service)) {
							feeds.put(service, Feed.createForType(service));
						}
					}
				});
			}
		} else {
			OSGiHelperTester.loadMockIModuleControls(feeds);
		}
	}

	/**
	 * Scan OSGi service registry and return a list of all available
	 * PublicWSProvider instances.
	 * 
	 * @throws Exception should not be thrown
	 */
	private void initializeWSProviders() throws Exception {		
		if (context != null) {
			synchronized (feeds) {
				OSGiServiceLoader.loadServices(
						context, PublicWSProvider.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
					public void load(final Object service) throws Exception {					
						if (!feeds.containsKey(service)) {
							feeds.put(service, Feed.createForType(service));
						}
					}
				});
			}
		} else {
			OSGiHelperTester.loadMockPublicWSProviders(feeds);
		}
	}

	/**
	 * Scan OSGi service registry and return a list of all available
	 * java.util.Map instances with swarm properties.
	 * 
	 * @throws Exception should not be thrown
	 */
	private void initializeFeedProviders() throws Exception {		
		if (context != null) {
			synchronized (feeds) {
				// TODO: Optimize by specifying a proper filter rather than filter in code.
				ServiceReference[] srs = context.getAllServiceReferences(Map.class.getName(), null);
				if (srs != null)
					for (ServiceReference sr : Arrays.asList(srs)) {
						Feed feed = Feed.createForType(sr);
						
						if (feed != null && !feeds.entrySet().contains(feed)) {
							feeds.put(context.getService(sr), feed);	
						} else {
							Activator.getLog().log(LogService.LOG_WARNING, Map.class.getName() + " ignored: " + Feed.FEED_SERVICE_NAME_PROPERTY + " is not a property.");
						}
					}
			}
		} else {
			OSGiHelperTester.loadMockFeedProviders(feeds);
		}
	}

	/**
	 * Clean up and stop listening to OSGi service registry.
	 */
	public void shutdown() {
		if (context != null) {
			context.removeServiceListener(this);
		}

		if (feeds != null) {
			feeds.clear();
		}
	}

	@Override
	public void serviceChanged(final ServiceEvent event) {
		if (isValidEvent(event)) {
			try {
				if (event.getType() == ServiceEvent.REGISTERED) {
					if (isFeedEvent(event))
						initializeFeedProviders();
					else if (isServiceEvent(event))
						initializeWSProviders();
					else if (isModuleEvent(event))
						initializeModuleProviders();
				} else if (event.getType() == ServiceEvent.UNREGISTERING) {
					feeds.remove(event.getSource());
				}
			} catch (Exception e) {
				Activator.getLog().log(LogService.LOG_ERROR, "Failed to update state from OSGi service event.", e);
			}

			//If we have event listeners, send notifications of the change.
			if (listeners != null && listeners.size() > 0) {				
				for (EntityChangeListener listener : listeners) 
					listener.change(event.getType(), event.getSource());
			}
		}
	}

	/**
	 * Is ServiceEvent relevant for swarm?
	 * 
	 * @param event service event
	 * @return true if the event pertains to bugswarm-connector
	 */
	private boolean isValidEvent(final ServiceEvent event) {
		boolean typeValid = event.getType() == ServiceEvent.REGISTERED || event.getType() == ServiceEvent.UNREGISTERING;
		boolean classValid = isModuleEvent(event) || isServiceEvent(event) || isFeedEvent(event);
		
		return typeValid && classValid;
	}

	/**
	 * @param event service event
	 * @return true if event is from a feed.
	 */
	private boolean isFeedEvent(final ServiceEvent event) {
		return event.getSource() instanceof Map;
	}

	/**
	 * @param event service event
	 * @return true if event is from a BUG service.
	 */
	private boolean isServiceEvent(final ServiceEvent event) {
		return event.getSource() instanceof PublicWSProvider;
	}

	/**
	 * @param event service event
	 * @return true if event is from a BUG module.
	 */
	private boolean isModuleEvent(final ServiceEvent event) {	
		return event.getSource() instanceof IModuleControl;
	}
}
