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
package au.gov.ansto.bragg.quokka.exp.core.command;

import java.io.File;
import java.net.URI;

import org.gumtree.dae.core.util.HistogramType;
import org.gumtree.dae.core.util.ILiveDataRetriever;
import org.gumtree.dae.core.util.LiveDataRetriever;

import au.gov.ansto.bragg.quokka.exp.core.Command;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;
import au.gov.ansto.bragg.quokka.exp.core.exception.InitializeCommandException;

/**
 * @author nxi
 * Created on 03/06/2008
 */
public class Histogram implements Command{

	private ILiveDataRetriever retriever;
	
	protected void setUp() {
	}
	
	public String getHelp() {
		// TODO Auto-generated method stub
		String help = "MODEL: Check the status of the Quokka instrument model.\n";
		help += "Usage: model\n";
		help += "This command requires no argument.\n";
		return help;
	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "MODEL: Check the status of the Quokka instrument model.\n";
		description += "Usage: model\n";
		description += "For more information, please use 'help model'.\n";
		return description;
	}

	public String run() {
		// TODO Auto-generated method stub
		String result = "";
		retriever = new LiveDataRetriever();
		retriever.setUser("Gumtree");
		retriever.setPassword("Gumtree");

		URI fileHandle = null;
		try {
			fileHandle = retriever.getHDFFileHandle("localhost", 8081, HistogramType.TOTAL_HISTO_XY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return "can not make the connection";
		}
		// Test if file is physically available
		File dataFile = new File(fileHandle);
		
		return dataFile.getPath();
	}

	public void setExperiment(QuokkaExperiment experiment) {
		// TODO Auto-generated method stub
		
	}

	public void setParameter(String... params)
			throws InitializeCommandException {
		// TODO Auto-generated method stub
		
	}

}
