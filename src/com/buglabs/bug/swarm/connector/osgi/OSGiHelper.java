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

	private static BundleContext context;
	private static OSGiHelper ref;

	/*private List<IModuleControl> moduleList;
	private List<PublicWSProvider> serviceList;*/
	private Map<Object, BUGSwarmFeed> feeds;

	private List<EntityChangeListener> listeners;

	private OSGiHelper() throws Exception {
		context = Activator.getContext();
		feeds = new HashMap<Object, BUGSwarmFeed>();
		
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

	public List<BUGSwarmFeed> getBUGFeeds() {
		return new ArrayList<BUGSwarmFeed>(feeds.values());
	}

	private void initializeModuleProviders() throws Exception {	
		if (context != null) {
			synchronized (feeds) {
				OSGiServiceLoader.loadServices(context, IModuleControl.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
					public void load(Object service) throws Exception {					
						if (!feeds.containsKey(service)) {
							feeds.put(service, BUGSwarmFeed.createForType(service));
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
		if (context != null) {
			synchronized (feeds) {
				OSGiServiceLoader.loadServices(context, PublicWSProvider.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
					public void load(Object service) throws Exception {					
						if (!feeds.containsKey(service)) {
							feeds.put(service, BUGSwarmFeed.createForType(service));
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
		if (context != null) {
			synchronized (feeds) {
				// TODO: Optimize by specifying a proper filter rather than filter in code.
				for (ServiceReference sr : Arrays.asList(context.getAllServiceReferences(Map.class.getName(), null))) {
					BUGSwarmFeed feed = BUGSwarmFeed.createForType(sr);
					
					if (feed != null && !feeds.entrySet().contains(feed)) {
						feeds.put(context.getService(sr), feed);	
					} else {
						//TODO: log that ignoring a map because it doesn't have required properties.
					}
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

		if (feeds != null) {
			feeds.clear();
		}
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
	 * Is ServiceEvent relevant for swarm?
	 * 
	 * @param event
	 * @return
	 */
	private boolean isValidEvent(ServiceEvent event) {
		boolean typeValid = event.getType() == ServiceEvent.REGISTERED || event.getType() == ServiceEvent.UNREGISTERING;
		boolean classValid = event.getSource() instanceof IModuleControl || event.getSource() instanceof PublicWSProvider || event.getSource() instanceof Map<?, ?>;
		
		return typeValid && classValid;
	}

	// //////////// Test code follows, can be removed for production
	
	private void loadMockFeedProviders() {
		Map<String, String> f1 = new HashMap<String, String>();
		feeds.put(f1, new BUGSwarmFeed("feed1", f1));
		f1 = new HashMap<String, String>();
		feeds.put(f1, new BUGSwarmFeed("feed2", new HashMap<String, String>()));
		f1 = new HashMap<String, String>();
		feeds.put(f1, new BUGSwarmFeed("feed3", new HashMap<String, String>()));
	}

	private void loadMockIModuleControls() {
		IModuleControl mc = new MockIModuleControl("GPS", 1, createMockProperties());
		feeds.put(mc, BUGSwarmFeed.createForType(mc));
		mc = new MockIModuleControl("LCD", 2, createMockProperties());
		feeds.put(mc, BUGSwarmFeed.createForType(mc));
		mc = new MockIModuleControl("CAMERA", 3, createMockProperties());
		feeds.put(mc, BUGSwarmFeed.createForType(mc));
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
		PublicWSProvider wsp = new MockPublicWSProvider("Picture", "Take a picture using the camera module.");
		feeds.put(wsp, BUGSwarmFeed.createForType(wsp));
		
		wsp = new MockPublicWSProvider("Location", "Determine your location using GPS services.");
		feeds.put(wsp, BUGSwarmFeed.createForType(wsp));
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
