package com.buglabs.bug.swarm.connector.osgi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleProperty;
import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.OSGiServiceLoader;

public class OSGiHelper implements ServiceListener {	

	/**
	 * A listener for events that occur in the OSGi context for any
	 * swarm-enabled activity.
	 * 
	 * @author kgilmer
	 * 
	 */
	public interface EntityChangeListener {
		public void change(int eventType, Object source);
	}

	/**
	 * Represents a swarm-feed.
	 * 
	 * @author kgilmer
	 * 
	 */
	public class BUGSwarmFeed {
		private String feedName;
		private Map<?, ?> feed;

		public BUGSwarmFeed(String feedName, Map<?, ?> feed) {

			this.feedName = feedName;
			this.feed = feed;
		}

		public String getName() {
			return feedName;
		}

		public Map<?, ?> getFeed() {
			return feed;
		}
	}

	private static BundleContext context;
	private static OSGiHelper ref;

	private List<IModuleControl> moduleList;
	private List<PublicWSProvider> serviceList;
	private List<BUGSwarmFeed> feedList;

	private List<EntityChangeListener> listeners;

	private OSGiHelper() throws Exception {
		context = Activator.getContext();
		initializeModuleProviders();
		initializeWSProviders();
		initializeFeedProviders();

		if (context != null)
			context.addServiceListener(this);

		listeners = new CopyOnWriteArrayList<OSGiHelper.EntityChangeListener>();
	}

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

