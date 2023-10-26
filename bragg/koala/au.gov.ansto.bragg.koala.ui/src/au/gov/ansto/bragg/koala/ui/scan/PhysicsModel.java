package au.gov.ansto.bragg.koala.ui.scan;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorComboText;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

import au.gov.ansto.bragg.koala.ui.Activator;

public class PhysicsModel extends AbstractScanModel {

	private FixedCellRenderer fixedRenderer;
	private TextCellRenderer liteOddTextRenderer;
	private TextCellRenderer liteEvenTextRenderer;
	
	private static final int[] COLUMN_WIDTH = {40, 40, 40, 40, 120, 180, 108, 80, 80, 108, 108, 150, 90, 150, 300};
	private static final int COLUMN_COUNTS = COLUMN_WIDTH.length;
	private static final String[] COLUMN_TITLE = {
			"",
			"",
			"",
			"", 
			"Status",
			"Scan variable",
			"Start", 
			"Incr", 
			"Num", 
			"Final", 
			"Expose",
//			Activator.PHI + " Value",
//			"Temp",
//			Activator.CHI + " Value",
			"Filename",
			"FileIdx",
			"Duration",
			"Comments"
	};
	
	public PhysicsModel() {
		super();
		liteOddTextRenderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW);
		liteOddTextRenderer.setForeground(Activator.getLightForgroundColor());
		liteOddTextRenderer.setFont(Activator.getMiddleFont());
		
		liteEvenTextRenderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW);
		liteEvenTextRenderer.setForeground(Activator.getLightForgroundColor());
		liteEvenTextRenderer.setFont(Activator.getMiddleFont());
		liteEvenTextRenderer.setBackground(
				Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		fixedRenderer = new FixedCellRenderer(DefaultCellRenderer.STYLE_FLAT);
		fixedRenderer.setFont(Activator.getMiddleFont());
	}
	
	@Override
	public ModelType getModelType() {
		return ModelType.PHYSICS;
	}
	
