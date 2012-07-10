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

import java.net.URI;

import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.process.common.Common;
import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;
/**
 * An interface for Exporter_ class, which is used by cicada algorithm manager 
 * to export certain information to target platform. 
 * <p>
 * @author nxi
 * @version M1
 * @since V1.0
 * @see au.gov.ansto.bragg.cicada.core.Control#addExporter(String)
 */

public interface Exporter extends Common {
	
	/**
	 * Get the filename property of the exporter instance
	 * @return filename with path information as String
	 */
	public URI getFileURI();
	
	/**
	 * Get the formater information from the exporter instance
	 * @return formater instance 
	 */
	public Formater getFormater();
	
	/**
	 * Export result signal of the instrument signal object to a certain file
	 * @param filename  as String
	 * @throws ExportException 
	 */
	public void resultExport(URI fileURI) throws ExportException;
	
	/**
	 * Set the filename property of the exporter
	 * @param filename as String
	 */
	public void setFileURI(URI fileURI);
	
	/**
	 * Set the formater property of the exporter
	 * @param format  as formater instance
	 * @throws IllegalNameSetException illegal format name
	 */
	public void setFormat(Formater format) throws IllegalNameSetException;
	
	/**
	 * Export any double array (with dimension 0 to 3) to a certain file
	 * @param signal  generic signal as Object
	 * @param filename  as String
	 * @throws ExportException 
	 */
	public void signalExport(Object signal, URI fileURI) 
	throws ExportException;
	
	/**
	 * Export any double array (with dimension 0 to 3) to a certain file
	 * @param signal  generic signal as Object
	 * @param filename  as String
	 * @param title as String
	 * @throws ExportException 
	 */
	public void signalExport(Object signal, URI fileURI, String title) 
	throws ExportException;

	/**
	 * @param signal
	 * @param filename
	 * @param transpose
	 * @throws ExportException 
	 */
	public void signalExport(Object signal, URI fileURI, boolean transpose)
	throws ExportException;

	public boolean is1D();
	
	public boolean is2D();
	
	public boolean isMultiD();
//	public void signalExport(Group group, URI fileURI);
}
