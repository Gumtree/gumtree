/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.backup;

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
import au.gov.ansto.bragg.koala.ui.parts.AbstractControlPanel;
import au.gov.ansto.bragg.koala.ui.parts.MainPart;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.koala.ui.parts.PanelUtils;
import au.gov.ansto.bragg.koala.ui.parts.RecurrentScheduler.IRecurrentTask;
import au.gov.ansto.bragg.koala.ui.parts.ScanStatusPart;
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
public abstract class AbstractExpPanelBackup extends AbstractControlPanel {

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
	private static final Logger logger = LoggerFactory.getLogger(AbstractExpPanelBackup.class);
	
	protected MainPart mainPart;
	protected KTable table;
	private int panelWidth = WIDTH_HINT;
	private int panelHeight = HEIGHT_HINT;
	private int tableWidth = WIDTH_TABLE;
	private int tableHeight = HEIGHT_TABLE;
	private int infoWidth = WIDTH_INFO;
	private int infoHeight = HEIGHT_INFO;
	private Text phiText;
	private Text tempText;
	private Text fileText;
	private Text numText;
	private Text timeTotalText;
	private Text timeLeftText;
	private Text startText;
	private Text finText;
	private Text estText;
	private Text comText;
	private ControlHelper controlHelper;
	private IExperimentModelListener experimentListener;
	
	/**
	 * @param parent
	 * @param style
	 */
	public AbstractExpPanelBackup(final Composite parent, int style, MainPart part) {
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
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(1).applyTo(scanBlock);
		GridDataFactory.fillDefaults().minSize(tableWidth, tableHeight).grab(true, true)
			.align(SWT.FILL, SWT.BEGINNING).applyTo(scanBlock);
		
	    table = new KTable(scanBlock, SWT.NONE 
	    							| SWT.MULTI 
	    							| SWT.FULL_SELECTION 
	    							| SWT.V_SCROLL 
	    							| SWT.H_SCROLL
	    							| SWTX.FILL_WITH_LASTCOL
	    							| SWT.FULL_SELECTION
	    							);
	    final AbstractScanModel model = getModel();
	    model.setFont(Activator.getMiddleFont());
	    table.setFont(Activator.getMiddleFont());
	    table.setCursor(Activator.getHandCursor());
	    table.setModel(model);
	    table.setPreferredSizeDefaultRowHeight(44);
	    model.setTable(table);
	    
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
		
	    Composite infoPart = new Composite(this, SWT.NONE);
	    GridLayoutFactory.fillDefaults().applyTo(infoPart);
	    GridDataFactory.fillDefaults().span(1, 2).grab(false, true).align(SWT.FILL, SWT.BEGINNING)
	    	.minSize(infoWidth, infoHeight).applyTo(infoPart);
	    
		final Group statusPart = new Group(infoPart, SWT.NONE);
		statusPart.setText("Current scan status");
	    GridLayoutFactory.fillDefaults().numColumns(3).margins(4, 4).applyTo(statusPart);
	    GridDataFactory.fillDefaults().grab(false, false).applyTo(statusPart);
	    	    
		final Label fileLabel = new Label(statusPart, SWT.NONE);
		fileLabel.setText("Last file");
		fileLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(180, 40).applyTo(fileLabel);
		
		fileText = new Text(statusPart, SWT.BORDER);
		fileText.setFont(Activator.getMiddleFont());
		fileText.setEditable(false);
//		fileText.setText("W:\\data\\koala\\d_14.tif");
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).hint(296, SWT.DEFAULT).applyTo(fileText);
		
