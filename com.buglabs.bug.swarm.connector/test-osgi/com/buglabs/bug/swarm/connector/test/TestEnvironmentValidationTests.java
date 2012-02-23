package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.touge.restclient.ReSTClient;

import com.buglabs.bug.swarm.client.ISwarmClient;
import com.buglabs.bug.swarm.client.ISwarmConfiguration;
import com.buglabs.bug.swarm.client.ISwarmKeysClient;
import com.buglabs.bug.swarm.client.ISwarmKeysClient.KeyType;
import com.buglabs.bug.swarm.client.ISwarmMessageListener;
import com.buglabs.bug.swarm.client.ISwarmSession;
import com.buglabs.bug.swarm.client.SwarmClientFactory;
import com.buglabs.bug.swarm.client.model.Configuration;
import com.buglabs.bug.swarm.client.model.Configuration.Protocol;
import com.buglabs.bug.swarm.client.model.SwarmKey;
import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.UserResourceModel;
import com.buglabs.bug.swarm.connector.osgi.OSGiUtil;
import com.buglabs.bug.swarm.connector.osgi.pub.IConnectorServiceStatus;
import com.buglabs.bug.swarm.connector.osgi.pub.IConnectorServiceStatus.Status;
import com.buglabs.bug.swarm.connector.ui.SwarmConfigKeys;

/**
 * Tests the high-level BUGSwarmConnector test environment.
 * 
 * @author kgilmer
 * 
 */
public class TestEnvironmentValidationTests extends TestCase {

	private boolean presenceRecieved;
	private boolean exceptionOccurred;

	/**
	 * Test that the bugswarm-connector bundle is installed and running in the
	 * OSGi framework instance.
	 * 
	 * @throws InterruptedException
	 */
	public void testConnectorAvailable() throws InterruptedException {

		assertNotNull(Activator.getDefault());
		assertNotNull(Activator.getDefault().getContext());

		BundleContext context = Activator.getDefault().getContext();

		Bundle swarmBundle = null;
		for (Bundle bundle : Arrays.asList(context.getBundles()))
			if (bundle.getHeaders().get("Bundle-SymbolicName") != null
					&& bundle.getHeaders().get("Bundle-SymbolicName")
							.equals("com.buglabs.bug.swarm.connector"))
				swarmBundle = bundle;

		assertNotNull(swarmBundle);

		assertTrue(swarmBundle.getState() == Bundle.ACTIVE);
	}

	/**
	 * Determine if system properties have enough information to run tests.
	 */
	public void testSystemBUGSwarmPropertiesAvailable() {
		assertNotNull(System.getProperty("report.misc"));
	}

	/**
	 * Create configuration client.
	 * 
	 * @throws IOException
	 */
	public void testCreateConfigurationClient() throws IOException {
		ISwarmKeysClient keyClient = SwarmClientFactory
				.getAPIKeyClient(AccountConfig.getConfiguration().getHostname(
						Protocol.HTTP));

		SwarmKey pkey = keyClient.create(
				AccountConfig.getConfiguration().getUsername(),
				AccountConfig.getConfiguration().getUsername(),
				KeyType.PARTICIPATION).get(0);
		SwarmKey ckey = keyClient.create(
				AccountConfig.getConfiguration().getUsername(),
				AccountConfig.getConfiguration().getUsername(),
				KeyType.CONFIGURATION).get(0);

		AccountConfig.getConfiguration().setParticipationAPIKey(pkey.getKey());
		AccountConfig.getConfiguration().setConfingurationAPIKey(ckey.getKey());

		ISwarmClient wsClient = SwarmClientFactory.getSwarmClient(AccountConfig
				.getConfiguration().getHostname(Protocol.HTTP), AccountConfig
				.getConfiguration().getConfingurationAPIKey());

		assertNotNull(wsClient);

		List<SwarmModel> swarms = wsClient.list();

		assertNotNull(swarms);

		ISwarmConfiguration client2 = SwarmClientFactory
				.getSwarmConfigurationClient(AccountConfig.getConfiguration()
						.getHostname(Protocol.HTTP), AccountConfig
						.getConfiguration().getConfingurationAPIKey());

		assertNotNull(client2);

		List<UserResourceModel> resources = client2.listResource();

		assertNotNull(resources);
	}

