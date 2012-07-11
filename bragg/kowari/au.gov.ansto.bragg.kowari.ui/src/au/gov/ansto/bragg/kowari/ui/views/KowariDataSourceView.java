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
package au.gov.ansto.bragg.kowari.ui.views;

import java.io.File;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.controllers.ComponentDataFormatException;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.instrument.InstrumentDataSourceView;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.port.Tuner;

/**
 * @author nxi
 * Created on 03/08/2009
 */
public class KowariDataSourceView extends InstrumentDataSourceView {

	private final static String FILENAME_NODE_PATH = "/experiment/file_name";
	private final static String CURRPOINT_NODE_PATH = "/experiment/currpoint";
	private final static String SAVE_COUNT_PATH = "/experiment/save_count";
//	private final static String CURRENT_POINT_PATH = "/experiment/currpoint";
	private int saveCount = 0;
	private String defaultFolderName;
	private IDynamicController saveCountNode;
	private IDynamicControllerListener statusListener;

	/**
	 * 
	 */
	public KowariDataSourceView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		defaultFolderName = System.getProperty("sics.data.path");
		try {
			initListeners();
		} catch (Exception e) {
			LoggerFactory.getLogger(this.getClass()).error("can not read folder " + defaultFolderName, e);
		}
		IActionBars bars = getViewSite().getActionBars();
		final IMenuManager menuManager = bars.getMenuManager();
		final IToolBarManager toolBarManager = bars.getToolBarManager();
		menuManager.remove(findAction(menuManager.getItems(), "Combine Data"));
		toolBarManager.remove(findAction(toolBarManager.getItems(), "Combine Data"));
		menuManager.remove(findAction(menuManager.getItems(), "Select All"));
		toolBarManager.remove(findAction(toolBarManager.getItems(), "Select All"));
		menuManager.remove(findAction(menuManager.getItems(), "Deselect All"));
		toolBarManager.remove(findAction(toolBarManager.getItems(), "Deselect All"));		
	}

	private IContributionItem findAction(IContributionItem[] items, String name) {
		for (IContributionItem item : items){
			if (item instanceof ActionContributionItem)
				if (((ActionContributionItem) item).getAction().getText().equals(name))
					return item;
		}
		return null;
	}

	protected void initListeners() throws ComponentDataFormatException, SicsIOException {
		final ISicsController sics = SicsCore.getSicsController();
		if (sics != null){
			final IDynamicController filenameNode = (IDynamicController) sics.findComponentController(
					FILENAME_NODE_PATH);
			final IDynamicController currentPointNode = (IDynamicController) sics.findComponentController(
					CURRPOINT_NODE_PATH);
			//		final IDynamicController currentPointNode = (IDynamicController) sics.findComponentController(
			//				CURRENT_POINT_PATH);
			saveCountNode = (IDynamicController) sics.findComponentController(SAVE_COUNT_PATH);
			//		for (IDynamicControllerListener listener : statusListeners)
			//			saveCountNode.removeComponentListener(listener);
			//		statusListeners.clear();
			saveCount = saveCountNode.getValue().getIntData();
			statusListener = new DynamicControllerListenerAdapter() {
				public void valueChanged(IDynamicController controller, final IComponentData newValue) {
					int newCount = Integer.valueOf(newValue.getStringData());
					if(newCount != saveCount) {
						saveCount = newCount;
						try{
							File checkFile = new File(filenameNode.getValue().getStringData());
							String dataPath = System.getProperty("sics.data.path");
							checkFile = new File(dataPath + "/" + checkFile.getName());
							final String filePath = checkFile.getAbsolutePath();
							if (!checkFile.exists()){
								String errorMessage = "The target file :" + checkFile.getAbsolutePath() + 
								" can not be found";
//								throw new FileNotFoundException(errorMessage);
								LoggerFactory.getLogger(this.getClass()).error(errorMessage);
								return;
							}
							Display.getDefault().asyncExec(new Runnable() {

								public void run() {
									boolean isFileSelected = DataSourceManager.getSelectedFile() 
										== DataSourceManager.getInstance().getDataSourceFile(filePath);
									AlgorithmTask task = ProjectManager.getCurrentAlgorithmTask();
									final Operation operation = task.getOperationManager(0).getOperation(AnalysisControlView.NAVIGATION_PROCESSOR_NAME);
									final Tuner currentStepIndexTuner = ((ProcessorAgent) operation.getAgent()).getTuner(AnalysisControlView.CURRENT_INDEX_TUNER_NAME);
									try {
										currentStepIndexTuner.setSignal(currentPointNode.getValue().getIntData() + 1);
									} catch (Exception e) {
										e.printStackTrace();
									} 
									dataSourceComposite.removeFile(filePath);
									dataSourceComposite.addFile(filePath, isFileSelected, 0);
									dataSourceComposite.refresh();
								}
							});
						}catch (Exception e) {
							LoggerFactory.getLogger(this.getClass()).error("can not read new file", e);
						}
					}
				}
			};
//			saveCountNode.addComponentListener(statusListener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (saveCountNode != null && statusListener != null)
			saveCountNode.removeComponentListener(statusListener);
	}

}
