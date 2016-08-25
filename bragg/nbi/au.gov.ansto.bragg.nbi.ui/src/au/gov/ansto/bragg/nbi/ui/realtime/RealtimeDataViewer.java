/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package au.gov.ansto.bragg.nbi.ui.realtime;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.data.ui.viewers.PlotViewer;
import org.gumtree.gumnix.sics.control.ServerStatus;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.ui.util.DynamicControllerNode;
import org.gumtree.vis.dataset.XYTimeSeriesSet;
import org.gumtree.vis.interfaces.ITimePlot;

import au.gov.ansto.bragg.nbi.ui.internal.Activator;

/**
 * @author nxi
 *
 */
public class RealtimeDataViewer extends Composite {

	private static final int DEFAULT_UPDATE_PERIOD = 2000;
	private int updatePeriod = DEFAULT_UPDATE_PERIOD;
	private IRealtimeResourceProvider resourceProvider;
	private FormToolkit formToolkit;
	private Combo resourceCombo;
	private Combo contentCombo;
	private Button addButton;
	private Button removeButton;
	private Text timeText;
	private ITimePlot timePlot;
	private Thread updateThread;
	private boolean isDisposed;
	private PlotViewer plotViewer;
	
	/**
	 * @param parent
	 * @param style
	 */
	public RealtimeDataViewer(Composite parent, int style) {
		super(parent, style);
		isDisposed = false;
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(this);
		
		Composite controlComposite = getFormToolkit().createComposite(this);
		GridLayoutFactory.fillDefaults().margins(0, 4).numColumns(3).applyTo(controlComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(controlComposite);
		
		Composite addResourceComposite = getFormToolkit().createComposite(controlComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(addResourceComposite);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).applyTo(addResourceComposite);
		Composite removeResourceComposite = getFormToolkit().createComposite(controlComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(removeResourceComposite);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).applyTo(removeResourceComposite);
		Composite timeComposite = getFormToolkit().createComposite(controlComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(timeComposite);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).applyTo(timeComposite);
		
//		Label resourceLabel = getFormToolkit().createLabel(addResourceComposite, "Add");
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(resourceLabel);
//		Label contentLabel = getFormToolkit().createLabel(removeResourceComposite, "Remove");
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(contentLabel);
		
		resourceCombo = new Combo(addResourceComposite, SWT.DROP_DOWN);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(resourceCombo);
		addButton = getFormToolkit().createButton(addResourceComposite, "", SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(addButton);
		addButton.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/add_resource.gif").createImage());
		
		contentCombo = new Combo(removeResourceComposite, SWT.DROP_DOWN);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(contentCombo);
		removeButton = getFormToolkit().createButton(removeResourceComposite, "", SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(removeButton);
		removeButton.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/remove_resource.gif").createImage());
		
		updateResourceCombo();
		
		timeText = getFormToolkit().createText(timeComposite, "2");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(16, SWT.DEFAULT).indent(0, 2).applyTo(timeText);
		Label timeUnits = getFormToolkit().createLabel(timeComposite, "s");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).indent(0, 2).applyTo(timeUnits);
		timeText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String text = timeText.getText();
				try {
					double value = Double.valueOf(text);
					int time = (int) (value * 1000);
					if (time < 500){
						time = 500;
					}
					setUpdatePeriod(time);
				} catch (Exception e2) {
				}
			}
		});
//		Composite plotComposite = getFormToolkit().createComposite(parent);
//		GridLayoutFactory.fillDefaults().applyTo(plotComposite);
//		plotComposite = new PlotComposite(this, SWT.NONE);
		plotViewer = new PlotViewer(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(plotViewer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plotViewer);
		DropTarget dt = new DropTarget(plotViewer, DND.DROP_MOVE);
	    dt.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
	    dt.addDropListener(new DropTargetAdapter() {
	    	public void drop(DropTargetEvent event) {
	    		if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
//	    			LocalSelectionTransfer.getTransfer().setSelection(treeViewer.getSelection());
	    			ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
	    			if (selection != null && selection instanceof IStructuredSelection) {
	    				IStructuredSelection structures = (IStructuredSelection) selection;
	    				for(Object struct : structures.toList()) {
	    					if(struct instanceof DynamicControllerNode) {
	    						IComponentController controller = ((DynamicControllerNode) struct).getController();
	    						try {
		    						String deviceId = controller.getId();
		    						if (deviceId != null) {
		    							IRealtimeResource resource = resourceProvider.getResource(deviceId);
		    							if (resource == null) {
		    								resource = resourceProvider.getResource(controller.getPath());
		    							}
		    							if (resource != null && resource instanceof SicsRealtimeResource) {
		    								timePlot.addTimeSeriesSet(resource.getTimeSeriesSet());
		    								resourceProvider.addResourceToUpdateList(resource);
		    								timePlot.updatePlot();
		    								contentCombo.add(deviceId);
		    								contentCombo.setData(deviceId, resource);
		    								contentCombo.update();
		    								contentCombo.getParent().getParent().layout(true, true);
		    							}
		    						}
								} catch (Exception e) {
									e.printStackTrace();
								}
	    					}
	    				}
	    			}
	    		}
	    	}
	    });
	    plotViewer.setDataset(new XYTimeSeriesSet());
		timePlot = (ITimePlot) plotViewer.getPlot();
		timePlot.setAutoUpdate(false);
		addListeners();
	}
	
	private void addListeners() {
		addButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						IRealtimeResource resource = null;
						try{
							String name = resourceCombo.getItem(resourceCombo.getSelectionIndex());
							resource = (IRealtimeResource) resourceCombo.getData(name);
						}catch (Exception e) {
						}
						if (resource != null) {
							addRealtimeResource(resource);
						}
					}
				});
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		removeButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						IRealtimeResource resource = null;
						try{
							String name = contentCombo.getItem(contentCombo.getSelectionIndex());
							resource = (IRealtimeResource) contentCombo.getData(name);
						}catch (Exception e) {
						}
						if (resource != null) {
							removeRealtimeResource(resource);
						}
					}
				});
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
	}

	private void addRealtimeResource(IRealtimeResource resource) {
		String resourceName = resource.getName();
		String[] existingItems = contentCombo.getItems();
		for (String item : existingItems) {
			if (item.equals(resourceName)) {
				return;
			}
		}
		timePlot.addTimeSeriesSet(resource.getTimeSeriesSet());
		resourceProvider.addResourceToUpdateList(resource);
		timePlot.updatePlot();
		contentCombo.add(resourceName);
		contentCombo.setData(resourceName, resource);
//		contentCombo.select(contentCombo.getItemCount() - 1);
	}

	private void removeRealtimeResource(IRealtimeResource resource) {
		timePlot.removeTimeSeriesSet(resource.getTimeSeriesSet());
		resourceProvider.removeResourceFromUpdateList(resource);
		timePlot.updatePlot();
		contentCombo.remove(resource.getName());
		contentCombo.setText("");
	}

	public void updateResourceCombo() {
		if (resourceCombo != null && !resourceCombo.isDisposed()) {
			resourceCombo.removeAll();
			if (resourceProvider == null) {
				return;
			}
			for (IRealtimeResource resource : resourceProvider.getResourceList()) {
				String resourceName = resource.getName();
				resourceCombo.add(resourceName);
				resourceCombo.setData(resourceName, resource);
			}
			resourceCombo.update();
			resourceCombo.getParent().getParent().layout(true, true);
		}
//		contentCombo.redraw();
	}

	/**
	 * @param resourceProvider the resourceProvider to set
	 */
	public void setResourceProvider(IRealtimeResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
//		Thread thread = new Thread(new Runnable(){
//			
//			@Override
//			public void run() {
//				LoopRunner.run(new ILoopExitCondition() {
//					
//					@Override
//					public boolean getExitCondition() {
//						if (SicsCore.getDefaultProxy() != null && SicsCore.getDefaultProxy().isConnected() && SicsCore.getSicsController() != null){
//							try {
//								Thread.sleep(1000);
//							} catch (InterruptedException e) {
//							}
//							Display.getDefault().asyncExec(new Runnable() {
//								
//								@Override
//								public void run() {
//									updateResourceCombo();
//								}
//							});
//							startUpdateThread();
//							return true;
//						}
//						return isDisposed;
//					}
//				}, -1, 1000);
//			}
//			
//		});
//		thread.start();
	}
	
	public void startUpdateThread() {
		if (updateThread == null) {
			updateThread = new Thread(new Runnable() {

				@Override
				public void run() {

					while (!isDisposed) {
						if (resourceProvider != null) {
							try {
								if (SicsCore.getDefaultProxy() != null && SicsCore.getDefaultProxy().isConnected() 
										&& SicsCore.getSicsController() != null 
										&& SicsCore.getSicsController().getServerStatus() != ServerStatus.UNKNOWN){
									resourceProvider.updateResource();
									timePlot.updatePlot();
//								((ChartPanel) timePlot).chartChanged(null);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						try {
							Thread.sleep(updatePeriod);
						} catch (InterruptedException e) {
							break;
						}
					}
				}
			});
			updateThread.start();
		}
	}

	/**
	 * @return the resourceProvider
	 */
	public IRealtimeResourceProvider getResourceProvider() {
		return resourceProvider;
	}

	protected FormToolkit getFormToolkit() {
		if (formToolkit == null) {
			formToolkit = new FormToolkit(Display.getCurrent());
		}
		return formToolkit;
	}
	
	@Override
	public void dispose() {
		if (updateThread != null) {
			try{
				if (updateThread.isAlive()){
					updateThread.interrupt();
				}
			}catch (Exception ex) {
			}
			updateThread = null;
		}
		if (resourceProvider != null) {
			resourceProvider.clear();
			resourceProvider = null;
		}
		plotViewer.dispose();
		timePlot = null;
		resourceCombo = null;
		contentCombo = null;
		addButton = null;
		removeButton = null;
		formToolkit.dispose();
		isDisposed = true;
		super.dispose();
	}

	/**
	 * @param updatePeriod the updatePeriod to set
	 */
	public void setUpdatePeriod(int updatePeriod) {
		this.updatePeriod = updatePeriod;
	}

	@Override
	public boolean isDisposed() {
		return super.isDisposed() || isDisposed;
	}
}
