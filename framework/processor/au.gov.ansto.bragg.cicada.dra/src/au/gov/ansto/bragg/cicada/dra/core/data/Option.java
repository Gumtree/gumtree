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
package au.gov.ansto.bragg.cicada.dra.core.data;

/**
 * @author nxi
 * Created on 14/04/2008
 */
public class Option {

	String[] options;
	String value;
	String name;
	
	public Option(String options, String value){
		this.value = value;
		this.options = getOptions(options);
	}

	public Option(String options){
		this.options = getOptions(options);
	}
	
	private String[] getOptions(String text) {
		// TODO Auto-generated method stub
		String[] words = text.split(",");
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].trim();
		}
		return words;
	}
	
	public void setValue(String value) throws IllegalOptionException{
		for (int i = 0; i < options.length; i++) {
			if (options[i].equals(value)){
				this.value = value;
				return;
			}
		}
		throw new IllegalOptionException();
	}
	
	public String getValue(){
		return value;
	}
}
