/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.SWTX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.koala.ui.parts.RecurrentScheduler.IRecurrentTask;
import au.gov.ansto.bragg.koala.ui.scan.AbstractScanModel;
import au.gov.ansto.bragg.koala.ui.scan.AbstractScanModel.ModelStatus;
import au.gov.ansto.bragg.koala.ui.scan.ExperimentModelAdapter;
import au.gov.ansto.bragg.koala.ui.scan.IExperimentModelListener;
import au.gov.ansto.bragg.koala.ui.scan.IModelListener;
import au.gov.ansto.bragg.koala.ui.scan.SingleScan;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

/**
 * @author nxi
 *
 */
public abstract class AbstractExpPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 2200;
	private static final int HEIGHT_HINT = 1080;
	private static final int WIDTH_TABLE = 1580;
	private static final int HEIGHT_TABLE = 1000;
	private static final int WIDTH_INFO = 480;
	private static final int HEIGHT_INFO = 1080;
	
	private static final int WIDTH_HINT_SMALL = 1560; 
	private static final int HEIGHT_HINT_SMALL = 720;
	private static final int WIDTH_TABLE_SMALL = 1080;
	private static final int HEIGHT_TABLE_SMALL = 620;
	private static final int WIDTH_INFO_SMALL = 480;
	private static final int HEIGHT_INFO_SMALL = 800;
	private static final Logger logger = LoggerFactory.getLogger(AbstractExpPanel.class);
	
	protected MainPart mainPart;
	protected KTable table;
	private int panelWidth = WIDTH_HINT;
	private int panelHeight = HEIGHT_HINT;
	private int tableWidth = WIDTH_TABLE;
	private int tableHeight = HEIGHT_TABLE;
	private int infoWidth = WIDTH_INFO;
	private int infoHeight = HEIGHT_INFO;
//	private Text phiText;
//	private Text tempText;
//	private Text fileText;
//	private Text numText;
	private Text timeTotalText;
	private Text timeLeftText;
	private Text startText;
	private Text finText;
//	private Text estText;
//	private Text comText;
	private ControlHelper controlHelper;
	private IExperimentModelListener experimentListener;
	private AbstractScanModel scanModel;
	
	/**
	 * @param parent
	 * @param style
	 */
	public AbstractExpPanel(final Composite parent, int style, MainPart part) {
		super(parent, style);
		controlHelper = ControlHelper.getInstance();
		if (Activator.getMonitorWidth() < 2500) {
			panelWidth = WIDTH_HINT_SMALL;
			panelHeight = HEIGHT_HINT_SMALL;
			tableWidth = WIDTH_TABLE_SMALL;
			tableHeight = HEIGHT_TABLE_SMALL;
			infoWidth = WIDTH_INFO_SMALL;
			infoHeight = HEIGHT_INFO_SMALL;
		}
		mainPart = part;
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(2).applyTo(this);
		GridDataFactory.swtDefaults().minSize(panelWidth, panelHeight)
			.align(SWT.CENTER, SWT.FILL).applyTo(this);
		
		final Composite scanBlock = new Composite(this, SWT.BORDER);
		GridLayoutFactory.fillDefaults().margins(4, 4).applyTo(scanBlock);
		GridDataFactory.fillDefaults().minSize(tableWidth, tableHeight).span(2, 1).grab(true, true)
			.align(SWT.FILL, SWT.BEGINNING).applyTo(scanBlock);
		
	    table = new KTable(scanBlock, SWT.NONE 
	    							| SWT.MULTI 
	    							| SWT.FULL_SELECTION 
	    							| SWT.V_SCROLL 
	    							| SWT.H_SCROLL
	    							| SWTX.FILL_WITH_LASTCOL
	    							| SWT.FULL_SELECTION
	    							);
	    scanModel = getModel();
	    scanModel.setFont(Activator.getMiddleFont());
	    table.setFont(Activator.getMiddleFont());
	    table.setCursor(Activator.getHandCursor());
	    table.setModel(scanModel);
	    table.setPreferredSizeDefaultRowHeight(44);
	    scanModel.setTable(table);
	    
//	    KTableCellSelectionAdapter listener = new KTableCellSelectionAdapter() {
//	    	@Override
//	    	public void cellSelected(int col, int row, int statemask) {
//	    		if (row > 0) {
//	    			if (col == 0) {
//	    				model.insertScan(row);
//	    				table.redraw();
//	    			} else if (col == 1) {
//	    				model.deleteScan(row - 1);
//	    				table.redraw();
//	    			}
//	    		}
//	    	}
//	    	
//	    };
//	    table.addCellSelectionListener(listener);
	    GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL)
	    	.applyTo(table);
		
		
	    final Group runGroup = new Group(this, SWT.NONE);
	    runGroup.setText("Time Estimation");
	    GridLayoutFactory.fillDefaults().margins(2, 0).numColumns(5).applyTo(runGroup);
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(runGroup);
	    
		final Label timeTotalLabel = new Label(runGroup, SWT.NONE);
		timeTotalLabel.setText("Total time");
		timeTotalLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER)
			.minSize(240, 40).applyTo(timeTotalLabel);
		
		timeTotalText = new Text(runGroup, SWT.BORDER);
		timeTotalText.setFont(Activator.getMiddleFont());
		timeTotalText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER)
			.minSize(180, 40).applyTo(timeTotalText);
		
		final Label finLabel = new Label(runGroup, SWT.NONE);
		finLabel.setText("To finish at");
		finLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER)
			.minSize(240, 40).applyTo(finLabel);
		
		finText = new Text(runGroup, SWT.BORDER);
		finText.setFont(Activator.getMiddleFont());
		finText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER)
			.minSize(180, 40).applyTo(finText);

	    final Button runButton = new Button(runGroup, SWT.PUSH);
		runButton.setImage(KoalaImage.PLAY48.getImage());
		runButton.setText("Run");
		runButton.setFont(Activator.getMiddleFont());
		runButton.setCursor(Activator.getHandCursor());
