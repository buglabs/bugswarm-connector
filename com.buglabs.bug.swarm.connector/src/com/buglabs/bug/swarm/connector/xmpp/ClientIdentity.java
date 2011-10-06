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
package com.buglabs.bug.swarm.connector.xmpp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.StringTokenizer;

/**
 * Encapsulates unique device id, aka XMPP resource.
 * 
 */
public final class ClientIdentity {
	private static final int MAC_ADDRESS_POSITION = 6;
	private static final int LINUX_MAC_ADDRESS_LENGTH = 17;
	private static ClientIdentity ref;
	private String id;

	/**
	 * Singleton that cannot be externally constructed.
	 */
	private ClientIdentity() {

	}

	/**
	 * @return A reference to ClientIdentity singleton.
	 */
	public static ClientIdentity getRef() {
		if (ref == null) {
			ref = new ClientIdentity();
		}

		return ref;
	}

	/**
	 * @return GUID for device.
	 * @throws IOException
	 *             on file I/O errors
	 */
	public String getId() throws IOException {
		if (id == null) {
			// For debugging, if userid passed as a system property use it.
			if (System.getProperty("SwarmUserId") != null) {
				return System.getProperty("SwarmUserId");
			}

			// Look to see if mac address is provided by sysfs.
			File f = new File("/sys/devices/platform/ehci-omap.0/usb2/2-2/2-2.5/2-2.5:1.0/net/eth0/address");

			if (f.exists()) {
				BufferedReader fr = new BufferedReader(new FileReader(f));
				id = fr.readLine();
				fr.close();
			} else {
				// Run ifconfig to get mac address.
				id = getMacAddress();
			}
		}

		return id;
	}

	/**
	 * Parse mac address from 'ifconfig' commmand.
	 * 
	 * @return String of mac address
	 * @throws IOException
	 *             on IO error
	 */
	private static String getMacAddress() throws IOException {
		try {
			return linuxParseMacAddress(linuxRunIfConfigCommand());
		} catch (ParseException ex) {
			ex.printStackTrace();
			throw new IOException(ex.getMessage());
		}
	}

	/**
	 * @return contents of 'ifconfig' command
	 * @throws IOException
	 *             on io error
	 */
	private static String linuxRunIfConfigCommand() throws IOException {
		Process p = Runtime.getRuntime().exec("ifconfig");
		InputStream stdoutStream = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		for (;;) {
			int c = stdoutStream.read();
			if (c == -1)
				break;
			buffer.append((char) c);
		}
		String outputText = buffer.toString();
		stdoutStream.close();
		return outputText;
	}

	/**
	 * @param ipConfigResponse
	 *            contents of 'ifconfig' command
	 * @return mac address
	 * @throws ParseException
	 *             on parse failure
	 */
	private static String linuxParseMacAddress(final String ipConfigResponse) throws ParseException {
		String localHost = null;
		try {
			localHost = InetAddress.getLocalHost().getHostAddress();
		} catch (java.net.UnknownHostException ex) {
			ex.printStackTrace();
			throw new ParseException(ex.getMessage(), 0);
		}
		StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
		String lastMacAddress = null;

		while (tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();
			boolean containsLocalHost = line.indexOf(localHost) >= 0;
			// see if line contains IP address
			if (containsLocalHost && lastMacAddress != null) {
				return lastMacAddress;
			}
			// see if line contains MAC address
			int macAddressPosition = line.indexOf("HWaddr");
			if (macAddressPosition <= 0)
				continue;
			String macAddressCandidate = line.substring(macAddressPosition + MAC_ADDRESS_POSITION).trim();
			if (linuxIsMacAddress(macAddressCandidate)) {
				lastMacAddress = macAddressCandidate;
				continue;
			}
		}

		// We didn't get the hw address of
		// InetAddress.getLocalHost().getHostAddress() but let's return the last
		// good hw address.
		if (lastMacAddress != null) {
			return lastMacAddress;
		}

		ParseException ex = new ParseException("cannot read MAC address for " + localHost + " from [" + ipConfigResponse + "]", 0);
		ex.printStackTrace();
		throw ex;
	}

	/**
	 * @param macAddressCandidate
	 *            mac address as string
	 * @return true if looks like a valid mac address, false otherwise
	 */
	private static boolean linuxIsMacAddress(final String macAddressCandidate) {
		return macAddressCandidate.length() == LINUX_MAC_ADDRESS_LENGTH;
	}
}
