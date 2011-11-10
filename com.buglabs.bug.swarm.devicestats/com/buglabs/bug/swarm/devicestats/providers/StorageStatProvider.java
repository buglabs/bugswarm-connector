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
 * Provide storage stats.
 * 
 * @author kgilmer
 *
 */
public class StorageStatProvider implements DeviceStatProviderService {

	private final ShellSession session;
	private final String lineSeperator;

	public StorageStatProvider(ShellSession session) {
		this.session = session;
		lineSeperator = System.getProperty("line.separator");
	}
	
	@Override
	public void addStats(Map<String, Serializable> propertyMap) {
		String rawResponse;
		try {
			rawResponse = session.execute("df -k");			

			if (rawResponse != null && rawResponse.trim().length() > 0) {
				List<String> lines = Arrays.asList(rawResponse.split(lineSeperator));
				
				for (String line : lines) {
					String [] elems = line.split(" ");
					
					if (elems[0].equals("rootfs")) {
						addDfData(propertyMap, "rootfs", elems);
					} else if (elems[0].contains("mmcblk1")) {
						addDfData(propertyMap, elems[0], elems);
					}
				}
			}
		} catch (IOException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Failed to add storage stats.", e);
		}
		
	}

	/**
	 * Add the relevant info from df output to the property map.
	 * @param propertyMap
	 * @param deviceName
	 * @param lineSegments
	 */
	private void addDfData(Map<String, Serializable> propertyMap, String deviceName, String[] lineSegments) {
		propertyMap.put("storage." + deviceName + ".total", lineSegments[1]);
		propertyMap.put("storage." + deviceName + ".used", lineSegments[2]);
		propertyMap.put("storage." + deviceName + ".available", lineSegments[3]);		
	}

}
