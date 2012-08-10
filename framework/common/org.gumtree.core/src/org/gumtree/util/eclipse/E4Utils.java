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

package org.gumtree.util.eclipse;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.gumtree.core.internal.Activator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public final class E4Utils {

	public static IEclipseContext createEclipseContext() {
		return createEclipseContext(Activator.getContext());
	}
	
	public static IEclipseContext createEclipseContext(Bundle bundle) {
		return createEclipseContext(bundle.getBundleContext());
	}

	public static IEclipseContext createEclipseContext(BundleContext context) {
		IEclipseContext eclipseContext = EclipseContextFactory
				.getServiceContext(context);
		// Fix for running Eclipse context in non OSGi environment
		try {
			eclipseContext.get(IEventBroker.class);
		} catch (InjectionException e) {
			eclipseContext.set(Logger.class, null);
		}
		return eclipseContext;
	}

	public static IEclipseContext getEclipseContext() {
		return Activator.getDefault().getEclipseContext();
	}
	
}
