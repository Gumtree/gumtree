package au.gov.ansto.bragg.koala.ui.scan;

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
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;
import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.ImageButtonCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.ImageButtonHighlightRenderer;

public class ScanTableModel implements KTableModel {

	private SingleScan initScan;
	private List<SingleScan> scanList;
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
	
	private KTable table;
	
	private static final int COLUMN_COUNTS = 12;
	private static final int[] COLUMN_WIDTH = {40, 40, 120, 120, 80, 120, 80, 120, 120, 120, 320, 200};
	private static final String[] COLUMN_TITLE = {
			"", 
			"", 
			"Start", 
			"INCR", 
			"NUM", 
			"Final", 
			"EXPO",
			"TEMP",
			"CHI",
			"Status",
			"Comments",
			"Filename",
	};
	
	public ScanTableModel() {
		scanList = new ArrayList<SingleScan>();
		initScan = new SingleScan();
//		scanList.add(initScan);
		oddTextRenderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW );
		evenTextRenderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW );
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
	}
	
	public void setTable(KTable table) {
		this.table = table;
		table.addMouseListener(new MouseListener() {
			
			int dcol = -1;
			int drow = -1;
			
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
		    				if (dcol == col && drow == row) {
		    					insertScan(row);
		    					table.redraw();
		    				}
		    			} else if (col == 1) {
		    				if (dcol == col && drow == row) {
		    					deleteScan(row - 1);
		    					table.redraw();
		    				}
		    			}
		    		}
				}
				if (!isValidCell) {
					table.clearSelection();
				}
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				Point cell = table.getCellForCoordinates(e.x, e.y);
				dcol = cell.x;
				drow = cell.y;
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
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
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void setFont(Font font) {
		oddTextRenderer.setFont(font);
		evenTextRenderer.setFont(font);
		columnHeaderRenderer.setFont(font);
	}
	
	@Override
	public Object getContentAt(int col, int row) {
		if (row == 0) {
			return COLUMN_TITLE[col];
		}
		if (row > scanList.size()) {
			throw new IllegalArgumentException("scan row index out of range");
		}
		SingleScan scan = scanList.get(row - 1);
		switch (col) {
		case 2:
			return scan.getStart();
		case 3:
			return scan.getInc();
		case 4:
			return scan.getNumber();
		case 5:
			return scan.getEnd();
		case 6: 
			return scan.getExposure();
		case 7:
			float temp = scan.getTemp();
			if (temp == 0) {
				return "";
			} 
			if (row == 2) {
				return temp;
			} else {
				float last = scanList.get(row - 2).getTemp();
				if (temp == last) {
					return "";
				} else {
					return temp;
				}
			}
		case 8:
			float chi = scan.getChi();
			if (chi == 0) {
				return "";
			} 
			if (row == 2) {
				return chi;
			} else {
				float last = scanList.get(row - 2).getChi();
				if (chi == last) {
					return "";
				} else {
					return chi;
				}
			}
		case 9:
			return scan.getStatus();
		case 10:
			return scan.getComments();
		case 11:
			return scan.getFilename();
		default:
			break;
		}
		return "";
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
	public KTableCellEditor getCellEditor(int col, int row) {
		if (row <= 0) {
			return null;
		}
		if (col > 1 && col != 9) {
			KTableCellEditorText editor = new KTableCellEditorText();
			return editor;
		}
		return null;
	}

	private void redrawRow(int row) {
		if (table != null) {
			table.redraw(2, row, 4, 1);
		}
	}
	
	@Override
	public void setContentAt(int col, int row, Object value) {
		if (row > scanList.size()) {
			throw new IllegalArgumentException("scan row index out of range");
		}
		SingleScan scan = scanList.get(row - 1);
		switch (col) {
		case 2:
			if (value instanceof Float) {
				scan.setStart((Float) value);
			} else {
				scan.setStart(Float.valueOf(value.toString()));
			}
			redrawRow(row);
			break;
		case 3:
			if (value instanceof Float) {
				scan.setInc((Float) value);
			} else {
				scan.setInc(Float.parseFloat(value.toString()));
			}
			redrawRow(row);
			break;
		case 4:
			if (value instanceof Integer) {
				scan.setNumber((Integer) value);
			} else {
				scan.setNumber(Integer.parseInt(value.toString()));
			}
			redrawRow(row);
			break;
		case 5:
			if (value instanceof Float) {
				scan.setEnd((Float) value);
			} else {
				scan.setEnd(Float.parseFloat(value.toString()));
			}
			redrawRow(row);
			break;
		case 6:
			if (value instanceof Integer) {
				scan.setExposure((Integer) value);
			} else {
				scan.setExposure(Integer.parseInt(value.toString()));
			}
			break;
		case 7:
			if (value instanceof Float) {
				scan.setTemp((Float) value);
			} else {
				scan.setTemp(Float.parseFloat(value.toString()));
			}
			break;
		case 8:
			if (value instanceof Float) {
				scan.setChi((Float) value);
			} else {
				scan.setChi(Float.parseFloat(value.toString()));
			}
			break;
		case 9:
			scan.setStatus(String.valueOf(value));
			break;
		case 10:
			scan.setComments(String.valueOf(value));
			break;
		case 11:
			scan.setFilename(String.valueOf(value));
			break;
		default:
			break;
		}
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
	
	@Override
	public Point belongsToCell(int col, int row) {
		if (row == 0 && col == 1) {
			return new Point(0, 0);
		} else {
			return null;
		}
	}

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
	public int getColumnCount() {
		return COLUMN_COUNTS;
	}

	@Override
	public int getFixedHeaderColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFixedSelectableColumnCount() {
		return 0;
	}

	@Override
	public int getColumnWidth(int col) {
		return COLUMN_WIDTH[col];
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
		SingleScan newScan = pre.getCopy();
		scanList.add(idx, newScan);
	}
	
	public void deleteScan(int idx) {
		if (scanList.size() == 1 && idx == 0) {
			scanList.add(initScan.getCopy());
		}
		scanList.remove(idx);
		
	}
	
	public SingleScan getInitScan() {
		return initScan;
	}
	
	public int getSize() {
		return scanList.size();
	}
	
	public void setHightlight(int col, int row) {
		highlightCol = col;
		highlightRow = row;
	}
}
