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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.Format;
import au.gov.ansto.bragg.cicada.core.Formater;
import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.process.common.Common_;
import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;

public class Formater_ extends Common_ implements Formater {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Format format = null;
//	protected Group resultSignal = null;
	protected FormatedExport exporter = null;
	protected Method method = null;
	protected String extensionName = null;
//	protected Object signal;

	public Formater_(Format format) {
		this.format = format;
		try {
			setName(format.name());
		} catch (IllegalNameSetException e) {
		}
		switch(format){
		case text:
			exporter = new TextExport();
			extensionName = "*.txt";
			break;
		case hdf:
			exporter = new HdfExport();
			extensionName = "*.hdf";
			break;
		case xml:
			exporter = new XMLExport();
			extensionName = "*.xml";
			break;
//		case bin:
//			exporter = new BinExport();
//			extensionName = "*.bin";
//			break;
		case XYSigma:
			exporter = new XYSigmaExport();
			extensionName = "*.x*d";
			break;
		case NakedXYSigma:
			exporter = new NakedXYSigmaExport();
			extensionName = "*.x*d";
			break;
		case sans:
			exporter = new SansExport();
			extensionName = "*.sans.txt";
			break;
		case pdCIF:
			exporter = new PdCIFExport();
			extensionName = "*.cif";
			break;
		case GSAS:
			exporter = new GsasExport();
			extensionName = "*.gsa";
			break;
		default:
			break;
		}
	}
	
	public String getExtensionName(){
		return extensionName;
	}
	
	public Method getMethod(){
		return method;
	}
	
//	protected void resultExport(URI fileURI) throws ExportException 
//	{
//		try {
//			exporter.signalExport(fileURI, resultSignal);
//		} catch (IOException e) {
//			throw new ExportException("failed to export to file " + fileURI.getPath() + 
//					", check if the file is locked\n" + e.getMessage(), e);
//		}
//	}

	public void resultExport(URI fileURI, IGroup signal) throws ExportException {
//		setResultSignal(signal);
//		resultExport(fileURI);
		try {
			exporter.signalExport(fileURI, signal);
		} catch (IOException e) {
			throw new ExportException("failed to export to file " + fileURI.getPath() + 
					", check if the file is locked\n" + e.getMessage(), e);
		}
	}

//	public void setResultSignal(Group signal){
//		this.resultSignal = signal;
//	}	
	
//	public void setSignal(Object signal){
//		this.signal = signal;
//	}

//	protected void export(URI fileURI, String title) throws ExportException 
//	{
//		try {
//			exporter.signalExport(fileURI, signal, title);
//		} catch (IOException e) {
//			throw new ExportException("failed to export to file " + fileURI.getPath() + 
//					", check if the file is locked\n" + e.getMessage(), e);
//		}
//	}

//	protected void signalExport(boolean transpose, URI fileURI) throws ExportException {
////		Object instance = exporter.newInstance();
////		method = instance.getClass().getMethod("signalExport", new Class[]{URI.class, Object.class, Boolean.class});
////		try {
////			method.invoke(exporter, new Object[]{fileURI, signal, Boolean.valueOf(transpose)});
////		}catch (Exception ex){
////			System.out.println(ex.getMessage());
////			System.out.println(signal);
////		}
//		try {
//			exporter.signalExport(fileURI, signal, transpose);
//		} catch (IOException e) {
//			throw new ExportException("failed to export to file " + fileURI.getPath() + 
//					", check if the file is locked\n" + e.getMessage(), e);
//		}
//	}
	
	public void signalExport(URI fileURI, Object signal) throws ExportException {
//		setSignal(signal);
//		export(fileURI, null);
		try {
			exporter.signalExport(fileURI, signal, null);
		} catch (IOException e) {
			throw new ExportException("failed to export to file " + fileURI.getPath() + 
					", check if the file is locked\n" + e.getMessage(), e);
		}
	}
	
	public void signalExport(URI fileURI, Object signal, String title) throws ExportException {
//		setSignal(signal);
//		export(fileURI, title);
		try {
			exporter.signalExport(fileURI, signal, title);
		} catch (IOException e) {
			throw new ExportException("failed to export to file " + fileURI.getPath() + 
					", check if the file is locked\n" + e.getMessage(), e);
		}
	}

	public void signalExport(URI fileURI, Object signal, boolean transpose) throws ExportException {
//		setSignal(signal);
//		signalExport(transpose, fileURI);
		try {
			exporter.signalExport(fileURI, signal, transpose);
		} catch (IOException e) {
			throw new ExportException("failed to export to file " + fileURI.getPath() + 
					", check if the file is locked\n" + e.getMessage(), e);
		}
	}

	public Format getFormat() {
		return format;
	}
}
