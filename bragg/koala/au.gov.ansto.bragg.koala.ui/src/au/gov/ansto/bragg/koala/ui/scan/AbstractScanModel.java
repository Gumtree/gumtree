package au.gov.ansto.bragg.koala.ui.scan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;
import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.EmptyCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.ImageButtonCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.ImageButtonHighlightRenderer;
import au.gov.ansto.bragg.koala.ui.parts.PanelUtils;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

public abstract class AbstractScanModel implements KTableModel {

	private static final Logger logger = LoggerFactory.getLogger(AbstractScanModel.class);
	
	private final static String PROP_BACKUPFOLDER = "gumtree.koala.backupFolder";
	private final static String XML_NAME_TITLE = "workflow";
	private final static String XML_ATTR_MODELTYPE = "model_type";
	
	
	private SingleScan initScan;
	protected List<SingleScan> scanList;
//	private Map<SingleScan, Button> dupButton;
	private TextCellRenderer oddTextRenderer;
	private TextCellRenderer evenTextRenderer;
//	private TextCellRenderer oddTextHighlightRenderer;
//	private TextCellRenderer evenTextHighlightRenderer;
	private TextCellRenderer highlightRender;
	private KTableCellRenderer emptyHeaderRenderer;
	private FixedCellRenderer columnHeaderRenderer;
	private ImageButtonCellRenderer saveButtonRenderer;
	private ImageButtonCellRenderer loadButtonRenderer;
	private ImageButtonCellRenderer addButtonRenderer;
	private ImageButtonHighlightRenderer highlightLoadButtonRenderer;
	private ImageButtonHighlightRenderer highlightSaveButtonRenderer;
	private ImageButtonHighlightRenderer highlightAddButtonRenderer;
	private ImageButtonCellRenderer oddUpButtonRenderer;
	private ImageButtonCellRenderer evenUpButtonRenderer;
	private ImageButtonCellRenderer greenUpButtonRenderer;
	private ImageButtonCellRenderer oddDownButtonRenderer;
	private ImageButtonCellRenderer evenDownButtonRenderer;
	private ImageButtonCellRenderer greenDownButtonRenderer;
	private ImageButtonHighlightRenderer highlightUpButtonRenderer;
	private ImageButtonHighlightRenderer highlightDownButtonRenderer;
	private ImageButtonCellRenderer oddDupButtonRenderer;
	private ImageButtonCellRenderer evenDupButtonRenderer;
	private ImageButtonCellRenderer greenDupButtonRenderer;
	private ImageButtonCellRenderer oddDelButtonRenderer;
	private ImageButtonCellRenderer evenDelButtonRenderer;
	private ImageButtonCellRenderer greenDelButtonRenderer;
	private ImageButtonHighlightRenderer highlightDupButtonRenderer;
	private ImageButtonHighlightRenderer highlightDelButtonRenderer;
	private int highlightRow = -1;
	private int highlightCol = -1;
	private boolean isRunning;
	private Calendar startTime;
	private List<IModelListener> modelListeners;
	private PropertyChangeListener propertyListener;
	private String backupFolder;
	
	private KTable table;
	
	public enum ModelType {
		PHYSICS,
		CHEMISTRY
	}
	
	public enum ModelStatus {
		STARTED,
		RUNNING,
		FINISHED
	}
	
	public enum ScanStatus {
		busy,
		waiting,
		done,
		error
	}
	
	public AbstractScanModel() {
		scanList = new ArrayList<SingleScan>();
		modelListeners = new ArrayList<IModelListener>();
//		initScan = new SingleScan();
//		scanList.add(initScan);
		saveButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH 
				| DefaultCellRenderer.INDICATION_CLICKED);
		saveButtonRenderer.setImage(KoalaImage.SAVE32.getImage());
		saveButtonRenderer.setBackground(FixedCellRenderer.COLOR_FIXEDBACKGROUND);
		loadButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH 
				| DefaultCellRenderer.INDICATION_CLICKED);
		loadButtonRenderer.setImage(KoalaImage.OPEN32.getImage());
		loadButtonRenderer.setBackground(FixedCellRenderer.COLOR_FIXEDBACKGROUND);
		addButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH 
				| DefaultCellRenderer.INDICATION_CLICKED);
		addButtonRenderer.setImage(KoalaImage.PLUS32.getImage());
		addButtonRenderer.setBackground(FixedCellRenderer.COLOR_FIXEDBACKGROUND);
		highlightSaveButtonRenderer = new ImageButtonHighlightRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		highlightSaveButtonRenderer.setImage(KoalaImage.SAVE32.getImage());
		highlightLoadButtonRenderer = new ImageButtonHighlightRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		highlightLoadButtonRenderer.setImage(KoalaImage.OPEN32.getImage());
		highlightAddButtonRenderer = new ImageButtonHighlightRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		highlightAddButtonRenderer.setImage(KoalaImage.PLUS_INV32.getImage());
		oddTextRenderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW );
		evenTextRenderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW );
