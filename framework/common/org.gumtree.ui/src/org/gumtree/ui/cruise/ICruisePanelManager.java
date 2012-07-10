package org.gumtree.ui.cruise;

import org.gumtree.core.service.IService;

public interface ICruisePanelManager extends IService {

	public ICruisePanelPage[] getRegisteredPages();
	
}
