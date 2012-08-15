/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.service.dataaccess.support;

import javax.management.MXBean;

import org.gumtree.core.management.IManageableBean;
import org.gumtree.core.service.IServiceManager;

@MXBean
public interface IDataAccessManagerMonitor extends IManageableBean {

	public DataAccessProviderInfo[] getAvailableProviders();
	
	public IServiceManager getServiceManager();
	
	public void setServiceManager(IServiceManager serviceManager);
	
}
