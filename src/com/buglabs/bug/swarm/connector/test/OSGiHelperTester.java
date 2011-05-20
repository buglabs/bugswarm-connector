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

	
	public static void loadMockFeedProviders(Map<Object, Feed> feeds) {
		Map<String, String> f1 = new HashMap<String, String>();
		feeds.put(f1, new Feed("feed1", f1));
		f1 = new HashMap<String, String>();
		feeds.put(f1, new Feed("feed2", new HashMap<String, String>()));
		f1 = new HashMap<String, String>();
		feeds.put(f1, new Feed("feed3", new HashMap<String, String>()));
	}

	public static void loadMockIModuleControls(Map<Object, Feed> feeds) {
		IModuleControl mc = new MockIModuleControl("GPS", 1, createMockProperties());
		feeds.put(mc, Feed.createForType(mc));
		mc = new MockIModuleControl("LCD", 2, createMockProperties());
		feeds.put(mc, Feed.createForType(mc));
		mc = new MockIModuleControl("CAMERA", 3, createMockProperties());
		feeds.put(mc, Feed.createForType(mc));
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

	public static void loadMockPublicWSProviders(Map<Object, Feed> feeds) {
		PublicWSProvider wsp = new MockPublicWSProvider("Picture", "Take a picture using the camera module.");
		feeds.put(wsp, Feed.createForType(wsp));
		
		wsp = new MockPublicWSProvider("Location", "Determine your location using GPS services.");
		feeds.put(wsp, Feed.createForType(wsp));
	}
}
