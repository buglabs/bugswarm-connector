package com.buglabs.bug.swarm.connector.osgi;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.sprinkles.Applier;
import org.sprinkles.Mapper;

import com.buglabs.bug.dragonfly.module.IModuleControl;

/**
 * A Feed for BUG modules.  Integrates with OSGi service registry to provide
 * updates to swarm when module state changes on device.
 * 
 * Example:
 * {
	    "modules": {
	        "slot1": "gps",
	        "slot3": "lcd",
	        "slot4": "video",
	        "stinger": ["4gmodem", "3gmodem", "keyboard"]
	    }
	}
 * @author kgilmer
 *
 */
public class ModulesFeed extends Feed implements ServiceListener {

	private final BundleContext context;

	/**
	 * @param context BundleContext
	 */
	public ModulesFeed(BundleContext context) {		
		super("modules", getModules(context, new HashMap<String, Object>()));
		this.context = context;		
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		update(getModules(context, super.getFeed()));
	}

	protected static Map<String, Object> getModules(final BundleContext context, final Map<String, Object> feedMap) {
		feedMap.clear();
		
		try {
			Mapper.map(new Applier.Fn<ServiceReference, Object>() {
	
				@Override
				public Object apply(ServiceReference input) {
					IModuleControl imc = (IModuleControl) context.getService(input);
					
					feedMap.put("slot" + imc.getSlotId(), imc.getModuleName().toLowerCase());
					
					Activator.getLog().log(
							LogService.LOG_DEBUG, 
								ModulesFeed.class.getSimpleName() + " updated with " + imc.getModuleName().toLowerCase() + " in slot " + imc.getSlotId());
					
					return imc;
				}
				
			}, context.getServiceReferences(IModuleControl.class.getName(), null));
		} catch (InvalidSyntaxException e) {
			throw new IllegalStateException(e);
		}
		
		return feedMap;
	}
}