	    final Button openButton = new Button(statusPart, SWT.PUSH);
	    openButton.setImage(KoalaImage.IMAGE32.getImage());
//	    openButton.setFont(Activator.getMiddleFont());
	    openButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER)
			.hint(40, 40).applyTo(openButton);

		openButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("open-last-file button clicked");
				final String fn = fileText.getText();
				File f = new File(fn);
				if (f.exists()) {
					try {
						Desktop.getDesktop().open(f);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else {
					MessageDialog.openWarning(getShell(), "Warning", "File not found: " + fn);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		final Label phiLabel = new Label(statusPart, SWT.NONE);
		phiLabel.setText(Activator.PHI + " value");
		phiLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(phiLabel);
		
		phiText = new Text(statusPart, SWT.BORDER);
		phiText.setFont(Activator.getMiddleFont());
		phiText.setEditable(false);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).minSize(180, 40).applyTo(phiText);
		
		final Label tempLabel = new Label(statusPart, SWT.NONE);
		tempLabel.setText("Temperature (K)");
		tempLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(tempLabel);
		
		tempText = new Text(statusPart, SWT.BORDER);
		tempText.setEditable(false);
		tempText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).minSize(180, 40).applyTo(tempText);

		final Label numLabel = new Label(statusPart, SWT.NONE);
		numLabel.setText("Step");
		numLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(numLabel);
		
		numText = new Text(statusPart, SWT.BORDER);
		numText.setFont(Activator.getMiddleFont());
		numText.setEditable(false);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).minSize(180, 40).applyTo(numText);
		
		new ScanStatusPart(infoPart, mainPart);
		
		final ScrolledComposite batchHolder = new ScrolledComposite(infoPart, SWT.NONE);
	    GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(batchHolder);
	    GridDataFactory.fillDefaults().grab(false, false).applyTo(batchHolder);
	    batchHolder.setExpandHorizontal(true);
	    batchHolder.setExpandVertical(true);

	    final Composite emptyPart = new Composite(batchHolder, SWT.NONE);
	    
	    final Group batchGroup = new Group(batchHolder, SWT.NONE);
	    batchGroup.setText("Batch update selected rows");
	    batchGroup.setBackground(Activator.getLightColor());
	    GridLayoutFactory.fillDefaults().numColumns(3).margins(4, 4).applyTo(batchGroup);
	    GridDataFactory.fillDefaults().grab(false, false).applyTo(batchGroup);

		final Label comLabel = new Label(batchGroup, SWT.NONE);
		comLabel.setText("Comments");
		comLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(comLabel);
		
		if (Activator.getMonitorWidth() < 2500) {
			comText = new Text(batchGroup, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(400, SWT.DEFAULT).applyTo(comText);
		} else {
			comText = new Text(batchGroup, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);	
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(400, 88).applyTo(comText);
		}
		comText.setFont(Activator.getMiddleFont());
		
		comText.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.LF || e.keyCode == SWT.CR || e.keyCode == 16777296) {
					try {
						String com = comText.getText();
//						String fn = fnText.getText();
						AbstractScanModel model = getModel();
						int[] rows = table.getRowSelection();
						for (int row : rows) {
							SingleScan scan = model.getItem(row);
							if (com != null && com.trim().length() > 0) {
								scan.setComments(com);
							}
//							if (fn != null && fn.trim().length() > 0) {
//								scan.setFilename(fn);
//							}
						}
					} catch (Exception e2) {
						mainPart.popupError("error in appling changes: " + e2.getMessage());
					}
					table.redraw();
				} 
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			
		});
		
		final Label fnLabel = new Label(batchGroup, SWT.NONE);
		fnLabel.setText("Filename");
		fnLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(fnLabel);
		
		final Text fnText = new Text(batchGroup, SWT.BORDER);
		fnText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).minSize(180, 40).hint(400, SWT.DEFAULT).applyTo(fnText);

		fnText.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.LF || e.keyCode == SWT.CR || e.keyCode == 16777296) {
					try {
						String fn = fnText.getText();
						AbstractScanModel model = getModel();
						int[] rows = table.getRowSelection();
						for (int row : rows) {
							SingleScan scan = model.getItem(row);
							if (fn != null && fn.trim().length() > 0) {
								scan.setFilename(fn);
							}
						}
					} catch (Exception e2) {
						mainPart.popupError("error in appling changes: " + e2.getMessage());
					}
					table.redraw();
				} 
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			
		});