//		runButton.setSize(240, 64);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.END, SWT.CENTER)
					.span(1, 2).hint(260, 64).applyTo(runButton);
		runButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Run-experiment button clicked");
				if (scanModel.isRunning()) {
					mainPart.popupError("The system is busy with the current experiment.");
					return;
				}
				if (scanModel.needToRun()) {
					scanModel.start();
				} else {
					mainPart.popupError("There is no collection defined. Please review the scan table.");
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		final Label startLabel = new Label(runGroup, SWT.NONE);
		startLabel.setText("Started at");
		startLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER)
			.minSize(240, 40).applyTo(startLabel);
		
		startText = new Text(runGroup, SWT.BORDER);
		startText.setText("--");
		startText.setFont(Activator.getMiddleFont());
		startText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER)
			.minSize(180, 40).applyTo(startText);
	    
		final Label timeLeftLabel = new Label(runGroup, SWT.NONE);
		timeLeftLabel.setText("Time left");
		timeLeftLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER)
			.minSize(240, 40).applyTo(timeLeftLabel);
		
		timeLeftText = new Text(runGroup, SWT.BORDER);
		timeLeftText.setFont(Activator.getMiddleFont());
		timeLeftText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER)
			.minSize(180, 40).applyTo(timeLeftText);
		
		final Composite statusComposite = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(statusComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusComposite);
		new QueueScanStatusPart(statusComposite, mainPart);
		
//		new StatusControl();
		IModelListener modelListener = new IModelListener() {
			
			@Override
			public void progressUpdated(final ModelStatus status) {
				updateRuningProgress(status);
			}
			
			@Override
			public void modelChanged() {
				updateTimeEstimation();
			}
		};
		scanModel.addModelListener(modelListener);
		updateTimeEstimation();
		
		mainPart.getRecurrentScheduler().addTask(new IRecurrentTask() {
			
			@Override
			public void run() {
				final IRecurrentTask task = this;
				
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if (!parent.isDisposed()) {
							updateTimeLeft();
						} else {
							mainPart.getRecurrentScheduler().removeTask(task);
						}
					}
				});
				
			}
		});
		
		mainPart.getRecurrentScheduler().addTask(new IRecurrentTask() {
			
			@Override
			public void run() {
				final IRecurrentTask task = this;
				
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if (!parent.isDisposed()) {
							try {
								updateFinText();
							} catch (Exception e) {
							}
						} else {
							mainPart.getRecurrentScheduler().removeTask(task);
						}
					}
				});
			}
		});
//		experimentListener = new ExperimentModelAdapter() {
//			
//			@Override
//			public void updateLastFilename(final String filename) {
//				Display.getDefault().asyncExec(new Runnable() {
//					
//					@Override
//					public void run() {
//						fileText.setText(filename);
//					}
//				});
//			}
//			
//		};
//		ControlHelper.experimentModel.addExperimentModelListener(experimentListener);
	}

	private void updateFinText() {
		if (!scanModel.isRunning()) {
			finText.setText(scanModel.getFinishTime());
		}
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#next()
	 */
	@Override
	public void next() {
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#back()
	 */
	@Override
	public void back() {
		mainPart.showInitScanPanel();
	}

	protected abstract AbstractScanModel getModel();
	
	@Override
	public void show() {
		AbstractScanModel model = getModel();
//		if (model.getSize() == 0) {
//			model.insertScan(0);
//			table.redraw();
//		}
		mainPart.showPanel(this, panelWidth, panelHeight);
		mainPart.enableBackButton();
		mainPart.disableNextButton();
		mainPart.setTitle("Full Experiment");
		mainPart.setCurrentPanelName(PanelName.EXPERIMENT);
	}

	private void updateTimeEstimation() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				timeTotalText.setText(getModel().getTotalTimeText());
				timeLeftText.setText(getModel().getTimeLeftText());
				finText.setText(getModel().getFinishTime());
				ControlHelper.publishFinishTime(getModel().getFinishInSeconds());
				
//				int[] rows = table.getRowSelection();
//				int time = 0;
//				for (int i = 0; i < rows.length; i++) {
//					SingleScan scan = getModel().getItem(rows[i]);
//					time += scan.getTotalTime();
//				}
//				String timeString = PanelUtils.convertTimeString(time);
//				if (time > 0) {
//					timeString += String.format(" (%ds)", time);
//				} else {
//					timeString = "0s";
//				}
//				if (estText != null) {
//					estText.setText(timeString);
//				}
			}
		});
	}
	
	private void updateRuningProgress(final ModelStatus status) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (ModelStatus.STARTED.equals(status)) {
//					String start = getModel().getStartedTime();
					startText.setText(getModel().getStartedTime());
					finText.setText(getModel().getFinishTime());
					ControlHelper.publishFinishTime(getModel().getFinishInSeconds());
				} else if (ModelStatus.FINISHED.equals(status)) {
					startText.setText("--");
					finText.setText("--");
					ControlHelper.publishFinishTime(0L);
				}
			}
		});
	}
	
	private void updateTimeLeft() {
		timeLeftText.setText(getModel().getTimeLeftText());
	}
	
