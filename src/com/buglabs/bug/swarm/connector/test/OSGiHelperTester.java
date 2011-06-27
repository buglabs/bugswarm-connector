package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.buglabs.bug.swarm.connector.osgi.Feed;
import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleProperty;
import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider;

public class OSGiHelperTester {

	
	public static void loadMockFeedProviders(Map<Object, Feed> feeds, Map<String, Feed> feeds2) {
		Map<String, String> f1 = generateRandomMap();
		Feed f = new Feed("feed1", f1);
		feeds.put(f1, f);
		feeds2.put(f.getName(), f);
		f1 = generateRandomMap();
		f = new Feed("feed2", f1);
		feeds.put(f1, f);
		feeds2.put(f.getName(), f);
		f1 = generateRandomMap();
		f = new Feed("feed3", f1);
		feeds.put(f1, f);
		feeds2.put(f.getName(), f);
	}

	private static Map<String, String> generateRandomMap() {
		Random r = new Random();
		Map<String, String> map = new HashMap<String, String>();
		
		for (int i = 0; i < (5 + r.nextInt(10)); ++i) {
			map.put("key-" + r.nextFloat(), "val:" + r.nextDouble());
		}
		
		return map;
	}

	public static void loadMockIModuleControls(Map<Object, Feed> feeds, Map<String, Feed> feeds2) {
		IModuleControl mc = new MockIModuleControl("GPS", 1, createMockProperties());
		Feed f = Feed.createForType(mc);
		feeds.put(mc, f);
		feeds2.put(f.getName(), f);
		
		mc = new MockIModuleControl("LCD", 2, createMockProperties());
		f = Feed.createForType(mc);
		feeds.put(mc, f);
		feeds2.put(f.getName(), f);
		
		mc = new MockIModuleControl("CAMERA", 3, createMockProperties());
		f = Feed.createForType(mc);
		feeds.put(mc, f);
		feeds2.put(f.getName(), f);
	}

	private static List createMockProperties() {
		List l = new ArrayList();
		final Random r = new Random();
		l.add(new IModuleProperty() {

			@Override
			public boolean isMutable() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Object getValue() {

				return "" + r.nextDouble();
			}

			@Override
			public String getType() {
				// TODO Auto-generated method stub
				return "String";
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "" + r.nextDouble();
			}
		});

		return l;
	}

	public static void loadMockPublicWSProviders(Map<Object, Feed> feeds, Map<String, Feed> feeds2) {
		PublicWSProvider wsp = new MockPublicWSProvider("Picture", "Take a picture using the camera module.");
		Feed feed = Feed.createForType(wsp);
		feeds.put(wsp, feed);
		feeds2.put(feed.getName(), feed);
		
		wsp = new MockPublicWSProvider("Location", "Determine your location using GPS services.");
		feed = Feed.createForType(wsp);
		feeds.put(wsp, feed);
		feeds2.put(feed.getName(), feed);
	}
}