//		final Label indexLabel = new Label(batchGroup, SWT.NONE);
//		indexLabel.setText("Start index");
//		indexLabel.setFont(Activator.getMiddleFont());
//		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(indexLabel);
//		
//		final Text indexText = new Text(batchGroup, SWT.BORDER);
//		indexText.setFont(Activator.getMiddleFont());
//		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).minSize(180, 40).hint(400, SWT.DEFAULT).applyTo(indexText);
//
//		indexText.addKeyListener(new KeyListener() {
//			
//			@Override
//			public void keyReleased(KeyEvent e) {
//				if (e.keyCode == SWT.LF || e.keyCode == SWT.CR || e.keyCode == 16777296) {
//					try {
//						String start = indexText.getText();
//						AbstractScanModel model = getModel();
//						int[] rows = table.getRowSelection();
//						for (int row : rows) {
//							SingleScan scan = model.getItem(row);
//							if (start != null && start.trim().length() > 0) {
//								scan.setStartIndex(Integer.valueOf(start));
//							}
//						}
//					} catch (Exception e2) {
//						mainPart.popupError("error in appling changes: " + e2.getMessage());
//					}
//					table.redraw();
//				} 
//			}
//			
//			@Override
//			public void keyPressed(KeyEvent e) {
//			}
//			
//		});

		final Button applyButton = new Button(batchGroup, SWT.PUSH);
	    applyButton.setImage(KoalaImage.MULTI_APPLY48.getImage());
	    applyButton.setText("Apply to Selected Entries");
	    applyButton.setFont(Activator.getMiddleFont());
	    applyButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER).span(3, 1)
			.hint(128, 64).applyTo(applyButton);

		applyButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String com = comText.getText();
					String fn = fnText.getText();