//		oddTextHighlightRenderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW );
//		evenTextHighlightRenderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW );
		highlightRender = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW );
//		evenTextRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_CENTER);
		evenTextRenderer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
//		evenTextHighlightRenderer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
//		oddTextHighlightRenderer.setForeground(Activator.getHighlightColor());
//		evenTextHighlightRenderer.setForeground(Activator.getHighlightColor());
		highlightRender.setBackground(Activator.getBusyColor());
		highlightRender.setForeground(Activator.getRunningForgroundColor());
		emptyHeaderRenderer = new DefaultCellRenderer(SWT.NONE);
		columnHeaderRenderer = new FixedCellRenderer(SWT.NONE);

		oddUpButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH 
				| DefaultCellRenderer.INDICATION_CLICKED);
		oddUpButtonRenderer.setImage(KoalaImage.MOVE_UP32.getImage());
		evenUpButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		evenUpButtonRenderer.setImage(KoalaImage.MOVE_UP32.getImage());
		evenUpButtonRenderer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		greenUpButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH 
				| DefaultCellRenderer.INDICATION_CLICKED);
		greenUpButtonRenderer.setImage(KoalaImage.MOVE_UP32.getImage());
		greenUpButtonRenderer.setBackground(Activator.getBusyColor());
		oddDownButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		oddDownButtonRenderer.setImage(KoalaImage.MOVE_DOWN32.getImage());
		evenDownButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		evenDownButtonRenderer.setImage(KoalaImage.MOVE_DOWN32.getImage());
		evenDownButtonRenderer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		greenDownButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		greenDownButtonRenderer.setImage(KoalaImage.MOVE_DOWN32.getImage());
		greenDownButtonRenderer.setBackground(Activator.getBusyColor());
		highlightUpButtonRenderer = new ImageButtonHighlightRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		highlightUpButtonRenderer.setImage(KoalaImage.MOVE_UP_INV32.getImage());
		highlightDownButtonRenderer = new ImageButtonHighlightRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		highlightDownButtonRenderer.setImage(KoalaImage.MOVE_DOWN_INV32.getImage());

		oddDupButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH 
				| DefaultCellRenderer.INDICATION_CLICKED);
		oddDupButtonRenderer.setImage(KoalaImage.COPY32.getImage());
		evenDupButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		evenDupButtonRenderer.setImage(KoalaImage.COPY32.getImage());
		evenDupButtonRenderer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		greenDupButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH 
				| DefaultCellRenderer.INDICATION_CLICKED);
		greenDupButtonRenderer.setImage(KoalaImage.COPY32.getImage());
		greenDupButtonRenderer.setBackground(Activator.getBusyColor());
		oddDelButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		oddDelButtonRenderer.setImage(KoalaImage.DELETE32.getImage());
		evenDelButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		evenDelButtonRenderer.setImage(KoalaImage.DELETE32.getImage());
		evenDelButtonRenderer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		greenDelButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		greenDelButtonRenderer.setImage(KoalaImage.DELETE32.getImage());
		greenDelButtonRenderer.setBackground(Activator.getBusyColor());
		highlightDupButtonRenderer = new ImageButtonHighlightRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		highlightDupButtonRenderer.setImage(KoalaImage.COPY_FILLED32.getImage());
		highlightDelButtonRenderer = new ImageButtonHighlightRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		highlightDelButtonRenderer.setImage(KoalaImage.DELETE_INV32.getImage());
		
		propertyListener = new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				fireModelChangeEvent();
			}
		};
		String folderPath = System.getProperty(PROP_BACKUPFOLDER);
		File folder = new File(folderPath);
		if (folder.exists()) {
			backupFolder = folderPath;
		}
	}
	
	public void setTable(final KTable table) {
		this.table = table;
		table.addMouseListener(new MouseListener() {
			
//			int dcol = -1;
//			int drow = -1;
			
			@Override
			public void mouseUp(MouseEvent e) {
				Point cell = table.getCellForCoordinates(e.x, e.y);
				boolean isValidCell = false;
				if (cell != null) {
					int row = cell.y;
					int col = cell.x;
					if (row == 0) {
						if (col == 0) {
							saveTable(table);
						} else if (col == 1) {
							loadTable(table);
						} else if (col == 2) {
							insertScan(getSize());
						}
					} else if (row > 0) {
						isValidCell = true;
		    			if (col == 0) {
		    				logger.info("duplicate-scan button clicked");
		    				swapRows(row - 2, row - 1);
		    				table.redraw();
		    			} else if (col == 1) {
	    					logger.info("delete-scan button clicked");
	    					swapRows(row - 1, row);
	    					table.redraw();
		    			} else if (col == 2) {
		    				logger.info("duplicate-scan button clicked");
		    				insertScan(row);
		    				table.redraw();
		    			} else if (col == 3) {
	    					logger.info("delete-scan button clicked");
	    					deleteScan(row - 1);
	    					table.redraw();
		    			}
		    		}
				}
				if (!isValidCell) {
					table.clearSelection();
				}
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
//				Point cell = table.getCellForCoordinates(e.x, e.y);
//				dcol = cell.x;
//				drow = cell.y;
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		table.addMouseMoveListener(new MouseMoveListener() {
			
			@Override
			public void mouseMove(MouseEvent e) {
				Point cell = table.getCellForCoordinates(e.x, e.y);
				boolean isButtonCell = false;
				if (cell != null) {
					int row = cell.y;
					int col = cell.x;
//					if (row == 0 && col <= 1) {
//						isButtonCell = true;
//					}
					if ((row > 0 && col >= 0 && col <= 3) || (row ==0 && col >= 0 && col <= 2)) {
						isButtonCell = true;
						if (highlightCol != col || highlightRow != row) {
							int hCol = highlightCol;
							int hRow = highlightRow;
							highlightCol = col;
							highlightRow = row;
							table.redraw(hCol, hRow, 1, 1);
							table.redraw(col, row, 1, 1);
						}
					}
				}
				if (isButtonCell) {
					table.setCursor(Activator.getHandCursor());
				} else {
					table.setCursor(Activator.getDefaultCursor());
					if (highlightCol >= 0 && highlightRow >= 0) {
						int oldC = highlightCol;
						int oldR = highlightRow;
						highlightCol = -1;
						highlightRow = -1;
						table.redraw(oldC, oldR, 1, 1);
					}
				}
			}
		});
		
		table.addMouseTrackListener(new MouseTrackListener() {
			
			@Override
			public void mouseHover(MouseEvent e) {
//				Point cell = table.getCellForCoordinates(e.x, e.y);
//				boolean isButtonCell = false;
//				if (cell != null) {
//					int row = cell.y;
//					int col = cell.x;
//					if (row > 0 && col >= 0 && col <= 1) {
//						isButtonCell = true;
//					}
//				}
//				if (isButtonCell) {
//					table.setCursor(Activator.getHandCursor());
//				} else {
//					table.setCursor(Activator.getDefaultCursor());
//				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (highlightCol >= 0 && highlightRow >= 0) {
					int oldC = highlightCol;
					int oldR = highlightRow;
					highlightCol = -1;
					highlightRow = -1;
					table.redraw(oldC, oldR, 1, 1);
				}
			}
			
			@Override
			public void mouseEnter(MouseEvent e) {
			}
		});
	}
	
	public void setFont(Font font) {
		oddTextRenderer.setFont(font);
		evenTextRenderer.setFont(font);
		highlightRender.setFont(font);
//		oddTextHighlightRenderer.setFont(font);
//		evenTextHighlightRenderer.setFont(font);
		columnHeaderRenderer.setFont(font);
	}
	
	@Override
	public String getTooltipAt(int col, int row) {
		if (col == 0) {
			if (row == 0) {
				return "click to save the current workflow to an XML file";
			} else if (row > 0) {
				return "click to shift this entry up";
			}
		} else if (col == 1) {
			if (row == 0) {
				return "click to load entries from XML file and append to the current workflow";
			} else if (row > 0) {
				return "click to shift this entry down";
			}
		} else if (col == 2) {
			if (row == 0) {
				return "click to add new entry to the end of the queue";
			} else {
				return "click to duplicate this entry";
			}
		} else if (col == 3) {
			if (row > 0) {
				return "click to delete this entry";
			}
		}
		return null;
	}

	@Override
	public KTableCellRenderer getCellRenderer(int col, int row) {
		if (row == 0) {
			if (col == 0) {
				if (col == highlightCol && row == highlightRow) {
					return highlightSaveButtonRenderer;
				} else {
					return saveButtonRenderer;
				}
			} else if (col == 1) {
				if (col == highlightCol && row == highlightRow) {
					return highlightLoadButtonRenderer;
				} else {
					return loadButtonRenderer;
				}
			} else if (col == 2) {
				if (col == highlightCol && row == highlightRow) {
					return highlightAddButtonRenderer;
				} else {
					return addButtonRenderer;
				}
			} else if (col == 3) {
				return emptyHeaderRenderer;
			} else {
				return columnHeaderRenderer;
			}
		} else {
		SingleScan scan = getItem(row);
		if (scan != null) {
			if (ScanStatus.busy.name().equals(scan.getStatus())) {
				if (col == 0) {
					if (col == highlightCol && row == highlightRow) {
						return highlightUpButtonRenderer;
					} else {
						return greenUpButtonRenderer;
					}
				} else if (col == 1) { 
					if (col == highlightCol && row == highlightRow) {
						return highlightDownButtonRenderer;
					} else {
						return greenDownButtonRenderer;
					}
				} else if (col == 2) {
					if (col == highlightCol && row == highlightRow) {
						return highlightDupButtonRenderer;
					} else {
						return greenDupButtonRenderer;
					}
				} else if (col == 3) { 
					if (col == highlightCol && row == highlightRow) {
						return highlightDelButtonRenderer;
					} else {
						return greenDelButtonRenderer;
					}
				} else {
					return highlightRender;
				}
			}
		}
		if (row % 2 == 0) {
			if (col == 0) {
				if (col == highlightCol && row == highlightRow) {
					return highlightUpButtonRenderer;
				} else {
					return evenUpButtonRenderer;
				}
			} else if (col == 1) { 
				if (col == highlightCol && row == highlightRow) {
					return highlightDownButtonRenderer;
				} else {
					return evenDownButtonRenderer;
				}
			} else if (col == 2) {
				if (col == highlightCol && row == highlightRow) {
					return highlightDupButtonRenderer;
				} else {
					return evenDupButtonRenderer;
				}
			} else if (col == 3) { 
				if (col == highlightCol && row == highlightRow) {
					return highlightDelButtonRenderer;
				} else {
					return evenDelButtonRenderer;
				}
//			} else if (col == getStatusColumnId()) {
//				SingleScan scan = getItem(row);
//				if (ScanStatus.busy.name().equals(scan.getStatus())) {
//					return evenTextHighlightRenderer;
//				} else {
//					return evenTextRenderer;
//				}
			} else {
				return evenTextRenderer;
			}
		} else {
			if (col == 0) {
				if (col == highlightCol && row == highlightRow) {
					return highlightUpButtonRenderer;
				} else {
					return oddUpButtonRenderer;
				}
			} else if (col == 1) { 
				if (col == highlightCol && row == highlightRow) {
					return highlightDownButtonRenderer;
				} else {
					return oddDownButtonRenderer;
				}
			} else if (col == 2) {
				if (col == highlightCol && row == highlightRow) {
					return highlightDupButtonRenderer;
				} else {
					return oddDupButtonRenderer;
				}
			} else if (col == 3) { 
				if (col == highlightCol && row == highlightRow) {
					return highlightDelButtonRenderer;
				} else {
					return oddDelButtonRenderer;
				}
//			} else if (col == getStatusColumnId()) {
//				SingleScan scan = getItem(row);
//				if (ScanStatus.busy.name().equals(scan.getStatus())) {
//					return oddTextHighlightRenderer;
//				} else {
//					return oddTextRenderer;
//				}
			} else {
				return oddTextRenderer;
			}
		}
		}
	}
	
	protected void redrawRow(int row) {
		if (table != null) {
			table.redraw(2, row, 4, 1);
		}
	}
	
	public SingleScan getItem(int row) {
		synchronized (scanList) {
			if (row > 0 && row <= scanList.size()) {
				return scanList.get(row - 1);
			} else {
				return null;
			}
		}
	}
	
	@Override
	public abstract Point belongsToCell(int col, int row);

	@Override
	public int getRowCount() {
		return scanList.size() + 1;
	}

	@Override
	public int getFixedHeaderRowCount() {
		return 0;
	}

	@Override
	public int getFixedSelectableRowCount() {
		return 1;
	}

	@Override
	public int getFixedHeaderColumnCount() {
		return 0;
	}

	@Override
	public int getFixedSelectableColumnCount() {
		return 0;
	}

	@Override
	public boolean isColumnResizable(int col) {
		return false;
	}

	@Override
	public void setColumnWidth(int col, int width) {
	}

	@Override
	public int getRowHeight(int row) {
		return 44;
	}

	@Override
	public boolean isRowResizable(int row) {
		return false;
	}

	@Override
	public int getRowHeightMinimum() {
		return 40;
	}

	@Override
	public void setRowHeight(int row, int value) {
	}

	public void swapRows(int idx1, int idx2) {
		if (idx1 == idx2) {
			return;
		} else if (idx1 > idx2) {
			int i = idx1;
			idx1 = idx2;
			idx2 = i;
		}
		if (idx1 < 0) {
			return;
		}
		if (idx2 >= getSize()) {
			return;
		}
		synchronized (scanList) {
			Collections.swap(scanList, idx1, idx2);
		}
	}
	
	public void insertScan(int idx) {
		synchronized (scanList) {
			if (idx > scanList.size()) {
				idx = scanList.size();
			} else if (idx < 0) {
				idx = 0;
			}
			SingleScan pre;
			if (idx > 0) {
				pre = scanList.get(idx - 1);
			} else {
				pre = initScan;
			}
			SingleScan newScan;
			if (pre == null) {
				newScan = new SingleScan();
			} else {
				newScan = pre.getCopy();
			}
			if (isRunning) {
				newScan.setStatus(ScanStatus.waiting.name());
			}
			newScan.addPropertyChangeListener(propertyListener);
			scanList.add(idx, newScan);
		}
		fireModelChangeEvent();
	}
	
	public void deleteScan(SingleScan scan) {
		synchronized (scanList) {
			scan.removePropertyChangeListener(propertyListener);
			scanList.remove(scan);
			fireModelChangeEvent();
		}
	}
	
	public void addScans(int idx, List<SingleScan> scans) {
		synchronized (scanList) {
			for (SingleScan scan : scans) {
				if (isRunning) {
					scan.setStatus(ScanStatus.waiting.name());
				}
				scan.addPropertyChangeListener(propertyListener);
			}
			scanList.addAll(idx, scans);
			fireModelChangeEvent();
		}
	}
	
	public void appendScan(SingleScan scan) {
		synchronized (scanList) {
			if (isRunning) {
				scan.setStatus(ScanStatus.waiting.name());
			}
			scan.addPropertyChangeListener(propertyListener);
			scanList.add(scan);
			fireModelChangeEvent();
		}
	}

	public void deleteScan(int idx) {
		synchronized (scanList) {
//			if (scanList.size() == 1 && idx == 0) {
//				SingleScan newScan;
//				if (initScan != null) {
//					newScan = initScan.getCopy();
//				} else {
//					newScan = new SingleScan();
//				}
//				newScan.addPropertyChangeListener(propertyListener);
//				scanList.add(newScan);
//			}
			if (idx < scanList.size()) {
				SingleScan toRemove = scanList.get(idx);
				toRemove.removePropertyChangeListener(propertyListener);
			}
			scanList.remove(idx);
		}
		fireModelChangeEvent();
	}
	
	public SingleScan getInitScan() {
		return initScan;
	}
	
	public void setInitScan(SingleScan initScan) {
		this.initScan = initScan;
	}
	
	public int getSize() {
		return scanList.size();
	}
	
	public void setHightlight(int col, int row) {
		highlightCol = col;
		highlightRow = row;
	}
	
	public void start() {
		if (backupFolder != null) {
			try {
				SimpleDateFormat timeFormat = new SimpleDateFormat("yyMM");
				String folderPath = backupFolder + "/" + timeFormat.format(Calendar.getInstance().getTime());
				File folder = new File(folderPath);
				if (!folder.exists()) {
					folder.mkdir();
				}
				String pid = ControlHelper.experimentModel.getProposalId();
				String filename = folderPath + "/" + pid + "_" + (System.currentTimeMillis() / 1000) + ".xml";
				serialize(filename);
			} catch (Exception e2) {
				logger.error("failed to backup workflow: " + e2.getMessage());
			}
		}
		Thread runnerThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				logger.warn("experiment started");
				logger.warn(String.format("estimatated time is %d seconds", getTimeEstimation()));
				prepareStatus();
				ControlHelper.getProxy().clearInterruptFlag();
				startTime = Calendar.getInstance();
				isRunning = true;
				fireProgressUpdatedEvent(ModelStatus.STARTED);
				int row = 1;
//				for (SingleScan scan : scanList) {
				while (hasNextScan()) {
					if (row > 1) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}
					}
					SingleScan scan = getFirstScan();
					if (scan == null) {
						break;
					}
					scan.addPropertyChangeListener(new PropertyChangeListener() {
						
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if ("status".equals(evt.getPropertyName())){
								safeRedrawStatus(0);
							}
						}
					});
					try {
//						scan.setStatus(ScanStatus.busy.name());
//						table.redraw(getStatusColumnId(), row, 1, 1);
						safeRedrawStatus(row);
						scan.run();
						scan.setStatus(ScanStatus.done.name());
//						popFirstScan();
						deleteScan(0);
						safeRedrawTable();
//						safeRedrawStatus(row);
						row++;
					} catch (KoalaInterruptionException e) {
						scan.setStatus(ScanStatus.error.name());
						safeRedrawStatus(row);
						e.printStackTrace();
						handleError("Experiment aborted");
						break;
					} catch (KoalaServerException e) {
						scan.setStatus(ScanStatus.error.name());
						safeRedrawStatus(row);
						e.printStackTrace();
						handleError("server error: " + e.getMessage());
						break;
					} catch (Exception e) {
						scan.setStatus(ScanStatus.error.name());
						safeRedrawStatus(row);
						e.printStackTrace();
						handleError("error: " + e.getMessage());
						break;
					}
				}
				startTime = null;
				isRunning = false;
				fireProgressUpdatedEvent(ModelStatus.FINISHED);
				logger.warn("experiment finished");;
			}
		});
		runnerThread.start();
	}
	
	private void handleError(String errorText) {
		ControlHelper.experimentModel.publishErrorMessage(errorText);
	}
	
	private SingleScan getFirstScan() {
		synchronized (scanList) {
			if (scanList.size() > 0) {
				return scanList.get(0);
			} else {
				return null;
			}
		}
	}
	
	private void popFirstScan() {
		synchronized (scanList) {
			if (scanList.size() > 0) {
				scanList.remove(0);
			}
		}
	}
	
	private boolean hasNextScan() {
//		ListIterator<SingleScan> iter = scanList.listIterator(scanList.size());
//		while(iter.hasPrevious()) {
//			SingleScan scan = iter.previous();
//			if ("".equals(scan.getStatus())) {
//				return true;
//			}
//		}
		synchronized (scanList) {
//			for (SingleScan scan : scanList) {
//				if ("".equals(scan.getStatus())) {
//					return true;
//				}
//			}
			return scanList.size() > 0;
		}
	}
	
