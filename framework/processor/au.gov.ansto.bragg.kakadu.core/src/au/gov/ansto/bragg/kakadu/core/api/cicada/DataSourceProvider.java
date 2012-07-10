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
package au.gov.ansto.bragg.kakadu.core.api.cicada;

import java.io.File;
import java.util.List;

import au.gov.ansto.bragg.kakadu.core.data.DataItem;

/**
 * The interface describes data source provider.
 * The methods are used to fill up Data Source view on UI.  
 * 
 * @author Danil Klimontov (dak)
 */
public interface DataSourceProvider {
	
	/**
	 * Gets list of DataItem objects wich describes the file content.
	 * @param file data file
	 * @return list of DataItem objects or empty list if the 
	 * are no right data inside. 
	 */
	List<DataItem> parseDataFile(File file);
	

}