//					int si = -1;
//					String startIndex = indexText.getText();
//					try {
//						si = Integer.valueOf(startIndex);
//					} catch (Exception e2) {
//					}
					AbstractScanModel model = getModel();
					int[] rows = table.getRowSelection();
					for (int row : rows) {
						SingleScan scan = model.getItem(row);
						if (com != null && com.trim().length() > 0) {
							scan.setComments(com);
						}
						if (fn != null && fn.trim().length() > 0) {
							scan.setFilename(fn);
						}
//						if (si >= 0) {
//							scan.setStartIndex(si);
//						}
					}
				} catch (Exception e2) {
					mainPart.popupError("error in appling changes: " + e2.getMessage());
				}
				table.redraw();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		final Label estLabel = new Label(batchGroup, SWT.NONE);
		estLabel.setText("Estimation of selected");
		estLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER)
			.span(2, 1).minSize(240, 40).applyTo(estLabel);
		
		estText = new Text(batchGroup, SWT.BORDER);
		estText.setFont(Activator.getMiddleFont());
		estText.setEditable(false);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER)
			.minSize(180, 40).applyTo(estText);
		
		final Composite ctrPart = new Composite(batchGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).applyTo(ctrPart);
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1).applyTo(ctrPart);
		
	    final Button dupButton = new Button(ctrPart, SWT.PUSH);
	    dupButton.setImage(KoalaImage.COPY48.getImage());
	    dupButton.setText("Duplicate");
	    dupButton.setFont(Activator.getMiddleFont());
	    dupButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER)
			.hint(128, 64).applyTo(dupButton);

		dupButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int[] rows = table.getRowSelection();
				List<SingleScan> items = new ArrayList<SingleScan>();
				AbstractScanModel model = getModel();
				for (int row : rows) {
					SingleScan scan = model.getItem(row);
					if (!items.contains(scan)) {
						items.add(scan);
					}
				}
				List<SingleScan> newItems = new ArrayList<SingleScan>();
				for (SingleScan scan : items) {
					newItems.add(scan.getCopy());
				}
				model.addScans((rows[rows.length - 1] / 2), newItems);
				table.redraw();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	    final Button removeButton = new Button(ctrPart, SWT.PUSH);
	    removeButton.setImage(KoalaImage.DELETE48.getImage());
	    removeButton.setText("Remove");
	    removeButton.setFont(Activator.getMiddleFont());
	    removeButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER)
			.hint(128, 64).applyTo(removeButton);

		removeButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int[] rows = table.getRowSelection();
				List<SingleScan> items = new ArrayList<SingleScan>();
				AbstractScanModel model = getModel();
				for (int row : rows) {
					SingleScan scan = model.getItem(row);
					if (!items.contains(scan)) {
						items.add(scan);
					}
				}
				for (SingleScan scan : items) {
					model.deleteScan(scan);
				}
				table.redraw();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	    batchHolder.setContent(emptyPart);
	    
	    table.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				int[] rows = table.getRowSelection();
				if (rows.length > 0) {
//					for (int row : rows) {
//						if (row % 2 == 1) {
//							int oRow = row + 1;
//							boolean inArray = false;
//							for (int v : rows) {
//								if (v == oRow) {
//									inArray = true;
//									break;
//								}
//							}
//							if (!inArray) {
//								table.addToSelectionWithoutRedraw(3, oRow);
//							}
//						} else {
//							int oRow = row - 1;
//							boolean inArray = false;
//							for (int v : rows) {
//								if (v == oRow) {
//									inArray = true;
//									break;
//								}
//							}
//							if (!inArray) {
//								table.addToSelectionWithoutRedraw(0, oRow);
//							}
//						}
//					}
//					table.redraw();
					AbstractScanModel model = getModel();
					if (rows.length == 1) {
						SingleScan scan = model.getItem(rows[0]);
						String com = scan.getComments();
						if (com != null && com.trim().length() > 0) {
							comText.setText(com);
						} else {
							comText.setText("");
						}
						String fn = scan.getFilename();
						if (fn != null && fn.trim().length() > 0) {
							fnText.setText(fn);
						} else {
							fnText.setText("");
						}
						int startIndex = scan.getStartIndex();
//						indexText.setText(String.valueOf(startIndex));
						int time = scan.getTotalTime();
						String timeString = PanelUtils.convertTimeString(time);
						if (time > 0) {
							timeString += String.format(" (%ds)", time);
						}
						estText.setText(timeString);
					} else {
						comText.setText("");
						fnText.setText("");
//						indexText.setText("");
						int time = 0;
						for (int i = 0; i < rows.length; i++) {
							SingleScan scan = model.getItem(rows[i]);
							time += scan.getTotalTime();
						}
//						estText.setText(String.valueOf(time));
						String timeString = PanelUtils.convertTimeString(time);
						if (time > 0) {
							timeString += String.format(" (%ds)", time);
						}
						estText.setText(timeString);
					}
					batchHolder.setContent(batchGroup);
					batchGroup.layout(true, true);
					batchHolder.setMinSize(batchGroup.computeSize(440, 300));
					batchHolder.getParent().layout();
					mainPart.layout();
				} else {
					batchHolder.setContent(emptyPart);
					emptyPart.layout();
					batchHolder.getParent().layout();
//					if (rows.length == 1) {
//						if (rows[0] % 2 == 1) {
//							table.addToSelection(3, rows[0] + 1);
//						} else {
//							table.addToSelection(0, rows[0] - 1);
//						}
//					}
				}
//				updateTimeEstimation();
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
	    
//	    table.addKeyListener(new KeyListener() {
//			
//			@Override
//			public void keyReleased(KeyEvent e) {
//				if (e.keyCode == SWT.LF || e.keyCode == SWT.CR || e.keyCode == 16777296) {
//					updateTimeEstimation();
//				} 
//			}
//			
//			@Override
//			public void keyPressed(KeyEvent e) {
//			}
//		});
		
//		final Composite runGroup = new Composite(this, SWT.NONE);
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
	    
	    final Button runButton = new Button(runGroup, SWT.PUSH);
		runButton.setImage(KoalaImage.PLAY48.getImage());
		runButton.setText("Run");
		runButton.setFont(Activator.getMiddleFont());
		runButton.setCursor(Activator.getHandCursor());
