package com.buglabs.bug.swarm.devicestats.providers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.devicestats.Activator;
import com.buglabs.bug.swarm.devicestats.pub.DeviceStatProviderService;

/**
 * Stat provider for the in-base BUG battery.
 * @author kgilmer
 *
 */
public class BatteryStatProvider extends SysfsStatProvider implements DeviceStatProviderService {

	@Override
	public void addStats(Map<String, Serializable> propertyMap) {
		File powerBase = new File("/sys/devices/platform/i2c_omap.2/i2c-2/2-0055/power_supply/bq27500-0");
		File capacity = new File(powerBase, "capacity");
		File currentNow = new File(powerBase, "current_now");
		File present = new File(powerBase, "present");
		File status = new File(powerBase, "status");
		File temp = new File(powerBase, "temp");
		File voltageNow = new File(powerBase, "voltage_now");

		try {
			addFileToMap(propertyMap, "battery.capacity", capacity);
			addFileToMap(propertyMap, "battery.current", currentNow);
			addFileToMap(propertyMap, "battery.present", present);
			addFileToMap(propertyMap, "battery.status", status);
			addFileToMap(propertyMap, "battery.temp", temp);
			addFileToMap(propertyMap, "battery.voltage", voltageNow);
		} catch (IOException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Failed to add battery stats.", e);
		}
	}
}
