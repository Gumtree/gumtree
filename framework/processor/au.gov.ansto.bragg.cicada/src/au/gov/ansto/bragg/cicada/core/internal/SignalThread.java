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

import java.util.List;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.AlgorithmManager;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.process.exception.NullSignalException;
import au.gov.ansto.bragg.process.processor.Sink;

/**
 * 
 * @author nxi
 * @since V1.0
 * @deprecated since V2.0
 */
public class SignalThread extends Thread {

	private AlgorithmManager parent;
	private IGroup signal = null;
	private Sink sink = null;
	
	public SignalThread(AlgorithmManager parent) throws NoneAlgorithmException, NullSignalException{
		this.parent = parent;
		signal = parent.getCurrentInputData();
		List<Sink> sinkList = parent.getCurrentAlgorithm().getSinkList();
		sink = sinkList.get(sinkList.size() - 1);
		sink.subscribe(this);
	}
	
	@Override
	public synchronized void run(){
		while(sink != null && !parent.isDisposed()){
//			System.out.println("signal thread is waiting");
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			System.out.println("signal thread awakened");
			if (sink != null && !parent.isDisposed() && signal != null)
				System.out.println("Insert result to databag");
//				signal.setResultData(sink.getSignal());
//				DataGroup newGroup = new DataGroup(null, )
//				signal.addGroup();
//			System.out.println(signal.getResultData().length);
		}
	}
}
