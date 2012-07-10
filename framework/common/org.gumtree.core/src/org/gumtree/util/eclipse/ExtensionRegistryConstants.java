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

/**
 * Collection of string contants used by the extenion registry.
 * <p>
 * This class is not intended to be implemented by clients.
 * <p>
 *
 * @since 1.0
 */
public final class ExtensionRegistryConstants {

	/**
	 * Identifies the element name "description" used by the extension point.
	 */
	public static final String ELEMENT_DESCRIPTION = "description";

	/**
	 * Identifies the element name "property" used by the extension point.
	 */
	public static final String ELEMENT_PROPERTY = "property";
	
	/**
	 * Identifies the attribute name "id" used by the extension point.
	 */
	public static final String ATTRIBUTE_ID = "id";

	/**
	 * Identifies the attribute name "class" used by the extension point.
	 */
	public static final String ATTRIBUTE_CLASS = "class";

	/**
	 * Identifies the attribute name "name" used by the extension point.
	 */
	public static final String ATTRIBUTE_NAME = "name";
	
	/**
	 * Identifies the attribute name "value" used by the extension point.
	 */
	public static final String ATTRIBUTE_VALUE = "value";

	/**
	 * Identifies the attribute name "icon" used by the extension point.
	 */
	public static final String ATTRIBUTE_ICON = "icon";

	/**
	 * Identifies the attribute name "label" used by the extension point.
	 */
	public static final String ATTRIBUTE_LABEL = "label";

	/**
	 * Identifies the attribute name "tags" used by the extension point.
	 */
	public static final String ATTRIBUTE_TAGS = "tags";
	
	/*
	public static final String ATTRIBUTE_CATEGORY = "category";

	public static final String ATTRIBUTE_GROUP = "group";

	public static final String ATTRIBUTE_IMAGE = "image";

	public static final String ATTRIBUTE_DEFAULT = "default";
	*/

	/**
	 * Private constructor to block instance creation.
	 */
	private ExtensionRegistryConstants() {
		super();
	}

}
