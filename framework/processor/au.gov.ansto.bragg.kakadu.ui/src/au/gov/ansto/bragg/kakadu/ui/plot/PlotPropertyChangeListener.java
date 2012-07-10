/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.plot;

/**
 * The listener notify about all property changes for a Plot.
 * See the list of available property IDs constants defined in the interface. 
 * 
 * @author Danil Klimontov (dak)
 */
public interface PlotPropertyChangeListener {
	/**
	 * The property ID is related for any plot title changes.
	 */
	int TITLE_PROPERTY_ID = 1;
	int DATA_ITEM_INDEX_PROPERTY_ID = 2;
	int PLOT_TYPE_PROPERTY_ID = 3;

	void propertyChanged(int propertyId, Object oldValue, Object newValue);
}
