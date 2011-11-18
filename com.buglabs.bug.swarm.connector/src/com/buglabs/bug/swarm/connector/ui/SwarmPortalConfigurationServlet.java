/*******************************************************************************
 * Copyright (c) 2010 Bug Labs, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package com.buglabs.bug.swarm.connector.ui;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;

import com.buglabs.bug.swarm.connector.osgi.Activator;
import com.buglabs.osgi.sewing.pub.SewingController;
import com.buglabs.osgi.sewing.pub.SewingHttpServlet;
import com.buglabs.osgi.sewing.pub.util.ControllerMap;
import com.buglabs.osgi.sewing.pub.util.RequestParameters;
import com.buglabs.util.osgi.ConfigAdminUtil;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelRoot;

/**
 * Servlet used to configure BUGswarm on client device and associate with
 * server.
 */
public class SwarmPortalConfigurationServlet extends SewingHttpServlet {

	private static final long serialVersionUID = 6913347882663532246L;
	private final Configuration config;
	private Dictionary confDictionary;

	/**
	 * @param ca
	 *            ConfigAdmin
	 * @throws IOException
	 *             thrown if ConfigAdmin fails
	 */
	public SwarmPortalConfigurationServlet(final ConfigurationAdmin ca) throws IOException {
		this.config = ca.getConfiguration(SwarmConfigKeys.CONFIG_PID_BUGSWARM, null);
	}

	@Override
	public ControllerMap getControllerMap() {
		ControllerMap controllers = new ControllerMap();
		controllers.put("index", new Index());
		return controllers;
	}

	/**	
	 *
	 */
	public class Index extends SewingController {

		@Override
		public String getTemplateName() {
			return "index.fml";
		}

		@Override
		public TemplateModelRoot get(final RequestParameters params, final HttpServletRequest req, final HttpServletResponse resp) {
			return loadSwarmInfo();
		}

		@Override
		public TemplateModelRoot post(final RequestParameters params, final HttpServletRequest req, final HttpServletResponse resp) {
			// Validate input
			String missingKey = missingParameter(params, "p-api-key", "c-api-key", "user-name", "device-label");
			if (missingKey != null)
				throw new RuntimeException("Missing expected key: " + missingKey);

			String action = params.get("action");
			String msg = null;

			if (action.equals("activate")) {
				String pApiKey = params.get("p-api-key");
				String cApiKey = params.get("c-api-key");
				String username = params.get("user-name");
				String deviceLabel = params.get("device-label");

				if (pApiKey != null && username != null && cApiKey != null && deviceLabel != null) {
					try {
						saveConfiguration(username, pApiKey, cApiKey, deviceLabel);

						setEnabled(true);
						msg = "BugSwarm has been activated";

					} catch (IOException e) {
						msg = "An error occurred: " + e.getMessage();
					}
				} else {
					msg = "Please check your server information";
				}

			} else if (action.equals("deactivate")) {
				setEnabled(false);

				msg = "BugSwarm has been deactivated";
			}

			SimpleHash root = (SimpleHash) loadSwarmInfo();
			if (msg != null) {
				root.put("message", msg);
			}
			return root;
		}

		/**
		 * Verify that all expected keys are present.
		 * 
		 * TODO: replace this with super-class method getMissingParameter() when
		 * available.
		 * 
		 * @param params
		 *            parameters to check
		 * @param requiredKeys
		 *            array of key names
		 * @return null if all keys are present, name of first missing key
		 *         otherwise
		 */
		protected final String missingParameter(RequestParameters params, String... requiredKeys) {
			for (String key : requiredKeys)
				if (params.get(key) == null)
					return key;

			return null;
		}

		/**
		 * @return template with swarm model
		 */
		private TemplateModelRoot loadSwarmInfo() {
			SimpleHash root = new SimpleHash();
			String msg = "";

			try {
				confDictionary = ConfigAdminUtil.getPropertiesSafely(config);
				
				if (isEnabled()) {
					root.put("action_label", "Deactivate");
					root.put("action", "deactivate");
				} else {
					root.put("action_label", "Activate");
					root.put("action", "activate");
				}
				
				String deviceName = getDeviceName();

				root.put("user_name", getValue(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME));
				root.put("p_api_key", getValue(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY));
				root.put("c_api_key", getValue(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY));
				root.put("device-name", deviceName);
				
				root.put("user-name", getValue(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME));
				root.put("p-api-key", getValue(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY));
				root.put("c-api-key", getValue(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY));
				root.put("device_name", deviceName);

				root.put("message", new SimpleScalar(msg));
			} catch (IOException e) {
				Activator.getLog().log(LogService.LOG_ERROR, "Failed to load swarm info.", e);
			}

			return root;
		}

		/**
		 * @return the stored device name or a generated one.
		 * @throws IOException on config admin error
		 */
		private String getDeviceName() throws IOException {
			String name = getValue(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY);
			
			if (name.equals(""))				
				return "BUG-" + Long.toHexString(System.currentTimeMillis());
			
			return name;
		}
	}

	/**
	 * Save the configuration as set by the webui user.
	 * 
	 * @param username user name
	 * @param partApiKey participation API key
	 * @param confApiKey configuration API key
	 * @param deviceLabel device label
	 * @throws IOException
	 *             on file I/O error
	 */
	private void saveConfiguration(final String username, final String partApiKey, final String confApiKei, String deviceLabel) throws IOException {
		Dictionary d = config.getProperties();

		if (d == null) {
			d = new Hashtable();
		}
		
		//Reset the resource id if this is first use or the username has changed.
		boolean resetResourceId = d.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME) == null || 
									!d.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME).equals(username);

		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME, username);
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY, partApiKey);
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY, confApiKei);
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_DEVICE_LABEL, deviceLabel);
		
		if (resetResourceId)
			d.remove(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_RESOURCE_ID);

		config.update(d);
	}

	/**
	 * Try to set bugswarm to enabled. This does not make a server call but does
	 * validate connection information.
	 * 
	 * @param enabled
	 *            true for enabled, false otherwise
	 * @return true if enable successful.
	 */
	public boolean setEnabled(final boolean enabled) {
		try {
			// validateServerAndClientId();
			updateConfig(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED, "" + enabled);
		} catch (Exception e) {
			Activator.getLog().log(LogService.LOG_WARNING, "Failed to enable BUGswarm.", e);
			return false;
		}
		return true;
	}

	/**
	 * @return true if bugswarm is enabled, false otherwise
	 * @throws IOException
	 *             on connection or file error
	 */
	public boolean isEnabled() throws IOException {
		Dictionary dict = ConfigAdminUtil.getPropertiesSafely(config);

		Object o = dict.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED);

		if (o == null) {
			return false;
		}

		return Boolean.parseBoolean(o.toString());
	}

	/**
	 * Get a value from the configuration, or return empty string if no value
	 * exists.
	 * 
	 * @param key
	 *            of dictionary
	 * @return value or empty string if not present
	 * @throws IOException
	 *             on file I/O error
	 */
	private String getValue(final String key) throws IOException {
		if (confDictionary == null)
			confDictionary = ConfigAdminUtil.getPropertiesSafely(config);

		Object val = confDictionary.get(key);

		if (val == null)
			return "";

		return val.toString();
	}

	/**
	 * @param key
	 *            of dictionary
	 * @param val
	 *            of dictionary
	 * @throws IOException
	 *             on file i/o error
	 */
	private void updateConfig(final String key, final String val) throws IOException {
		Dictionary d = config.getProperties();
		if (d == null) {
			d = new Hashtable();
		}

		d.put(key, val);
		config.update(d);
	}
}
