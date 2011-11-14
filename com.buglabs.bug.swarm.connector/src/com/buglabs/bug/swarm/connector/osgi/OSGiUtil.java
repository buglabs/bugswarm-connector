package com.buglabs.bug.swarm.connector.osgi;
/*
 * This file is in the public domain, furnished "as is", without technical
 * support, and with no warranty, express or implied, as to its usefulness for
 *	any purpose.
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * General utilities for OSGi service binding and other common operations.
 * 
 * @author kgilmer
 *
 */
public class OSGiUtil {
	
	/**
	 * An exception for errors accessing OSGi services.
	 *
	 */
	public static class OSGiServiceException extends Exception {
		private static final long serialVersionUID = 2528015088957764029L;

		/**
		 * @param message message for exception.
		 */
		public OSGiServiceException(String message) {
			super(message);
		}
	}
	/**
	 * Visit services via the onServices() method.
	 *
	 */
	public interface ServiceVisitor<T> {
		/**
		 * @param sr ServiceReference
		 * @param service service instance
		 */
		void apply(ServiceReference sr, T service);
	}
	
	/**
	 * Visit bundles via the onBundles() method.
	 *
	 */
	public interface BundleVisitor {
		/**
		 * @param bundle Bundle
		 */
		void apply(Bundle bundle);
	}
	
	/**
	 * Follow service binding events, a simplification of ServiceTracker for a subset of use cases.
	 *
	 */
	public interface ServiceFollower {
		void allAvailable(Map<String, Object> services, Map<String, ServiceReference> references);
		void unavailable(Object service);
	}
	
	/**
	 * Extends collection and allows client to stop tracking services by calling close.
	 *
	 * @param <E>
	 */
	public interface TrackingCollection<E> extends Collection<E> {
		/**
		 * Stop tracking services.
		 */
		void close();
	}
	
	/**
	 * Create a Filter of OSGi OBJECTCLASS keys.  Used commonly with ServiceTracker to specify
	 * the complete set of OSGi services required.
	 * 
	 * @param services A variable number of services.
	 * @return A LDAP filter string that can be used to construct a Filter.  If null passed in, null is returned to caller.
	 */
	public static String createServiceFilter(String ... serviceNames) {
		return createFilter(Constants.OBJECTCLASS, Arrays.asList(serviceNames));
	}
	
	/**
	 * Create a Filter of keys.  Used commonly with ServiceTracker to specify
	 * the complete set of OSGi services required.
	 * 
	 * @param key Filter key
	 * @param values A variable number of values that can match (OR).
	 * @return A LDAP filter string that can be used to construct a Filter.  If null passed in, null is returned to caller.
	 */
	public static String createFilter(String key, String ... values) {
		return createFilter(key, Arrays.asList(values));
	}
	
	/**
	 * Generate a Filter with one key and a list of values.
	 * 
	 * @param values A String List of class names.
	 * @return A LDAP filter string that can be used to construct a Filter.  If null passed in, null is returned to caller.
	 */
	private static String createFilter(String property, List<String> values) {
		if (values == null) {
			return null;
		} 
		
		if (values.size() == 1) {
			return "(" + property + "=" + values.get(0) + ")";
		} else if (values.size() > 1) {
			return "(|" + createFilter(property, values.subList(0, 1)) 
				+ createFilter(property, values.subList(1, values.size())) + ")";
		}

		return "";
	}
	
	/**
	 * Apply applicator to bundles.
	 * 
	 * @param context
	 * @param applicator
	 * @return
	 */
	public static boolean onBundles(BundleContext context, BundleVisitor applicator) {
		Bundle[] bundles = context.getBundles();
		
		if (bundles != null && bundles.length > 0) {
			for (Bundle b : Arrays.asList(bundles))
				applicator.apply(b);
			
			return true;
		}
		
		return false;
	}
	
