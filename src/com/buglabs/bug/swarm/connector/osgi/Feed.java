package com.buglabs.bug.swarm.connector.osgi;

import java.util.Map;

import org.osgi.framework.ServiceReference;

import com.buglabs.module.IModuleControl;
import com.buglabs.services.ws.PublicWSProvider;

/**
 * Internally represents a swarm-feed.  Essentially a named Map.
 * 
 * @author kgilmer
 * 
 */
public class Feed {
	/**
	 * OSGi service property key for the feed name.
	 */
	public static final String FEED_SERVICE_NAME_PROPERTY = "SWARM.FEED.NAME";
	
	private String feedName;
	private Map<?, ?> feed;

	protected Feed(String feedName, Map<?, ?> feed) {

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
		if (obj instanceof Feed)
			return ((Feed) obj).getName().equals(feedName);
			
		return super.equals(obj);
	}
	
	/**
	 * Convenience factory for feed type.
	 * @param input
	 * @return
	 */
	public static Feed createForType(Object input) {
		if (input instanceof IModuleControl)
			return new ModuleFeedAdapter((IModuleControl) input);
		
		if (input instanceof PublicWSProvider)
			return new ServiceFeedAdapter((PublicWSProvider) input);
		
		if (input instanceof ServiceReference) {
			ServiceReference sr = (ServiceReference) input;
			if (sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY) != null) 
				return new Feed((String) sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY), (Map <?, ?>) Activator.getContext().getService(sr));
		}
		
		return null;
	}
}