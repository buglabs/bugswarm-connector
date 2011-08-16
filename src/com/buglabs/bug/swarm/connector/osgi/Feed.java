package com.buglabs.bug.swarm.connector.osgi;

import java.util.Map;

import org.osgi.framework.ServiceReference;

import com.buglabs.bug.dragonfly.module.IModuleControl;
import com.buglabs.services.ws.PublicWSProvider;

/**
 * Internally represents a swarm-feed. Essentially a named Map.
 * 
 * @author kgilmer
 * 
 */
public class Feed {
	/**
	 * OSGi service property key for the feed name.
	 */
	public static final String FEED_SERVICE_NAME_PROPERTY = "SWARM.FEED.NAME";
	/**
	 * OSGi service property key to denote a Binary Feed.
	 */
	public static final String FEED_SERVICE_BINARY_PROPERTY = "SWARM.FEED.BINARY";

	/**
	 * Name of feed.
	 */
	private String feedName;

	/**
	 * Name/value pairs of Feed.
	 */
	private Map<?, ?> feed;

	/**
	 * @param feedName
	 *            Name of feed
	 * @param feed
	 *            feed contents
	 */
	public Feed(final String feedName, final Map<?, ?> feed) {

		this.feedName = feedName;
		this.feed = feed;
	}

	/**
	 * @return Name of feed
	 */
	public String getName() {
		return feedName;
	}

	/**
	 * @return Feed contents
	 */
	public Map<?, ?> getFeed() {
		return feed;
	}

	@Override
	public boolean equals(final Object obj) {
		// Feed names are unique
		if (obj instanceof Feed)
			return ((Feed) obj).getName().equals(feedName);

		return super.equals(obj);
	}

	/**
	 * Convenience factory for feed type.
	 * 
	 * @param input
	 *            IModuleControl, PublicWSProvider, or ServiceReference of Map.
	 *            Other types will yield null on return.
	 * @return new Feed based on input
	 */
	public static Feed createForType(final Object input) {
		if (input instanceof IModuleControl)
			return new ModuleFeedAdapter((IModuleControl) input);

		if (input instanceof PublicWSProvider)
			return new ServiceFeedAdapter((PublicWSProvider) input);

		if (input instanceof ServiceReference) {
			ServiceReference sr = (ServiceReference) input;
			if (sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY) != null) {

				if (sr.getProperty(Feed.FEED_SERVICE_BINARY_PROPERTY) == null
						|| !Boolean.parseBoolean(sr.getProperty(Feed.FEED_SERVICE_BINARY_PROPERTY).toString())) {
					return new Feed((String) sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY), (Map<?, ?>) Activator.getContext()
							.getService(sr));
				} else {
					return new BinaryFeed((String) sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY), (Map<?, ?>) Activator.getContext()
							.getService(sr));
				}
			}
		}

		return null;
	}
}
