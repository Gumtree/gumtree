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
package au.gov.ansto.bragg.cicada.core.internal;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.AlgorithmManager;

public class AlgorithmThread extends Thread {
	
	
	private int id;
	private AlgorithmManager owner = null;
	protected static int nextID;
	protected Algorithm algorithm = null;
	
	public AlgorithmThread(Algorithm algorithm, AlgorithmManager owner){
		this.algorithm = algorithm;
		this.owner = owner;
		setID(nextID);
		nextID++;
		if (owner != null)
			owner.addThread(this);
	}
	
	public int getID(){
		return id;
	}
	
	@Override
	public void run() {
		if (algorithm != null){
			algorithm.setRunningFlag();
			try {
//				algorithm.setUnchangedTuners();
				algorithm.transfer();
			} catch (Exception e) {
//				e.printStackTrace();
				if (owner != null)
					owner.catchTransferException(algorithm, e);
				algorithm.catchTransferException(e);
//				ThreadExceptionHandler catcher = owner.getExceptionHandlerList();
//				if (catcher != null) catcher.catchException(algorithm, e);
			} 
			algorithm.resetRunningFlag();
//			owner.getRunningAlgorithmList().remove(algorithm);
			if (owner != null)
				owner.removeThread(this);
		}
//		owner.notify();
		algorithm = null;
		owner = null;
	}
	
	public void setID(int id){
		this.id = id;
	}
}
