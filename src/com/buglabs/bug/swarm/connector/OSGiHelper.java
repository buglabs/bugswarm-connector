package com.buglabs.bug.swarm.connector;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import com.buglabs.module.IModuleControl;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.OSGiServiceLoader;

public class OSGiHelper implements ServiceListener {

	private static BundleContext context;
	private static OSGiHelper ref;

	private List<IModuleControl> moduleList;
	private List<PublicWSProvider> serviceList;

	private OSGiHelper() throws Exception {
		context = Activator.getContext();
		initializeModuleProviders();
		initializeWSProviders();
		context.addServiceListener(this);
	}

	public static OSGiHelper getRef() throws Exception {
		if (ref == null)
			ref = new OSGiHelper();

		return ref;
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

	public List getBUGFeeds() {
		return new ArrayList();
	}

	private void initializeModuleProviders() throws Exception {		
		if (moduleList == null)
			moduleList = new ArrayList<IModuleControl>();
		else 
			moduleList.clear();

		synchronized (moduleList) {
			OSGiServiceLoader.loadServices(context, IModuleControl.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
				public void load(Object service) throws Exception {
					if (!moduleList.contains(service)) {
						moduleList.add((IModuleControl) service);
					}
				}
			});
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
	
		synchronized (serviceList) {
			OSGiServiceLoader.loadServices(context, PublicWSProvider.class.getName(), null, new OSGiServiceLoader.IServiceLoader() {
				public void load(Object service) throws Exception {
					if (!serviceList.contains(service)) {
						serviceList.add((PublicWSProvider) service);
					}
				}
			});
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
		for (PublicWSProvider p: serviceList)
			if (p.getPublicName().equals(name))
				return p;
		
		return null;
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		if (isValidEvent(event)) {
			//To keep the code simple, just repopulate the Module and Service lists from the service registry.
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

}
