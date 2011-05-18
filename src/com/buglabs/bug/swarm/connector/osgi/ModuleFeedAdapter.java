package com.buglabs.bug.swarm.connector.osgi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleProperty;

/**
 * Adapts IModule control to Feed.
 * 
 * @author kgilmer
 *
 */
public class ModuleFeedAdapter extends Feed {

	public ModuleFeedAdapter(String feedName, Map<?, ?> feed) {
		super(feedName, feed);
		
	}

	public ModuleFeedAdapter(IModuleControl service) {
		super(service.getModuleName() + service.getSlotId(), adaptModulePropertiesToFeedMap(service.getModuleProperties()));
	}

	public static Map<?, ?> adaptModulePropertiesToFeedMap(List moduleProperties) {
		Map<String, Object> m = new HashMap<String, Object>();
		
		for (Iterator i = moduleProperties.iterator(); i.hasNext();) {
			IModuleProperty mp = (IModuleProperty) i.next();
			
			m.put(mp.getName(), mp.getValue());
		}
		
		return m;
	}

}
