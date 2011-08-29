package org.bndtools.runtime.controller.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bndtools.runtime.controller.IHandler;
import org.bndtools.runtime.controller.IServer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

/**
 * An IServer implementation that fronts an OSGi Http Service instance.
 * 
 * @author kgilmer
 *
 */
public class HttpServiceServer implements IServer {
	
	private static final String DEFAULT_GLOBAL_PREFIX = "/btrc";
	private final String globalPrefix;
	private final BundleContext context;
	private HttpService httpService;
	private ServiceReference httpServiceReference;
	private final Log log;
	private final Map registrations;

	public HttpServiceServer(BundleContext context, Log log) {
		this.context = context;
		this.log = log;
		this.globalPrefix = DEFAULT_GLOBAL_PREFIX;
		this.registrations = new HashMap();
	}	

	public void registerHandler(String prefix, IHandler handler) {
		registrations.put(prefix, handler);
	}

	public void unregisterHandler(String prefix) {
		registrations.remove(prefix);
		
		if (httpService != null)
			httpService.unregister(globalPrefix + "/" + prefix);
	}

	public void start() throws IOException {
		if (httpService != null || httpServiceReference != null)
			throw new IllegalStateException("Server has already been started.");
		
		httpServiceReference = context.getServiceReference(HttpService.class.getName());
		
		if (httpServiceReference != null) {
			httpService = (HttpService) context.getService(httpServiceReference);
			
			if (httpService == null)
				throw new IOException("Unable to access Http Service");
		} else {
			throw new IOException("Unable to access Http Service");
		}		
		
		for (Iterator i = registrations.keySet().iterator(); i.hasNext();) {
			String prefix = (String) i.next();
			IHandler handler = (IHandler) registrations.get(prefix);
			try {
				httpService.registerServlet(globalPrefix + "/" + prefix, new HandlerServletAdapter(handler), null, null);
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}		
	}

	public void stop() throws IOException {
		if (context != null && httpServiceReference != null)
			context.ungetService(httpServiceReference);
		
		httpService = null;
		httpServiceReference = null;
		registrations.clear();
	}

}