	/**
	 * Can create the participation client.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void testCreateParticipationClient() throws UnknownHostException, IOException {
		Configuration c = AccountConfig.getConfiguration();
		ISwarmConfiguration cclient = SwarmClientFactory.getSwarmConfigurationClient(
				AccountConfig.getConfiguration().getHostname(Protocol.HTTP), 
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		
		List<UserResourceModel> existingResources = cclient.getResources();
		
		assertNotNull(existingResources);
		
		UserResourceModel urm = null;
		if (existingResources.size() == 0) {
			urm = cclient.createResource(
					AccountConfig.generateRandomResourceName(), 
					AccountConfig.getTestSwarmDescription(), 
					"pc", 0, 0);
		} else {
			urm = existingResources.get(0);
		}
		
		AccountConfig.testUserResource = urm;
		
		List<SwarmModel> swarms = cclient.listSwarms();
		
		assertNotNull(swarms);
		
		String testSwarmId = null;
		if (swarms.size() == 0) {
			testSwarmId = cclient.createSwarm(AccountConfig.generateRandomSwarmName(), true, AccountConfig.getTestSwarmDescription());
		} else {
			testSwarmId = swarms.get(0).getId();
		}
			
		
		ISwarmSession psession = SwarmClientFactory.createProductionSession(
				c.getHostname(Protocol.HTTP), 
				c.getParticipationAPIKey(), 
				urm.getResourceId(), AccountConfig.CONNECTOR_KEEPALIVE, AccountConfig.CONNECTOR_AUTORECONNECT, testSwarmId);
		
		assertNotNull(psession);
		assertTrue(psession.isConnected());
		
		AccountConfig.testSwarmId = testSwarmId;
	}

	/**
	 * Can load the web config page.
	 * 
	 * @throws IOException
	 */
	public void testLocalWebConfigPresent() throws IOException {
		ReSTClient rc = new ReSTClient();

		String url = rc.buildURL("http://localhost:8080/bugswarm").toString();

		assertTrue(url.equals("http://localhost:8080/bugswarm"));

		String html = rc.callGet(url);

		assertNotNull(html);

		assertTrue(html.contains("Device settings"));
	}

	public void testConnectorStatusServiceAvailable() {
		assertNotNull(Activator.getDefault());
		BundleContext context = Activator.getDefault().getContext();
		assertNotNull(context);

		Object svc = OSGiUtil.getServiceInstance(context,
				IConnectorServiceStatus.class.getName());

		assertNotNull(svc);

	}

