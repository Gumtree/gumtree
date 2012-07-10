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
package au.gov.ansto.bragg.process.factory;

import org.dom4j.Document;

import au.gov.ansto.bragg.process.configuration.FrameworkConfiguration;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.processor.Framework;
import au.gov.ansto.bragg.process.processor.Framework_;
import au.gov.ansto.bragg.process.processor.Processor;
/**
 * A static method class for creating 
 * processors from configuration objects.
 * <p>
 * The ProcessorFactory will processor framework configuration handle as input argument, 
 * and build processor framework with multiple processors.
 * <p>
 * Created on 01/02/2007, 4:08:07 PM
 * <p>
 * Last modified 19/04/2007, 4:08:07 PM
 * @author nxi
 * @version V1.0
 * @since M1
 * @see ConfigurationFactory
 */
public class ProcessorFactory {

	public final static long serialVersionUID = 1L;
	
	/**
	 * By nxi on 01/02/2007, 4:08:07 PM
	 * A handle to access processor created.
	 */
	protected Processor processorHandle = null;

	/**
	 * This method returns the processor created.
	 * @return processor object
	 */
	public Processor getProcessorHandle(){
		return processorHandle;
	}

	/**
	 * A static method of loading algorithm from a given receipe file with a 
	 * Document handle.
	 * @param file  Document type file handle
	 * @return processor framework handle
	 * @throws ProcessorChainException 
	 */
	public static Framework loadAlgorithm(Document file) throws ProcessorChainException {
		Framework frameworkHandle = null;
		FrameworkConfiguration frameworkConfiguration = null; 
		try {
			frameworkConfiguration = ConfigurationFactory.createConfiguration(file);
		} catch (Exception e) {
			throw new ProcessorChainException("failed to read from the recipe file " + file.getName() + 
					": " + e.getMessage(), e);
		} 
		frameworkHandle = new Framework_(frameworkConfiguration);
		return frameworkHandle;
	}

	/**
	 * A static method of load algorithm from a framework configuration object.
	 * @param frameworkConfiguration framework configuration object
	 * @return handle to access processor framework
	 * @throws ProcessorChainException 
	 */
	public static Framework loadAlgorithm(FrameworkConfiguration frameworkConfiguration) 
	throws ProcessorChainException 
	{
//		public static CompositeProcessorConfiguration_ loadAlgorithm(Document file) throws NullConfigurationPointerException{
		Framework frameworkHandle = null;
		frameworkHandle = new Framework_(frameworkConfiguration);
		/*			int i= 0;
			System.out.println(frameworkHandle.getPortArray().size());
			for (Iterator<?> iter = frameworkHandle.getPortArray().iterator(); iter.hasNext();)
			{	System.out.println(iter.next().toString());
				System.out.println("***********" + ++i);
			}
		 */			
//		System.out.println(frameworkHandle.toString());
		return frameworkHandle;
	}
}
