package com.buglabs.bug.swarm.connector;

import java.util.List;

import com.buglabs.module.IModuleControl;
import com.buglabs.services.ws.PublicWSProvider;
import com.buglabs.util.XmlNode;

/**
 * This stateless class handles all xml message creation for bugswarm-connector.
 * @author kgilmer
 *
 */
public class XmlMessageCreator {

	/**
	 * See https://github.com/buglabs/bugswarm/wiki/Advertise-Member-Capabilities (step 6)
	 * 
	 * @param services
	 * @param modules
	 * @param feeds
	 * @return
	 */
	public static XmlNode createServiceModuleFeedDocument(List<PublicWSProvider> services, List<IModuleControl> modules, List feeds) {
		return null;
	}
}