//		runButton.setSize(240, 64);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.END, SWT.CENTER)
					.span(1, 2).hint(240, 64).applyTo(runButton);
		runButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Run-experiment button clicked");
				if (model.isRunning()) {
					mainPart.popupError("The system is busy with the current experiment.");
					return;
				}
				if (model.needToRun()) {
					model.start();
				} else {
					mainPart.popupError("There is no collection defined. Please review the scan table.");
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
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

		new StatusControl();
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
		model.addModelListener(modelListener);
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
		
		experimentListener = new ExperimentModelAdapter() {
			
			@Override
			public void updateLastFilename(final String filename) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						fileText.setText(filename);
					}
				});
			}
			
		};
		ControlHelper.experimentModel.addExperimentModelListener(experimentListener);
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
		if (model.getSize() == 0) {
			model.insertScan(0);
			table.redraw();
		}
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
				
				int[] rows = table.getRowSelection();
				int time = 0;
				for (int i = 0; i < rows.length; i++) {
					SingleScan scan = getModel().getItem(rows[i]);
					time += scan.getTotalTime();
				}
//				estText.setText(String.valueOf(time));
//				estText.setText(PanelUtils.convertTimeString(time) + String.format(" (%ds)", time));
				String timeString = PanelUtils.convertTimeString(time);
				if (time > 0) {
					timeString += String.format(" (%ds)", time);
				}
				estText.setText(timeString);
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
	
	class StatusControl {
		
		ISicsController phiController;
		ISicsController tempController;
		ISicsController stepController;
//		ISicsController fnController;
		
		boolean initialised = false;
		
		public StatusControl() {
			
			if (controlHelper.isConnected()) {
				initControllers();
			}
			
			ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
				
				@Override
				public void modelUpdated() {
					initControllers();
				}
				
				@Override
				public void disconnect() {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							try {
								if (phiText != null) {
									phiText.setText("");
								}
								if (tempText != null) {
									tempText.setText("");
								}
								if (numText != null) {
									numText.setText("");
								}
							} catch (Exception e) {
							}
						}
					});
				}
				
			};
			controlHelper.addProxyListener(proxyListener);
		}
		
		public void initControllers() {

			phiController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.SAMPLE_PHI));
			tempController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.ENV_VALUE));
			stepController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.STEP_TEXT_PATH));
//			fnController = SicsManager.getSicsModel().findController(
//					System.getProperty(ControlHelper.FILENAME_PATH));
			
			if (phiController != null) {
				phiController.addControllerListener(
						new ControllerListener(phiText));
			}
			if (tempController != null) {
				tempController.addControllerListener(
						new ControllerListener(tempText));
			}
			if (stepController != null) {
				stepController.addControllerListener(
						new ControllerListener(numText));
			}
//			if (fnController != null) {
//				fnController.addControllerListener(
//						new ControllerListener(fileText));
//			}
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
						if (phiController != null) {
							try {
								phiText.setText(String.format("%.2f",
										((DynamicController) phiController).getControllerDataValue().getFloatData()));
							} catch (Exception e) {
							}
						}
						if (tempController != null) {
							try {
								tempText.setText(String.format("%.2f",
										((DynamicController) tempController).getControllerDataValue().getFloatData()));
							} catch (Exception e) {
							}
						}
						if (stepController != null) {
							try {
								numText.setText(String.valueOf(
										((DynamicController) stepController).getValue()));
							} catch (Exception e) {
							}
						}
//						if (fnController != null) {
//							fileText.setText(String.valueOf(
//									((DynamicController) fnController).getValue()));
//						}
					} catch (Exception e) {
					}
				}
			});
			initialised = true;
		}
	}
	
	class ControllerListener implements ISicsControllerListener {
		
		private Text widget;
//		private DynamicController controller;
		
		public ControllerListener(Text widget) {
			this.widget = widget;
//			this.controller = controller;
		}
		
		@Override
		public void updateState(final ControllerState oldState, final ControllerState newState) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (newState == ControllerState.BUSY) {
						widget.setForeground(Activator.getBusyColor());
					} else {
						widget.setForeground(Activator.getIdleColor());
					}
				}
			});
		}

		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
//					widget.setText(String.valueOf(newValue));
					String res;
					if (newValue instanceof Float) {
						res = String.format("%.2f", newValue);
					} else {
						res = String.valueOf(newValue);
					}
					widget.setText(res);
				}
			});
		}

		@Override
		public void updateEnabled(boolean isEnabled) {
		}

		@Override
		public void updateTarget(Object oldValue, Object newValue) {
		}
		
	}

}
