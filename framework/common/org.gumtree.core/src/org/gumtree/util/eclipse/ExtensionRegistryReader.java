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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;

/*
 * COMMENT: tla @ 2006-05-08
 * Modified from org.eclipse.ui.internal.registry.RegistryReader
 */
/**
 * Template extension reader for the plugin architecture.  This is
 * intented to be implemented by clients for reading various type
 * of extension points.
 *
 * @since 1.0
 */
public abstract class ExtensionRegistryReader {

	private Plugin plugin;

	/**
	 * Class constructor.  Plugin should be supplied for logging
	 * errors and warnings occured during reading of the extension.
	 *
	 * @param plugin the plugin which the extended reader belongs
	 */
	protected ExtensionRegistryReader(Plugin plugin) {
		this.plugin = plugin;
    }

	/**
	 * Returns the plugin assigned for the reader.
	 *
	 * @return plugin supplied for the reader
	 */
	protected Plugin getPlugin() {
		return plugin;
	}

	/**
     * Logs the error in the workbench log using the provided
     * text and the information in the configuration element.
     */
    protected void logError(IConfigurationElement element, String text) {
        IExtension extension = element.getDeclaringExtension();
        StringBuffer buf = new StringBuffer();
        buf
                .append("Plugin " + extension.getNamespaceIdentifier() + ", extension " + extension.getExtensionPointUniqueIdentifier());//$NON-NLS-2$//$NON-NLS-1$
        buf.append("\n" + text);//$NON-NLS-1$
        String message = buf.toString();
        plugin.getLog().log(
                StatusUtils.newStatus(IStatus.ERROR, message, null));
    }

    /**
     * Logs a very common registry error when a required attribute is missing.
     */
    protected void logMissingAttribute(IConfigurationElement element,
            String attributeName) {
        logError(element,
                "Required attribute '" + attributeName + "' not defined");//$NON-NLS-2$//$NON-NLS-1$
    }

    /**
     * Logs a very common registry error when a required child is missing.
     */
    protected void logMissingElement(IConfigurationElement element,
            String elementName) {
        logError(element,
                "Required sub element '" + elementName + "' not defined");//$NON-NLS-2$//$NON-NLS-1$
    }

    /**
     * Logs a registry error when the configuration element is unknown.
     */
    protected void logUnknownElement(IConfigurationElement element) {
        logError(element, "Unknown extension tag found: " + element.getName());//$NON-NLS-1$
    }

    /**
     * Apply a reproducable order to the list of extensions
     * provided, such that the order will not change as
     * extensions are added or removed.
     * @param extensions the extensions to order
     * @return ordered extensions
     */
    public static IExtension[] orderExtensions(IExtension[] extensions) {
        // By default, the order is based on plugin id sorted
        // in ascending order. The order for a plugin providing
        // more than one extension for an extension point is
        // dependent in the order listed in the XML file.
        IExtension[] sortedExtension = new IExtension[extensions.length];
        System.arraycopy(extensions, 0, sortedExtension, 0, extensions.length);
        Comparator<IExtension> comparer = new Comparator<IExtension>() {
            public int compare(IExtension ext1, IExtension ext2) {
                String s1 = ext1.getNamespaceIdentifier();
                String s2 = ext2.getNamespaceIdentifier();
                return s1.compareToIgnoreCase(s2);
            }
        };
        Collections.sort(Arrays.asList(sortedExtension), comparer);
        return sortedExtension;
    }

    /**
     * Implement this method to read element's attributes.
     * If children should also be read, then implementor
     * is responsible for calling <code>readElementChildren</code>.
     * Implementor is also responsible for logging missing
     * attributes.
     *
     * @return true if element was recognized, false if not.
     */
    protected abstract boolean readElement(IConfigurationElement element);

    /**
     * Read the element's children. This is called by
     * the subclass' readElement method when it wants
     * to read the children of the element.
     */
    protected void readElementChildren(IConfigurationElement element) {
        readElements(element.getChildren());
    }

    /**
     * Read each element one at a time by calling the
     * subclass implementation of <code>readElement</code>.
     *
     * Logs an error if the element was not recognized.
     */
    protected void readElements(IConfigurationElement[] elements) {
        for (int i = 0; i < elements.length; i++) {
            if (!readElement(elements[i])) {
				logUnknownElement(elements[i]);
			}
        }
    }

    /**
     * Read one extension by looping through its
     * configuration elements.
     */
    protected void readExtension(IExtension extension) {
        readElements(extension.getConfigurationElements());
    }

    /**
     *	Start the registry reading process using the
     * supplied plugin ID and extension point.
     *
     * @param registry the registry to read from
     * @param pluginId the plugin id of the extenion point
     * @param extensionPoint the extension point id
     */
    public void readRegistry(IExtensionRegistry registry, String pluginId,
            String extensionPoint) {
        IExtensionPoint point = registry.getExtensionPoint(pluginId,
                extensionPoint);
        if (point == null) {
			return;
		}
        IExtension[] extensions = point.getExtensions();
        extensions = orderExtensions(extensions);
        for (int i = 0; i < extensions.length; i++) {
			readExtension(extensions[i]);
		}
    }

    /**
     * Utility for extracting the description child of an element.
     *
     * @param configElement the element
     * @return the description
     * @since 3.1
     */
    public static String getDescription(IConfigurationElement configElement) {
		IConfigurationElement[] children = configElement.getChildren(ExtensionRegistryConstants.ELEMENT_DESCRIPTION);
	    if (children.length >= 1) {
	        return children[0].getValue();
	    }
	    return "";//$NON-NLS-1$
    }

    /**
	 * Utility for extracting the value of a class attribute or a nested class
	 * element that follows the pattern set forth by
	 * {@link org.eclipse.core.runtime.IExecutableExtension}.
	 *
	 * @param configElement
	 *            the element
	 * @param classAttributeName
	 *            the name of the class attribute to check
	 * @return the value of the attribute or nested class element
	 * @since 3.1
	 */
    public static String getClassValue(IConfigurationElement configElement, String classAttributeName) {
    	String className = configElement.getAttribute(classAttributeName);
    	if (className != null) {
			return className;
		}
		IConfigurationElement [] candidateChildren = configElement.getChildren(classAttributeName);
		if (candidateChildren.length == 0) {
			return null;
		}

		return candidateChildren[0].getAttribute(ExtensionRegistryConstants.ATTRIBUTE_CLASS);
    }
}
