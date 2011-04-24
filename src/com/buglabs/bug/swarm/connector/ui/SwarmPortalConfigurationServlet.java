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
import com.buglabs.bug.swarm.connector.ui.SwarmConfigKeys;
import com.buglabs.osgi.sewing.pub.SewingController;
import com.buglabs.osgi.sewing.pub.SewingHttpServlet;
import com.buglabs.osgi.sewing.pub.util.ControllerMap;
import com.buglabs.osgi.sewing.pub.util.RequestParameters;
import com.buglabs.util.ConfigAdminUtil;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleList;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelRoot;

/**
 * Servlet used to configure BUGswarm on client device and associate with
 * server.
 */
public class SwarmPortalConfigurationServlet extends SewingHttpServlet {

	private static final long serialVersionUID = 6913347882663532246L;
	private LogService log;
	private Configuration config;

	public SwarmPortalConfigurationServlet(ConfigurationAdmin ca) throws IOException {
		this.config = ca.getConfiguration(SwarmConfigKeys.CONFIG_PID_BUGSWARM, null);
		this.log = Activator.getLog();
	}

	@Override
	public ControllerMap getControllerMap() {
		ControllerMap controllers = new ControllerMap();
		controllers.put("index", new index());
		return controllers;
	}

	public class index extends SewingController {

		@Override
		public String getTemplateName() {
			return "index.fml";
		}

		@Override
		public TemplateModelRoot get(RequestParameters params, HttpServletRequest req, HttpServletResponse resp) {
			return loadSwarmInfo();
		}

		@Override
		public TemplateModelRoot post(RequestParameters params, HttpServletRequest req, HttpServletResponse resp) {

			String action = params.get("action");
			String msg = null;

			if (action.equals("activate")) {
				String server = params.get("server");
				String userKey = params.get("user-key");
				String deviceName = params.get("device-name");

				if (server != null && userKey != null && deviceName != null) {
					try {
						saveConfiguration(server, deviceName, userKey);

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
			} else if (action.equals("join")) {
				throw new RuntimeException("Unimplemented");
			}

			SimpleHash root = (SimpleHash) loadSwarmInfo();
			if (msg != null) {
				root.put("message", msg);
			}
			return root;
		}

		private TemplateModelRoot loadSwarmInfo() {
			SimpleHash root = new SimpleHash();
			String msg = "";

			try {
				if (isEnabled()) {
					root.put("action_label", "Deactivate");
					root.put("action", "deactivate");
				} else {
					root.put("action_label", "Activate");
					root.put("action", "activate");
				}

				root.put("server", getSwarmServer());

				root.put("device_name", getNickname());
				root.put("user_key", getUserKey());

				root.put("message", new SimpleScalar(msg));
			} catch (IOException e) {
				e.printStackTrace();
			}

			return root;
		}
	}

	public void saveConfiguration(String server, String nick, String userKey) throws IOException {
		Dictionary d = config.getProperties();

		if (d == null) {
			d = new Hashtable();
		}

		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER, server);
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_NICKNAME, nick);
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERKEY, userKey);

		config.update(d);
	}

	/**
	 * Try to set bugswarm to enabled. This does not make a server call but does
	 * validate connection information.
	 * 
	 * @param enabled
	 * @return true if enablement successful.
	 */
	public boolean setEnabled(boolean enabled) {
		try {
			// validateServerAndClientId();
			updateConfig(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED, "" + enabled);
		} catch (Exception e) {
			Activator.getLog().log(LogService.LOG_WARNING, "Failed to enable BUGswarm.", e);
			return false;
		}
		return true;
	}

	public boolean isEnabled() throws IOException {
		Dictionary dict = ConfigAdminUtil.getPropertiesSafely(config);

		Object o = dict.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED);

		if (o == null) {
			return false;
		}

		return Boolean.parseBoolean(o.toString());
	}

	public String getSwarmServer() throws IOException {
		Dictionary dict = ConfigAdminUtil.getPropertiesSafely(config);

		Object o = dict.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER);

		if (o == null) {
			return null;
		}

		return o.toString();
	}

	private String getUserKey() throws IOException {
		Dictionary dict = ConfigAdminUtil.getPropertiesSafely(config);

		Object o = dict.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERKEY);

		if (o == null) {
			return null;
		}

		return o.toString();
	}

	private String getNickname() throws IOException {
		Dictionary dict = ConfigAdminUtil.getPropertiesSafely(config);

		Object o = dict.get(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_NICKNAME);

		if (o == null) {
			return null;
		}

		return o.toString();
	}

	private void updateConfig(String key, String val) throws IOException {
		Dictionary d = config.getProperties();
		if (d == null) {
			d = new Hashtable();
		}

		d.put(key, val);
		config.update(d);
	}
}
