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
package au.gov.ansto.bragg.echidna.ui.views;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.controllers.ComponentDataFormatException;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.ui.instrument.SingleEntryDataSourceView;

/**
 * @author nxi
 * Created on 29/07/2009
 */
public class EchidnaDataSourceView extends SingleEntryDataSourceView {

	protected final static int AUTOLOAD_FILE_NUMBER_LIMIT = 300;

	private final static String FILENAME_NODE_PATH = "/experiment/file_name";
	private final static String SAVE_COUNT_PATH = "/experiment/save_count";
//	private final static String CURRENT_POINT_PATH = "/experiment/currpoint";
	private int saveCount = 0;
	private String defaultFolderName;
	private IDynamicController saveCountNode;
	private IDynamicControllerListener statusListener;
	private boolean loadingFinished = false;
	/**
	 * 
	 */
	public EchidnaDataSourceView() {
		super();
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		dataSourceComposite.setSortedColumn(0);
		dataSourceComposite.setSortedColumn(0);
		try {
			initListeners();
		} catch (Exception e) {
			LoggerFactory.getLogger(this.getClass()).error("can not read folder " + defaultFolderName, e);
		}
		defaultFolderName = System.getProperty("sics.data.path");
		if (defaultFolderName != null){
			final File defaultFolder = new File(defaultFolderName);
			if (defaultFolder.exists()){
				
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						dataSourceComposite.addDirectory(
								defaultFolder.getAbsolutePath(), AUTOLOAD_FILE_NUMBER_LIMIT);
						setLoadingFinished(true);
					}
				});
				thread.start();
				
//				SafeRunner.run(new ISafeRunnable() {
//					
//					@Override
//					public void run() throws Exception {
//						// TODO Auto-generated method stub
//						Thread.sleep(4000);
//						dataSourceComposite.addDirectory(
//								defaultFolder.getAbsolutePath(), AUTOLOAD_FILE_NUMBER_LIMIT);
//					}
//					
//					@Override
//					public void handleException(Throwable e) {
//						// TODO Auto-generated method stub
//						Util.handleException(getSite().getShell(), new Exception(e));
//					}
//				});
				
//				try{
//					BusyIndicator.showWhile(getSite().getShell().getDisplay(), 
//							new Runnable() {
//								
//								@Override
//								public void run() {
//									dataSourceComposite.addDirectory(
//											defaultFolder.getAbsolutePath(), AUTOLOAD_FILE_NUMBER_LIMIT);
//								}
//							});
//				} catch (Exception e) {
//					Util.handleException(getSite().getShell(), e);
//				} 
				
//				DisplayManager.getDefault().asyncExec(new Runnable() {
////				SafeRunner.run(new ISafeRunnable() {
//					
//					@Override
//					public void run()  {
//						try{
//							BusyIndicator.showWhile(getSite().getShell().getDisplay(), 
//									new Runnable() {
//										
//										@Override
//										public void run() {
//											dataSourceComposite.addDirectory(
//													defaultFolder.getAbsolutePath(), AUTOLOAD_FILE_NUMBER_LIMIT);
//										}
//									});
//						} catch (Exception e) {
//							handleException(e);
//						} 
//					}
//					
//					public void handleException(Throwable e) {
//						Util.handleException(getSite().getShell(), new FileAccessException(
//								"failed to load folder " + defaultFolderName, e));
//					}
//				});
			}
		}
	}

	protected void initListeners() throws ComponentDataFormatException, SicsIOException {
		final ISicsController sics = SicsCore.getSicsController();
		if (sics != null){
			final IDynamicController filenameNode = (IDynamicController) sics.findComponentController(
					FILENAME_NODE_PATH);
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
								throw new FileNotFoundException(errorMessage);
							}
							Display.getDefault().asyncExec(new Runnable() {

								public void run() {
									boolean isFileSelected = DataSourceManager.getSelectedFile() 
										== DataSourceManager.getInstance().getDataSourceFile(filePath);
									dataSourceComposite.removeFile(filePath);
									dataSourceComposite.addFile(filePath, isFileSelected, 0);
									dataSourceComposite.refresh();
								}
							});
						}catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				}
			};
			saveCountNode.addComponentListener(statusListener);
		}
//		statusListeners.add(statusListener);
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

	@Override
	public void showBusy(boolean busy) {
		super.showBusy(busy);
		if (busy){
			getSite().getShell().setCursor(getSite().getShell().getDisplay(
					).getSystemCursor(SWT.CURSOR_WAIT));
		} else {
			getSite().getShell().setCursor(null);
		}
		
	}

	/**
	 * @param loadingFinished the loadingFinished to set
	 */
	public void setLoadingFinished(boolean loadingFinished) {
		this.loadingFinished = loadingFinished;
	}

	/**
	 * @return the loadingFinished
	 */
	public boolean isLoadingFinished() {
		return loadingFinished;
	}
	
	
}
