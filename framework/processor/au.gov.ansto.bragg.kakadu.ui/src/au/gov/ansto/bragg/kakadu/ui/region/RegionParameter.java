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

import java.util.ArrayList;
import java.util.List;

import org.gumtree.vis.mask.Abstract2DMask;
import org.gumtree.vis.mask.AbstractMask;

import au.gov.ansto.bragg.kakadu.core.data.Operation;

/**
 * @author nxi
 *
 */
public class RegionParameter {

	private Operation operation;
	private List<AbstractMask> maskList = new ArrayList<AbstractMask>();
	private List<RegionEventListener> regionListenerList = new ArrayList<RegionEventListener>();
	private String name;
	
	public RegionParameter(final Operation operation, final String name) {
		this.operation = operation;
		this.name = name;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public List<AbstractMask> getMaskList() {
		return maskList;
	}
	
	public void addMask(AbstractMask mask) {
		if (mask instanceof Abstract2DMask && !maskList.contains(mask)) {
			maskList.add(mask);
			fireMaskAddedEvent(mask);
		}
	}
	
	public void addMasks(List<AbstractMask> masks) {
		for (AbstractMask mask : masks) {
			addMask(mask);
		}
	}
	
	public void updateMask(AbstractMask mask) {
		if (maskList.contains(mask)) {
			fireMaskUpdatedEvent(mask);
			return;
		}
		for (AbstractMask maskItem : maskList) {
			if (maskItem.getName().equals(mask.getName())) {
				removeMask(maskItem);
				addMask(mask);
				break;
			}
		}
	}
	
	public void removeMask(AbstractMask mask) {
		maskList.remove(mask);
		fireMaskRemovedEvent(mask);
	}
	
	public void clearMask() {
		maskList.clear();
	}
	
	public void addRegionListener(RegionEventListener listener) {
		regionListenerList.add(listener);
	}
	
	public void removeRegionListener(RegionEventListener listener) {
		regionListenerList.remove(listener);
	}
	
	public void fireMaskAddedEvent(AbstractMask mask) {
		for (RegionEventListener listener : regionListenerList) {
			listener.maskAdded(mask);
		}
	}
	
	public void fireMaskRemovedEvent(AbstractMask mask) {
		for (RegionEventListener listener : regionListenerList) {
			listener.maskRemoved(mask);
		}
	}
	
	public void fireMaskUpdatedEvent(AbstractMask mask) {
		for (RegionEventListener listener : regionListenerList) {
			listener.maskUpdated(mask);
		}
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
