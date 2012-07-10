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
package au.gov.ansto.bragg.kakadu.ui.region;

import org.gumtree.vis.mask.AbstractMask;



/**
 * @author nxi
 *
 */
public interface RegionEventListener {

	public abstract void maskUpdated(AbstractMask mask);
	
	public abstract void maskAdded(AbstractMask mask);
	
	public abstract void maskRemoved(AbstractMask mask);
	
}
