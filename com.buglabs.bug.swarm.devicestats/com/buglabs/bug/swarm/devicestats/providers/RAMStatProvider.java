package com.buglabs.bug.swarm.devicestats.providers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.devicestats.Activator;
import com.buglabs.bug.swarm.devicestats.pub.DeviceStatProviderService;
import com.buglabs.util.shell.pub.ShellSession;


/**
 * Provide RAM usage stats.
 * 
 * @author kgilmer
 *
 */
public class RAMStatProvider implements DeviceStatProviderService {

	private final ShellSession session;
	private final String lineSeperator;

	public RAMStatProvider(ShellSession session) {
		this.session = session;
		lineSeperator = System.getProperty("line.separator");
	}
	
	@Override
	public void addStats(Map<String, Serializable> propertyMap) {
		String rawResponse;
		try {
			rawResponse = session.execute("free -b");			

			if (rawResponse != null && rawResponse.trim().length() > 0) {
				List<String> lines = Arrays.asList(rawResponse.split(lineSeperator));
				
				for (String line : lines) {
					String [] elems = StorageStatProvider.filterWhitespace(line.split(" "));
					
					if (elems[0].equals("Mem:")) {
						addFreeData(propertyMap, "ram", elems);
					} else if (elems[0].equals("Swap:")) {
						addFreeData(propertyMap, "swap", elems);
					}
				}
			}
		} catch (IOException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Failed to add ram stats.", e);
		}
		
	}

	/**
	 * Add the relevant info from df output to the property map.
	 * @param propertyMap
	 * @param deviceName
	 * @param lineSegments
	 */
	private void addFreeData(Map<String, Serializable> propertyMap, String deviceName, String[] lineSegments) {
		propertyMap.put(deviceName + ".total", lineSegments[1]);
		propertyMap.put(deviceName + ".used", lineSegments[2]);
		propertyMap.put(deviceName + ".available", lineSegments[3]);		
	}

}