	public static ServiceTracker withServices(final BundleContext context, final List<String> services, final ServiceFollower follower) {
		ServiceTracker st;
		try {
			st = new ServiceTracker(context, context.createFilter(createFilter(Constants.OBJECTCLASS, services)), new ServiceTrackerCustomizer() {
				Map<String, Object> servicesMap = new HashMap<String, Object>();
				Map<String, ServiceReference> referencesMap = new HashMap<String, ServiceReference>();
				
				@Override
				public void removedService(ServiceReference reference, Object service) {
					follower.unavailable(service);
				}
				
				@Override
				public void modifiedService(ServiceReference reference, Object service) {
					
				}
				
				@Override
				public Object addingService(ServiceReference reference) {
					Object svn = context.getService(reference);
					String svcNames[] = ((String []) reference.getProperty(Constants.OBJECTCLASS));
					
					for (String refServices : Arrays.asList(svcNames))
						for (String cs : services)
							if (refServices.equals(cs)) {
								servicesMap.put(refServices, svn);
								referencesMap.put(refServices, reference);
							}
							
					if (servicesMap.size() == services.size())
						follower.allAvailable(servicesMap, referencesMap);
					
					return svn;
				}
			});
			st.open();
			return st;
		} catch (InvalidSyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Visit a set of services.  Unchecked exception InvalidSyntaxException will be thrown if OSGi filter syntax is invalid.
	 * 
	 * @param <T>
	 * 
	 * @param context BundleContext
	 * @param service The class name with which the service was registered or null for all services.
	 * @param filter The filter expression or null for all services.
	 * @param applicator code that acts upon the service.
	 * @return true if at least one service was found.  False otherwise.
	 */
	public static <T> boolean onServices(BundleContext context, String service, String filter, ServiceVisitor<T> applicator) {
		ServiceReference[] srefs;
		try {
			srefs = context.getServiceReferences(service, filter);
			
			if (srefs != null && srefs.length > 0) {				
				for (ServiceReference sr : Arrays.asList(srefs))
					if (sr != null && service != null)
						applicator.apply(sr, (T) context.getService(sr));				
				
				
				return true;
			} 
		} catch (InvalidSyntaxException e) {
			throw new IllegalArgumentException(e);
		}
		
		return false;
	}

	/**
	 * Try to get a single instance of a service, or throw exception if unavailable.
	 * 
	 * @param context BundleContext
	 * @param service Name of service
	 * @return reference to OSGi service or throw IllegalStateException if unavailable.
	 * @throws OSGiServiceException if unable to access service
	 */
	public static Object getServiceInstance(BundleContext context, String service) throws OSGiServiceException {
		if (context == null)
			throw new IllegalArgumentException("BundleContext is not available.");
		
		ServiceReference casr = context.getServiceReference(service);
		
		if (casr == null)
			throw new OSGiServiceException(service + " is not in the service registry.");
		
		Object ca = context.getService(casr);
		
		if (ca == null)
			throw new OSGiServiceException(service + " cannot be referenced.");
		
		return ca;
	}
	
	/**
	 * Try to get a single instance of a service, or throw exception if unavailable.
	 * 
	 * @param context BundleContext
	 * @param service Name of service
	 * @param filter for service
	 * @return reference to OSGi service or throw IllegalStateException if unavailable.
	 * @throws OSGiServiceException if unable to access service
	 */
	public static Object getServiceInstance(BundleContext context, String service, String filter) throws OSGiServiceException {
		if (context == null)
			throw new IllegalArgumentException("BundleContext is not available.");
		
		ServiceReference[] casr;
		try {
			casr = context.getServiceReferences(service, filter);
		} catch (InvalidSyntaxException e) {
			throw new OSGiServiceException("Invalid filter syntax: " + filter);
		}
		
		if (casr == null || casr.length == 0)
			throw new OSGiServiceException(service + " is not in the service registry.");
		
		Object ca = context.getService(casr[0]);
		
		if (ca == null)
			throw new OSGiServiceException(service + " cannot be referenced.");
		
		return ca;
	}
	
	/**
	 * Provide a compact way to bind/unbind to a single OSGi service.  Alliviates need for client to keep an instance of BundleContext.  Only
	 * works for one service type.  More complex use-cases should use the ServiceTrackerCustomizer.
	 * 
	 * @param <T>
	 * 
	 * @param context
	 * @param service
	 * @param container
	 * @return ServiceTracker that has been opened.
	 */
	public static <T> ServiceTracker collectServices(final BundleContext context, String service, final Collection<T> container) {
		
		ServiceTracker st = new ServiceTracker(context, service, new ServiceTrackerCustomizer() {
			
			@Override
			public void removedService(ServiceReference reference, Object service) {
				container.remove(service);
			}
			
			@Override
			public void modifiedService(ServiceReference reference, Object service) {
				
			}
			
			@Override
			public Object addingService(ServiceReference reference) {
				Object svc = context.getService(reference);
				container.add((T)svc);
				return svc;
			}
		});
	
		
		st.open();
		
		return st;		
	}
	
	public static ServiceTracker collectServices(final BundleContext context, Filter filter, final Collection<Object> container) {
		
		ServiceTracker st = new ServiceTracker(context, filter, new ServiceTrackerCustomizer() {
			
			@Override
			public void removedService(ServiceReference reference, Object service) {
				container.remove(service);
			}
			
			@Override
			public void modifiedService(ServiceReference reference, Object service) {
				
			}
			
			@Override
			public Object addingService(ServiceReference reference) {
				Object svc = context.getService(reference);
				container.add(svc);
				return svc;
			}
		});
	
		st.open();
		
		return st;		
	}
	
	/**
	 * Create a TrackingCollection in which service instances will be maintained by a ServiceTracker.
	 * 
	 * A tracking collection is a collection of OSGi service references that is updated by the OSGi framework
	 * when service events occur that are relevant to the collection.
	 * 
	 * @param <T> class being tracked.
	 * @param context BundleContext
	 * @param serviceName name of service being tracked.  
	 * @return An instance of TrackingCollection containing objects of type T
	 */
	public static  <T> TrackingCollection<T> trackingCollection(final BundleContext context, String serviceName) {
		TrackingCollection<T> tal = new TrackingArrayList<T>();
		((TrackingArrayList)tal).setServiceTracker(collectServices(context, serviceName, tal));
		
		return tal;
	}
	
	/**
	 * Get a property from the bundle context or return the default if doesn't exist.
	 * 
	 * @param context BundleContext
	 * @param key key to find
	 * @param defaultValue if key does not exist
	 * @return value of property or default if key does not exist.
	 */
	public static boolean getProperty(BundleContext context, String key, boolean defaultValue) {
		String value = context.getProperty(key);
		if (value == null) 
			return defaultValue;

		return Boolean.parseBoolean(value);		
	}
	
	/**
	 * Get a property from the bundle context or return the default if doesn't exist.
	 * 
	 * @param context BundleContext
	 * @param key key to find
	 * @param defaultValue if key does not exist
	 * @return value of property or default if key does not exist.
	 */
	public static String getProperty(BundleContext context, String key, String defaultValue) {
		String value = context.getProperty(key);
		if (value == null) 
			return defaultValue;
		
		return value;		
	}

	/**
	 * Get a property from the bundle context or return the default if doesn't exist.
	 * Will not throw NumberFormatException on invalid value, rather it will return the default.
	 * 
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return value of property or default if key does not exist.
	 */
	public static long getProperty(BundleContext context, String key, long defaultValue) {
		String value = context.getProperty(key);
		if (value == null) 
			return defaultValue;
		
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	/**
	 * Get a property from the bundle context or return the default if doesn't exist.
	 * Will not throw NumberFormatException on invalid value, rather it will return the default.
	 * 
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return value of property or default if key does not exist.
	 */
	public static int getProperty(BundleContext context, String key, int defaultValue) {
		String value = context.getProperty(key);
		if (value == null) 
			return defaultValue;
		
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	
	/**
	 * Convert an array of Strings into a Dictionary.
	 * 
	 * @param elements a list of elements of even size.  First element is key, second is value, and repeat.
	 * @return Dictionary of key value pairs of input elements
	 */
	public static Dictionary toDictionary(String ... elements) {
		if (elements.length % 2 != 0) {
			throw new IllegalStateException("Input parameters must be even.");
		}

		Iterator<String> i = Arrays.asList(elements).iterator();
		Hashtable<String, String> m = new Hashtable<String, String>();

		while (i.hasNext()) {
			m.put(i.next().toString(), i.next());
		}

		return m;
	}
	
	/**
	 * Get or create a LogService reference.
	 * @param context or null if none available.
	 * @return a LogService
	 */
	public static LogService getLogService(BundleContext context) {
		
		try {
			return (LogService) getServiceInstance(context, LogService.class.getName());
		} catch (OSGiServiceException e) {
			return new LogService() {
				/**
				 * @param level
				 * @param sb
				 */
				private void getLevelLabel(int level, StringBuilder sb) {
									
					switch (level) {
					case 1:		
						sb.append("ERROR  ");
						break;
					case 2:		
						sb.append("WARNING");
						break;
					case 3:		
						sb.append("INFO   ");	
						break;
					case 4:			
						sb.append("DEBUG  ");		
						break;
					default:
						sb.append("UNKNOWN");		
						break;
					}	
				}
				
				@Override
				public void log(ServiceReference sr, int level, String message, Throwable exception) {
					StringBuilder sb = new StringBuilder();
					
					if (sr != null) {
						sb.append("ServiceReference: ");
						sb.append(sr.toString());
						sb.append(' ');
					}
					
					getLevelLabel(level, sb);
					sb.append("  ");
					
					if (message != null)
						sb.append(message);
					
					if (exception != null) {
						sb.append('\n');
						sb.append(exception.getMessage());
					}
					
					if (level == LogService.LOG_ERROR)
						System.err.println(sb.toString());
					else
						System.out.println(sb.toString());
				}
				
				@Override
				public void log(ServiceReference sr, int level, String message) {
					log(sr, level, message, null);
				}
				
				@Override
				public void log(int level, String message, Throwable exception) {
					log(null, level, message, exception);
				}
				
				@Override
				public void log(int level, String message) {
					log(null, level, message, null);
				}
			};
		}		
	}

	/**
	 * @param psr ServiceRegistration to de-register.  If null, no operation is taken.
	 */
	public static void safelyUnregister(ServiceRegistration psr) {
		if (psr != null) {
			psr.unregister();
		}
	}
	
	public static class TrackingArrayList<E> extends ArrayList<E> implements TrackingCollection<E> {
		private static final long serialVersionUID = -1484912671382626351L;
		private ServiceTracker st;

		@Override
		public void close() {
			if (st == null)
				throw new IllegalArgumentException("ServiceTracker field has not been set.");
			
			st.close();
		}
		
		protected void setServiceTracker(ServiceTracker st) {
			this.st = st;			
		}
	}
}
