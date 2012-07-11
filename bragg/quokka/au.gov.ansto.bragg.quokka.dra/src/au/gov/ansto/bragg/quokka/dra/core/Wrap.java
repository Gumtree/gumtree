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
package au.gov.ansto.bragg.quokka.dra.core;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.quokka.dra.core.internal.ConcreteProcessor;

public class Wrap implements ConcreteProcessor {

	IGroup wrap_databag = null;
	IGroup wrap_result = null;
	String wrap_inputName = "data";
	String wrap_resultName = "dataReduction_result";
	IGroup wrap_output = null;
	public Boolean process() throws Exception{
//		List<Object> result = new LinkedList<Object>();
//		result.add(array2theta);
//		CachedVariable inputVariable = databag.getVariable(inputName);
//		Group parentGroup = (Group) inputVariable.getParentGroup();
//		resultGroup.setShortName(resultName);
//		parentGroup.addGroup(resultGroup);
//		Group group = new Group(databag.getFile(), parentGroup, resultName + "_group", true); 
//		CachedVariable variable = new CachedVariable(databag.getFile(), group, null, resultName);
//		variable.setCachedData(array, true);
//		group.insertVariable(variable);
//		detectorGroup.insertGroup(group);
//		result.add(databag);
//		return result;
		wrap_result.setShortName(wrap_resultName);
		wrap_output = wrap_databag;
		return false;
	}
	public IGroup getWrap_output() {
		return wrap_output;
	}
	public void setWrap_databag(IGroup wrap_databag) {
		this.wrap_databag = wrap_databag;
	}
	public void setWrap_result(IGroup wrap_result) {
		this.wrap_result = wrap_result;
	}
	public void setWrap_inputName(String wrap_inputName) {
		this.wrap_inputName = wrap_inputName;
	}
	public void setWrap_resultName(String wrap_resultName) {
		this.wrap_resultName = wrap_resultName;
	}


	
}
