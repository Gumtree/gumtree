package au.gov.ansto.bragg.koala.ui.scan;

import org.eclipse.swt.graphics.Point;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;

import au.gov.ansto.bragg.koala.ui.Activator;

public class ChemistryModel extends AbstractScanModel {

	private FixedCellRenderer fixedRenderer;
	private static final int COLUMN_COUNTS = 11;
	private static final int[] COLUMN_WIDTH = {40, 40, 120, 120, 80, 120, 80, 120, 120, 320, 80};
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
			"Filename",
			"Status",
	};
	
	public ChemistryModel() {
		super();
		fixedRenderer = new FixedCellRenderer(DefaultCellRenderer.STYLE_FLAT);
		fixedRenderer.setFont(Activator.getMiddleFont());
	}
	
	@Override
	public KTableCellRenderer getCellRenderer(int col, int row) {
		if (row > 0 && col == 2 && row % 2 == 0 ) {
			return fixedRenderer;
		}
		return super.getCellRenderer(col, row);
	}
	
	@Override
	public Object getContentAt(int col, int row) {
		if (row == 0) {
			return COLUMN_TITLE[col];
		}
		if (row > scanList.size() * 2) {
			throw new IllegalArgumentException("scan row index out of range");
		}
		SingleScan scan = scanList.get((row - 1) / 2);
		if (row % 2 == 1) {
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
				if (row == 1) {
					return temp;
				} else {
					float last = scanList.get((row - 1) / 2 - 1).getTemp();
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
				if (row == 1) {
					return chi;
				} else {
					float last = scanList.get((row - 1) / 2 - 1).getChi();
					if (chi == last) {
						return "";
					} else {
						return chi;
					}
				}
			case 9:
				return scan.getFilename();
			case 10:
				return scan.getStatus();
			default:
				break;
			}
		} else {
			switch (col) {
			case 2:
				return "Comments:";
			case 4:
				return scan.getComments();
			default:
				break;
			}
		}
		return "";
	}

	@Override
	public void setContentAt(int col, int row, Object value) {
		if (row / 2 > scanList.size()) {
			throw new IllegalArgumentException("scan row index out of range");
		}
		SingleScan scan = scanList.get((row - 1) / 2);
		if (row % 2 == 1) {
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
				scan.setFilename(String.valueOf(value));
				break;
			default:
				break;
			}
		} else {
			switch (col) {
			case 4:
				scan.setComments(String.valueOf(value));
				break;
			default:
				break;
			}
		}
	}

	@Override
	public KTableCellEditor getCellEditor(int col, int row) {
		if (row <= 0) {
			return null;
		}
		if (row % 2 == 1) {
			if (col > 1 && col < 10) {
				KTableCellEditorText editor = new KTableCellEditorText();
				return editor;
			}
		} else {
			if (col == 4) {
				return new KTableCellEditorText();
			}
		}
		return null;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNTS;
	}

	@Override
	public int getColumnWidth(int col) {
		return COLUMN_WIDTH[col];
	}

	@Override
	public Point belongsToCell(int col, int row) {
		if (row == 0) {
			if (col == 1) {
				return new Point(0, 0);
			} else {
				return null;
			}
		} else {
			if (row % 2 == 0) {
				if (col == 0) {
					return new Point(0, row - 1);
				} else if (col == 1) {
					return new Point(1, row - 1);
				} else if (col == 3) {
					return new Point(2, row);
				} else if (col > 4 && col < 10) {
					return new Point(4, row);
				}
			} else {
			}
			return null;
		}
	}
}
