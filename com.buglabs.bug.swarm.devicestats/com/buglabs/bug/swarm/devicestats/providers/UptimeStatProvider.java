package com.buglabs.bug.swarm.devicestats.providers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.devicestats.Activator;
import com.buglabs.bug.swarm.devicestats.pub.DeviceStatProviderService;
import com.buglabs.util.shell.pub.ShellSession;


/**
 * Provide Uptime stats.
 * 
 * @author kgilmer
 *
 */
public class UptimeStatProvider implements DeviceStatProviderService {

	private static final String COMMAND = "uptime";
	private final ShellSession session;
	private final String lineSeperator;

	/**
	 * @param session
	 */
	public UptimeStatProvider(ShellSession session) {
		this.session = session;
		lineSeperator = System.getProperty("line.separator");
	}
	
	@Override
	public void addStats(Map<String, Serializable> propertyMap) {
		String rawResponse;
		try {
			rawResponse = session.execute(COMMAND);			

			if (rawResponse != null && rawResponse.trim().length() > 0) {
				String [] elems = StorageStatProvider.filterWhitespace(rawResponse.split(" "));
				
				if (elems != null && elems.length < 4) {
					propertyMap.put("uptime", elems[0]);
					propertyMap.put("load", elems[4]);
					propertyMap.put("users", elems[1]);
				}						
			} else {
				Activator.getLog().log(LogService.LOG_WARNING, "Command returned no usable data:" + COMMAND);
			}
		} catch (IOException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Failed to add uptime stats.", e);
		}		
	}
}
