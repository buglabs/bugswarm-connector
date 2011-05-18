package com.buglabs.bug.swarm.connector.osgi;

import java.util.Map;

import com.buglabs.services.ws.PublicWSProvider;

public class ServiceFeed extends BUGSwarmFeed {

	public ServiceFeed(PublicWSProvider service) {
		super(service.getPublicName(), adaptServiceToFeedMap(service));
	}

	public static Map<?, ?> adaptServiceToFeedMap(PublicWSProvider service) {
	
		return null;
	}

}
