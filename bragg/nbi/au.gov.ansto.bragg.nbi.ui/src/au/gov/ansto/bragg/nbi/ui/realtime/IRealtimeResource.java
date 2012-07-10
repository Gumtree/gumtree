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

import org.gumtree.vis.interfaces.ITimeSeriesSet;

/**
 * @author nxi
 *
 */
public interface IRealtimeResource {

	public abstract String getName();

	public abstract void setName(String name);

	public abstract ITimeSeriesSet getTimeSeriesSet();

	public abstract void update();

	public abstract void clear();
}