//	@Override
//	public KTableCellRenderer getCellRenderer(int col, int row) {
//		if (row > 0) {
//			if (row % 2 == 1 && col == 8) {
//				if (getItem(row).getTarget().isTemperature()) {
//					return fixedRenderer;
//				}
//			}
////			if (row % 2 == 0 && col == 2) {
////				return fixedRenderer;
////			}
//		}
//		return super.getCellRenderer(col, row);
//	}
	@Override
	public KTableCellRenderer getCellRenderer(int col, int row) {
		try {
			if (row > 0) {
				if (col == 4) {
					SingleScan scan = scanList.get(row - 1);
					if (scan.getTarget().isPoints() && scan.getPoints().trim().length() == 0) {
						if (row % 2 == 0) {
							return liteEvenTextRenderer;
						} else {
							return liteOddTextRenderer;
						}
					}
				}
			} else if (row > 1) {
				System.err.println(row);
			}
			return super.getCellRenderer(col, row);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
		case 4:
			return scan.getStatus();
		case 5:
			return scan.getTarget().getText();
		case 6:
			if (scan.getTarget().isDrive()) {
				return "to:";
			} else if (scan.getTarget().isPoints()) {
				return "values:";
			} else {
				return scan.getStart();
			}
		case 7:
			if (scan.getTarget().isDrive()) {
				return scan.getDriveValue();
			} else if (scan.getTarget().isPoints()) {
				String points = scan.getPoints();
				if (points.trim().length() == 0) {
					return "#, #, ...";
				} else {
					return scan.getPoints();
				}
			} else {
				return scan.getInc();
			}
		case 8:
//			if (scan.getTarget().isPoints()) {
//				return "";
//			} else {
				return scan.getNumber();
//			}
		case 9:
//			if (scan.getTarget().isPoints()) {
//				return "";
//			} else {
				return scan.getEnd();
//			}
		case 10: 
			return scan.getExposure();
		case 11:
			return scan.getFilename();
		case 12:
			return scan.getStartIndex();
		case 13:
			return scan.getTimeEstimation();
		case 14:
			return scan.getComments();
		default:
			break;
		}
		return "";
	}

	@Override
	public KTableCellEditor getCellEditor(int col, int row) {
		if (row <= 0) {
			return null;
		}
		if (col == 5) {
			KTableCellEditorComboText editor = new KTableCellEditorComboText();
			editor.setVisibleItemCount(4);
			editor.setFont(Activator.getMiddleFont());
			editor.setItems(ScanTarget.getAllText());
			editor.setSelectionOnly(true);
			return editor;
		} else if (col == 6) {
			if (getItem(row).getTarget().isDrive()) {
				return null;
			} else if (getItem(row).getTarget().isPoints()) {
				return null;
			} else {
				return new KTableCellEditorText();
			}
		} else if (col == 7) {
//			if (getItem(row).getTarget().isPoints()) {
//				return new KTableCellEditorText();
//			} else {
//				return new KTableCellEditorText();
//			}
			return new KTableCellEditorText();
		} else if (col > 7 && col != 13) {
			return new KTableCellEditorText();
		} else {
			return null;
		}
	}

	@Override
	public void setContentAt(int col, int row, Object value) {
		if (row > scanList.size()) {
			throw new IllegalArgumentException("scan row index out of range");
		}
		SingleScan scan = scanList.get(row - 1);
		switch (col) {
		case 5:
			scan.setTarget(ScanTarget.valueOfText(String.valueOf(value)));
			redrawRow(row);
			break;
		case 6:
			if (value instanceof Float) {
				scan.setStart((Float) value);
			} else {
				String t = String.valueOf(value);
				if (t.length() > 0) {
					scan.setStart(Float.valueOf(t));
				}
			}
			break;
		case 7:
			if (scan.getTarget().isDrive()) {
				if (value instanceof Float) {
					scan.setDriveValue((Float) value);
				} else {
					String v = String.valueOf(value);
					if (v.length() > 0) {
						scan.setDriveValue(Float.parseFloat(v));
					}
				}
			} else if (scan.getTarget().isPoints()) {
				scan.setPoints(String.valueOf(value));
			} else {
				if (value instanceof Float) {
					scan.setInc((Float) value);
				} else {
					String t = String.valueOf(value);
					if (t.length() > 0) {
						scan.setInc(Float.parseFloat(t));
					}
				}
			}
			redrawRow(row);
			break;
		case 8:
			if (value instanceof Integer) {
				scan.setNumber((Integer) value);
			} else {
				String t = String.valueOf(value);
				if (t.length() > 0) {
					scan.setNumber(Integer.parseInt(t));
				}
			}
			redrawRow(row);
			break;
		case 9:
			if (value instanceof Float) {
				scan.setEnd((Float) value);
			} else {
				String t = String.valueOf(value);
				if (t.length() > 0) {
					scan.setEnd(Float.parseFloat(t));
				}
			}
			redrawRow(row);
			break;
		case 10:
			if (value instanceof Integer) {
				scan.setExposure((Integer) value);
			} else {
				String t = String.valueOf(value);
				if (t.length() > 0) {
					scan.setExposure(Integer.parseInt(t));
				}
			}
			break;
		case 11:
			scan.setFilename(String.valueOf(value));
			break;
		case 12:
			if (value instanceof Integer) {
				scan.setStartIndex((Integer) value);
			} else {
				String t = String.valueOf(value);
				if (t.length() > 0) {
					scan.setStartIndex(Integer.parseInt(t));
				}
			}
			break;
		case 14:
			scan.setComments(String.valueOf(value));
			break;
		default:
			break;
		}
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
			if (col == 3) {
				return new Point(2, 0);
			}
		} else {
			if (col > 7 && col <= 9) {
				SingleScan scan = scanList.get(row - 1);
				if (scan.getTarget().isDrive() || scan.getTarget().isPoints()) {
					return new Point(7, row);
				}
			} else if (col > 9) {
				SingleScan scan = scanList.get(row - 1);
				if (scan.getTarget().isDrive()) {
					return new Point(7, row);
				}
			}
		}
//		else {
//			if (row % 2 == 1) {
//				if (col == 5 || col == 6) {
//					SingleScan scan = getItem(row);
//					if (scan.getTarget().isPoints()) {
//						return new Point(4, row);
//					}
//				} else {
//					return null;
//				}
//			} else {
//				if (col == 0) {
//					return new Point(0, row - 1);
//				} else if (col == 1) {
//					return new Point(1, row - 1);
//				} else if (col > 3 && col < 11) {
//					return new Point(3, row);
//				} else if (col == 11) {
//					return new Point(11, row - 1);
//				}
//			}
//		}
		return null;
	}

	@Override
	protected int getStatusColumnId() {
		return 4;
	}
}
