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
package au.gov.ansto.bragg.process.configuration;

/**
 * @author nxi
 *
 */
public class SinkConfiguration_ extends ProcessorConfiguration_ implements
		SinkConfiguration {
	public final static long serialVersionUID = 1L;
	private String autoPlot = "false";
	private boolean isDefault = false;
	
	public SinkConfiguration_(final String name, final String parentName){
		super(name, parentName);
	}
	public SinkConfiguration_(String name, String parentName, String autoPlot,
			String isDefaultSink) {
		// TODO Auto-generated constructor stub
		this(name, parentName);
		if (autoPlot != null)
			this.autoPlot = autoPlot;
		setDefault(isDefaultSink);
	}
	
	public String getAutoPlot(){
		return autoPlot;
	}
	
	private void setDefault(String isDefaultString){
		try {
			isDefault = Boolean.valueOf(isDefaultString);
		} catch (Exception e) {}
	}
	
	public boolean isDefault() {
		// TODO Auto-generated method stub
		return isDefault;
	}
}
