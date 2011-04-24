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
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;

import com.buglabs.application.ServiceTrackerHelper.ManagedRunnable;
import com.buglabs.osgi.sewing.pub.ISewingService;

/**
 * Startup/tear down configuration servlet.
 * 
 * @author kgilmer
 * 
 */
public class ConfigInitRunnable implements ManagedRunnable {
	private ISewingService sewingService;
	private SwarmPortalConfigurationServlet servlet;
	private final LogService log;
	private final BundleContext context;

	public ConfigInitRunnable(BundleContext context, LogService log) {
		this.context = context;
		this.log = log;
	}

	@Override
	public void run(Map services) {
		ConfigurationAdmin ca = (ConfigurationAdmin) services.get(ConfigurationAdmin.class.getName());

		try {
			sewingService = (ISewingService) services.get(ISewingService.class.getName());
			servlet = new SwarmPortalConfigurationServlet(ca);
			sewingService.register(context, "/bugswarm", servlet);
		} catch (IOException e) {
			log.log(LogService.LOG_ERROR, "BUGSwarm Connector unable to create configuration ui.", e);
		}
	}

	@Override
	public void shutdown() {		
		if (sewingService != null && servlet != null) {
			sewingService.unregister(servlet);
		}
	}

}
