/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.ui.service.launcher.support;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.core.object.IConfigurable;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.object.ObjectFactory;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.workbench.ContentViewUtils;
import org.gumtree.ui.util.workbench.IViewContentContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WidgetViewLauncher extends AbstractLauncher implements IExecutableExtension {

	private static final Logger logger = LoggerFactory.getLogger(WidgetViewLauncher.class);
	
	private String widgetClass;
	
	public void launch() throws LauncherException {
		if (widgetClass == null) {
			return;
		}
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				try {
					ContentViewUtils.createContentView(new IViewContentContributor() {
						public Image getTitleImage() {
							if (getDescriptor() != null) {
								return getDescriptor().getIcon16().createImage();
							}
							return null;
						}
						public String getTitle() {
							if (getDescriptor() != null) {
								return getDescriptor().getLabel();
							}
							return "View";
						}
						public void dispose() {
						}
						public void createContentControl(Composite parent) {
							parent.setLayout(new FillLayout());
							try {
								Object widget = ObjectFactory.instantiateObject(widgetClass,
										new Class[] { Composite.class, int.class },
										parent, SWT.INHERIT_DEFAULT);
								if (widget instanceof IConfigurable) {
									((IConfigurable) widget).afterParametersSet();
								}
							} catch (ObjectCreateException e) {
								logger.error("Failed to create control " + widgetClass, e);
							}
						}
					});
				} catch (Exception e) {
					// TODO: generate error view
					logger.error("Failed to create content view " + widgetClass, e);
				}
			}
		});
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		widgetClass = (String) data;
	}

}
