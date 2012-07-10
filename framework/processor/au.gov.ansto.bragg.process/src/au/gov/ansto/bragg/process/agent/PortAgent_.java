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
package au.gov.ansto.bragg.process.agent;

import au.gov.ansto.bragg.process.configuration.AgentConfiguration;
import au.gov.ansto.bragg.process.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.process.exception.NullPrincipalException;
import au.gov.ansto.bragg.process.port.Port;
import au.gov.ansto.bragg.process.processor.Framework;
import au.gov.ansto.bragg.process.util.SortedArrayList;

/**
 * @author nxi
 * Created on 20/02/2007, 5:23:15 PM
 * Last modified 20/02/2007, 5:23:15 PM
 * 
 */
public class PortAgent_ extends Agent_ implements PortAgent {

	public final static long serialVersionUID = 1L;
//	private Port principal;
	
	public PortAgent_(AgentConfiguration configuration){
		super(configuration);
//		setID();
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.agent.PortAgent#getLockStatus()
	 */
	public boolean getLockStatus() {
		// TODO Auto-generated method stub
		return ((Port) principal).getLockStatus();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.agent.PortAgent#getSignalFlag()
	 */
	public boolean getSignalFlag() {
		// TODO Auto-generated method stub
		return getLockStatus();
//		return ((Port) principal).getLockStatus();
	}
	
	protected Port getPrincipal() throws NullPrincipalException{
		if (principal == null) throw new NullPrincipalException("no port principal");
		return (Port) principal;
	}
	
	public void setPrincipal(Framework framework) throws IndexOutOfBoundException{
		super.setPrincipal(SortedArrayList.getPortFromName(framework.getPortArray(), pName));
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.agent.PortAgent#getSignal()
	 */
	public Object getSignal() throws NullPrincipalException{
		return getPrincipal().getSignal();
	}

	public String getStatus() {
		try {
			return getPrincipal().getLockStatus()?"Locked":"Unlocked";
		} catch (NullPrincipalException e) {
			// TODO Auto-generated catch block
			return "Error";
		}
	}
	
	public String toString(){
		String result = "<agent id=\"" + getID() + "\" name=\"" + getName() + "\" principal=\"" + getPrincipalName() + "\">\n";
//		result += "<receipe_id>" + getReceipeID() + "</receipe_id>\n";
		result += "<description>" + getDescription() + "</description>\n";
		if (principal != null){
		result += "<status>" + ((Port) principal).getStatus() + "</status>\n";
		result += "<signal>" + ((Port) principal).getSignal().toString() + "</signal>";
		}
		result += "</agent>\n";
		return result;
	}
}
