package org.gumtree.ui.missioncontrol.support;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.gumtree.ui.missioncontrol.IApp;
import org.gumtree.ui.missioncontrol.IAppRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppRegistry implements IAppRegistry {

	private static final Logger logger = LoggerFactory
			.getLogger(AppRegistry.class);

	private SortedSet<IApp> apps;

	private IExtensionRegistry extensionRegistry;

	public AppRegistry() {
		apps = new TreeSet<IApp>(new Comparator<IApp>() {
			@Override
			public int compare(IApp app1, IApp app2) {
				return app1.getLabel().compareTo(app2.getLabel());
			}
		});
	}

	/*************************************************************************
	 * Life cycle
	 *************************************************************************/

	public void activate() {
		if (getExtensionRegistry() != null) {
			AppExtensionReader reader = new AppExtensionReader();
			reader.setExtensionRegistry(getExtensionRegistry());
			List<IApp> registeredApps = reader.getRegisteredApps();
			apps.addAll(registeredApps);
		}
	}

	public void deactivate() {
		extensionRegistry = null;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IExtensionRegistry getExtensionRegistry() {
		return extensionRegistry;
	}

	@Inject
	public void setExtensionRegistry(IExtensionRegistry extensionRegistry) {
		this.extensionRegistry = extensionRegistry;
	}

}
