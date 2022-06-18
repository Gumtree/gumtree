package au.gov.ansto.bragg.koala.ui.scan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;
import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.ImageButtonCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.ImageButtonHighlightRenderer;

public abstract class AbstractScanModel implements KTableModel {

	private SingleScan initScan;
	protected List<SingleScan> scanList;
//	private Map<SingleScan, Button> dupButton;
	private TextCellRenderer oddTextRenderer;
	private TextCellRenderer evenTextRenderer;
	private FixedCellRenderer columnHeaderRenderer;
	private ImageButtonCellRenderer oddDupButtonRenderer;
	private ImageButtonCellRenderer evenDupButtonRenderer;
	private ImageButtonCellRenderer oddDelButtonRenderer;
	private ImageButtonCellRenderer evenDelButtonRenderer;
	private ImageButtonHighlightRenderer highlightDupButtonRenderer;
	private ImageButtonHighlightRenderer highlightDelButtonRenderer;
	private int highlightRow = -1;
	private int highlightCol = -1;
	private boolean isRunning;
	private LocalDateTime startTime;
	private List<IModelListener> modelListeners;
	private PropertyChangeListener propertyListener;
	
	private KTable table;
	
	public enum ModelStatus {
		STARTED,
		RUNNING,
		FINISHED
	}
	
	public AbstractScanModel() {
		scanList = new ArrayList<SingleScan>();
		modelListeners = new ArrayList<IModelListener>();
//		initScan = new SingleScan();
//		scanList.add(initScan);
		oddTextRenderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW );
		evenTextRenderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW );
//		evenTextRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_CENTER);
		evenTextRenderer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		columnHeaderRenderer = new FixedCellRenderer(SWT.NONE);
		oddDupButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH 
				| DefaultCellRenderer.INDICATION_CLICKED);
		oddDupButtonRenderer.setImage(KoalaImage.COPY32.getImage());
		evenDupButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		evenDupButtonRenderer.setImage(KoalaImage.COPY32.getImage());
		evenDupButtonRenderer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		oddDelButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		oddDelButtonRenderer.setImage(KoalaImage.DELETE32.getImage());
		evenDelButtonRenderer = new ImageButtonCellRenderer(DefaultCellRenderer.STYLE_PUSH
				| DefaultCellRenderer.INDICATION_CLICKED);
		evenDelButtonRenderer.setImage(KoalaImage.DELETE32.getImage());
		evenDelButtonRenderer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
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
					if (row > 0) {
						isValidCell = true;
		    			if (col == 0) {
//		    				if (dcol == col && drow == row) {
//		    					insertScan((row - 1) / 2 + 1);
		    					insertScan(row);
		    					table.redraw();
//		    				}
		    			} else if (col == 1) {
//		    				if (dcol == col && drow == row) {
//		    					deleteScan((row - 1) / 2);
		    					deleteScan(row - 1);
		    					table.redraw();
//		    				}
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
					if (row > 0 && col >= 0 && col <= 1) {
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
		columnHeaderRenderer.setFont(font);
	}
	
	@Override
	public String getTooltipAt(int col, int row) {
		if (col == 0) {
			if (row > 0) {
				return "click to duplicate this entry";
			}
		} else if (col == 1) {
			if (row > 0) {
				return "click to delete this entry";
			}
		}
		return null;
	}

	@Override
	public KTableCellRenderer getCellRenderer(int col, int row) {
		if (row == 0) {
			return columnHeaderRenderer;
		}
		if (row % 2 == 0) {
			if (col == 0) {
				if (col == highlightCol && row == highlightRow) {
					return highlightDupButtonRenderer;
				} else {
					return evenDupButtonRenderer;
				}
			} else if (col == 1) { 
				if (col == highlightCol && row == highlightRow) {
					return highlightDelButtonRenderer;
				} else {
					return evenDelButtonRenderer;
				}
			} else {
				return evenTextRenderer;
			}
		} else {
			if (col == 0) {
				if (col == highlightCol && row == highlightRow) {
					return highlightDupButtonRenderer;
				} else {
					return oddDupButtonRenderer;
				}
			} else if (col == 1) { 
				if (col == highlightCol && row == highlightRow) {
					return highlightDelButtonRenderer;
				} else {
					return oddDelButtonRenderer;
				}
			} else {
				return oddTextRenderer;
			}
		}
	}
	
	protected void redrawRow(int row) {
		if (table != null) {
			table.redraw(2, row, 4, 1);
		}
	}
	
	public SingleScan getItem(int row) {
		if (row <= scanList.size()) {
			return scanList.get(row - 1);
		} else {
			return null;
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
		return 1;
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

	public void insertScan(int idx) {
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
		newScan.addPropertyChangeListener(propertyListener);
		scanList.add(idx, newScan);
		fireModelChangeEvent();
	}
	
	public void deleteScan(SingleScan scan) {
		scan.removePropertyChangeListener(propertyListener);
		scanList.remove(scan);
		fireModelChangeEvent();
	}
	
	public void addScans(int idx, List<SingleScan> scans) {
		for (SingleScan scan : scans) {
			scan.addPropertyChangeListener(propertyListener);
		}
		scanList.addAll(idx, scans);
		fireModelChangeEvent();
	}
	
	public void deleteScan(int idx) {
		if (scanList.size() == 1 && idx == 0) {
			SingleScan newScan;
			if (initScan != null) {
				newScan = initScan.getCopy();
			} else {
				newScan = new SingleScan();
			}
			newScan.addPropertyChangeListener(propertyListener);
			scanList.add(newScan);
		}
		if (idx < scanList.size()) {
			SingleScan toRemove = scanList.get(idx);
			toRemove.removePropertyChangeListener(propertyListener);
		}
		scanList.remove(idx);
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
		startTime = LocalDateTime.now();
		isRunning = true;
		fireProgressUpdatedEvent(ModelStatus.STARTED);
	}
	
	public void finish() {
		isRunning = false;
		startTime = null;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public int getTimeEstimation() {
		int time = 0;
		for (SingleScan scan : scanList) {
			time += scan.getTotalTime();
		}
		return time;
	}

	public int getTimeLeft() {
		int time = 0;
		for (SingleScan scan : scanList) {
			time += scan.getTimeLeft();
		}
		return time;
	}
	
	public String getStartedTime() {
		if (startTime != null) {
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter timeFormat;
			if (now.getDayOfMonth() == startTime.getDayOfMonth()) {
				timeFormat = DateTimeFormatter.ofPattern("HH:mm");
				return startTime.format(timeFormat);
			} else {
				timeFormat = DateTimeFormatter.ofPattern("HH:mm 'on' dd/MM");
				return startTime.format(timeFormat);
			}
		} else {
			return "--";
		}
	}
	
	public String getFinishTime() {
		if (startTime != null) {
			int totalTime = getTimeEstimation();
			LocalDateTime finish = startTime.plusSeconds(totalTime);
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter timeFormat;
			if (now.getDayOfMonth() == startTime.getDayOfMonth()) {
				timeFormat = DateTimeFormatter.ofPattern("HH:mm");
				return finish.format(timeFormat);
			} else {
				timeFormat = DateTimeFormatter.ofPattern("HH:mm 'on' dd/MM");
				return finish.format(timeFormat);
			}
		} else {
			return "--";
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
}
