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
package au.gov.ansto.bragg.cicada.core;

import java.lang.reflect.Method;
import java.net.URI;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.process.common.Common;
/**
 * An interface for Formater_ class, which is used by cicada algorithm manager 
 * to format the output data set.  
 * <p>
 * The formater can take signal type object and generic Object signal to export.
 * @author nxi
 * @version M1
 * @since V1.0
 * @see au.gov.ansto.bragg.cicada.core.Control#resultExport(String)
 * @see au.gov.ansto.bragg.cicada.core.Control#signalExport(String, Object)
 */

public interface Formater extends Common {
	
	/**
	 * Get the extension name of a formater instance
	 * @return extension name in String type
	 */
	public String getExtensionName();
	
	/**
	 * Get the method object of the formater instance, which will be used to perform exporting.
	 * @return Method instance
	 */
	public Method getMethod();
	
	/**
	 * Export result signal from the instrument signal object: SignalType
	 * The result signal is in double[][] type
	 * @param filename  in String type
	 * @param signal  SignalType data
	 * @throws ExportException 
	 */
	public void resultExport(URI fileURI, IGroup signal) 
	throws ExportException;
	
	/**
	 * Set result signal in the instrument signal object
	 * @param signal  in SignalType
	 * @see au.gov.ansto.bragg.dra.data.databag.GroupData
	 */
//	public void setResultSignal(Group signal);
	
	/**
	 * Set signal property of the formater
	 * The signal is an inhirance of Object type, it can a double array 
	 * with dimension from 0 to 3  
	 * @param signal  generic signal in Object type
	 */
//	public void setSignal(Object signal);
	
	/**
	 * Export any double array signal to target file
	 * @param filename  in String type
	 * @param signal  generic signal in Object type
	 * @throws ExportException 
	 */
	public void signalExport(URI fileURI, Object signal) 
	throws ExportException;

	public void signalExport(URI fileURI, Object signal, boolean transpose)
	throws ExportException;

	public void signalExport(URI fileURI, Object signal, String title)
	throws ExportException;

	public Format getFormat();
}
