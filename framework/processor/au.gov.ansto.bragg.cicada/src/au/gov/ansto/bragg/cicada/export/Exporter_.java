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
package au.gov.ansto.bragg.cicada.export;

import java.net.URI;

import au.gov.ansto.bragg.cicada.core.AlgorithmManager;
import au.gov.ansto.bragg.cicada.core.Exporter;
import au.gov.ansto.bragg.cicada.core.Format;
import au.gov.ansto.bragg.cicada.core.Formater;
import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.process.common.Common_;
import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;

/**
 * A interface for Exporter_ class, which is used by cicada algorithm manager 
 * to export certain information to target platform. 
 * <p>
 * @author nxi
 * @version M1
 * @since V1.0
 *
 */
public class Exporter_ extends Common_ implements Exporter{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected AlgorithmManager parent = null;
	protected URI fileURI = null;
	protected Formater format = null;
	
	public Exporter_(AlgorithmManager parent){
		super();
		this.parent = parent;
		
	}
	
	public Exporter_(Formater format){
		this.format = format;
	}
	
	public Exporter_(AlgorithmManager parent, Format format) throws ClassNotFoundException, IllegalNameSetException {
		this(parent);
		Formater formater = new Formater_(format);
		setFormat(formater);
		setName(format.name());
	}

	public Exporter_(AlgorithmManager parent, URI fileURI){
		this(parent);
		setFileURI(fileURI);
	}
	
	public Exporter_(AlgorithmManager parent, URI fileURI, Formater format) throws IllegalNameSetException{
		this(parent, fileURI);
		setFormat(format);
		setName(format.getName());
	}
	
	public URI getFileURI(){
		return fileURI;
	}
	
	public Formater getFormater(){
		return format;
	}
	
	public void resultExport(URI fileURI) throws ExportException {
		this.fileURI = fileURI;
		if (parent != null)
			format.resultExport(fileURI, parent.getCurrentInputData());
	}
	
	public void setFileURI(URI fileURI){
		this.fileURI = fileURI;
	}
	
	public void setFormat(Formater format) throws IllegalNameSetException{
		this.format = format;
		setName(format.getName());
	}	
	
	public void signalExport(Object signal, URI fileURI) throws ExportException {
		this.fileURI = fileURI;
		format.signalExport(fileURI, signal);
	}

	public void signalExport(Object signal, URI fileURI, String title) throws ExportException {
		this.fileURI = fileURI;
		format.signalExport(fileURI, signal, title);
	}

	public void signalExport(Object signal, URI fileURI, boolean transpose) throws ExportException {
		this.fileURI = fileURI;
		format.signalExport(fileURI, signal, transpose);		
	}

	public boolean is1D() {
		if (format.getFormat() == Format.text)
			return false;
		return true;
	}

	public boolean is2D() {
		if (format.getFormat() == Format.XYSigma)
			return false;
		return true;
	}

	public boolean isMultiD() {
		if (format.getFormat() == Format.XYSigma)
			return false;
		return true;
	}
}
