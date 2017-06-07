/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package au.gov.ansto.bragg.nbi.ui.realtime;

import java.util.List;

/**
 * @author nxi
 *
 */
public interface IRealtimeResourceProvider {

	public abstract List<IRealtimeResource> getResourceList();

	public abstract void addResourceToUpdateList(IRealtimeResource resource);
	
	public abstract void updateResource();

	public abstract void clear();

	public abstract void removeResourceFromUpdateList(IRealtimeResource resource);
	
	public abstract IRealtimeResource getResource(String name);
}
