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

/*
 * Project ServiceLocator
 * Created on Apr 9, 2006
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * A copy of the LGPL is available also at
 * http://www.gnu.org/copyleft/lesser.html
 *
 */
package au.gov.ansto.bragg.cicada.core.extension;

import static org.gumtree.util.eclipse.ExtensionRegistryConstants.*;
//import static org.gumtree.core.internal.servicelocator.ServiceFactoryExtensionConstants.*;

import org.eclipse.core.runtime.IConfigurationElement;


/**
 * A lightweight descriptor which represents a {@link IServiceFactory}.
 *
 * @author Riccardo "battlehorse" Govoni [battlehorse@gmail.com]
 * @since Apr 9, 2006
 */
public class AlgorithmSetDescriptor {

	/* The configuration element which this descriptor is based upon */
	private IConfigurationElement conf;

//	private IServiceFactory factory;

	/**
	 * Creates a new descriptor
	 *
	 * @param conf the configuration element which is represented by this descriptor
	 */
	public AlgorithmSetDescriptor(IConfigurationElement conf) {
		this.conf = conf;
	}

	/**
	 * Returns the service factory id
	 *
	 * @return the service factory id
	 */
	public String getId() {
		return conf.getAttribute(ATTRIBUTE_ID);
	}

	/**
	 * Returns the service factory name
	 *
	 * @return the service factory name. The factory id is used whenever the name is not available
	 */
	public String getName() {
		String name = conf.getAttribute(ATTRIBUTE_NAME);
		if (name == null)
			return getId();
		else
			return name;
	}

	/**
	 * Returns an instance of the service factory described by this descriptor
	 *
	 * @return an instance of the service factory described by this descriptor
	 * @throws ServiceException if an error occurs while instantiating the factory
	 */
//	public IServiceFactory getFactoryInstance() throws ServiceException {
//		if(factory == null) {
//			try {
//				factory = (IServiceFactory) conf.createExecutableExtension(ATTRIBUTE_CLASS);
//			}
//			catch(Exception e) {
//				throw new ServiceException(e);
//			}
//		}
//		return factory;
//	}

	/**
	 * Returns the className of the service (aka class type) which is handled by this factory
	 *
	 * @return the className of the service (aka class type) which is handled by this factory
	 */
//	public String getResourceClass() {
//		return conf.getAttribute(ATTRIBUTE_RESOURCE_CLASS);
//	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Return the version of the algorithm set extension.
	 * @return version number in String type.
	 */
	public String getVersion(){
		String version = conf.getAttribute("version");
		if (version == null)
			return "0.0";
		else
			return version;		
	}
}
