package com.buglabs.bug.swarm.connector.osgi;

import java.util.List;
import java.util.Map;

import com.buglabs.module.IModuleControl;

public class ModuleFeed extends BUGSwarmFeed {

	public ModuleFeed(String feedName, Map<?, ?> feed) {
		super(feedName, feed);
		
	}

	public ModuleFeed(IModuleControl service) {
		super(service.getModuleName() + service.getSlotId(), adaptModulePropertiesToFeedMap(service.getModuleProperties()));
	}

	public static Map<?, ?> adaptModulePropertiesToFeedMap(List moduleProperties) {
	
		return null;
	}

}
