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
import au.gov.ansto.bragg.process.agent.ProcessorAgent;

public class AgentThread extends Thread {

	/**
	 * @field id thread id number unique in an application
	 * @field agent processor agent instance
	 */
	private int id;
	private AlgorithmManager owner = null;
	protected static int nextID;
	protected ProcessorAgent agent = null;

	/**
	 * Parameterized constructor.
	 * @param agent processor agent handle
	 * @param owner algorithm manager handle
	 */
	public AgentThread(ProcessorAgent agent, AlgorithmManager owner){
		this.agent = agent;
		this.owner = owner;
		setID(nextID);
		nextID++;
	}

	/**
	 * This method return the unique id of the thread
	 * @return thread id in int type
	 */
	public int getID(){
		return id;
	}

	@Override
	public void run() {
		if (agent != null){
			Algorithm algorithm = owner.findAlgorithmWithAgent(agent);
			if (algorithm != null) {
				algorithm.setRunningFlag();
//				owner.getRunningAlgorithmList().add(algorithm);
			}
//			try {
//				owner.executeFrom(agent);
//			} catch (TunerNotReadyException e) {
//				e.printStackTrace();
//			} catch (TransferFailedException e) {
//				e.printStackTrace();
//				owner.catchTransferException(algorithm);
//			}
			try {
				agent.trigger();
			} catch (Exception e) {
				e.printStackTrace();
				if (algorithm != null) {
					owner.catchTransferException(algorithm, e);
					algorithm.catchTransferException(e);
				}
			} 
//			try {
//			algorithm.transfer();
//			} catch (TransferFailedException e) {
////			e.printStackTrace();
//			e.printStackTrace();
//			owner.catchTransferException(algorithm);
//			} 
			if (algorithm != null) {
				algorithm.resetRunningFlag();
//				owner.getRunningAlgorithmList().remove(algorithm);
			}
			agent = null;
			owner = null;
		}
//		owner.notify();
	}

	/**
	 * Set unique id for the thread.
	 * @param id in int type.
	 */
	public void setID(int id){
		this.id = id;
	}
}
