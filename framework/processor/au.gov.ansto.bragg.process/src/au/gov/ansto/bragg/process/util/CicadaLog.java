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
package au.gov.ansto.bragg.process.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nxi
 * Created on 07/04/2009
 */
public class CicadaLog {

	public static final String CICADA_LOG_NAME = "cicada";
	public static Logger getLog(){
		return LoggerFactory.getLogger(CICADA_LOG_NAME);
	}
}