	public void testConfigurationAdminPresent() throws IOException,
			InterruptedException {
		assertNotNull(Activator.getDefault());
		BundleContext context = Activator.getDefault().getContext();
		assertNotNull(context);

		Object svc = OSGiUtil.getServiceInstance(context,
				ConfigurationAdmin.class.getName());

		assertNotNull(svc);
		assertTrue(svc instanceof ConfigurationAdmin);

		ConfigurationAdmin ca = (ConfigurationAdmin) svc;

		org.osgi.service.cm.Configuration config = ca.getConfiguration(
				SwarmConfigKeys.CONFIG_PID_BUGSWARM, null);

		assertNotNull(config);

		Dictionary d = config.getProperties();

		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_SERVER, AccountConfig
				.getConfiguration().getHostname(Protocol.XMPP));
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME, AccountConfig
				.getConfiguration().getUsername());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY,
				AccountConfig.getConfiguration().getParticipationAPIKey());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY,
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_DEVICE_LABEL,
				AccountConfig.generateRandomResourceName());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_XMPP_PORT, AccountConfig
				.getConfiguration().getXMPPPort());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_HTTP_PORT, AccountConfig
				.getConfiguration().getHTTPPort());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED,
				Boolean.toString(true));

		IConnectorServiceStatus statusService = (IConnectorServiceStatus) OSGiUtil
				.getServiceInstance(context,
						IConnectorServiceStatus.class.getName());

		assertNotNull(statusService);

		config.update(d);

		Thread.sleep(1000);

		assertTrue(statusService.getStatus() == IConnectorServiceStatus.Status.ACTIVE);
	}

	public void testConnectorSendsPresence() throws UnknownHostException,
			IOException, InterruptedException {
		// testCreateParticipationClient();
		Configuration c = AccountConfig.getConfiguration();

		assertNotNull(Activator.getDefault());
		BundleContext context = Activator.getDefault().getContext();
		assertNotNull(context);

		Object svc = OSGiUtil.getServiceInstance(context,
				ConfigurationAdmin.class.getName());

		assertNotNull(svc);
		assertTrue(svc instanceof ConfigurationAdmin);

		ConfigurationAdmin ca = (ConfigurationAdmin) svc;

		org.osgi.service.cm.Configuration config = ca.getConfiguration(
				SwarmConfigKeys.CONFIG_PID_BUGSWARM, null);

		assertNotNull(config);

		Dictionary d = config.getProperties();
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME, AccountConfig
				.getConfiguration().getUsername());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY,
				AccountConfig.getConfiguration().getParticipationAPIKey());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY,
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_DEVICE_LABEL,
				AccountConfig.generateRandomResourceName());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED,
				Boolean.toString(false));

		config.update(d);

		Thread.sleep(1000);

		IConnectorServiceStatus statusService = (IConnectorServiceStatus) OSGiUtil
				.getServiceInstance(context,
						IConnectorServiceStatus.class.getName());
		assertNotNull(statusService);

		assertTrue(statusService.getStatus() == Status.INACTIVE);

		ISwarmSession psession = SwarmClientFactory.createProductionSession(
				c.getHostname(Protocol.HTTP), c.getParticipationAPIKey(),
				AccountConfig.testUserResource.getResourceId(),
				AccountConfig.CONNECTOR_KEEPALIVE,
				AccountConfig.CONNECTOR_AUTORECONNECT,
				AccountConfig.testSwarmId);

		assertNotNull(psession);
		assertTrue(psession.isConnected());

		presenceRecieved = false;
		exceptionOccurred = false;

		psession.addListener(new ISwarmMessageListener() {

			@Override
			public void presenceEvent(String fromSwarm, String fromResource,
					boolean isAvailable) {
				presenceRecieved = true;
			}

			@Override
			public void exceptionOccurred(ExceptionType type, String message) {
				exceptionOccurred = false;
			}
		});

		d = config.getProperties();
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_USERNAME, AccountConfig
				.getConfiguration().getUsername());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_PARTICIPATION_APIKEY,
				AccountConfig.getConfiguration().getParticipationAPIKey());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_CONFIGURATION_APIKEY,
				AccountConfig.getConfiguration().getConfingurationAPIKey());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_DEVICE_LABEL,
				AccountConfig.generateRandomResourceName());
		d.put(SwarmConfigKeys.CONFIG_KEY_BUGSWARM_ENABLED,
				Boolean.toString(true));

		config.update(d);
		Thread.sleep(1000);

		assertTrue(statusService.getStatus() == IConnectorServiceStatus.Status.ACTIVE);

		assertTrue(presenceRecieved);
		assertFalse(exceptionOccurred);
	}

	/**
	 * Taken from the swarm-server tests:
	 * 
	 * it('should allow to connect as a producer resource only' it('should allow
	 * to connect as a consumer resource only' it('should allow to connect as
	 * producer and consumer' it('should connect, join and send messages to more
	 * than one swarm' it('should not lose messages if connection goes down'
	 * it('should re-connect if connection goes down'
	 */
}