/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.osgi.touchkit;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.http.HttpService;

//import aQute.bnd.annotation.component.Activate;
//import aQute.bnd.annotation.component.Component;
//import aQute.bnd.annotation.component.Deactivate;
//import aQute.bnd.annotation.component.Reference;

import com.vaadin.Application;

/**
 * This class runs as an OSGi component and looks for OSGi component factories
 * with their <code>component.factory</code> property set to
 * <code>vaadin.app</code>.
 * <p/>
 * 
 * The component factory name (specified as <code>component.name</code>) is used
 * as the alias to register an instance of {@link VaadinOSGiTouchKitServlet} which then
 * uses the component factory to create an instance of {@link Application}. For
 * example, if <code>component.name</code> was set to myapp, the servlet would
 * be registered at <code>http://<em>localhost:8080</em>/myapp</code>.
 * 
 * <p/>
 * Properties registered against the component factory are passed in to the
 * servlet as init params. Thus to make your application production safe specify
 * productionMode=true as a property of your application component.
 * 
 * @author brindy
 */
//@Component(properties = { "productionMode=false" })
public class VaadinOSGiTouchKitApplicationManager {

	private HttpService httpService;

	private Map<String, HttpServlet> servlets = new HashMap<String, HttpServlet>();

	private Set<Runnable> waiting = new HashSet<Runnable>();

	private Dictionary<String, String> initParams;

	private boolean started;

//	@Activate
	public void start(Map<?, ?> properties) {
		System.out.println("VaadinOSGiTouchKitApplicationManager.start: " + properties);

		// by default we not in production mode, so check to see if we need to
		// change that
		String productionMode = (String) properties.get("productionMode");
		if (null != productionMode) {
			initParams = new Hashtable<String, String>();
			initParams.put("productionMode", productionMode);
			// [ANSTO][Tony][2912-05-24] Added necessary servlet param
			initParams.put("application", "com.vaadin.Application");
			// com.vaadin.addon.widgetset.AddonWidgetset is specific for Gumtree
			// we may need to may this configurable if some touchkit applications
			// has their own gwt widgetset
			initParams.put("widgetset", "com.vaadin.addon.widgetset.AddonWidgetset");
		}

		synchronized (waiting) {
			// applications maybe have been registered before we were started
			for (Runnable run : waiting) {
				run.run();
			}
			waiting.clear();
		}
		started = true;
	}

//	@Deactivate
	public void stop() {
		started = false;
		initParams = null;
	}

//	@Reference
	public void bindHttpService(HttpService service) {
		this.httpService = service;
	}

//	@Reference(type = '*', target = "(component.factory=vaadin.app)", unbind = "applicationRemoved", dynamic = true)
	public void applicationRegistered(ComponentFactory factory,
			Map<String, Object> properties) throws Exception {
		System.out.println(properties);
		final String path = "/" + (String) properties.get("component.name");
		System.out.println("New Vaadin context : " + path);
		final HttpServlet servlet = new VaadinOSGiTouchKitServlet(factory);
		servlets.put(path, servlet);

		System.out
				.println("VaadinOSGiTouchKitApplicationManager.bindComponentFactory: initParams="
						+ initParams);

		// We're going to register the new servlet when a http service is
		// available which we can only be sure is current if we're in the
		// started state.
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					httpService
							.registerServlet(path, servlet, initParams, null);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		if (started) {
			// because we're started, we know we can register the servlet now
			run.run();
		} else {
			// otherwise, we'll do it later
			synchronized (waiting) {
				waiting.add(run);
			}
		}
	}

	public void applicationRemoved(ComponentFactory factory,
			Map<String, Object> properties) {
		String path = "/" + (String) properties.get("component.name");
		System.out.println("Removing Vaadin context : " + path);
		httpService.unregister(path);
		servlets.remove(path).destroy();
	}

}