	public void addListener(EntityChangeListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeListener(EntityChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Get the list of WS providers at time of call.
	 * 
	 * @return
	 */
	public List<PublicWSProvider> getBUGServices() {
		return serviceList;
	}

	/**
	 * Get the list of BUG modules attached at time of call.
	 * 
	 * @return
	 */
	public List<IModuleControl> getBUGModules() {
		return moduleList;
	}

	public List<BUGSwarmFeed> getBUGFeeds() {
		return new ArrayList<BUGSwarmFeed>();
	}

	private void initializeModuleProviders() throws Exception {
		if (moduleList == null)
			moduleList = new ArrayList<IModuleControl>();
		else
			moduleList.clear();

		if (context != null) {
			synchronized (moduleList) {
				OSGiServiceLoader.loadServices(context, IModuleControl.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
					public void load(Object service) throws Exception {
						if (!moduleList.contains(service)) {
							moduleList.add((IModuleControl) service);
						}
					}
				});
			}
		} else {
			loadMockIModuleControls();
		}
	}

	/**
	 * Scan OSGi service registry and return a list of all available
	 * PublicWSProvider instances.
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private void initializeWSProviders() throws Exception {
		if (serviceList == null)
			serviceList = new ArrayList<PublicWSProvider>();
		else
			serviceList.clear();

		if (context != null) {
			synchronized (serviceList) {
				OSGiServiceLoader.loadServices(context, PublicWSProvider.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
					public void load(Object service) throws Exception {
						if (!serviceList.contains(service)) {
							serviceList.add((PublicWSProvider) service);
						}
					}
				});
			}
		} else {
			loadMockPublicWSProviders();
		}
	}

	/**
	 * Scan OSGi service registry and return a list of all available
	 * java.util.Map instances with swarm properties.
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private void initializeFeedProviders() throws Exception {
		if (feedList == null)
			feedList = new ArrayList<BUGSwarmFeed>();
		else
			feedList.clear();

		if (context != null) {
			synchronized (feedList) {
				// TODO: specify a proper filter rather than filter in code.
				for (ServiceReference sr : Arrays.asList(context.getAllServiceReferences(Map.class.getName(), null)))
					if (sr.getProperty("SWARM.FEED.NAME") != null) {
						BUGSwarmFeed feed = new BUGSwarmFeed((String) sr.getProperty("SWARM.FEED.NAME"), (Map) context.getService(sr));
						if (!feedList.contains(feed))
							feedList.add(feed);
					}

			}
		} else {
			loadMockFeedProviders();
		}
	}

	/**
	 * Clean up and stop listening to OSGi service registry.
	 */
	public void shutdown() {
		if (context != null) {
			context.removeServiceListener(this);
		}

		if (serviceList != null) {
			serviceList.clear();
		}

		if (moduleList != null) {
			moduleList.clear();
		}
	}

	/**
	 * Find the PublicWSProvider service that uses specific name, or null if no
	 * instance is available.
	 * 
	 * @param name
	 * @return
	 */
	public PublicWSProvider getPublicWSProviderByName(String name) {
		for (PublicWSProvider p : serviceList)
			if (p.getPublicName().equals(name))
				return p;

		return null;
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		if (isValidEvent(event)) {
			// To keep the code simple, just repopulate the Module and Service
			// lists from the service registry.
			try {
				initializeModuleProviders();
			} catch (Exception e) {
				// TODO: handle exception
			}

			try {
				initializeWSProviders();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			try {
				initializeFeedProviders();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			//If we have event listeners, send notifications of the change.
			if (listeners != null && listeners.size() > 0) {				
				for (EntityChangeListener listener: listeners) 
					listener.change(event.getType(), event.getSource());
			}
		}
	}

	/**
	 * Is ServiceEvent relevent for swarm?
	 * 
	 * @param event
	 * @return
	 */
	private boolean isValidEvent(ServiceEvent event) {
		boolean typeValid = event.getType() == ServiceEvent.REGISTERED || event.getType() == ServiceEvent.UNREGISTERING;
		boolean classValid = event.getSource() instanceof IModuleControl || event.getSource() instanceof PublicWSProvider;
		return typeValid && classValid;
	}

	// //////////// Test code follows, can be removed for production
	
	private void loadMockFeedProviders() {
		feedList.add(new BUGSwarmFeed("feed1", new HashMap<String, String>()));
		feedList.add(new BUGSwarmFeed("feed2", new HashMap<String, String>()));
		feedList.add(new BUGSwarmFeed("feed3", new HashMap<String, String>()));
	}

	private void loadMockIModuleControls() {
		moduleList.add(new MockIModuleControl("GPS", 1, createMockProperties()));
		moduleList.add(new MockIModuleControl("LCD", 2, createMockProperties()));
		moduleList.add(new MockIModuleControl("CAMERA", 3, createMockProperties()));
	}

	private List createMockProperties() {
		List l = new ArrayList();
		final Random r = new Random();
		l.add(new IModuleProperty() {

			@Override
			public boolean isMutable() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Object getValue() {

				return "" + r.nextDouble();
			}

			@Override
			public String getType() {
				// TODO Auto-generated method stub
				return "String";
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "" + r.nextDouble();
			}
		});

		return l;
	}

	private void loadMockPublicWSProviders() {
		serviceList.add(new MockPublicWSProvider("Picture", "Take a picture using the camera module."));
		serviceList.add(new MockPublicWSProvider("Location", "Determine your location using GPS services."));
	}

	private class MockPublicWSProvider implements PublicWSProvider {

		private final String name;
		private final String desc;

		public MockPublicWSProvider(String name, String desc) {
			this.name = name;
			this.desc = desc;

		}

		@Override
		public PublicWSDefinition discover(int operation) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IWSResponse execute(int operation, String input) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getPublicName() {
			return name;
		}

		@Override
		public String getDescription() {
			return desc;
		}

	}

	private class MockIModuleControl implements IModuleControl {

		private final String name;
		private final int slot;
		private final List properties;

		MockIModuleControl(String name, int slot, List properties) {
			this.name = name;
			this.slot = slot;
			this.properties = properties;

		}

		@Override
		public List getModuleProperties() {
			return properties;
		}

		@Override
		public String getModuleName() {
			return name;
		}

		@Override
		public int getSlotId() {
			return slot;
		}

		@Override
		public boolean setModuleProperty(IModuleProperty property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int suspend() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int resume() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

	}
}
