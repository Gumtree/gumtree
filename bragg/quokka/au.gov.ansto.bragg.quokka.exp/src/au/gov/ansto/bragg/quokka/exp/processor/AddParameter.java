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
package au.gov.ansto.bragg.quokka.exp.processor;

import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.dom.sics.SicsDOM;
import org.gumtree.gumnix.sics.dom.sics.SicsDOMFactory;

import au.gov.ansto.bragg.kakadu.core.data.DataItem;

/**
 * @author nxi
 * Created on 07/08/2008
 */
public class AddParameter implements ConcreteProcessor {

	public static final String PARAMETER_PATH = "/instrument/parameters";
	private IGroup addParameter_inputGroup;
	private Boolean addParrameter_skip = false;
	private Boolean addParrameter_stop = false;
	private IGroup addParameter_outputGroup;

	/**
	 * @return the addParameter_outputGroup
	 */
	public IGroup getAddParameter_outputGroup() {
		return addParameter_outputGroup;
	}

	/**
	 * @param addParameter_inputGroup the addParameter_inputGroup to set
	 */
	public void setAddParameter_inputGroup(IGroup addParameter_inputGroup) {
		this.addParameter_inputGroup = addParameter_inputGroup;
	}

	/**
	 * @param addParrameter_skip the addParrameter_skip to set
	 */
	public void setAddParrameter_skip(Boolean addParrameter_skip) {
		this.addParrameter_skip = addParrameter_skip;
	}

	/**
	 * @param addParrameter_stop the addParrameter_stop to set
	 */
	public void setAddParrameter_stop(Boolean addParrameter_stop) {
		this.addParrameter_stop = addParrameter_stop;
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.processor.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		if (addParrameter_skip)
			return addParrameter_stop;
		SicsDOM sics = (SicsDOM) SicsDOMFactory.getSicsDOM();
		List<IComponentController> controllerList = sics.getSubDynamicControllerList("/instrument/parameters");
		for (IComponentController controller : controllerList){
			if (controller instanceof IDynamicController){
				String controllerId = controller.getComponent().getId();
				String value = ((IDynamicController) controller).getValue().getSicsString();
				IDataItem item = Factory.createDataItem(addParameter_inputGroup, controllerId, 
						Factory.createArray(value.toCharArray()));
				addParameter_inputGroup.addDataItem(item);
			}
		}
		addParameter_outputGroup = addParameter_inputGroup;
		return addParrameter_stop;
	}

}
