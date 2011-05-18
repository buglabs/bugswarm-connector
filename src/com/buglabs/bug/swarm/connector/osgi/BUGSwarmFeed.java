package com.buglabs.bug.swarm.connector.osgi;

import java.util.Map;

import org.osgi.framework.ServiceReference;

import com.buglabs.module.IModuleControl;
import com.buglabs.services.ws.PublicWSProvider;

/**
 * Internall represents a swarm-feed.
 * 
 * @author kgilmer
 * 
 */
public class BUGSwarmFeed {
	public static final String FEED_SERVICE_NAME_PROPERTY = "SWARM.FEED.NAME";
	
	private String feedName;
	private Map<?, ?> feed;

	protected BUGSwarmFeed(String feedName, Map<?, ?> feed) {

		this.feedName = feedName;
		this.feed = feed;
	}

	public String getName() {
		return feedName;
	}

	public Map<?, ?> getFeed() {
		return feed;
	}
	
	@Override
	public boolean equals(Object obj) {
		//Feed names are unique
		if (obj instanceof BUGSwarmFeed)
			return ((BUGSwarmFeed) obj).getName().equals(feedName);
			
		return super.equals(obj);
	}
	
	/**
	 * Convenience factory for feed type.
	 * @param input
	 * @return
	 */
	public static BUGSwarmFeed createForType(Object input) {
		if (input instanceof IModuleControl)
			return new ModuleFeed((IModuleControl) input);
		
		if (input instanceof PublicWSProvider)
			return new ServiceFeed((PublicWSProvider) input);
		
		if (input instanceof ServiceReference) {
			ServiceReference sr = (ServiceReference) input;
			if (sr.getProperty(BUGSwarmFeed.FEED_SERVICE_NAME_PROPERTY) != null) 
				return new BUGSwarmFeed((String) sr.getProperty(BUGSwarmFeed.FEED_SERVICE_NAME_PROPERTY), (Map <?, ?>) Activator.getContext().getService(sr));
		}
		
		return null;
	}
}