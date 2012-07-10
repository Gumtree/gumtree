/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.gumnix.sics.core;

public class PropertySelectionCriterion {

	private String propertyId;
	
	private String propertyValue;
	
	private PropertySelectionType selectionType;
	
	public PropertySelectionCriterion(
			String propertyId,
			String propertyValue,
			PropertySelectionType selectionType) {
		this.propertyId = propertyId;
		this.propertyValue = propertyValue;
		this.selectionType = selectionType;
	}

	public String getPropertyId() {
		return propertyId;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public PropertySelectionType getSelectionType() {
		return selectionType;
	}

	public static PropertySelectionCriterion createEqual(String propertyId, String propertyValue) {
		return new PropertySelectionCriterion(propertyId, propertyValue, PropertySelectionType.EQUALS);
	}
	
	public static PropertySelectionCriterion createNotEqual(String propertyId, String propertyValue) {
		return new PropertySelectionCriterion(propertyId, propertyValue, PropertySelectionType.NOT_EQUAL);
	}
	
	public static PropertySelectionCriterion createContain(String propertyId, String text) {
		return new PropertySelectionCriterion(propertyId, text, PropertySelectionType.CONTAINS);
	}
	
	public static PropertySelectionCriterion createNotContain(String propertyId, String text) {
		return new PropertySelectionCriterion(propertyId, text, PropertySelectionType.NOT_CONTAIN);
	}
	
	public static PropertySelectionCriterion createStartsWith(String propertyId, String startText) {
		return new PropertySelectionCriterion(propertyId, startText, PropertySelectionType.STARTS_WITH);
	}
	
	public static PropertySelectionCriterion createEndsWith(String propertyId, String endText) {
		return new PropertySelectionCriterion(propertyId, endText, PropertySelectionType.ENDS_WITH);
	}
	
	public static PropertySelectionCriterion createStartsWith(String propertyId) {
		return new PropertySelectionCriterion(propertyId, null, PropertySelectionType.STARTS_WITH);
	}
	
	public static PropertySelectionCriterion createEndsWith(String propertyId) {
		return new PropertySelectionCriterion(propertyId, null, PropertySelectionType.ENDS_WITH);
	}
	
}
