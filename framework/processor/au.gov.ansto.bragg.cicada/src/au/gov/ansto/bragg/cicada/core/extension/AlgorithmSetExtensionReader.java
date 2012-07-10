/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.cicada.core.extension;

import static au.gov.ansto.bragg.cicada.core.extension.AlgorithmSetExtensionConstants.CICADA_CONFIGURATION_EXTENSION_ELEMENT;
import static au.gov.ansto.bragg.cicada.core.extension.AlgorithmSetExtensionConstants.CICADA_CONFIGURATION_EXTENSION_POINT;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.util.eclipse.ExtensionRegistryReader;

import au.gov.ansto.bragg.cicada.core.internal.Activator;

public class AlgorithmSetExtensionReader extends ExtensionRegistryReader {

	private AlgorithmRegistration algorithmRegistration;

	protected AlgorithmSetExtensionReader(AlgorithmRegistration algorithmRegistration) {
		super(Activator.getDefault());
		this.algorithmRegistration = algorithmRegistration;
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		if(element.getName().equals(CICADA_CONFIGURATION_EXTENSION_ELEMENT)) {
			readFactory(element);
			return true;
		}
		return false;
	}

	private void readFactory(IConfigurationElement element) {
		algorithmRegistration.addAlgorithmSet(element);
	}

	public void readCicadaConfigurationExtensions() {
		IExtensionRegistry in = Platform.getExtensionRegistry();
		readRegistry(in, getPlugin().getBundle().getSymbolicName(), CICADA_CONFIGURATION_EXTENSION_POINT);
	}

}
