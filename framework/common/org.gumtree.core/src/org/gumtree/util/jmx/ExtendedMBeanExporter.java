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

package org.gumtree.util.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.core.management.IManageableBean;
import org.gumtree.core.management.IManageableBeanProvider;
//import org.springframework.jmx.export.MBeanExporter;

/**
 * ExtendedMBeanExporter makes MBeanExporter easier to use as it automatically
 * retrieves MBean keys from the input (if input objects are
 * IManageableBeanProvider)
 * 
 * @author Tony Lam
 * @since 1.5
 */
//public class ExtendedMBeanExporter extends MBeanExporter {
public class ExtendedMBeanExporter {
	private List<IManageableBeanProvider> beanProviders;

	private List<IManageableBean> beans;

	public ExtendedMBeanExporter() {
		super();
		beanProviders = new ArrayList<IManageableBeanProvider>(2);
		beans = new ArrayList<IManageableBean>(2);
	}
	
	// Overwrite by merging the beanProviders with the standard objetcs
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void afterPropertiesSet() {
		// Create a final storage
		Map finalBeans = new HashMap();
		// Store the beanProvider objects into the final storage
		if (beanProviders != null) {
			for (Object beanProviderObject : beanProviders) {
				if (beanProviderObject instanceof IManageableBeanProvider) {
					IManageableBeanProvider beanProvider = (IManageableBeanProvider) beanProviderObject;
					IManageableBean[] beans = beanProvider.getManageableBeans();
					if (beans != null) {
						for (IManageableBean bean : beans) {
							if (bean.getRegistrationKey() != null) {
								finalBeans.put(bean.getRegistrationKey(), bean);
							}
						}
					}
				}
			}
		}
		// Store the bean objects into the final storage
		if (beans != null) {
			for (IManageableBean bean : beans) {
				if (bean.getRegistrationKey() != null) {
					finalBeans.put(bean.getRegistrationKey(), bean);
				}
			}
		}
		// Update parent beans
//		super.setBeans(finalBeans);
		// Continue with the default behaviour
//		super.afterPropertiesSet();
	}

	public void addBeanProvider(IManageableBeanProvider beanProvider) {
		beanProviders.add(beanProvider);
	}
	
	public void addBean(IManageableBean bean) {
		beans.add(bean);
	}
	
	public void setBeanProviders(List<IManageableBeanProvider> beanProviders) {
		this.beanProviders = beanProviders;
	}

	public void setBeans(List<IManageableBean> beans) {
		this.beans = beans;
	}

}
