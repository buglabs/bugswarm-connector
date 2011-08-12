package com.buglabs.bug.swarm.connector.osgi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.buglabs.bug.dragonfly.module.IModuleControl;
import com.buglabs.bug.dragonfly.module.IModuleProperty;

/**
 * Adapts IModule control to Feed.
 * 
 * @author kgilmer
 *
 */
public class ModuleFeedAdapter extends Feed {

	/**
	 * @param feedName name of feed
	 * @param feed contents of feed
	 */
	public ModuleFeedAdapter(final String feedName, final Map<?, ?> feed) {
		super(feedName, feed);
		
	}

	/**
	 * @param moduleControl instance of IModuleControl to create the feed from
	 */
	public ModuleFeedAdapter(final IModuleControl moduleControl) {
		super(moduleControl.getModuleName()
				+ moduleControl.getSlotId(), 
				adaptModulePropertiesToFeedMap(moduleControl.getModuleProperties()));
	}

	/**
	 * @param moduleProperties List of IModuleProperty
	 * @return A Feed map from list of module properties.
	 */
	public static Map<?, ?> adaptModulePropertiesToFeedMap(final List moduleProperties) {
		Map<String, Object> m = new HashMap<String, Object>();
		
		for (Iterator i = moduleProperties.iterator(); i.hasNext();) {
			IModuleProperty mp = (IModuleProperty) i.next();
			
			m.put(mp.getName(), mp.getValue());
		}
		
		return m;
	}

}
