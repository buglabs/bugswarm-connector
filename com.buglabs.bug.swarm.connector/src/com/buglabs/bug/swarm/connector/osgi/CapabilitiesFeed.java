package com.buglabs.bug.swarm.connector.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.sprinkles.Applier.Fn;
import org.sprinkles.Mapper;

/**
 * Implements the capabilities feed.
 * 
 * See https://github.com/buglabs/bugswarm-connector/issues/20
 * 
 * Example:
 * {
    "capabilities": {
        "modules": {
            "slot1": "gps",
            "slot2": "camera",
            "slot3": "lcd",
            "slot4": "video",
            "stinger": ["4gmodem", "3gmodem", "keyboard"]
        },
        "feeds": ["picture", "location", "acceleration"]
    }
}
 * @author kgilmer
 *
 */
public class CapabilitiesFeed extends Feed {

	private final ModulesFeed modulesFeed;
	private final BundleContext context;

	/**
	 * @param context BundleContext
	 * @param modulesFeed ModulesFeed
	 */
	public CapabilitiesFeed(BundleContext context, ModulesFeed modulesFeed) {
		super("capabilities", Collections.EMPTY_MAP);
		this.context = context;	
		this.modulesFeed = modulesFeed;
	}

	
	@Override
	public Map<?, ?> getFeed() {
		
		try {
			Map<String, Object> cfeed = new HashMap<String, Object>();
			
			cfeed.put("modules", modulesFeed);
			
			cfeed.put("feeds", getNonSpecialFeeds());
			
			return cfeed;
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}		
	}


	/**
	 * @return All feed names except modules and capabilities.
	 * 
	 * @throws InvalidSyntaxException should never be thrown
	 */
	private List<String> getNonSpecialFeeds() throws InvalidSyntaxException {
		Collection<String> feedNames = Mapper.map(new Fn<ServiceReference, String>() {

			@Override
			public String apply(ServiceReference input) {				
				String name = (String) input.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY);
				
				if (name.equals("modules") || name.equals("capabilities"))
					return null;
				
				return name;
			}
			
		}, context.getServiceReferences(Map.class.getName(), null));
			
		return new ArrayList<String>(feedNames);
	}
}
