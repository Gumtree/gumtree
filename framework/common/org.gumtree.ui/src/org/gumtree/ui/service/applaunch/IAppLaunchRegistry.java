package org.gumtree.ui.service.applaunch;

import org.gumtree.core.service.IService;

public interface IAppLaunchRegistry extends IService {

	public IAppLaunchDescriptor[] getAllAppLaunches();
	
}
