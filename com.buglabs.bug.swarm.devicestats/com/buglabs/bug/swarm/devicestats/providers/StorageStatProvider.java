package com.buglabs.bug.swarm.devicestats.providers;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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

	private static final String COMMAND = "df -k";
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
			rawResponse = session.execute(COMMAND);			

			if (rawResponse != null && rawResponse.trim().length() > 0) {
				List<String> lines = Arrays.asList(rawResponse.split(lineSeperator));
				
				for (String line : lines) {
					String [] elems = filterWhitespace(line.split(" "));
					
					if (elems[0].equals("rootfs")) {
						addDfData(propertyMap, "rootfs", elems);
					} else if (elems[0].contains("mmcblk1")) {
						addDfData(propertyMap, "mmcblk1", elems);
					}
				}
			} else {
				Activator.getLog().log(LogService.LOG_WARNING, "Command returned no usable data:" + COMMAND);
			}
		} catch (IOException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Failed to add storage stats.", e);
		}
		
	}

	protected static String[] filterWhitespace(String[] elems) {
		List<String> nl = new ArrayList<String>();
		
		for (int i = 0; i < elems.length; ++i)
			if (elems[i].trim().length() > 0)
				nl.add(elems[i]);
		
		return nl.toArray(new String[nl.size()]);
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
