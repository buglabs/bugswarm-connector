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

/**
 * Configuration keys used for configuration of BUGswarm.
 *
 */
public interface SwarmConfigKeys {
	
	/**
	 * ConfigAdmin ID for swarm configuration dictionary.
	 */
	public static final String CONFIG_PID_BUGSWARM 				= "BUGSWARM"; 
	
	/**
	 * A local-only value to determine if bugswarm is enabled
	 */
	public static final String CONFIG_KEY_BUGSWARM_ENABLED 		= "bugdash.swarm.boolean.enabled";
	
	/**
	 * The root hostname which is used to access WS APIs and XMPP server.
	 */
	public static final String CONFIG_KEY_BUGSWARM_SERVER 		= "bugdash.swarm.string.serverurl";
	
	/**
	 * The resource is the device id.  One user can have multiple resources, or 'contexts'.  Refer
	 * to XMPP specs for further details on Resource.
	 */
	public static final String CONFIG_KEY_BUGSWARM_RESOURCE		= "bugdash.swarm.string.nickname";
	
	/**
	 * The API key is what identifies the user and is issued via human transaction.
	 */
	public static final String CONFIG_KEY_BUGSWARM_APIKEY		= "bugdash.swarm.string.userkey";

	/**
	 * The username used in the XMPP server.
	 */
	public static final String CONFIG_KEY_BUGSWARM_USERNAME = "bugdash.swarm.string.username";
}
