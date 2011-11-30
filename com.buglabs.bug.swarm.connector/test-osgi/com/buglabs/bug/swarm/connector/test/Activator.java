package com.buglabs.bug.swarm.connector.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * An activator for OSGi-context JUnit tests.  Requires com.buglabs.osgi.tester or similar OSGi-specific test runner.
 * 
 * @author kgilmer
 *
 */
public class Activator implements BundleActivator {

	private static Activator instance;
	private BundleContext context;

	private List<ServiceRegistration> regs;
	
	public Activator()	{
		instance = this;
	}
	
	public void start(BundleContext context) throws Exception {
		this.context = context;
		regs = new ArrayList<ServiceRegistration>();
		regs.add(context.registerService(TestSuite.class.getName(), new TestSuite(TestEnvironmentValidationTests.class), null));
		
		System.out.println(this.getClass().getName() + " added " + regs.size() + " suites for OSGi testing.");
	}

	public static synchronized Activator getDefault() {
		return instance;
	}
	
	public BundleContext getContext() {
		return context;
	}
	
	public void stop(BundleContext context) throws Exception {
		for (ServiceRegistration sr : regs)
			sr.unregister();
	}
}