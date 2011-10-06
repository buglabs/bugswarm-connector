package com.buglabs.bug.swarm.connector.osgi;

import java.util.HashMap;
import java.util.Map;

import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider;

/**
 * Adapts PublicWSProvider to Feed.
 * 
 * @author kgilmer
 * 
 */
public class ServiceFeedAdapter extends Feed {

	/**
	 * @param service
	 *            ws provider to adapt to a feed
	 */
	public ServiceFeedAdapter(final PublicWSProvider service) {
		super(service.getPublicName(), adaptServiceToFeedMap(service));
	}

	/**
	 * @param service
	 *            ws provider to create feed from
	 * @return feed as Map of service
	 */
	public static Map<?, ?> adaptServiceToFeedMap(final PublicWSProvider service) {
		Map<String, Object> sm = new HashMap<String, Object>();
		PublicWSDefinition def = service.discover(PublicWSProvider.GET);

		if (def != null)
			sm.put("GET", adaptServiceDefinition(def));

		def = service.discover(PublicWSProvider.POST);
		if (def != null)
			sm.put("POST", adaptServiceDefinition(def));

		def = service.discover(PublicWSProvider.PUT);
		if (def != null)
			sm.put("PUT", adaptServiceDefinition(def));

		def = service.discover(PublicWSProvider.DELETE);
		if (def != null)
			sm.put("DELETE", adaptServiceDefinition(def));

		return sm;
	}

	/**
	 * @param def
	 *            ws definition
	 * @return map to adapt feed
	 */
	private static Map<String, Object> adaptServiceDefinition(final PublicWSDefinition def) {
		Map<String, Object> sm = new HashMap<String, Object>();
		sm.put("returns", def.getReturnType());
		sm.put("params", def.getParameters());

		return sm;
	}

}
