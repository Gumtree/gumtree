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
package au.gov.ansto.bragg.process.processor;

/**
 * Interface of Source_, which is a Child class of Processor. 
 * <p> 
 * Source processor checks the input
 * signal to see if it is a valid input to the algorithm. Source processor can also
 * load a new signal.  
 * <p>
 * The Source processor takes some customized signal as input and create prime type
 * signals for export. 
 * <p>
 * Created on 20/02/2007, 9:58:24 AM
 * <p>
 * Last modified 19/04/2007, 9:58:24 AM
 *
 * @author nxi
 * @version V1.0
 * @since M1
 * @see Processor
 */

public interface Source extends Processor {

}
