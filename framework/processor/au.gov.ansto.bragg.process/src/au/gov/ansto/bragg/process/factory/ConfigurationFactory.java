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

/**
 * A static method class for parsing algorithm recipe files into 
 * processor configuration objects.
 * <p>
 * The ConfigurationFactory will take xml file handle as input argument, and
 * parse it to get processor configuration information.
 * @author nxi
 * @version V1.0
 * @since M1
 */

import au.gov.ansto.bragg.process.configuration.FrameworkConfiguration;
import au.gov.ansto.bragg.process.configuration.ProcessorConfiguration;
import au.gov.ansto.bragg.process.configuration.ProcessorConfiguration_;
import au.gov.ansto.bragg.process.factory.exception.NullConfigurationPointerException;
import au.gov.ansto.bragg.process.parse.Parse;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

public class ConfigurationFactory {

	public static final long serialVersionUID = 1L;
	
	/**
	 * A list of configuration objects.
	 */
	protected List<ProcessorConfiguration_> configurationList = null;
	
	/**
	 * A handle to access the processor created
	 */
	protected ProcessorConfiguration configurationHandle = null;
	
	/**
	 * A static method to create a framework configuration from a given XML receipe file
	 * @param file  file handle as File instance
	 * @return  a handle of framework configuration object
	 * @throws NullConfigurationPointerException  can not create a configuration object
	 * @throws DocumentException  invalid recipe file
	 */
	public static FrameworkConfiguration createConfiguration(Document file) 
	throws NullConfigurationPointerException, DocumentException{
		FrameworkConfiguration frameworkConfiguration = null;
		Element rootElement = file.getRootElement();
		try{
			frameworkConfiguration = Parse.parseFile(rootElement);
		}catch(NullConfigurationPointerException ex){
			throw new NullConfigurationPointerException("ConfigurationFactory ");
		}
//		System.out.println(frameworkConfiguration.toString());
		return frameworkConfiguration;
	}
			
}
