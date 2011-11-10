package com.buglabs.bug.swarm.devicestats.providers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.devicestats.Activator;
import com.buglabs.bug.swarm.devicestats.pub.DeviceStatProviderService;


/**
 * Statistics provider for wifi device.
 * 
 * @author kgilmer
 *
 */
public class WifiStatProvider extends SysfsStatProvider implements DeviceStatProviderService {

	@Override
	public void addStats(Map<String, Serializable> propertyMap) {
		File wifiBase = new File("/sys/class/net/wlan0/");
		File wifiState = new File(wifiBase, "operstate");
		File wifiLevel = new File(wifiBase, "wireless/level");
		File wifiNoise = new File(wifiBase, "wireless/noise");
		File wifiRxPackets = new File(wifiBase, "statistics/rx_packets");
		File wifiTxPackets = new File(wifiBase, "statistics/tx_packets");

		try {
			addFileToMap(propertyMap, "wifi.status", wifiState);
			addFileToMap(propertyMap, "wifi.signal", wifiLevel);
			addFileToMap(propertyMap, "wifi.noise", wifiNoise);
			addFileToMap(propertyMap, "wifi.packets.rx", wifiRxPackets);
			addFileToMap(propertyMap, "wifi.packets.tx", wifiTxPackets);
		} catch (IOException e) {
			Activator.getLog().log(LogService.LOG_ERROR, "Failed to add wifi stats.", e);
		}
	}
}