//	private SingleScan getNextScan() {
////		ListIterator<SingleScan> iter = scanList.listIterator(scanList.size());
////		while(iter.hasPrevious()) {
////			SingleScan scan = iter.previous();
////			if ("".equals(scan.getStatus())) {
////				next = scan;
////			} else {
////				break;
////			}
////		}
//		synchronized (scanList) {
//			SingleScan next = null;
//			for (SingleScan scan : scanList) {
//				if ("".equals(scan.getStatus())) {
//					next = scan;
//					break;
//				}
//			}
//			return next;
//		}
//	}
	
	public void finish() {
		isRunning = false;
		startTime = null;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public boolean needToRun() {
		synchronized (scanList) {
			for (SingleScan scan : scanList) {
				if (scan.needToRun()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public int getTimeEstimation() {
		synchronized (scanList) {
			int time = 0;
			for (SingleScan scan : scanList) {
				time += scan.getTotalTime();
			}
			return time;
		}
	}

	public String getTotalTimeText() {
		int time = getTimeEstimation();
		if (time == 0) {
			return "empty table";
		} else {
			return PanelUtils.convertTimeString(time);
		}
	}
	
	public int getTimeLeft() {
		int time = 0;
		synchronized (scanList) {
			for (SingleScan scan : scanList) {
				time += scan.getTimeLeft();
			}
		}
		return time;
	}
	
	public String getTimeLeftText() {
		if (isRunning) {
			int time = getTimeLeft();
			return PanelUtils.convertTimeString(time);
		} else {
			return "--";
		}
	}
	
	public String getStartedTime() {
		if (startTime != null) {
			Calendar now = Calendar.getInstance();
			SimpleDateFormat timeFormat;
			if (now.get(Calendar.DAY_OF_MONTH) == startTime.get(Calendar.DAY_OF_MONTH)) {
				timeFormat = new SimpleDateFormat("hh:mma");
			} else {
				timeFormat = new SimpleDateFormat("hh:mma 'on' dd/MM");
			}
			return timeFormat.format(startTime.getTime());
		} else {
			return "--";
		}
	}
	
	public String getFinishTime() {
		int totalTime;
		Calendar start;
		if (startTime != null) {
			totalTime = getTimeLeft();
			start = startTime;
		} else {
			totalTime = getTimeEstimation();
			start = Calendar.getInstance();
		}
		if (totalTime == 0) {
			return "--";
		}
		Calendar finish = Calendar.getInstance();
		finish.add(Calendar.SECOND, totalTime);
		SimpleDateFormat timeFormat;
		if (finish.get(Calendar.DAY_OF_MONTH) == start.get(Calendar.DAY_OF_MONTH)) {
			timeFormat = new SimpleDateFormat("hh:mma");
		} else {
			timeFormat = new SimpleDateFormat("hh:mma dd/MM");
		}
		return timeFormat.format(finish.getTime());
	}

	public long getFinishInSeconds() {
		if (startTime != null) {
			int totalTime = getTimeLeft();
//			Calendar finish = (Calendar) startTime.clone();
			Calendar finish = Calendar.getInstance();
			finish.add(Calendar.SECOND, totalTime);
			return finish.getTimeInMillis() / 1000;
		} else {
			return 0;
		}
	}

	public void addModelListener(IModelListener listener) {
		modelListeners.add(listener);
	}
	
	public void removeModelListener(IModelListener listener) {
		modelListeners.remove(listener);
	}
	
	protected void fireModelChangeEvent() {
		for (IModelListener listener : modelListeners) {
			listener.modelChanged();
		}
	}

	protected void fireProgressUpdatedEvent(ModelStatus status) {
		for (IModelListener listener : modelListeners) {
			listener.progressUpdated(status);
		}
	}
	
	protected abstract int getStatusColumnId();
	
	private void safeRedrawStatus(final int row) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				table.redraw(0, row, getColumnCount(), 1);
			}
		});
	}
	
	private void safeRedrawTable() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				table.redraw();
			}
		});
	}
	
	private void prepareStatus() {
		synchronized (scanList) {
			for (SingleScan scan : scanList) {
				scan.setStatus(ScanStatus.waiting.name());
			}
		}
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				table.redraw(getStatusColumnId(), 1, 1, scanList.size());
			}
		});
	}
	
	public abstract ModelType getModelType();
	
	private void saveTable(final KTable table) {
		FileDialog dialog = new FileDialog(table.getShell(), SWT.SAVE);
		String propFolder = ControlHelper.experimentModel.getProposalFolder();
		propFolder = propFolder.substring(0, propFolder.length() - 1);
		dialog.setFilterPath(propFolder);
		String ext = "*.xml,*.XML";
		dialog.setFilterExtensions(ext.split(","));
		dialog.open();
		if (dialog.getFileName() == null || dialog.getFileName().trim().length() == 0) {
			return;
		}
		final String filePath = dialog.getFilterPath() + File.separator + dialog.getFileName();
		serialize(filePath);
		ControlHelper.experimentModel.publishSystemMessage("Successfully exported to " + filePath);
	}
	
	public void loadTable(KTable table) {
		FileDialog dialog = new FileDialog(table.getShell(), SWT.OPEN);
		String propFolder = ControlHelper.experimentModel.getProposalFolder();
		propFolder = propFolder.substring(0, propFolder.length() - 1);
		dialog.setFilterPath(propFolder);
		String ext = "*.xml,*.XML";
		dialog.setFilterExtensions(ext.split(","));
		dialog.open();
		if (dialog.getFileName() == null || dialog.getFileName().trim().length() == 0) {
			return;
		}
		String filterPath = dialog.getFilterPath();
		final String filePath = filterPath + File.separator + dialog.getFileName();
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();

			File file = new File(filePath);
			if (!file.exists()) {
				throw new KoalaModelException("file not found");
			}
			Document document = documentBuilder.parse(file);
			Node root = document.getFirstChild();
			if (root == null || !root.getNodeName().equals(XML_NAME_TITLE)) {
				throw new KoalaModelException("invalid XML file format");
			}
			Node modelType = root.getAttributes().getNamedItem(XML_ATTR_MODELTYPE);
			if (modelType == null) {
				throw new KoalaModelException("invalid XML file format");
			}
			String typeValue = modelType.getTextContent();
			ModelType loadType = null;
			try {
				loadType = ModelType.valueOf(typeValue);
			} catch (Exception e) {
				throw new KoalaModelException("invalid XML file format");
			}
			if (getModelType() != loadType) {
				throw new KoalaModelException("the workflow of the XML file is in " 
						+ typeValue + " mode and not suitable for the current experiment mode.");
			}
			NodeList nodeList = root.getChildNodes();
			List<SingleScan> newList = new ArrayList<SingleScan>();
			for (int i = 0; i < nodeList.getLength(); i ++) {
				Node item = nodeList.item(i);
				String nodeName = item.getNodeName();
				if (SingleScan.NAME_SCAN.equals(nodeName)) {
					SingleScan scan = new SingleScan();
					scan.fromNode(item);
					newList.add(scan);
				}
			}
			scanList.addAll(newList);
			fireModelChangeEvent();
		} catch (Exception e) {
			handleError("failed to load file: " + filePath + ", " + e.getMessage());
		}
	}
	
	public void serialize(String filename) {
		try {
//			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
			DocumentBuilder documentBuilder = ControlHelper.getDocumentBuilder();
			Document document = documentBuilder.newDocument();
			document.setXmlStandalone(true);
			
			Element root = document.createElement(XML_NAME_TITLE);
			root.setAttribute(XML_ATTR_MODELTYPE, getModelType().name());
			synchronized (scanList) {
				for (SingleScan scan : scanList) {
					root.appendChild(scan.serialize(document));
				}
			}
			document.appendChild(root);

			// write to buffer
			Writer stringWriter = new StringWriter();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();			
			Transformer transformer = transformerFactory.newTransformer();
			
		    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		    
			transformer.transform(
					new DOMSource(document),
					new StreamResult(stringWriter));
			
			try (FileOutputStream stream = new FileOutputStream(new File(filename))) {
				try (Writer writer = new OutputStreamWriter(stream)) {
					// quick fix: ensure that every xml element starts on a new line
					BufferedReader reader = new BufferedReader(new StringReader(stringWriter.toString()));
					final String newLine = System.getProperty("line.separator");
					
					String line = reader.readLine();
					if (line != null) {
						writer.append(line.replace("><", '>' + newLine + '<'));
						while (null != (line = reader.readLine())) {
							writer.append(newLine);
							writer.append(line);
						}
					}
				}
			}
		} catch (Exception e) {
			handleError("failed to write to file: " + filename + ", " + e.getMessage());
		}
	}
}
