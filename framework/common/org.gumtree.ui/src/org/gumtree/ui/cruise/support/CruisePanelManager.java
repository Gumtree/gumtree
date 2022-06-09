package org.gumtree.ui.cruise.support;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.core.service.IServiceDescriptor;
import org.gumtree.core.service.IServiceManager;
import org.gumtree.ui.cruise.ICruisePanelManager;
import org.gumtree.ui.cruise.ICruisePanelPage;
import org.gumtree.ui.internal.UIProperties;
import org.gumtree.util.string.StringUtils;

public class CruisePanelManager implements ICruisePanelManager {

	private IServiceManager serviceManager;

	@Override
	public ICruisePanelPage[] getRegisteredPages() {
		// Get all pages from service registery
		List<ICruisePanelPage> pages = new ArrayList<ICruisePanelPage>();
		for (IServiceDescriptor<ICruisePanelPage> desc : getServiceManager()
				.getServiceDescriptorsNow(ICruisePanelPage.class)) {
			pages.add(desc.getService());
		}
		// Sort
		List<ICruisePanelPage> orderedPage = new ArrayList<ICruisePanelPage>();
		List<String> suggestedOrder = StringUtils.split(UIProperties.PAGE_ODER.getValue(), ",");
		for (String pageName : suggestedOrder) {
			ICruisePanelPage result = null;
			for (ICruisePanelPage page : pages) {
				if (page.getName().equalsIgnoreCase(pageName)) {
					result = page;
					break;
				}
			}
			if (result != null) {
				pages.remove(result);
				orderedPage.add(result);
			}
		}
		// Add remaining pages
//		orderedPage.addAll(pages);
		return orderedPage.toArray(new ICruisePanelPage[orderedPage.size()]);
	}
	
	/*************************************************************************
	 * Referenced services
	 *************************************************************************/
	
	public IServiceManager getServiceManager() {
		return serviceManager;
	}
	
	public void setServiceManager(IServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

}
