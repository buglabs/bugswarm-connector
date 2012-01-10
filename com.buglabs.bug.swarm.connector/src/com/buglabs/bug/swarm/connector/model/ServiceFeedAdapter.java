package com.buglabs.bug.swarm.connector.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.service.log.LogService;

import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider;

/**
 * Adapts PublicWSProvider to Feed.
 * 
 * @author kgilmer
 * 
 */
public class ServiceFeedAdapter extends Feed {

	private static final int GET = 1;
	private static final int PUT = 2;
	private static final int POST = 3;
	private static final int DELETE = 4;
	  
	private final PublicWSProvider service;
	
	private final static ObjectMapper mapper = new ObjectMapper();

	/**
	 * @param service
	 *            ws provider to adapt to a feed
	 */
	public ServiceFeedAdapter(final PublicWSProvider service) {
		super(service.getPublicName(), adaptServiceToFeedMap(service));
		System.out.println("service: "+service.getPublicName());
		this.service = service;
	}
	
	/**
	 * @param parameters parameters for method
	 * @return contents of the GET method if is text data
	 * @throws IOException if unable to call method or error in call.
	 */
	public String callGet(String parameters) throws IOException {
		System.out.println(parameters);
		IWSResponse response = service.execute(GET, parameters);
		
		
		if (response != null && response.getMimeType().indexOf("text") > -1) {
			//Response is text.  Wrap it in JSon.
			Map<String, String> m = new HashMap<String, String>();
			m.put(service.getPublicName(), response.getContent().toString());
			
			return mapper.writeValueAsString(m);
		}
		if (response != null && response.getMimeType().indexOf("image") > -1) {
			
		}
		if (response == null){			
			return "";
		}
		throw new IOException("Unable to execute method.");
	}

	/**
	 * @param service
	 *            ws provider to create feed from
	 * @return feed as Map of service
	 */
	public static Map<String, Object> adaptServiceToFeedMap(final PublicWSProvider service) {
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
