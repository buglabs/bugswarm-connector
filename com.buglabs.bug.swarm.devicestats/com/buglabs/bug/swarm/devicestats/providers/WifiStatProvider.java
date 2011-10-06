package com.buglabs.bug.swarm.devicestats.providers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import com.buglabs.bug.swarm.devicestats.pub.DeviceStatProviderService;


/**
 * Statistics provider for wifi device.
 * 
 * @author kgilmer
 *
 */
public class WifiStatProvider implements DeviceStatProviderService {

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
			logError("I/O error occurred: " + e.getMessage());
		}
	}

	private void addFileToMap(Map<String, Serializable> propertyMap, String name, File wifiState) throws IOException {
		if (wifiState.exists()) {
			propertyMap.put(name, readFile(wifiState));
		} else {
			logError("Unable to load file " + wifiState);
		}
	}

	/**
	 * TODO: Tie into logging
	 * 
	 * @param error
	 */
	private void logError(String error) {
		System.err.println(error);
	}

	private Serializable readFile(File f) throws IOException {
		StringBuilder sb = new StringBuilder();

		BufferedReader br = new BufferedReader(new FileReader(f));

		sb.append(br.readLine());

		br.close();

		return sb.toString();
	}

}