//	class StatusControl {
//		
//		ISicsController phiController;
//		ISicsController tempController;
//		ISicsController stepController;
////		ISicsController fnController;
//		
//		boolean initialised = false;
//		
//		public StatusControl() {
//			
//			if (controlHelper.isConnected()) {
//				initControllers();
//			}
//			
//			ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
//				
//				@Override
//				public void modelUpdated() {
//					initControllers();
//				}
//				
//				@Override
//				public void disconnect() {
//					Display.getDefault().asyncExec(new Runnable() {
//
//						@Override
//						public void run() {
//							try {
//								if (phiText != null) {
//									phiText.setText("");
//								}
//								if (tempText != null) {
//									tempText.setText("");
//								}
//								if (numText != null) {
//									numText.setText("");
//								}
//							} catch (Exception e) {
//							}
//						}
//					});
//				}
//				
//			};
//			controlHelper.addProxyListener(proxyListener);
//		}
//		
//		public void initControllers() {
//
//			phiController = SicsManager.getSicsModel().findController(
//					System.getProperty(ControlHelper.SAMPLE_PHI));
//			tempController = SicsManager.getSicsModel().findController(
//					System.getProperty(ControlHelper.ENV_VALUE));
//			stepController = SicsManager.getSicsModel().findController(
//					System.getProperty(ControlHelper.STEP_TEXT_PATH));
////			fnController = SicsManager.getSicsModel().findController(
////					System.getProperty(ControlHelper.FILENAME_PATH));
//			
//			if (phiController != null) {
//				phiController.addControllerListener(
//						new ControllerListener(phiText));
//			}
//			if (tempController != null) {
//				tempController.addControllerListener(
//						new ControllerListener(tempText));
//			}
//			if (stepController != null) {
//				stepController.addControllerListener(
//						new ControllerListener(numText));
//			}
////			if (fnController != null) {
////				fnController.addControllerListener(
////						new ControllerListener(fileText));
////			}
//			Display.getDefault().asyncExec(new Runnable() {
//
//				@Override
//				public void run() {
//					try {
//						if (phiController != null) {
//							try {
//								phiText.setText(String.format("%.2f",
//										((DynamicController) phiController).getControllerDataValue().getFloatData()));
//							} catch (Exception e) {
//							}
//						}
//						if (tempController != null) {
//							try {
//								tempText.setText(String.format("%.2f",
//										((DynamicController) tempController).getControllerDataValue().getFloatData()));
//							} catch (Exception e) {
//							}
//						}
//						if (stepController != null) {
//							try {
//								numText.setText(String.valueOf(
//										((DynamicController) stepController).getValue()));
//							} catch (Exception e) {
//							}
//						}
////						if (fnController != null) {
////							fileText.setText(String.valueOf(
////									((DynamicController) fnController).getValue()));
////						}
//					} catch (Exception e) {
//					}
//				}
//			});
//			initialised = true;
//		}
//	}
	
//	class ControllerListener implements ISicsControllerListener {
//		
//		private Text widget;
////		private DynamicController controller;
//		
//		public ControllerListener(Text widget) {
//			this.widget = widget;
////			this.controller = controller;
//		}
//		
//		@Override
//		public void updateState(final ControllerState oldState, final ControllerState newState) {
//			Display.getDefault().asyncExec(new Runnable() {
//
//				@Override
//				public void run() {
//					if (newState == ControllerState.BUSY) {
//						widget.setForeground(Activator.getBusyColor());
//					} else {
//						widget.setForeground(Activator.getIdleColor());
//					}
//				}
//			});
//		}
//
//		@Override
//		public void updateValue(final Object oldValue, final Object newValue) {
//			Display.getDefault().asyncExec(new Runnable() {
//				
//				@Override
//				public void run() {
////					widget.setText(String.valueOf(newValue));
//					String res;
//					if (newValue instanceof Float) {
//						res = String.format("%.2f", newValue);
//					} else {
//						res = String.valueOf(newValue);
//					}
//					widget.setText(res);
//				}
//			});
//		}
//
//		@Override
//		public void updateEnabled(boolean isEnabled) {
//		}
//
//		@Override
//		public void updateTarget(Object oldValue, Object newValue) {
//		}
//		
//	}

}
