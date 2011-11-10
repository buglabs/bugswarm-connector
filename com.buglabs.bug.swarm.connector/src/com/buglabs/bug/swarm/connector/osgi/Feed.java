package com.buglabs.bug.swarm.connector.osgi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

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
	 * OSGi service property key for the feed update time.
	 */
	public static final String FEED_SERVICE_TIMESTAMP_PROPERTY = "SWARM.FEED.TIMESTAMP";
	
	/**
	 * OSGi service property key to denote a Binary Feed.
	 */
	public static final String FEED_SERVICE_BINARY_PROPERTY = "SWARM.FEED.BINARY";

	/**
	 * Name of feed.
	 */
	private final String feedName;

	/**
	 * Name/value pairs of Feed.
	 */
	private Map<String, Object> feed;

	private ServiceRegistration serviceRegistration;

	/**
	 * @param feedName
	 *            Name of feed
	 * @param feed
	 *            feed contents
	 */
	public Feed(final String feedName, final Map<String, Object> feed) {
		//The server specifies the feed name as "feeds" if it wants
		//a list of all available feeds.
		if (feedName.equalsIgnoreCase("feeds")) 
			throw new IllegalArgumentException("'feeds' is special and cannot be used as a feed name.");
		
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
	public Map<String, Object> getFeed() {
		return feed;
	}

	@Override
	public boolean equals(final Object obj) {
		// Feed names are unique
		if (obj instanceof Feed)
			return ((Feed) obj).getName().equals(feedName);

		return super.equals(obj);
	}
	
	public void register(BundleContext context) {
		serviceRegistration = context.registerService(
				Map.class.getName(), 
				feed, 
				OSGiHelper.toDictionary(
						FEED_SERVICE_NAME_PROPERTY, feedName,
						FEED_SERVICE_TIMESTAMP_PROPERTY, Long.toString(System.currentTimeMillis())));
	}
	
	public void unregister() {
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}			
	}
	
	public void update(Map<String, Object> feed) {
		this.feed = feed;
		
		if (serviceRegistration != null)
			serviceRegistration.setProperties(OSGiHelper.toDictionary(
							FEED_SERVICE_NAME_PROPERTY, feedName,
							FEED_SERVICE_TIMESTAMP_PROPERTY, Long.toString(System.currentTimeMillis())));
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
		if (input instanceof PublicWSProvider)
			return new ServiceFeedAdapter((PublicWSProvider) input);

		if (input instanceof ServiceReference) {
			ServiceReference sr = (ServiceReference) input;
			if (sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY) != null) {

				if (sr.getProperty(Feed.FEED_SERVICE_BINARY_PROPERTY) == null
						|| !Boolean.parseBoolean(sr.getProperty(Feed.FEED_SERVICE_BINARY_PROPERTY).toString())) {
					return new Feed((String) sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY), (Map<String, Object>) Activator.getContext()
							.getService(sr));
				} else {
					return new BinaryFeed((String) sr.getProperty(Feed.FEED_SERVICE_NAME_PROPERTY), (Map<String, Object>) Activator.getContext()
							.getService(sr));
				}
			}
		}
		
		if (input instanceof IModuleControl)
			return new ModulesFeed(Activator.getContext());

		throw new IllegalArgumentException("Unable to create Feed for type " + input.getClass().getName());
	}
	
	/**
	 * Given a variable number of <String, Object> pairs, construct a Map and
	 * return it with values loaded.
	 * 
	 * @param elements
	 *            name1, value1, name2, value2...
	 * @return a Map and return it with values loaded.
	 */
	protected static Map<String, Object> toMap(Object... elements) {
		if (elements.length % 2 != 0) {
			throw new IllegalStateException("Input parameters must be even.");
		}

		Iterator<Object> i = Arrays.asList(elements).iterator();
		Map<String, Object> m = new HashMap<String, Object>();

		while (i.hasNext()) {
			m.put(i.next().toString(), i.next());
		}

		return m;
	}
}
