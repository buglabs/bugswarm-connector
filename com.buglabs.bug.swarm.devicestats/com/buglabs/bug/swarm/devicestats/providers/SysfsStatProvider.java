package com.buglabs.bug.swarm.devicestats.providers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.devicestats.Activator;
import com.buglabs.bug.swarm.devicestats.pub.DeviceStatProviderService;


/**
 * Statistics base class with helper methods to read from sysfs.
 * 
 * @author kgilmer
 *
 */
public abstract class SysfsStatProvider implements DeviceStatProviderService {

	protected void addFileToMap(Map<String, Serializable> propertyMap, String name, File file) throws IOException {
		if (file.exists()) {
			propertyMap.put(name, readFile(file));
		} else {
			Activator.getLog().log(LogService.LOG_ERROR, "Sysfs file does not exist: " + file);
		}
	}

	protected Serializable readFile(File f) throws IOException {
		StringBuilder sb = new StringBuilder();

		BufferedReader br = new BufferedReader(new FileReader(f));

		sb.append(br.readLine());

		br.close();

		return sb.toString();
	}

}
