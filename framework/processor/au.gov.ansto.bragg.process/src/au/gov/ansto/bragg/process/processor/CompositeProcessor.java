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

import java.util.List;
/**
 * Interface of CompositeProcessor, a Child class of Processor.
 * <p> 
 * A CompositeProcessor is a processor that can nest other processors inside.
 * The CompositeProcessor instances are built from an algorithm recipe xml file. They 
 * usually contain multiple processor chains or other composite processors. 
 * A composite processor do not do processing directly. Instead it will triger 
 * its nested processor to do the processing. 
 * <p>
 * A composite process has its own In, Out and Var ports, which will passing
 * their signals to their consumers or taking signals from their producers 
 * respectively.
 * <p>
 * Created on 20/02/2007, 9:58:24 AM
 * <p>
 * Last modified 19/04/2007, 9:58:24 AM
 *
 * @author nxi
 * @version V1.0
 * @since M1
 * @see Processor
 * @see Framework
 */
public interface CompositeProcessor extends Processor {

	/**
	 * This method add a processor to the processor list. The processor list
	 * contains all the processors that is nested in this composite processor.
	 * @param processor a Processor instance
	 */
	public void addProcessor(Processor processor);

	/**
	 * Get the processor list of the composite processor.
	 * @return processor list as List<Processor> type
	 */
	public List<Processor> getProcessorList();
	
}
