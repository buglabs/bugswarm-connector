package com.buglabs.bug.swarm.connector.xmpp;

import java.util.List;

import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleProperty;
import com.buglabs.services.ws.PublicWSDefinition;
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
		XmlNode root = new XmlNode("device_info");
		
		//Services
		XmlNode servicesNode = new XmlNode(root, "services");
		for (PublicWSProvider service: services)
			servicesNode.addChild(createServiceNode(service));
		
		//Modules
		XmlNode modulesNode = new XmlNode(root, "modules");
		for (IModuleControl module: modules)
			modulesNode.addChild(createModuleNode(module));
		
		//Feeds
		//Need futher definition of feeds
		
		return root;
	}

	/**
	 * Create xml document for a module.
	 * 
	 * @param module
	 * @return
	 */
	private static XmlNode createModuleNode(IModuleControl module) {
		XmlNode n = new XmlNode(module.getModuleName());
		n.addAttribute("slot", ""+ module.getSlotId());
		
		for (Object o: module.getModuleProperties()) {
			IModuleProperty mp = (IModuleProperty) o;
			
			XmlNode pn = new XmlNode(n, "property");
			pn.addAttribute("name", mp.getName());
			pn.addAttribute("value", mp.getValue().toString());
		}
		
		return n;
	}

	/**
	 * Create xml document for a service.
	 * 
	 * @param service
	 * @return
	 */
	private static XmlNode createServiceNode(PublicWSProvider service) {
		XmlNode n = new XmlNode(service.getPublicName());
		n.addAttribute("description", "" + service.getDescription());
		
		PublicWSDefinition method = service.discover(PublicWSProvider.GET);
		if (method != null) {
			XmlNode methodXml = new XmlNode(n, "GET");
			for (Object param: method.getParameters())
				new XmlNode(methodXml, "parameter").addAttribute("name", param.toString());
			
			methodXml.addAttribute("returns", method.getReturnType());
		}
		
		method = service.discover(PublicWSProvider.DELETE);
		if (method != null) {
			XmlNode methodXml = new XmlNode(n, "DELETE");
			for (Object param: method.getParameters())
				new XmlNode(methodXml, "parameter").addAttribute("name", param.toString());
			
			methodXml.addAttribute("returns", method.getReturnType());
		}
		
		method = service.discover(PublicWSProvider.POST);
		if (method != null) {
			XmlNode methodXml = new XmlNode(n, "POST");
			for (Object param: method.getParameters())
				new XmlNode(methodXml, "parameter").addAttribute("name", param.toString());
			
			methodXml.addAttribute("returns", method.getReturnType());
		}
		
		method = service.discover(PublicWSProvider.PUT);
		if (method != null) {
			XmlNode methodXml = new XmlNode(n, "PUT");
			for (Object param: method.getParameters())
				new XmlNode(methodXml, "parameter").addAttribute("name", param.toString());
			
			methodXml.addAttribute("returns", method.getReturnType());
		}
		
		return n;
	}
}
