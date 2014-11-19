package de.kupzog.ktable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

import de.kupzog.ktable.IKTableTooltip.TooltipAssistant;
import de.kupzog.ktable.models.KTableModel;
import de.kupzog.ktable.models.KTableSortedModel;
import de.kupzog.ktable.renderers.TextCellRenderer;

/**
 * Custom drawn tabel widget for SWT GUIs.
 * <p>
 * The idea of KTable is to have a flexible grid of cells to display data in it.
 * The class focuses on displaying data and not on collecting the data to
 * display. The latter is done by the <code>KTableModel</code> which has to be implemented
 * for each specific case. Some default tasks are done by a base implementation
 * called <code>KTableDefaultModel</code>. Look also into <code>KTableSortedModel</code> that provides 
 * a transparent sorting of cells.<br>
 * The table asks the table model for the amount of
 * columns and rows, the sizes of columns and rows and for the content of the
 * cells which are currently drawn. Even if the table has a million rows, it
 * won't get slower because it only requests those cells it currently draws.
 * Only a bad table model can influence the drawing speed negatively.
 * <p>
 * When drawing a cell, the table calls a <code>KTableCellRenderer</code> to do this work.
 * The table model determines which cell renderer is used for which cell. A
 * default renderer is available (<code>KTableCellRenderer.defaultRenderer</code>), but the
 * creation of self-written renderers for specific purposes is assumed. 
 * Some default renderers are available in the package <code>de.kupzog.ktable.cellrenderers.*</code>.
 * <p>
 * KTable allows to F columns and rows. Each column can have an individual
 * size while the rows are all of the same height except the first row. Multiple
 * column and row headers are possible. These "fixed" cells will not be scrolled
 * out of sight. The column and row count always starts in the upper left corner
 * with 0, independent of the number of column headers or row headers.
 * <p>
 * It is also possible to span cells over several rows and/or columns. The KTable
 * asks the model do provide this information via <code>belongsToCell(col, row)</code>.
 * This method must return the cell the given cell should be merged with.
 * <p>
 * Changing of model values is possible by implementations of <code>KTableCellEditor</code>.
 * Again the KTable asks the model to provide an implementation. Note that there
 * are multiple celleditors available in the package <code>de.kupzog.ktable.editors</code>!
 * 
 * @author Friederich Kupzog
 * @see de.kupzog.ktable.models.KTableModel
 * @see de.kupzog.ktable.models.KTableDefaultModel
 * @see de.kupzog.ktable.models.KTableSortedModel
 * @see de.kupzog.ktable.KTableCellRenderer
 * @see de.kupzog.ktable.KTableCellEditor
 * @see de.kupzog.ktable.KTableCellSelectionListener
 *  
 */
public class KTable extends Canvas {    

    private BottomSpaceStrategy bottomSpaceStrategy;
    private class BottomSpaceStrategy {
        protected Rectangle getBottomArea() {
            Rectangle r = getClientArea();
            if (m_Model.getRowCount() > 0) {
                r.y = getFixedHeight();
                for (int i = 0; i < m_RowsVisible; i++) {
                    r.y += m_Model.getRowHeight(i + m_TopRow);
                }
            }

            return r;
        }

        protected int getLastColumnRight() {
            return getColumnRight(Math.min(m_LeftColumn + m_ColumnsVisible, m_Model.getColumnCount() - 1));
        }

        public void draw(GC gc) {
            Rectangle r = getBottomArea();
            int lastColRight = getLastColumnRight();

            gc.setBackground(getBackground());
            gc.fillRectangle(r);
            gc.fillRectangle(lastColRight + 2, 0, r.width, r.height);
        }
    }
    private class BottomSpaceStrategyDummy extends BottomSpaceStrategy {
        public void draw(GC gc) {
            Rectangle r = getBottomArea();
            int lastColRight = getLastColumnRight();

            lastColRight--;
            int lastCol = m_Model.getColumnCount()-1;

            //int defaultrowheight = m_Model.getRowHeight()-1;
            int ystart = 1;
            for (int row=0; row<getFixedRowCount(); row++) {
                Point last = getValidCell(lastCol, row);
                KTableCellRenderer fixedRenderer = m_Model.getCellRenderer(last.x, last.y);

                int rowheight = m_Model.getRowHeight(row);
                fixedRenderer.drawCell(gc, 
                        new Rectangle(lastColRight + 2, ystart, r.width-1, rowheight-1),
                        -1, -1, "", false, true, false, m_Model);
                
                ystart += rowheight;
            }
            TextCellRenderer defaultRenderer = new TextCellRenderer(SWT.NONE);
            for (int row = m_TopRow; row < m_TopRow + m_RowsVisible; row++) {
                int rowHeight = m_Model.getRowHeight(row);
                defaultRenderer.drawCell(gc, 
                        new Rectangle(lastColRight + 2, ystart, r.width - 1, rowHeight - 1),
                        -1, -1, "", false, false, false, m_Model);
                ystart += rowHeight;
            }

            // draw bottom areas
            gc.setBackground(getBackground());
            gc.fillRectangle(r);
        }
    }
    private class BottomSpaceStrategyLast extends BottomSpaceStrategy {
        public void draw(GC gc) {
            Rectangle r = getBottomArea();

            gc.setBackground(getBackground());
            gc.fillRectangle(r);
        }
    }

    // Data and data editing facilities:
    protected KTableModel m_Model;
    protected KTableCellEditor m_CellEditor;

    /**
     * 软溴犟 耱痤觇, 觐蝾疣���弪� �磬耱��祛戾眚 镥疴铋 桤 怦艴 忤滂禧�
     * 礤翳犟桊钼囗睇�耱痤�
     */
    protected int m_TopRow;
    /**
     * 软溴犟 觐腩黻� 觐蝾疣���弪� �磬耱��祛戾眚 镥疴铋 桤 怦艴 忤滂禧�
     * 礤翳犟桊钼囗睇�觐腩眍�
     */
    protected int m_LeftColumn;

    // Selection
    protected HashMap<Object, Object> m_Selection;
    protected int m_FocusRow;
    protected int m_FocusCol;
    protected int m_MainFocusRow; 
    protected int m_MainFocusCol;
    protected int m_ClickColumnIndex;
    protected int m_ClickRowIndex;
    private int m_Style = SWT.NONE;

    // important measures
    /**
     * 暑腓麇耱忸 躅� 猁 鬣耱梓眍 忤滂禧�礤翳犟桊钼囗睇�耱痤�
     */
    protected int m_RowsVisible;
    protected int m_RowsFullyVisible;
    protected int m_ColumnsVisible;
    protected int m_ColumnsFullyVisible;

    // column sizes
    protected int m_ResizeColumnIndex;
    protected int m_ResizeColumnLeft;
    protected int m_ResizeRowIndex;
    protected int m_ResizeRowTop;
    protected int m_NewRowSize;
    protected int m_NewColumnSize;
    protected boolean m_Capture = false;
    protected Image m_LineRestore;
    protected int m_LineX;
    protected int m_LineY;

	// resize area
    protected int m_ResizeAreaSize = 10;
    
    // diverse
    protected Display m_Display;
    protected ArrayList<KTableCellSelectionListener> cellSelectionListeners;
    protected ArrayList<KTableCellDoubleClickListener> cellDoubleClickListeners;
    protected ArrayList<KTableCellResizeListener> cellResizeListeners;
    protected Cursor m_defaultCursor;
    protected Point  m_defaultCursorSize;
    protected Cursor m_defaultRowResizeCursor;
    protected Cursor m_defaultColumnResizeCursor;
    protected IKTableTooltip m_nativTooltip;
    protected Point m_tt_cell;
    protected KTableCursorProvider m_CursorProvider;
    protected int m_numRowsVisibleInPreferredSize = 1;
    protected int m_numColsVisibleInPreferredSize = 1;

    private boolean fluentScrollVertical = true;
    private boolean fluentScrollHorizontal = true;

    // [+] CONSTRUCTOR & DISPOSE 

    /**
     * Creates a new KTable.
     * 
     * possible styles: 
     * <ul>
     * <li><b>SWT.V_SCROLL</b> - show vertical scrollbar and allow vertical scrolling by arrow keys</li>
     * <li><b>SWT.H_SCROLL</b> - show horizontal scrollbar and allow horizontal scrolling by arrow keys</li>
     * <li><b>SWTX.AUTO_SCROLL</b> - Dynamically shows vertical and horizontal scrollbars when they are necessary.</li>
     * <li><b>SWTX.FILL_WITH_LASTCOL</b> - Makes the table enlarge the last column to always fill all space.</li>
     * <li><b>SWTX.FILL_WITH_DUMMYCOL</b> - Makes the table fill any remaining space with dummy columns to fill all space.</li>
     * <li><b>SWT.FLAT</b> - Does not paint a dark outer border line.</li>
     * <li><b>SWT.MULTI</b> - Sets the "Multi Selection Mode".
     * In this mode, more than one cell or row can be selected.
     * The user can achieve this by shift-click and ctrl-click.
     * The selected cells/rows can be scattored ofer the complete table.
     * If you pass false, only a single cell or row can be selected.
     * This mode can be combined with the "Row Selection Mode".</li>
     * <li><b>SWT.FULL_SELECTION</b> - Sets the "Full Selection Mode".
     * In the "Full Selection Mode", the table always selects a complete row.
     * Otherwise, each individual cell can be selected.
     * This mode can be combined with the "Multi Selection Mode".</li>
     * <li><b>SWTX.EDIT_ON_KEY</b> - Activates a possibly present cell editor
     * on every keystroke. (Default: only ENTER). However, note that editors
     * can specify which events they accept.</li>
     * <li><b>SWTX.MARK_FOCUS_HEADERS</b> - Makes KTable draw left and top header cells
     * in a different style when the focused cell is in their row/column.
     * This mimics the MS Excel behavior that helps find the currently
     * selected cell(s).</li>
     * <li><b>SWT.HIDE_SELECTION</b> - Hides the selected cells when the KTable
     * looses focus.</li>
     * After creation a table model should be added using setModel().
     */
    public KTable(Composite parent, int style) {
        // Initialize canvas to draw on.
        super(parent, SWT.NO_BACKGROUND | (style & ~SWTX.EDIT_ON_KEY));

        m_Style = style;

        if ((m_Style & SWTX.FILL_WITH_DUMMYCOL) == SWTX.FILL_WITH_DUMMYCOL) {
            bottomSpaceStrategy = new BottomSpaceStrategyDummy();
        } else if ((m_Style & SWTX.FILL_WITH_LASTCOL) == SWTX.FILL_WITH_LASTCOL) {
            bottomSpaceStrategy = new BottomSpaceStrategyLast();
        } else {
            bottomSpaceStrategy = new BottomSpaceStrategy();
        }

        //inits
        m_Display = Display.getCurrent();
        m_Selection = new HashMap<Object, Object>();
        m_CellEditor = null;

        m_TopRow = 0;
        m_LeftColumn = 0;
        m_FocusRow = 0;
        m_FocusCol = 0;
        m_RowsVisible = 0;
        m_RowsFullyVisible = 0;
        m_ColumnsVisible = 0;
        m_ColumnsFullyVisible = 0;
        m_ResizeColumnIndex = -1;
        m_ResizeRowIndex = -1;
        m_ResizeRowTop = -1;
        m_NewRowSize = -1;
        m_NewColumnSize = -1;
        m_ResizeColumnLeft = -1;
        m_ClickColumnIndex = -1;
        m_ClickRowIndex = -1;

        m_LineRestore = null;
        m_LineX = 0;
        m_LineY = 0;

        cellSelectionListeners = new ArrayList<KTableCellSelectionListener>(10);
        cellDoubleClickListeners = new ArrayList<KTableCellDoubleClickListener>(10);
        cellResizeListeners = new ArrayList<KTableCellResizeListener>(10);

        // Listener creation
        createListeners();

        // handle tooltip initialization:
	    m_nativTooltip = new DefaultTooltip(super.getToolTipText());
	    super.setToolTipText("");

	    // apply various style bits:
	    if ((style & SWTX.AUTO_SCROLL) == SWTX.AUTO_SCROLL) {
	        addListener(SWT.Resize, new Listener() {
	            public void handleEvent(Event event) {
	                updateScrollbarVisibility();
	            }
	        });
            addCellResizeListener(new KTableCellResizeListener() {
                public void rowResized(int row, int newHeight) {
                    updateScrollbarVisibility();
                }
                public void columnResized(int col, int newWidth) {
                    updateScrollbarVisibility();
                }
            });
	    }	    
    }

    public void dispose() {
    	if (m_defaultCursor != null)
            m_defaultCursor.dispose();

    	if (m_defaultRowResizeCursor != null)
    		m_defaultRowResizeCursor.dispose();

       	if (m_defaultColumnResizeCursor != null)
    		m_defaultColumnResizeCursor.dispose();

       	super.dispose();
    }

    protected void createListeners() {
        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent event) {
                onPaint(event);
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                onMouseDown(e);
            }

            public void mouseUp(MouseEvent e) {
                onMouseUp(e);
            }

            public void mouseDoubleClick(MouseEvent e) {
                onMouseDoubleClick(e);
            }
        });

        addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                onMouseMove(e);
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                onKeyDown(e);
            }
        });

        addFocusListener(new FocusListener() {
		    private Point[] oldSelection;
            public void focusGained(FocusEvent e) {
               if (!isShowSelectionWithoutFocus() && 
                       oldSelection!=null) {
                   setSelection(oldSelection, false);
                   for (int i=0; i<oldSelection.length; i++) 
                       updateCell(oldSelection[i].x, oldSelection[i].y);
                   oldSelection=null;
               }
            }

            public void focusLost(FocusEvent e) {
                if (!isShowSelectionWithoutFocus()) {
                    oldSelection = getCellSelection();
                    clearSelection();
                    if (oldSelection != null) {
                        for (int i = 0; i < oldSelection.length; i++) {
                            updateCell(oldSelection[i].x, oldSelection[i].y);
                        }
                    }
                }
            }
		});

    	TooltipListener tooltipListener = new TooltipListener();
    	addListener(SWT.Dispose, tooltipListener);
    	addListener(SWT.KeyDown, tooltipListener);
    	addListener(SWT.MouseDown, tooltipListener);
    	addListener(SWT.MouseDoubleClick, tooltipListener);
    	addListener(SWT.MouseMove, tooltipListener);
    	addListener(SWT.MouseHover, tooltipListener);
    	addListener(SWT.MouseExit, tooltipListener);

    	if (getVerticalBar() != null) {
    	    getVerticalBar().addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (fluentScrollVertical || e.detail != SWT.DRAG) {
                        int oldTopRow = m_TopRow;
                        m_TopRow = getVerticalBar().getSelection();
                        if (oldTopRow != m_TopRow) {
                            Rectangle rect = getClientArea();
                            redraw(rect.x, rect.y, rect.width, rect.height, false);
                        }
                    }
                }
    	    });
    	    getVerticalBar().addListener(SWT.Selection, tooltipListener);
    	}

    	if (getHorizontalBar() != null) {
    	    getHorizontalBar().addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (fluentScrollHorizontal || e.detail != SWT.DRAG) {
                        int oldLeftCol = m_LeftColumn;
                        m_LeftColumn = getHorizontalBar().getSelection();
                        if (oldLeftCol != m_LeftColumn) {
                            Rectangle rect = getClientArea();
                            redraw(rect.x, rect.y, rect.width, rect.height, false);
                        }
                    }
                }
    	    });
    	    getHorizontalBar().addListener(SWT.Selection, tooltipListener);
    	}

    	addCellSelectionListener(new KTableCellSelectionAdapter() {
    		public void fixedCellSelected(int col, int row, int statemask) {
    			KTableCellAction cellAction = m_Model.getCellAction(col, row);
    			if (cellAction != null) {
    			    setSelection(col, m_FocusRow, true);
    				Rectangle r = getCellRect(col, row);
    				cellAction.run(KTable.this, col, row, r);
    			}
    		}
    	});
    }
    // [.]

    //////////////////////////////////////////////////////////////////////////////
    // CALCULATIONS
    //////////////////////////////////////////////////////////////////////////////

    /**
     * @return 洋祆囵磬�痂磬 翳犟桊钼囗睇�觐腩眍�
     */
    protected int getFixedWidth() {
        int width = 0;
        for (int i = 0; i < getFixedColumnCount(); i++)
            width += getColumnWidth(i);
        return width;
    }

    /**
     * @return 洋祆囵磬�恹耦蜞 翳犟桊钼囗睇�耱痤�(玎泐腩怅�
     */
    protected int getHeaderWidth() {
        int width = 0;
        for (int i = 0; i < m_Model.getFixedHeaderColumnCount(); i++)
            width += getColumnWidth(i);
        return width;
    }

    /**
     * @param index 皖戾�鲥脲忸�觐腩黻�
     * @return x-觐铕滂磬蝮 忤滂祛�脲忸�沭囗桷�箨噻囗眍�觐腩黻�
     */
    protected int getColumnLeft(int index) {
        if (index < getFixedColumnCount()) {
            int x = 0;
            for (int i = 0; i < index; i++) {
                x += getColumnWidth(i);
            }
            return x;
        } else if (index > m_LeftColumn + m_ColumnsVisible) {
            return -1;
        } else {
            int x = getFixedWidth();
            if (index > m_LeftColumn) {
                for (int i = m_LeftColumn; i < index; i++) {
                    x += getColumnWidth(i);
                }
            }
            return x;
        }
    }

    /**
     * @param index 软溴犟 鲥脲忸�觐腩黻�
     * @return x-觐铕滂磬蝮 脲忸�沭囗桷�鲥脲忸�觐腩黻� 橡�铎,
     * 潆�耠篦�, 觐沅�觐腩黻�忤漤�礤 镱腠铖螯� 蜞�赅�脲忄�鬣耱�
     * 耜瘥蜞 翳犟桊钼囗睇扈 觐腩黻囔� 蝾 忸玮疣屐� 觐铕滂磬蜞 礤 钺疱玎弪�
     * 镱 沭囗桷�翳犟桊钼囗睇�觐腩眍� 锐镱朦珞弪� 耦忪羼蝽��觌栾镨磴铎 镱
     * 沭囗桷�翳犟桊钼囗睇�觐腩眍�镳�铗痂耦怅�鬣耱梓眍 耜瘥蝾��彘觇.
     */
    protected int getColumnLeftForDrawing(int index) {
        if (index < getFixedColumnCount()) {
            int x = 0;
            for (int i = 0; i < index; i++) {
                x += getColumnWidth(i);
            }
            return x;
        } else if (index > m_LeftColumn + m_ColumnsVisible) {
            return -1;
        } else {
            int x = getFixedWidth();
            if (index < m_LeftColumn) {
                for (int i = index; i < m_LeftColumn; i++)
                    x -= m_Model.getColumnWidth(i);
            } else if (index > m_LeftColumn) {
                for (int i = m_LeftColumn; i < index; i++) {
                    x += getColumnWidth(i);
                }
            }
            return x;
        }
    }

    /**
     * @param index 皖戾�鲥脲忸�觐腩黻�
     * @return x-觐铕滂磬蝮 潆�镳噔钽�牮� 觐腩黻�(蝾黜邋, 潆�镥疴铋 蝾麝� 磬躅�轳�
     * 耩噻�耧疣忄 铗 鲥脲忸�觐腩黻�
     */
    protected int getColumnRight(int index) {
        if (index < 0) return 0;
        return getColumnLeft(index) + getColumnWidth(index);
    }

    protected int getRowBottom(int index) {

    	if ( index < 0)
    		return 0;
    	
    	int y = getFixedHeight();
    	for ( int i = m_TopRow; i <= index; i++) {
    	   y += m_Model.getRowHeight(i);
    	}
    	
    	return y;

    }

    /**
     * @return 蔓耦蝮 �镨犟咫圊, 礤钺躅滂祗�潆�铗钺疣驽龛�怦艴 翳犟桊钼囗睇�耱痤�
     */
    private int getFixedHeight() {
    	int height = 0;
        for (int i = 0; i < getFixedRowCount(); i++)
            height += m_Model.getRowHeight(i);
    	return height;
    }

    /**
     * @return Returns the number of visible rows in the table.
     * This always contains the fixed rows, and adds the number of 
     * rows that fit in the rest based on the currently shown top-row.
     */
    public int getVisibleRowCount() {
        if (m_Model == null) return 0;
        return getFixedRowCount() + getFullyVisibleRowCount(getFixedHeight());
    }

    private int getFullyVisibleRowCount(int fixedHeight) {
    	Rectangle rect = getClientArea();

    	int count = 0;
    	int heightSum = fixedHeight;
        for (int i = m_TopRow; heightSum < rect.height && i < m_Model.getRowCount(); i++) {
			int rowHeight = m_Model.getRowHeight(i);
			if (heightSum + rowHeight <= rect.height) {
				count++;
				heightSum += rowHeight;
			} else {
				break;
			}
    	}
    	return count;
    }

    /**
     * @return 暑腓麇耱忸 礤翳犟桊钼囗睇�耱痤��觐眦�祛溴腓, 觐蝾瘥�祛泱�猁螯 镱腠铖螯�
     * 铗钺疣驽睇 镳�蝈牦�篑腩忤�.
     */
	private int getFullyVisibleRowCountAtEndOfTable() {
		Rectangle rect = getClientArea();

		int count = 0;
		int heightSum = getFixedHeight();
		for (int i = m_Model.getRowCount() - 1; heightSum < rect.height && i >= getFixedRowCount(); i--) {
			int rowHeight = m_Model.getRowHeight(i);
			if (heightSum + rowHeight <= rect.height) {
				count++;
				heightSum += rowHeight;
			} else {
				break;
			}
		}
		return count;
	}
    /**
     * @return 暑腓麇耱忸 礤翳犟桊钼囗睇�觐腩眍��觐眦�祛溴腓, 觐蝾瘥�祛泱�猁螯 镱腠铖螯�
     * 铗钺疣驽睇 镳�蝈牦�篑腩忤�.
     */
	private int getFullyVisibleColCountAtEndOfTable() {
		Rectangle rect = getClientArea();

		int count = 0;
		int widthSum = getFixedWidth();
		for (int i = m_Model.getColumnCount() - 1; widthSum < rect.width && i >= getFixedColumnCount(); i--) {
			int colWidth = m_Model.getColumnWidth(i);
			if (widthSum + colWidth <= rect.width) {
				count++;
				widthSum += colWidth;
			} else {
				break;
			}
		}
		return count;
	}

	protected void doCalculations() {
	    // [+] 篷腓 礤 玎溧磬 祛溴朦
        if (m_Model == null) {
            ScrollBar sb = getHorizontalBar();
            if (sb != null) {
                sb.setMinimum(0);
                sb.setMaximum(1);
                sb.setPageIncrement(1);
                sb.setThumb(1);
                sb.setSelection(1);
            }
            sb = getVerticalBar();
            if (sb != null) {
                sb.setMinimum(0);
                sb.setMaximum(1);
                sb.setPageIncrement(1);
                sb.setThumb(1);
                sb.setSelection(1);
            }
            return;
        }
        // [.]

        // [+] 暑痧尻蜩痤怅�m_LeftColumn
        if (m_LeftColumn < getFixedColumnCount()) {
            m_LeftColumn = getFixedColumnCount();
        }
        if (m_LeftColumn > m_Model.getColumnCount()) {
            m_LeftColumn = Math.max(
                    getFixedColumnCount(),
                    m_Model.getColumnCount() - getFullyVisibleColCountAtEndOfTable());
        }
        m_LeftColumn = Math.min(
                m_LeftColumn,
                m_Model.getColumnCount() - getFullyVisibleColCountAtEndOfTable());
        // [.]

        // [+] 暑痧尻蜩痤怅�m_TopRow
        if (m_TopRow < getFixedRowCount()) {
            m_TopRow = getFixedRowCount();
        }
        if (m_TopRow > m_Model.getRowCount()) {
            m_TopRow = Math.max(
                    getFixedRowCount(),
                    m_Model.getRowCount() - getFullyVisibleRowCountAtEndOfTable());
        }
        m_TopRow = Math.min(
                m_TopRow,
                m_Model.getRowCount() - getFullyVisibleRowCountAtEndOfTable());
        // [.]

        if (    m_Model.getRowCount() <= getFixedRowCount() ||
                m_Model.getColumnCount() <= getFixedColumnCount()) {
            clearSelectionWithoutRedraw();
            m_FocusCol = -1;
            m_FocusRow = -1;
        } else {
            // [+] 暑痧尻蜩痤怅�m_FocusCol
            if (m_FocusCol < m_Model.getFixedHeaderColumnCount()) {
                m_FocusCol = m_Model.getFixedHeaderColumnCount();
            } else if (m_FocusCol >= m_Model.getColumnCount()) {
                m_FocusCol = m_Model.getColumnCount() - 1;
            }
            // [.]
            // [+] 暑痧尻蜩痤怅�m_FocusRow
            if (m_FocusRow < m_Model.getFixedHeaderRowCount()) {
                m_FocusRow = m_Model.getFixedHeaderRowCount();
            } else if (m_FocusRow >= m_Model.getRowCount()) {
                m_FocusRow = m_Model.getRowCount() - 1;
            }
            // [.]
        }

        int fixedHeight = getFixedHeight();
        m_ColumnsVisible = 0;
        m_ColumnsFullyVisible = 0;

        if (m_Model.getColumnCount() > getFixedColumnCount()) {
            Rectangle rect = getClientArea();
            int runningWidth = getColumnLeft(m_LeftColumn);
            for (int col = m_LeftColumn; col < m_Model.getColumnCount(); col++) {
                if (runningWidth < rect.width + rect.x)
                    m_ColumnsVisible++;
                runningWidth += getColumnWidth(col);
                if (runningWidth <= rect.width + rect.x)
                    m_ColumnsFullyVisible++;
                else
                    break;
            }
        }

        ScrollBar sb = getHorizontalBar();
        if (sb != null) {
            if (m_Model.getColumnCount() <= getFixedColumnCount()) {
                sb.setMinimum(0);
                sb.setMaximum(1);
                sb.setPageIncrement(1);
                sb.setThumb(1);
                sb.setSelection(1);
            } else {
                sb.setValues(
                        m_LeftColumn,
                        getFixedColumnCount(),
                        m_Model.getColumnCount() - getFullyVisibleColCountAtEndOfTable() + 5,
                        5, 1, 1);
            }
        }

        m_RowsFullyVisible = getFullyVisibleRowCount(fixedHeight);
        m_RowsFullyVisible = Math.min(m_RowsFullyVisible, m_Model.getRowCount()
                - getFixedRowCount());
        m_RowsFullyVisible = Math.max(0, m_RowsFullyVisible);

        m_RowsVisible = m_RowsFullyVisible + 1;

        if (m_TopRow + m_RowsFullyVisible >= m_Model.getRowCount()) {
            m_RowsVisible--;
        }

        sb = getVerticalBar();
        if (sb != null) {
            if (m_Model.getRowCount() <= getFixedRowCount()) {
                sb.setMinimum(0);
                sb.setMaximum(1);
                sb.setPageIncrement(1);
                sb.setThumb(1);
                sb.setSelection(1);
            } else {
                sb.setValues(
                        m_TopRow,
                        getFixedRowCount(),
                        m_Model.getRowCount() - getFullyVisibleRowCountAtEndOfTable() + 5,
                        5,
                        1,
                        m_RowsFullyVisible);
            }
        }
    }

    /**
     * Returns the area that is occupied by the given cell. Does not
     * take into account any cell span.
     * @param col
     * @param row
     * @return Rectangle
     */
    protected Rectangle getCellRectIgnoreSpan(int col, int row) {
        return getCellRectIgnoreSpan(col, row, getColumnLeft(col)/* + 1*/);
    }

    /**
     * Returns the area that is occupied by the given cell. Does not
     * take into account any cell span.<p>
     * This version is an optimization if the x value is known. Use the
     * version with just col and row as parameter if this is not known.
     * @param col The column index
     * @param row The row index
     * @param x_startValue The right horizontal start value - if this is not 
     * known, there is a version without this parameter! Note that this is the
     * end value of the last cell + 1 px border.  
     * @return Returns the area of a cell
     */
    protected Rectangle getCellRectIgnoreSpan(int col, int row, int x_startValue) {
        if ((col < 0) || (col >= m_Model.getColumnCount()) ||
            (row < 0) || (row >= m_Model.getRowCount()))
            return new Rectangle(-1, -1, 0, 0);

        int y;

        if (row >= getFixedRowCount() && row < m_TopRow) {
            y = getFixedHeight();
        } else {
            y = getYforRow(row);
        }

        // take into account 1px bottom and right border that
        // belongs to the cell and is rendered by the cellrenderer
        // TODO: Make lines seperately configured and seperately drawn items
        int width = getColumnWidth(col) - 1;
        int height = m_Model.getRowHeight(row) - 1;

        return new Rectangle(x_startValue, y, width, height);
    }

    private int getRowForY(int y) {
    	int rowSum = 1;
    	for (int i=0; i<getFixedRowCount(); i++) {
    		int height =m_Model.getRowHeight(i); 
    		if (rowSum<=y && rowSum+height>y) return i;
    		rowSum+=height;
    	}
    	for (int i=m_TopRow; i<m_Model.getRowCount(); i++) {
    		int height =m_Model.getRowHeight(i); 
    		if (rowSum<=y && rowSum+height>y) return i;
    		rowSum+=height;
    	}
    	return -1;
    }

    /**
     * @param row 羊痤赅, 潆�觐蝾痤�礤钺躅滂祛 铒疱溴腓螯 y-觐铕滂磬蝮 磬鬣豚
     * @return 骂玮疣弪 y-觐铕滂磬蝮, �觐蝾痤�磬麒磬弪� 桤钺疣驽龛�箨噻囗眍�耱痤觇
     */
    private int getYforRow(int row) {
        if (row == 0) return 0;

		int y;
		if (row < getFixedRowCount()) {
			y = 0;
			for (int i = 0; i < row; i++)
				y += m_Model.getRowHeight(i);
    	} else {
            y = getFixedHeight();
            for (int i = m_TopRow; i < row; i++)
                y += m_Model.getRowHeight(i);
    	}
    	return y;
    }

    private Rectangle getCellRect(int col, int row, int left_X) {
        Rectangle bound = getCellRectIgnoreSpan(col, row, left_X);

        // determine the cells that are contained:
        bound.width += calculateExtraCellWidth(col, row, /* in pixels */true);
		bound.height += calculateExtraCellHeight(col, row, /* in pixels */true);

        return bound;
    }

    /**
     * Returns the area that is occupied by the given cell. 
     * Respects cell span.
     * @param col the column index
     * @param row the row index
     * @return returns the area the cell content is drawn at.
     * @throws IllegalArgumentException if the given cell is not a cell
     * that is visible, but overlapped by a spanning cell. Call getValidCell()
     * first to ensure it is a visible cell.
     */
    public Rectangle getCellRect(int col, int row) {
        checkWidget();

        // be sure that it is a valid cell that is not overlapped by some other:
        Point valid = getValidCell(col, row);
        if (valid.x != col || row != valid.y) return new Rectangle(0, 0, 0, 0);

        Rectangle bound = getCellRectIgnoreSpan(col, row);
        // determine the cells that are contained:
		bound.width += calculateExtraCellWidth(col, row, /* in pixels */true);
		bound.height += calculateExtraCellHeight(col, row, /* in pixels */true);
        return bound;
    }

    public Rectangle getCellRectForDrawing(int col, int row) {
        checkWidget();

        if (    (col < 0) || (col >= m_Model.getColumnCount()) ||
                (row < 0) || (row >= m_Model.getRowCount()))
            return new Rectangle(-1, -1, 0, 0);

        // be sure that it is a valid cell that is not overlapped by some other:
        Point valid = getValidCell(col, row);
        if (valid.x != col || row != valid.y) return new Rectangle(0, 0, 0, 0);

        Rectangle bound = new Rectangle(
                getColumnLeftForDrawing(col), 0, -1, -1);

        if (row >= getFixedRowCount() && row < m_TopRow) {
            bound.y = getFixedHeight();
            for (int i = row; i < m_TopRow; i++)
                bound.y -= m_Model.getRowHeight(i);
        } else {
            bound.y = getYforRow(row);
        }

        for (int i = col; i < m_Model.getColumnCount() && getValidCell(i, row).equals(valid); i++) {
            bound.width += getColumnWidth(i);
        }
        for (int i = row; i < m_Model.getRowCount() && getValidCell(col, i).equals(valid); i++) {
            bound.height += m_Model.getRowHeight(i);
        }

        return bound;
    }

	/**
	 * Calculates the extra <i>visible</i> width of the cell due to cell merging, both in pixels and in number of
	 * columns.
	 * 
	 * @param col
	 *            The column index of the cell.
	 * @param row
	 *            The row index of the cell.
	 * @param resultInPixels
	 *            If <code>true</code>, returns the width in pixels. If <code>false</code>, returns the result in
	 *            number of columns.
	 * @return The width, unit depending on the parameter <code>resultInPixels</code>.
	 */
	private int calculateExtraCellWidth(int col, int row, boolean resultInPixels) {
		// check: only the root cells of merged cells are actually drawn, so all other cells have span width of 0
		Point valid = getValidCell(col, row);
		if (valid.x != col || valid.y != row) {
			return 0;
		}

		// we determine the width of the cell both in pixels and in number of columns
		int widthInPx = 0, widthInColumns = 0;

		// check adjacent cells (towards the right) to see if they are merged with this cell
		int i;
		for (i = 1; col + i < m_Model.getColumnCount() && getValidCell(col + i, row).equals(new Point(col, row)); i++) {
			/*
			 * if we get to the border of the fixed cell block, we must stop counting; otherwise, we run into problems
			 * when cells are merged across this border and we start scrolling
			 */
			if (col < getFixedColumnCount() && col + i >= getFixedColumnCount()) {
				break;
			}
			widthInPx += getColumnWidth(col + i);
			widthInColumns = i;
		}

		/*
		 * if we did stop counting at the border of the fixed cell block above, we must now continue the count at the
		 * first visible cell after the break. this takes into account that a middle part of the merged cell is not
		 * visible anymore because the user has scrolled, while the first and last parts are still visible (the first
		 * part is visible because it is in the fixed cell block, while the last part is still visible because the user
		 * has not scrolled very far).
		 */
		if (col < getFixedColumnCount() && col + i >= getFixedColumnCount()) {
			for (int j = 0; getValidCell(m_LeftColumn + j, row).equals(new Point(col, row)); j++) {
				widthInPx += getColumnWidth(m_LeftColumn + j);
				widthInColumns = m_LeftColumn + j - row;
			}
		}

		/*
		 * another special case: if a merged cell is only partially visible (because of scrolling), we first determine
		 * the theoretical width of the cell, disregarding that some parts are not visible. therefore, we must now
		 * subtract the width of all those columns that are not visible anymore because the user has already scrolled
		 * away.
		 */
		if (col >= getFixedColumnCount() && col - m_LeftColumn < 0) {
			for (int k = 0; k < Math.abs(col - m_LeftColumn); k++) {
				widthInPx -= getColumnWidth(col + k);
			}
		}

		if (resultInPixels) {
			return widthInPx;
		} else {
			return widthInColumns;
		}
	}

	/**
	 * Calculates the extra <i>visible</i> height of the cell due to cell merging, both in pixels and in number of
	 * rows.
	 * 
	 * @param col
	 *            The column index of the cell.
	 * @param row
	 *            The row index of the cell.
	 * @param resultInPixels
	 *            If <code>true</code>, returns the height in pixels. If <code>false</code>, returns the result in
	 *            number of rows.
	 * @return The height, unit depending on the parameter <code>resultInPixels</code>.
	 */
	private int calculateExtraCellHeight(int col, int row, boolean resultInPixels) {
		// check: only the root cells of merged cells are actually drawn, so all other cells have span width of 0
		Point valid = getValidCell(col, row);
		if (valid.x != col || valid.y != row) {
			return 0;
		}

		// we determine the height of the cell both in pixels and in number of rows
		int heightInPx = 0, heightInRows = 0;

		// check adjacent cells (towards the bottom) to see if they are merged with this cell
		int i;
		for (i = 1; row + i < m_Model.getRowCount() && getValidCell(col, row + i).equals(new Point(col, row)); i++) {
			/*
			 * if we get to the border of the fixed cell block, we must stop counting; otherwise, we run into problems
			 * when cells are merged across this border and we start scrolling
			 */
			if (row < getFixedRowCount() && row + i >= getFixedRowCount()) {
				break;
			}
			heightInPx += m_Model.getRowHeight(row + i);
			heightInRows = i;
		}

		/*
		 * if we did stop counting at the border of the fixed cell block above, we must now continue the count at the
		 * first visible cell after the break. this takes into account that a middle part of the merged cell is not
		 * visible anymore because the user has scrolled, while the first and last parts are still visible (the first
		 * part is visible because it is in the fixed cell block, while the last part is still visible because the user
		 * has not scrolled very far).
		 */
		if (row < getFixedRowCount() && row + i >= getFixedRowCount()) {
			for (int j = 0; getValidCell(col, m_TopRow + j).equals(new Point(col, row)); j++) {
				heightInPx += m_Model.getRowHeight(m_TopRow + j);
				heightInRows = m_TopRow + j - row;
			}
		}

		/*
		 * another special case: if a merged cell is only partially visible (because of scrolling), we first determine
		 * the theoretical height of the cell, disregarding that some parts are not visible. therefore, we must now
		 * subtract the height of all those rows that are not visible anymore because the user has already scrolled
		 * away.
		 */
		if (row >= getFixedRowCount() && row - m_TopRow < 0) {
			for (int k = 0; k < Math.abs(row - m_TopRow); k++) {
				heightInPx -= m_Model.getRowHeight(row + k);
			}
		}

		if (resultInPixels) {
			return heightInPx;
		} else {
			return heightInRows;
		}
	}
	
	/**
	 * Calculates the number of extra columns this cell occupies due to cell merging. For example, if the cell does not
	 * span any columns, this method will return 0. If the cell spans across two columns, it will return one. However,
	 * if parts of the cell are not visible (for example, because of a combination of fixed columns and scrolling), then
	 * these parts are not counted here. This way, the caller can determine where the boundary of a cell is, taking into
	 * account the current scrolling position.
	 * 
	 * @param col
	 *            The column index.
	 * @param row
	 *            The row index.
	 * @return The number of extra columns this cell occupies due to cell merging.
	 */
	public int getVisibleCellSpanWidth(int col, int row) {
		return calculateExtraCellWidth(col, row, /* in pixels */false);
	}

	/**
	 * Calculates the number of extra rows this cell occupies due to cell merging. For example, if the cell does not
	 * span any rows, this method will return 0. If the cell spans across two rows, it will return one. However, if
	 * parts of the cell are not visible (for example, because of a combination of fixed rows and scrolling), then these
	 * parts are not counted here. This way, the caller can determine where the boundary of a cell is, taking into
	 * account the current scrolling position.
	 * 
	 * @param col
	 *            The column index.
	 * @param row
	 *            The row index.
	 * @return The number of extra rows this cell occupies due to cell merging.
	 */
	public int getVisibleCellSpanHeight(int col, int row) {
		return calculateExtraCellHeight(col, row, /* in pixels */false);
	}
	
	/**
	 * Calculates the area (in terms of columns and rows) that a certain cell occupies due to cell spanning. The return
	 * value is a {@link Rectangle}, where {@link Rectangle#x} is the column index of the upper-left corner of the cell,
	 * {@link Rectangle#y} is the row index of the upper-left corner of the cell, {@link Rectangle#width} is the width
	 * of the cell (in number of columns) and {@link Rectangle#height} is the height of the cell (in number of rows).
	 * The width and height of a cell are always at least one if the cell is visible. A cell that does not span any other
	 * cells has height and width equal to 1.
	 * 
	 * @param col The column index.
	 * @param row The row index.
	 * @return The area that a certain cell occupies due to cell spanning.
	 */
	public Rectangle getCellSpanExtent(int col, int row) {
		Point valid = getValidCell(col, row);
		Rectangle extent = new Rectangle(valid.x, valid.y, 1, 1);
		
		for(int i = extent.width; getValidCell(valid.x + i, valid.y).equals(valid); i++) {
			extent.width++;
		}
		
		for (int j = extent.height; getValidCell(valid.x, valid.y + j).equals(valid); j++) {
			extent.height++;
		}
		
		return extent;
	}

	protected boolean canDrawCell(Rectangle r, Rectangle clipRect) {
        if (r.height == 0 || r.width == 0) return false;
        return r.intersects(clipRect);
    }

    //////////////////////////////////////////////////////////////////////////////
    // PAINTING & MORE
    //////////////////////////////////////////////////////////////////////////////

    // Paint-Result
	/**
	 * �戾蝾�漕徉怆屙�铒蜩扈玎鲨� 橡�镱腠铋 镥疱痂耦怅�蝈镥瘘 耥圜嚯�镳铊聃钿栩 铗痂耦怅�
	 * �狍翦痦铎 桤钺疣驽龛� �镱耠�铌铐鬣龛�镳铕桉钼觇 铐�鲥腓觐�恹忸滂蝰��觐眚痤� 蒡�
	 * 镱玮铍��桤徨驵螯 戾瘀囗�.
	 * @param event
	 */
    protected void onPaint(PaintEvent event) {
        // implement autoscrolling if needed:
        if ((getStyle() & SWTX.AUTO_SCROLL) == SWTX.AUTO_SCROLL)
            updateScrollbarVisibility();

        doCalculations();

        Rectangle rect = getClientArea();
        Image image = new Image(getDisplay(), rect);
        GC gc = new GC(image);

        if (m_Model != null) {
            // be sure that clipping cuts off all the unneccessary part of a spanned cell:
            Rectangle oldClipping = setClippingContentArea(gc);
            drawCells(gc, oldClipping, m_LeftColumn, m_Model.getColumnCount(), m_TopRow, m_TopRow + m_RowsVisible);
            // content has been drawn, so set back clipping:
            setClippingTopArea(gc, oldClipping, false);
            drawCells(gc, gc.getClipping(), 0, getFixedColumnCount(), m_TopRow, m_TopRow + m_RowsVisible);
            setClippingLeftArea(gc, oldClipping, false);
            drawCells(gc, gc.getClipping(), m_LeftColumn, m_Model.getColumnCount(), 0, getFixedRowCount());
            gc.setClipping(oldClipping);
            drawCells(gc, gc.getClipping() , 0, getFixedColumnCount(), 0, getFixedRowCount());

            bottomSpaceStrategy.draw(gc);
        } else {
            gc.fillRectangle(rect);
        }

        gc.dispose();

        event.gc.drawImage(image, 0, 0);
        image.dispose();
    }

    private void setClippingTopArea(GC gc, Rectangle oldClipping, boolean headerOnly) {
        Rectangle contentClip = getClientArea();

        /*contentClip.x = 1;
        contentClip.y = 1;
        contentClip.width -= 1;
        contentClip.height -= 1;*/

        int count = headerOnly ? m_Model.getFixedHeaderRowCount() : getFixedRowCount();
        for (int i = 0; i < count; i++) {
            int height = getCellRectIgnoreSpan(0, i).height;
            contentClip.y += height + 1;
            contentClip.height -= height + 1;
        }

        contentClip.intersect(oldClipping);
        gc.setClipping(contentClip);
    }

    private void setClippingLeftArea(GC gc, Rectangle oldClipping, boolean headerOnly) {
        Rectangle contentClip = getClientArea();

        /*contentClip.x = 1;
        contentClip.y = 1;
        contentClip.width -= 1;
        contentClip.height -= 1;*/

        int count = headerOnly ? m_Model.getFixedHeaderColumnCount() : getFixedColumnCount();
        for (int i = 0; i < count; i++) {
            int width = getCellRectIgnoreSpan(i, 0).width;
            contentClip.x += width + 1;
            contentClip.width -= width + 1;
        }

        contentClip.intersect(oldClipping);
        gc.setClipping(contentClip);
    }

    /**
     * This sets the clipping area of the GC to the content 
     * area of the table, ignoring all header cells. This
     * has the result that fixed cells are not drawn by this gc!
     * @param gc The gc to manipulate.
     * @return The old clipping area. It you want to paint fixed cells
     * with this GC, re-set this with gc.setClipping();
     */
    private Rectangle setClippingContentArea(GC gc) {
        Rectangle oldClipping = gc.getClipping();
        Rectangle contentClip = getClientArea();

        /*contentClip.x = 1;
        contentClip.y = 1;
        contentClip.width -= 1;
        contentClip.height -= 1;*/

        for (int i = 0; i < getFixedColumnCount(); i++) {
            int width = getCellRectIgnoreSpan(i, 0).width;
            contentClip.x += width + 1;
            contentClip.width -= width + 1;
        }

        for (int i = 0; i < getFixedRowCount(); i++) {
            int height = getCellRectIgnoreSpan(0, i).height;
            contentClip.y += height + 1;
            contentClip.height -= height + 1;
        }

        contentClip.intersect(oldClipping);
        gc.setClipping(contentClip);
        return oldClipping;
    }

    // Cells
    /**
     * Redraws the the cells only in the given area.
     * 
     * @param cellsToRedraw
     *            Defines the area to redraw. The rectangles elements are not
     *            pixels but cell numbers.
     */
    public void redraw(Rectangle cellsToRedraw) {
        checkWidget();
        redraw(cellsToRedraw.x, cellsToRedraw.y, cellsToRedraw.width, cellsToRedraw.height);
    }

    /**
     * Redraws the the cells only in the given area.
     * 
     * @param firstCol the first column to draw. 
     * @param firstRow the first row to draw. (Map if you use a sorted model!)
     * @param numOfCols the number of columns to draw.
     * @param numOfRows the number of rows to draw.
     */
    public void redraw(int firstCol, int firstRow, int numOfCols, int numOfRows) {
        checkWidget();
        boolean redrawFixedRows = false;
        if (firstRow < getFixedRowCount()) {
            firstRow = m_TopRow;
            redrawFixedRows = true;
        }
        boolean redrawFixedCols = false;
        if (firstCol < getFixedColumnCount()) {
            firstCol = m_LeftColumn;
            redrawFixedCols = true;
        }

        Rectangle clipRect = getClientArea();
        if (clipRect.width == 0 || clipRect.height == 0) {
        	return;
        }
        Image image = new Image(getDisplay(), clipRect);
        GC cgc = new GC(this);
        cgc.copyArea(image, clipRect.x, clipRect.y);
        GC gc = new GC(image);

        Rectangle oldClip = setClippingContentArea(gc);
        drawCells(
                gc, clipRect,
                Math.max(firstCol, m_LeftColumn),
                Math.min(firstCol + numOfCols, m_Model.getColumnCount()),
                Math.max(firstRow, m_TopRow),
                Math.min(firstRow + numOfRows, m_Model.getRowCount()));
        gc.setClipping(oldClip);

        if (redrawFixedRows) {
            setClippingLeftArea(gc, oldClip, false);
            drawCells(
                    gc, gc.getClipping(), m_LeftColumn,
                    m_Model.getColumnCount(), 0, getFixedRowCount());
        }
        if (redrawFixedCols) {
            setClippingTopArea(gc, oldClip, false);
            drawCells(
                    gc, gc.getClipping(), 0, getFixedColumnCount(),
                    m_TopRow, m_TopRow + m_RowsVisible);
        }
        if (redrawFixedCols || redrawFixedRows) {
            gc.setClipping(oldClip);
            drawCells(
                    gc, gc.getClipping() , 0, getFixedColumnCount(),
                    0, getFixedRowCount());
            bottomSpaceStrategy.draw(gc);
        }

        gc.dispose();
        cgc.drawImage(image, clipRect.x, clipRect.y);
        image.dispose();
        cgc.dispose();
    }

    /**
     * 蔓镱腠��铗痂耦怅�箨噻囗眍泐 犭铌��邋�<br/>
     * <i>马桁囗桢!</i> 体蝾�镱漯噻箪邂噱� 黩�fromCol �fromRow 磬躅�蝰��珙礤 忤滂祛耱�
     * @param gc
     * @param clipRect
     * @param fromCol
     * @param toCol
     * @param fromRow
     * @param toRow
     */
    protected void drawCells(GC gc, Rectangle clipRect, int fromCol, int toCol, int fromRow, int toRow) {
        Rectangle r;

        if (m_CellEditor != null) {
            if (!isCellVisible(m_CellEditor.m_Col, m_CellEditor.m_Row)) {
                Rectangle hide = new Rectangle(-101, -101, 100, 100);
                m_CellEditor.setCellRect(hide);
            } else {
                m_CellEditor.setCellRect(getCellRect(m_CellEditor.m_Col, m_CellEditor.m_Row));
            }
        }

        int fromCol_X = getCellRectIgnoreSpan(fromCol, fromRow).x;
        for (int row = fromRow; row < toRow; row++) {
            r = getCellRectIgnoreSpan(fromCol, row, fromCol_X);

            // 茹眍痂痼屐 耱痤觇, 恹躅��玎 镳邃咫�忤滂祛耱�
            if (r.y > clipRect.y + clipRect.height) break;

            // The right cell border is cached to avoid the expensive col loop.
            int right_border_x = r.x;
            for (int col = fromCol; col < toCol; col++) {
                r = getCellRect(col, row, right_border_x);

                if (r.x > clipRect.x + clipRect.width)  break;
                if (r.y > clipRect.y + clipRect.height) return;

                right_border_x += getColumnWidth(col);

				// check if it is an overlapped cell
				Point valid = getValidCell(col, row);
				if (valid.x != col || valid.y != row) {
					// is this a partially visible merged cell? then we still have to draw it!
					if (    (valid.x < col && col - 1 < m_LeftColumn && col > getFixedColumnCount()) ||
					        (valid.y < row && row - 1 < m_TopRow     && row > getFixedRowCount())) {
						drawCell(gc, valid.x, valid.y);
					}
					// merged cell that is not partially visible -> don't draw
                    continue;
				}

                // perform real work:
                if (canDrawCell(r, clipRect)) drawCell(gc, col, row, r);
            }
        }
    }

    /**
     * Looks into the model to determine if the given cell is overlapped by 
     * a cell that spans several columns/rows. In that case the index of the
     * cell responsible for the content (in the left upper corner) is returned.
     * Otherwise the given cell is returned.
     * 
     * @param colToCheck The column index of the cell to check. 
     * @param rowToCheck The row index of the cell to check. 
     *      (as seen by the KTable. Map if you use a sorted model!)
     * @return returns the cell that overlaps the given cell, or the given
     *         cell if no cell overlaps it.
     * @throws IllegalArgumentException If the model returns cells on <code>
     * Model.belongsToCell()</code> that are on the right or below the given cell.
     * @see KTableSortedModel#mapToTable(int);
     */
    public Point getValidCell(int colToCheck, int rowToCheck) {
        checkWidget();
        Point found = new Point(colToCheck, rowToCheck);

        // check input parameters
        if (colToCheck >= m_Model.getColumnCount() || rowToCheck >= m_Model.getRowCount() || 
      		  colToCheck < 0 || rowToCheck < 0) {
        	return found;
        }

        Point lastFound = null;
        while (!found.equals(lastFound)) {
            lastFound = found;
            found = m_Model.belongsToCell(found.x, found.y);
            if (found != null && (found.x > lastFound.x || found.y > lastFound.y)) 
                throw new IllegalArgumentException("When spanning over several cells, " +
                		"supercells that determine the content of the large cell must " +
                		"always be in the left upper corner!");
            if (found == null) return lastFound;
        }
        return found;
    }

    /**
     * 橡铊玮钿栩 镱桉�怦艴 �邋� 觐蝾瘥�镥疱牮帼蝰��桉耠邃箦祛��彘觐�<br>
     * 篷腓 �彘赅 礤 镥疱箴噱蝰�龛 �钿眍�漯筱铋 �彘觐� 蝾 忸玮疣弪� NULL.
     * @param col
     * @param row
     * @throws IllegalArgumentException 羼腓 镥疱溧睇 礤耋耱怏桢 觐铕滂磬螓 �彘觇
     */
    public Rectangle getOverlappedArea (int col, int row) {
        checkWidget();
        // check input parameters
        if (col >= m_Model.getColumnCount() || row >= m_Model.getRowCount() || 
        		col < 0 || row < 0) {
        	throw new IllegalArgumentException();
        }
        
        int x, y;
        Point pHome = m_Model.belongsToCell(col, row);
        if (pHome != null) {
        	x = pHome.x;
        	y = pHome.y;
        	Point step = m_Model.belongsToCell(++x, y);
        	while (step != null && step.equals(pHome) && x+1 < m_Model.getColumnCount()) {
        		step = m_Model.belongsToCell(++x, y);
        	}
        	--x;
        	step = m_Model.belongsToCell(x, ++y);
        	while (step != null && step.equals(pHome) && y+1 < m_Model.getRowCount()) {
        		step = m_Model.belongsToCell(x, ++y);
        	}
        	--y;
        	if (x != pHome.x || y != pHome.y) {
        		return new Rectangle(pHome.x, pHome.y, x - pHome.x, y - pHome.y);
        	}
        }
        return null;
    }

    /**
     * Call when a manual redraw on a cell should be performed.
     * In case headers should be updated to reflect a focus change, this is performed.
     * @param gc
     * @param col
     * @param row
     */
    protected void drawCell(GC gc, int col, int row) {
        drawCell(gc, col, row, getCellRectForDrawing(col, row));

        if ((getStyle() & SWTX.MARK_FOCUS_HEADERS) == SWTX.MARK_FOCUS_HEADERS) {
            int headerRowCount = m_Model.getFixedHeaderRowCount();
            if (row >= headerRowCount) {
                Rectangle oldClip = gc.getClipping();
                Rectangle clientClip = getClientArea();

                setClippingTopArea(gc, clientClip, row < m_TopRow);
                Point pred = null;
                for (int i = 0; i < m_Model.getFixedHeaderColumnCount(); i++) {
                    Point valid = getValidCell(i, row);
                    if (!valid.equals(pred)) {
                        pred = valid;
                        drawCell(gc, valid.x, valid.y, getCellRectForDrawing(valid.x, valid.y));
                    }
                }
                if (row >= m_TopRow) gc.setClipping(oldClip);

                setClippingLeftArea(gc, clientClip, col < m_LeftColumn);
                pred = null;
                for (int i = 0; i < headerRowCount; i++) {
                    Point valid = getValidCell(col, i);
                    if (!valid.equals(pred)) {
                        pred = valid;
                        drawCell(gc, valid.x, valid.y, getCellRectForDrawing(valid.x, valid.y));
                    }
                }

                gc.setClipping(oldClip);
            }
        }
    }

    /**
     * 体蝾�铗忮鬣弪 玎 镳铕桉钼牦 箨噻囗眍��彘觇
     * @param gc 灭圄梓羼觇�觐眚尻耱, �篑蜞眍怆屙睇�觌栾镨磴铎
     * @param col 暑腩黻�
     * @param row 羊痤赅
     * @param rect 橡�铙泐朦龛� 玎溧栝 钺豚耱� 觐蝾痼�玎龛爨弪 铗痂耦恹忄屐� �彘赅
     */
    protected void drawCell(GC gc, int col, int row, Rectangle rect) {
        if ((row < 0) || (row >= m_Model.getRowCount())) return;
        if (rect.width <= 0 || rect.height <= 0) return;

        // set up clipping so that the renderer is only
        // allowed to paint in his area:
        Rectangle oldClip = gc.getClipping();
        Rectangle newClip = new Rectangle(rect.x, rect.y, rect.width + 1, rect.height + 1);
        newClip.intersect(oldClip);
        gc.setClipping(newClip);

        Point validClickedCell = getValidCell(m_ClickColumnIndex, m_ClickRowIndex);

        m_Model.getCellRenderer(col, row).drawCell(
        		gc,
        		rect,
        		col,
        		row,
        		m_Model.getContentAt(col, row),
        		showAsSelected(col, row) || highlightSelectedRowCol(col, row),
        		isHeaderCell(col, row),
        		col == validClickedCell.x && row == validClickedCell.y,
        		m_Model);

        gc.setClipping(oldClip);
    }

    /**
     * Interface method to update the content of a cell.<p>
     * Don't forget to map the row index if a sorted model is used.
     * @param col The column index
     * @param row The row index.
     * @see KTableSortedModel#mapRowIndexToTable(int)
     */
    public void updateCell(int col, int row) {
        checkWidget();
        if ((row < 0) || (row >= m_Model.getRowCount()) || 
            (col < 0) || (col >= m_Model.getColumnCount()))
            return;

        // be sure it is a valid cell if cells span 
        Point valid = getValidCell(col, row);
        // update it:
        GC gc = new GC(this);
        drawCell(gc, valid.x, valid.y);
        gc.dispose();
    }

    /**
     * @param col The column index
     * @param row The row index
     * @return Returns true if the given cell is a fixed cell,
     * that is a header cell. Returns false otherwise.
     */
    public boolean isFixedCell(int col, int row) {
        return col < getFixedColumnCount()
        || row < getFixedRowCount();
    }
    
    /**
     * @param col The column index
     * @param row The row index
     * @return Returns true if the given cell is within the region specified by the model 
     * using the methods <code>getFixedHeaderColumnCount()</code> and <code>getFixedHeaderRowCount()</code>
     */
    public boolean isHeaderCell(int col, int row) {
        return col < m_Model.getFixedHeaderColumnCount() 
        || row < m_Model.getFixedHeaderRowCount();
    }
    
    protected boolean showAsSelected(int col, int row) {
        // A cell with an open editor should be drawn without focus
        if (m_CellEditor != null) {
            if (col == m_CellEditor.m_Col && row == m_CellEditor.m_Row 
            		&& m_CellEditor.getControl() != null)
                return false;
        }
        return isCellSelected(col, row) && (isFocusControl() || isShowSelectionWithoutFocus());
    }

    protected void drawRow(GC gc, int row) {
        drawCells(gc, getClientArea(), 0, getFixedColumnCount(), row,
                row + 1);
        drawCells(gc, getClientArea(), m_LeftColumn, m_Model.getColumnCount(),
                row, row + 1);
    }

    protected void drawCol(GC gc, int col) {
        if (col<getFixedColumnCount()) {
            drawCells(gc, getClientArea(), col, col+1, 0, m_TopRow + m_RowsVisible);
        } else {
            drawCells(gc, getClientArea(), col, col + 1, 0, getFixedRowCount());
            Rectangle oldClip = setClippingContentArea(gc);
            drawCells(gc, gc.getClipping(), col, col + 1, m_TopRow, m_TopRow + m_RowsVisible);
            gc.setClipping(oldClip);    
        }
    }

    /**
     * Sets the default cursor to the given cursor. This instance is saved
     * internally and displayed whenever no linecursor or resizecursor is shown.
     * <p>
     * The difference to setCursor is that this cursor will be preserved over
     * action cursor changes.
     * 
     * @param cursor
     *            The cursor to use, or <code>null</code> if the OS default
     *            cursor should be used.
     * @param size_below_hotspot The number of pixels that are needed to paint the 
     * 	      cursor below and right of the cursor hotspot (that is the actual location the cursor
     *        is pointing to).<p>
     *        NOTE that this is just there to allow better positioning of tooltips.
     * 	      Currently SWT does not provide an API to get the size of the cursor. So
     *        these values are taken to calculate the position of the tooltip. The
     *        the tooltip is placed pt.x pixels left and pt.y pixels below the mouse location.<br>
     *        If you don't know the size of the cursor (for example you use a default one), set 
     *        <code>null</code> or <code>new Point(-1, -1)</code>. 
     */
    public void setDefaultCursor(Cursor cursor, Point size_below_hotspot) {
        checkWidget();
        if (m_defaultCursor != null)
            m_defaultCursor.dispose();
        m_defaultCursor = cursor;
        m_defaultCursorSize = size_below_hotspot;
        setCursor(cursor);
    }
    
    public void setDefaultRowResizeCursor(Cursor cursor) {
    	
    	checkWidget();
    	
    	if ( m_defaultRowResizeCursor != null ) {
    		m_defaultRowResizeCursor.dispose();
    	}
    	
    	m_defaultRowResizeCursor = cursor;
    	
    }
    
    public void setDefaultColumnResizeCursor(Cursor cursor) {
    	
    	checkWidget();
    	
    	if ( m_defaultColumnResizeCursor != null ) {
    		m_defaultColumnResizeCursor.dispose();
    	}
    	
    	m_defaultColumnResizeCursor = cursor;

    }

    //////////////////////////////////////////////////////////////////////////////
    // REACTIONS ON USER ACTIONS
    //////////////////////////////////////////////////////////////////////////////

    private int getHeaderHeight() {
    	int height = 1;
    	for (int i=0; i<m_Model.getFixedHeaderRowCount(); i++)
    		height+=m_Model.getRowHeight(i);
    	return height;
    }

    /* returns the index of a model column */
    protected int getColumnForResize(int x, int y) {

    	if (m_Model == null || y <= 0 || y >= getHeaderHeight() ) {
            return -1;
    	}

        if ( x < getFixedWidth() + m_ResizeAreaSize / 2 ) {
        	
            for (int i = 0; i < getFixedColumnCount(); i++)
            	
                if (Math.abs(x - getColumnRight(i)) < m_ResizeAreaSize / 2) {
                    
                	if (m_Model.isColumnResizable(i)) {
                        return i;
                    }
                    
                    return -1;
                    
                }
            
        }

        int left = getColumnLeft(m_LeftColumn);
        // -1
        for (int i = m_LeftColumn; i < (m_LeftColumn + m_ColumnsVisible + 1) && i<m_Model.getColumnCount(); i++) {
        	
            if ( i >= m_Model.getColumnCount() ) {
            	return -1;
            }
            
            int right = left + getColumnWidth(i);
            
            if ( Math.abs(x - right) < m_ResizeAreaSize / 2 ) {
            	
                if (m_Model.isColumnResizable(i)) {
                    return i;
                }
                
                return -1;
                
            }
            
            if ( (x >= left + m_ResizeAreaSize / 2) &&
                 (x <= right - m_ResizeAreaSize / 2) ) {
                break;
            }
            
            left=right;
        }
        
        return -1;
        
    }

    /* returns the number of a row in the view (!) */
    protected int getRowForResize(int x, int y) {
        
    	if ( m_Model == null || x <= 0 || x >= getHeaderWidth() ) {
            return -1;
        }

       if ( y < m_Model.getRowHeight(0) + m_ResizeAreaSize / 2 ) {
    	   
           // allow first row height resize?
    	   
           	if ( Math.abs(m_Model.getRowHeight(0) - y) < m_ResizeAreaSize / 2 &&
           	     m_Model.isRowResizable(0) ) {
           		
           	    return 0;
           	    
           	}
           	
            return -1;
            
       }

        int row        = getRowForY(y);
        int row_y      = getYforRow(row);
        int next_row_y = getYforRow(row+1);

        if ( ( Math.abs(y - next_row_y) < m_ResizeAreaSize / 2 ) &&
        	 m_Model.isRowResizable(row) ) {
            return row;
        } else if ( ( Math.abs(y - row_y) < m_ResizeAreaSize / 2 ) &&
        		    m_Model.isRowResizable(row-1) ) {
        	return row-1;
        }
        
        return -1;
        
    }

    /**
     * Returns the number of the column that is present at position x or -1, if
     * out of area. When cells are spanned, returns the address of the spanned cell.
     * @param x the x location of an event in table coordinates
     * @param y The y location of an event in table coordinates
     * @deprecated Use {@link #getCellForCoordinates(int, int) instead.
     * @return Returns the point where x is the column index, y is the row index.
     */
    public Point calcColumnNum(int x, int y) {
        if (m_Model == null)
            return new Point(-1, -1);
        
        Point toFind = new Point(x, y);
        Point valid = getValidCell(m_LeftColumn, m_TopRow);
        
        // at first iterate over the fixed columns, that are always shown:
        Point found = checkIfMatchesInColumns(0, getFixedRowCount(), 
                0, getFixedColumnCount(), toFind, true);
        if (found!=null) return found;
        found = checkIfMatchesInColumns(valid.y, m_TopRow + m_RowsVisible, 
                0, getFixedColumnCount(), toFind, true);
        if (found!=null) return found;

        found = checkIfMatchesInColumns(0, getFixedRowCount(), 
                valid.x, m_LeftColumn+m_ColumnsVisible, toFind, true);
        if (found!=null) return found;
        found = checkIfMatchesInColumns(valid.y, m_TopRow + m_RowsVisible, 
                valid.x, m_LeftColumn+m_ColumnsVisible, toFind, true);
        if (found!=null) return found;
        
        return new Point(-1, -1);
        
    }

    protected Point calcNonSpanColumnNum(int x, int y) {
        if (m_Model == null) return new Point(-1, -1);

        Point toFind = new Point(x, y);
        Point valid = new Point(m_LeftColumn, m_TopRow);

        // at first iterate over the fixed columns, that are always shown:
        Point found = checkIfMatchesInColumns(0, getFixedRowCount(), 
                0, getFixedColumnCount(), toFind, false);
        if (found!=null) return found;
        found = checkIfMatchesInColumns(valid.y, m_TopRow + m_RowsVisible, 
                0, getFixedColumnCount(), toFind, false);
        if (found!=null) return found;

        found = checkIfMatchesInColumns(0, getFixedRowCount(), 
                valid.x, m_LeftColumn+m_ColumnsVisible, toFind, false);
        if (found!=null) return found;
        found = checkIfMatchesInColumns(valid.y, m_TopRow + m_RowsVisible, 
                valid.x, m_LeftColumn+m_ColumnsVisible, toFind, false);
        if (found!=null) return found;
        
        return new Point(-1, -1);
    }

    /**
     * Checks for the event location in table coordinates within the region covered
     * by the columns beginning by startCol and ending by endCol.
     * @param span Set to true if for spanning cells we just want to have the left-upper-most cell.
     */
    protected Point checkIfMatchesInColumns(int startRow, int endRow, int startCol, int endCol, Point toFind, boolean span) {
        
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                
                Rectangle rect = getCellRectIgnoreSpan(col, row);
                // take into account the 1px right and bottom border
                rect.width+=1;
                rect.height+=1;
                if (rect.contains(toFind))
                    if (span)
                    // take into account the spanning when reporting a match:
                        return getValidCell(col, row);
                    else
                        return new Point(col, row);
            }
        }
        return null;
    }

    public boolean isCellVisible(int col, int row) {
        checkWidget();
        if (m_Model == null)
            return false;
        return ((col >= m_LeftColumn && col < m_LeftColumn + m_ColumnsVisible) || col < getFixedColumnCount())
                && ((row >= m_TopRow && row < m_TopRow + m_RowsVisible) || row < getFixedRowCount());
    }

    public boolean isCellFullyVisible(int col, int row) {
        checkWidget();
        if (m_Model == null) return false;
        return ((col >= m_LeftColumn && col < m_LeftColumn + m_ColumnsFullyVisible) || col < getFixedColumnCount())
				&& ((row >= m_TopRow && row < m_TopRow + m_RowsFullyVisible) || row < getFixedRowCount());
    }

    /**
     * @param row The row index AS SEEN BY KTABLE. If you use a sorted model, don't forget
     * to map the index.
     * @return Returns true if the row is visible.
     */
    public boolean isRowVisible(int row) {
        checkWidget();
        if (m_Model == null)
            return false;
        return ((row >= m_TopRow && row < m_TopRow + m_RowsVisible) || 
                row < (getFixedRowCount()));

    }

    /**
     * @param row the row index to check - as seen by ktable. If you use a 
     * sorted model, don't forget to map.
     * @return Returns true if the row is fully visible.
     */
    public boolean isRowFullyVisible(int row) {
        checkWidget();
        if (m_Model == null)
            return false;
        return ((row >= m_TopRow && row < m_TopRow + m_RowsFullyVisible) 
                || row <getFixedRowCount());
    }

    public void focusCellPub(int col, int row, int stateMask) {
        focusCell(col, row, stateMask);
    }
    /**
     * Focusses the given Cell. Assumes that the given cell is in the viewable
     * area. Does all neccessary redraws.
     */
    protected void focusCell(int col, int row, int stateMask) {
        // assure it is a valid cell:
        Point orig = new Point(col, row);
        Point valid = getValidCell(col, row);
        if (valid != null) {
            col = valid.x;
            row = valid.y;
        }

        GC gc = new GC(this);

        // close cell editor if active
        closeCellEditor();

        /* Special rule: in row selection mode the selection
		 * if a fixed cell in a non-fixed row is allowed and
		 * handled as a selection of a non-fixed cell.
		 */
        if (row >= m_Model.getFixedHeaderRowCount() && (col >= m_Model.getFixedHeaderColumnCount() || isRowSelectMode())) {
            if ((stateMask & SWT.CTRL) == 0 && (stateMask & SWT.SHIFT) == 0) {
                // case: no modifier key
                boolean redrawAll = (m_Selection.size() > 1);
                int oldFocusRow = m_FocusRow;
                int oldFocusCol = m_FocusCol;

                clearSelectionWithoutRedraw();
                addToSelectionWithoutRedraw(col, row);
                m_FocusRow = row;
                m_FocusCol = col;
                m_MainFocusRow = row;
                m_MainFocusCol = col;

                // 蒡�篑腩忤�箫疱驿噱�镥疱痂耦怅��邋��耠篦�� 觐沅�怦�疣忭�
                // 镱磬漕徼蝰�镥疱痂耦怅�怦彘 蜞犭桷�
                if (!needScroll()) {
                    if (redrawAll) {
                        redraw();
                    } else if (isRowSelectMode()) {
                        // redraw previously focused row, if there was one
                        if (oldFocusRow >= 0) drawRow(gc, oldFocusRow);
                        drawRow(gc, m_FocusRow);
                    } else {
                            Rectangle origClipping = null;
                            if (!isFixedCell(oldFocusCol, oldFocusRow)) origClipping = setClippingContentArea(gc);
                            // redraw previously focused cell, if there was one
                            if (oldFocusCol >= 0 && oldFocusRow >= 0) drawCell(gc, oldFocusCol, oldFocusRow);
                            if (origClipping != null) gc.setClipping(origClipping);
    
                            if (!isFixedCell(m_FocusCol, m_FocusRow)) origClipping = setClippingContentArea(gc);
                            drawCell(gc, m_FocusCol, m_FocusRow);
                    }
                }

                // notify non-fixed cell listeners
                if (oldSelections == null) oldSelections = new Point[] {new Point(oldFocusCol, oldFocusRow)};
                fireCellSelection(orig.x, orig.y, stateMask);
            } else if ((stateMask & SWT.CTRL) != 0) {
                // [+] case: CTRL key pressed
                boolean success = toggleSelection(col, row);  
                if (success) {
                    m_FocusCol = col;
                    m_FocusRow = row;
                }

                if (isRowSelectMode()) {
                    drawRow(gc, row);
                } else {
                    drawCell(gc, col, row);
                }

                // notify non-fixed cell listeners
                if (success) fireCellSelection(m_FocusCol, m_FocusRow, stateMask);
                // [.]
            } else if ((stateMask & SWT.SHIFT) != 0) {
                // [+] Ignore when not a multi-selection table.
                if (!isMultiSelectMode()) {
                    if (isRowSelectMode()) {
                        drawRow(gc, row);
                    } else {
                        drawCell(gc, col, row);
                    }
                    return;
                }
                // [.]
                // [+] case: SHIFT key pressed
                if (isRowSelectMode()) {
                	HashMap<Object, Object> oldSelection = new HashMap<Object, Object>(m_Selection);
                    if (row < m_FocusRow) {
                        // backword selection
                        while (row != m_FocusRow)
                            addToSelectionWithoutRedraw(0, --m_FocusRow);
                    } else {
                        // foreward selection
                        while (row != m_FocusRow)
                            addToSelectionWithoutRedraw(0, ++m_FocusRow);
                    }
                    if (!oldSelection.equals(m_Selection)) {
                    	oldSelection.putAll(m_Selection);
                    	Iterator<Object> rowIt = oldSelection.values().iterator();
                    	int min=0, max=0;
                        if (rowIt.hasNext()) {
                            min = ((Integer) rowIt.next()).intValue();
                            max = min;
                        }
                        while (rowIt.hasNext()) {
                            int r = ((Integer) rowIt.next()).intValue();
                            if (r < min) min = r;
                            if (r > max) max = r;
                        }
                        redraw(0, min, m_Model.getColumnCount(), max - min + 1);

                    	// notify non-fixed cell listeners
                        fireCellSelection(orig.x, orig.y, stateMask);
                    }
                } else {// cell selection mode

                	Point[] sel = getCellSelection();
                	Point min= new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
                	Point max = new Point(-1,-1);
                	boolean containsCell = false;
                	for (int i=0; i<sel.length; i++) {
                		if (sel[i].x>max.x) max.x=sel[i].x;
                		if (sel[i].y>max.y) max.y=sel[i].y;
                		if (sel[i].x<min.x) min.x=sel[i].x;
                		if (sel[i].y<min.y) min.y=sel[i].y;
                		if (!containsCell && sel[i].x==col && sel[i].y==row)
                			containsCell = true;
                	}
                	
                	if (col<m_MainFocusCol && max.x>m_MainFocusCol) {
        				min.x=col; max.x=m_MainFocusCol;
        			} else if (col>m_MainFocusCol && min.x<m_MainFocusCol) {
        				min.x=m_MainFocusCol; max.x=col;
        			}
        			if (row<m_MainFocusRow && max.y>m_MainFocusRow) {
        				min.y=row; max.y=m_MainFocusRow;
        			} else if (row>m_MainFocusRow && min.y<m_MainFocusRow) {
        				min.y=m_MainFocusRow; max.y=row;
        			}
        			
                	HashMap<Object, Object> oldSelection = new HashMap<Object, Object>(m_Selection);
            		if (containsCell) {
            			clearSelectionWithoutRedraw();
            			                        			
            			if (max.x==m_FocusCol) max.x=col;
            			if (max.y==m_FocusRow) max.y=row;
            			if (min.x==m_FocusCol) min.x=col;
            			if (min.y==m_FocusRow) min.y=row;
            			
            			// set selection:
            			for (int r=min.y; r<=max.y; r++)
            				for (int c=min.x; c<=max.x; c++)
            					addToSelectionWithoutRedraw(c, r);
            			if (!oldSelection.equals(m_Selection)) {
            				redraw();
            				// notify non-fixed cell listeners
            				fireCellSelection(orig.x, orig.y, stateMask);
            			}
            		} else {           			
            			
            			if (col>max.x) max.x=col;
                		if (row>max.y) max.y=row;
                		if (col<min.x) min.x=col;
                		if (row<min.y) min.y=row;
            		

            			for (int r=min.y; r<=max.y; r++) {
            				
            				for (int c=min.x; c<=max.x; c++) {
            					addToSelectionWithoutRedraw(c, r);
            				}
            				
            			}

            			// If this is multi selection mode and the highlight
            			// selection header style bit is set then we want
            			// to force a redraw of the fixed rows and columns.
            			// This makes sure that the highlighting of those
            			// fixed rows and columns happens.
            			
            			if ( isMultiSelectMode() && isHighlightSelectionInHeader() ) {
            				redraw(-1, row, 1, 1);
            				redraw(col, -1, 1, 1);
            			}
            			
            			if (!oldSelection.equals(m_Selection)) {
            				redraw(min.x, min.y, max.x-min.x+1, max.y-min.y+1);
            				// notify non-fixed cell listeners
            				fireCellSelection(orig.x, orig.y, stateMask);
            			}
            		}
            	
            		 m_FocusRow = row;
                     m_FocusCol = col;
                     
                }
                // [.]
            }
        } else {
            // a fixed cell was focused
            drawCell(gc, col, row);
            // notify fixed cell listeners
            fireFixedCellSelection(orig.x, orig.y, stateMask);
        }

        gc.dispose();
    }

    /**
     * Closes the cell editor, if one is open.
     */
	public void closeCellEditor() {
		if (m_CellEditor != null)
			m_CellEditor.close(true);
	}

    protected void onMouseDown(MouseEvent e) {
        if (e.button == 1) {
            setCapture(true);
            m_Capture = true;

            // Resize column?
            int columnIndex = getColumnForResize(e.x, e.y);
            if (columnIndex >= 0) {
                m_ResizeColumnIndex = columnIndex;
                m_ResizeColumnLeft = getColumnLeft(columnIndex);
                m_NewColumnSize = m_Model.getColumnWidth(columnIndex);
                return;
            }

            // Resize row?
            int rowIndex = getRowForResize(e.x, e.y);
            if (rowIndex >= 0) {
                m_ResizeRowIndex = rowIndex;
                m_ResizeRowTop = getYforRow(rowIndex);
                m_NewRowSize = m_Model.getRowHeight(rowIndex);
                return;
            }
        }

        if (e.button == 1 || isMultiSelectMode() && !clickInSelectedCells(new Point(e.x, e.y))) {
            //focus change
            Point cell = calcNonSpanColumnNum(e.x, e.y);
            if (cell.x == -1 || cell.y == -1) {
            	closeCellEditor();
                return;
            }

//            if (isRowSelectMode()) cell.x = m_FocusCol;
            m_ClickRowIndex = cell.y;
            m_ClickColumnIndex = cell.x;

            focusCell(cell.x, cell.y, e.stateMask);
            if (needScroll()) scrollToFocus(true);
        }

        if (e.button == 3) {
            if (isRowSelectMode()) {
                Point cell = getCellForCoordinates(e.x, e.y);
                int[] arrayRows = getRowSelection();
                if (arrayRows != null && arrayRows.length == 1) {
                    int y = arrayRows[0];
                    if (cell.y != y) {
                        focusCell(cell.x, cell.y, e.stateMask);
                    }
                }
            } else {
                Point cell = getCellForCoordinates(e.x, e.y);
                Point sel = getCellSelection()[0];
                if (sel != null && !sel.equals(cell)) {
                    focusCell(cell.x, cell.y, e.stateMask);
                    if (needScroll()) scrollToFocus();
                }
            }
        }
    }

    protected boolean clickInSelectedCells(Point click) {
        Point[] selection = getCellSelection();
        if (selection==null || selection.length<0) return false;
        for (int i=0; i<selection.length; i++){
            if (getCellRect(selection[i].x, selection[i].y).contains(click))
                return true;
        }
        return false;
    }

    protected void onMouseMove(MouseEvent e) { 
        if (m_Model == null) return;

        // show resize cursor?
        checkCursorToDisplay(e.x, e.y);

        if (e.stateMask == SWT.BUTTON1 && m_CellEditor==null) {            
            // extend selection?
            if (m_ClickColumnIndex != -1 && isMultiSelectMode()) {
                Point cell = calcNonSpanColumnNum(e.x, e.y);
                if (cell.x==-1 || cell.y == -1)
                    return;
                if (    cell.y >= m_Model.getFixedHeaderRowCount() &&
                        cell.x >= m_Model.getFixedHeaderColumnCount()) {

                	if (cell.x != m_ClickColumnIndex || cell.y != m_ClickRowIndex) {
                		m_ClickColumnIndex = cell.x;
                		m_ClickRowIndex = cell.y;
                    
                		focusCell(cell.x, cell.y, (e.stateMask | SWT.SHIFT));
                	}
                }
            }
        }
        // column resize?
        if (m_ResizeColumnIndex != -1) {
        	
            Rectangle rect = getClientArea();

            if (e.x > rect.x + rect.width - 1) {
                e.x = rect.x + rect.width - 1;
            }
            
            int newSize = e.x - m_ResizeColumnLeft;
            
            if (newSize < 5) {
                newSize = 5;
            }
            
            m_NewColumnSize = newSize;
            
            GC gc = new GC(this);
            
            // restore old line area
            if (m_LineRestore != null) {
                gc.drawImage(m_LineRestore, m_LineX, m_LineY);
            }
            
            // safe old picture and draw line
            gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));

            int lineEnd = getRowBottom(m_TopRow + m_RowsVisible - 1);
  
            m_LineRestore = new Image(m_Display, 1, lineEnd);
            m_LineY = rect.y + 1;
            m_LineX = m_ResizeColumnLeft + m_NewColumnSize - 1;
            
            gc.copyArea(m_LineRestore, m_LineX, m_LineY);
            gc.drawLine(m_LineX, m_LineY, m_LineX, lineEnd);
            gc.dispose();
        }

        // row resize?
        if (m_ResizeRowIndex != -1) {
        	
            Rectangle rect = getClientArea();
            GC gc = new GC(this);
            
            // calculate new size
            if (e.y > rect.y + rect.height - 1)
                e.y = rect.y + rect.height - 1;
            m_NewRowSize = e.y - m_ResizeRowTop;
            if (m_NewRowSize < m_Model.getRowHeightMinimum())
                m_NewRowSize = m_Model.getRowHeightMinimum();
            
            // restore old line area
            if (m_LineRestore != null) {
                gc.drawImage(m_LineRestore, m_LineX, m_LineY);
            }
            
            // safe old picture and draw line
            gc.setForeground(Display.getCurrent().getSystemColor(
                    SWT.COLOR_BLACK));
            int lineEnd = getColumnRight(m_LeftColumn + m_ColumnsVisible - 1);
            m_LineRestore = new Image(m_Display, lineEnd, 1);
            m_LineX = rect.x + 1;
            m_LineY = m_ResizeRowTop + m_NewRowSize - 1;
            gc.copyArea(m_LineRestore, m_LineX, m_LineY);
            gc.drawLine(m_LineX, m_LineY, rect.x + lineEnd, m_LineY);
            gc.dispose();
        }
    }
    
    private void checkCursorToDisplay(int x, int y) {
		if ((m_ResizeColumnIndex != -1) || (getColumnForResize(x, y) >= 0)) {

			if (m_defaultColumnResizeCursor == null) {
				setCursor(new Cursor(m_Display, SWT.CURSOR_SIZEWE));
			} else {
				setCursor(m_defaultColumnResizeCursor);
			}

		} else if ((m_ResizeRowIndex != -1) || (getRowForResize(x, y) >= 0)) {

			if (m_defaultRowResizeCursor == null) {
				setCursor(new Cursor(m_Display, SWT.CURSOR_SIZENS));
			} else {
				setCursor(m_defaultRowResizeCursor);
			}

		} else if (m_CursorProvider != null) { // check cursor provider
			Point cell = getCellForCoordinates(x, y);
			Cursor cursor = m_CursorProvider.getCursor(cell.x, cell.y, x, y);
			if (cursor == null) {
				setCursor(m_defaultCursor);
			} else {
				setCursor(cursor);
			}
		} else { // show default cursor:
			setCursor(m_defaultCursor);
		}
	}

    protected void onMouseUp(MouseEvent e) {
       // if (e.button == 1)
        {
            if (m_Model == null) return;

            setCapture(false);
            m_Capture = false;

            // do resize:
            
            if ( m_ResizeColumnIndex!=-1 || m_ResizeRowIndex != -1 ) {
                
            	// Do Resize Column
                if ( m_ResizeColumnIndex != -1 ) {
                	int column = m_ResizeColumnIndex;
                	m_ResizeColumnIndex = -1;

                	m_Model.setColumnWidth(column, m_NewColumnSize);
                    fireColumnResize(column, m_Model.getColumnWidth(column));
                    redraw();
                }

                // Do Resize Row
                if ( m_ResizeRowIndex != -1 ) {
                	
                	int row = m_ResizeRowIndex;
                    m_ResizeRowIndex = -1;
                    
                    m_Model.setRowHeight(row, m_NewRowSize);
                    
                    m_LineRestore = null;
                    
                    fireRowResize(row, m_NewRowSize);
                    redraw();
                    
                }

				checkCursorToDisplay(e.x, e.y);
                
            } else { // check if we have to edit:
                Point click = new Point(e.x, e.y);
                Point cell = getCellForCoordinates(e.x, e.y);
                if (cell.x >= 0 && cell.y >= 0 && cell.x < m_Model.getColumnCount() 
                		&& cell.y < m_Model.getRowCount()) {
                    if (isHeaderCell(cell.x, cell.y)) {
                        KTableCellEditor editor = m_Model.getCellEditor(cell.x, cell.y);
                        if ( editor!=null && 
                                ((editor.getActivationSignals() & KTableCellEditor.SINGLECLICK)!=0) && 
                                editor.isApplicable(KTableCellEditor.SINGLECLICK, this, cell.x, cell.y, click, null, e.stateMask)) {
                            int oldFocusCol = m_FocusCol;
                            int oldFocusRow = m_FocusRow;
                            m_FocusCol=cell.x; m_FocusRow=cell.y;
                            openEditorInFocus();
                            m_FocusCol=oldFocusCol;
                            m_FocusRow=oldFocusRow;
                        }
                    } else {
                        if (m_FocusCol >= 0 && m_FocusRow >= 0) {
                        	KTableCellEditor editor = m_Model.getCellEditor(m_FocusCol, m_FocusRow);
                            Rectangle rect = getCellRect(m_FocusCol, m_FocusRow);
                            if (editor!=null && rect.contains(click) && 
                                    (editor.getActivationSignals() & KTableCellEditor.SINGLECLICK)!=0 && 
                                    editor.isApplicable(KTableCellEditor.SINGLECLICK, this, m_FocusCol, m_FocusRow, click, null, e.stateMask)) {
                                openEditorInFocus();
                            }
                        }
                    }
                }
            }

//            if (m_ClickColumnIndex != -1) {
//                int col = m_ClickColumnIndex;
//                int row = m_ClickRowIndex;
//                m_ClickColumnIndex = -1;
//                m_ClickRowIndex = -1;
//                if (m_CellEditor == null) {
//                    GC gc = new GC(this);
//                    if (!isFixedCell(col, row) && !isRowSelectMode())
//                        setContentAreaClipping(gc);
//                    Point valid = getValidCell(col, row);
//                    drawCell(gc, valid.x, valid.y);
//                    gc.dispose();
//                }
//            }
        }
    }

    /**
     * Finds and returns the cell that is below the control coordinates.
     * @param x The x coordinate in the ktable control
     * @param y The y coordinate in the ktable control
     * @return Returns the cell under the given point, or (-1, -1).
     * @since Public since March 2006 based on a newsgroup request.
     * @throws IllegalArgumentException If there is something wrong 
     *   with the spanning values given in the model. 
     */
    public Point getCellForCoordinates(int x, int y) {
        Point cell = new Point(-1,-1);
        cell.y = getRowForY(y);
        
        int colSum = 0;
		for (int i = 0; i < getFixedColumnCount(); i++) {
			int width = m_Model.getColumnWidth(i);
			if (colSum <= x && colSum + width > x) {
				cell.x = i;
				return getValidCell(cell.x, cell.y);
			}
			colSum += width;
		}
		for (int i = m_LeftColumn; i < m_Model.getColumnCount(); i++) {
			int width = m_Model.getColumnWidth(i);
			if (colSum <= x && colSum + width > x) {
				cell.x = i;
				return getValidCell(cell.x, cell.y);
			}
			colSum += width;
		}

		// if FILL_WITH_LASTCOL is set, we must take into account that the column expands all the way to the right
		if (x >= colSum && x < getClientArea().x + getClientArea().width && (getStyle() & SWTX.FILL_WITH_LASTCOL) != 0) {
			cell.x = m_Model.getColumnCount() - 1;
		}

        return getValidCell(cell.x, cell.y);
    }

    private Point basicCell(int col, int row) {
        Point cell = m_Model.belongsToCell(col, row);
        return cell != null ? cell : new Point(col, row);
    }

    protected void onKeyDown(KeyEvent e) {
        int newFocusRow = m_FocusRow;
        int newFocusCol = m_FocusCol;

        if (m_Model == null) return;

        Point cell = basicCell(m_FocusCol, m_FocusRow);

        if ((e.character == ' ') || (e.character == '\r')) {
            KTableCellEditor editor = m_Model.getCellEditor(m_FocusCol, m_FocusRow);
            if (editor != null) {
                if ((editor.getActivationSignals() & KTableCellEditor.KEY_RETURN_AND_SPACE) != 0
                        && editor.isApplicable(KTableCellEditor.KEY_RETURN_AND_SPACE, this, m_FocusCol, m_FocusRow, null, e.character + "", e.stateMask)) {
                    openEditorInFocus();
                    if (m_CellEditor != null) {
                        m_CellEditor.setPressedChar(e.character);
                    }
                    return;
                }
            } else {
                notifyActivationListeners(m_FocusCol, m_FocusRow, e.character, e.stateMask);
            }
        }

        if (e.keyCode == SWT.HOME) {
            newFocusCol = m_Model.getFixedHeaderColumnCount();
            if (newFocusRow == -1) newFocusRow = m_Model.getFixedHeaderRowCount();
        } else if (e.keyCode == SWT.END) {
            newFocusCol = m_Model.getColumnCount() - 1;
            if (newFocusRow == -1) newFocusRow = m_Model.getFixedHeaderRowCount();
        } else if (e.keyCode == SWT.ARROW_LEFT) {
            if (!isRowSelectMode() &&
                    newFocusCol > m_Model.getFixedHeaderColumnCount()) {
                Point newPt = basicCell(cell.x - 1, cell.y);
                newFocusCol = newPt.x;
                newFocusRow = newPt.y;
            }
        } else if (e.keyCode == SWT.ARROW_RIGHT) {
            if (!isRowSelectMode()) {
                if (newFocusCol == -1) {
                    newFocusCol = m_Model.getFixedHeaderColumnCount();
                    newFocusRow = m_Model.getFixedHeaderRowCount();
                } else if (cell.x < basicCell(m_Model.getColumnCount() - 1, m_FocusRow).x) {
                    Point old = new Point(m_FocusCol, m_FocusRow);
                    newFocusCol++;
                    Point next = m_Model.belongsToCell(newFocusCol, newFocusRow);
                    if (next == null) next = new Point(newFocusCol, newFocusRow);
                    while (next.equals(old) && newFocusCol < m_Model.getColumnCount() - 1) { 
                        newFocusCol++;
                        next = m_Model.belongsToCell(newFocusCol, newFocusRow);
                        if (next == null) next = new Point(newFocusCol, newFocusRow);
                    }
                }
            }
        } else if (e.keyCode == SWT.ARROW_DOWN) {
            if (newFocusRow == -1) {
                newFocusRow = m_Model.getFixedHeaderRowCount();
                newFocusCol = m_Model.getFixedHeaderColumnCount();
            } else if (cell.y < basicCell(m_FocusCol, m_Model.getRowCount() - 1).y) {
                Point old = new Point(m_FocusCol, m_FocusRow);
                newFocusRow++;
                Point next = m_Model.belongsToCell(newFocusCol, newFocusRow);
                if (next == null) next = new Point(newFocusCol, newFocusRow);
                while (next.equals(old)) { 
                    newFocusRow++;
                    next = m_Model.belongsToCell(newFocusCol, newFocusRow);
                    if (next == null) next = new Point(newFocusCol, newFocusRow);
                }
            }
        } else if (e.keyCode == SWT.ARROW_UP) {
            if (newFocusRow > m_Model.getFixedHeaderRowCount()) {
                newFocusCol = cell.x;
                newFocusRow = cell.y - 1;
            }
        } else if (e.keyCode == SWT.PAGE_DOWN) {
            if (cell.y < basicCell(m_FocusCol, m_Model.getRowCount() - 1).y) {
                newFocusRow += m_RowsVisible - 1;
                if (newFocusRow >= m_Model.getRowCount())
                    newFocusRow = m_Model.getRowCount() - 1;
                if (newFocusCol == -1)
                    newFocusCol = m_Model.getFixedHeaderColumnCount();
            }
        } else if (e.keyCode == SWT.PAGE_UP) {
            if (newFocusRow > m_Model.getFixedHeaderRowCount()) {
                newFocusRow -= m_RowsVisible - 1;
                if (newFocusRow < m_Model.getFixedHeaderRowCount())
                    newFocusRow = m_Model.getFixedHeaderRowCount();
                if (newFocusCol == -1)
                    newFocusCol = m_Model.getFixedHeaderColumnCount();
            }
        } else if (e.keyCode == SWT.TAB) {
            traverse((e.stateMask & SWT.SHIFT) == 0 ? SWT.TRAVERSE_TAB_NEXT : SWT.TRAVERSE_TAB_PREVIOUS);
        } else if (isEditOnKeyEvent()) {
            // check if a key was pressed that forces editing:
            if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS)  {
            	KTableCellEditor editor = m_Model.getCellEditor(m_FocusCol, m_FocusRow);
            	if (editor != null
            	        && (editor.getActivationSignals() & KTableCellEditor.KEY_ANY) !=0
            	        && editor.isApplicable(KTableCellEditor.KEY_ANY, this, m_FocusCol, m_FocusRow, null, e.character+"", e.stateMask )) {
            		openEditorInFocus();
                    if (m_CellEditor != null) {
                        m_CellEditor.setPressedChar((char)0);
                    }
            	}
                return;
            } else if ((Character.isLetterOrDigit(e.character) || 
            		e.keyCode>32 && e.keyCode<254 && e.keyCode!=127 || 
            		e.keyCode == SWT.KEYPAD_ADD || e.keyCode == SWT.KEYPAD_SUBTRACT) && 
                    e.keyCode != SWT.CTRL && e.keyCode != SWT.ALT && 
                    (e.stateMask & SWT.CONTROL)==0 && (e.stateMask & SWT.ALT)==0) {
                
                KTableCellEditor editor = m_Model.getCellEditor(m_FocusCol, m_FocusRow);
                if (editor != null
                        && (editor.getActivationSignals() & KTableCellEditor.KEY_ANY)!=0
                        && editor.isApplicable(KTableCellEditor.KEY_ANY, this, m_FocusCol, m_FocusRow, null, e.character+"", e.stateMask )) {
                    openEditorInFocus();
                    if (m_CellEditor != null) {
                        m_CellEditor.setPressedChar(e.character);
                    }
                    return;
                }
            }
        }

        if (!cell.equals(basicCell(newFocusCol, newFocusRow))) {
            // make sure it is a valid, visible cell and not overlapped:
            Point valid = m_Model.belongsToCell(newFocusCol, newFocusRow);
            if (valid != null) {
                newFocusCol = valid.x;
                newFocusRow = valid.y;
            }

            focusCell(newFocusCol, newFocusRow, e.stateMask);
            if (needScroll()) scrollToFocus();
        }
    }

    private List<KTableCellActivationListener> activationListeners = new ArrayList<KTableCellActivationListener>();
    private void notifyActivationListeners(int col, int row, char ch, int stateMask) {
        for (KTableCellActivationListener listener: activationListeners) {
            listener.activation(col, row, ch, stateMask);
        }
    }
    public void addActivationListener(KTableCellActivationListener listener) {
        activationListeners.add(listener);
    }
    public void removeActivationListener(KTableCellActivationListener listener) {
        activationListeners.remove(listener);
    }

    /**
     * @return 噪嚆 礤钺躅滂祛耱�恹镱腠栩�耜痤腚桧� 黩钺�蝈牦��彘赅 铌噻嚯囫��忤滂祛�珙礤.
     */
    private boolean needScroll() {
    	boolean flag = false;
    	flag |= (m_FocusCol <  m_LeftColumn && m_LeftColumn != getFixedColumnCount());
    	if (m_FocusCol >= m_LeftColumn + m_ColumnsFullyVisible) {
            int oldLeftCol = m_LeftColumn;
            int tempLeftCol = m_LeftColumn;
            Rectangle rect = getClientArea();
            int leftColX = getColumnLeft(m_FocusCol);
            int focusCellWidth = getCellRect(m_FocusCol, m_FocusRow).width;
            while (tempLeftCol < m_FocusCol && tempLeftCol < m_Model.getColumnCount()
            		&& ( leftColX<0 || leftColX + focusCellWidth > rect.width + rect.x)) {
            	tempLeftCol++;
            	leftColX = getColumnLeft(m_FocusCol);
            }
            flag |= (oldLeftCol != tempLeftCol);
        }
    	flag |= (m_FocusRow < m_TopRow && m_TopRow != getFixedRowCount());
    	flag |= (m_FocusRow >= m_TopRow + m_RowsFullyVisible);
    	return flag;
    }

    protected void onMouseDoubleClick(MouseEvent e) {
        if (m_Model == null)
            return;
        if (e.button == 1) {

            if (e.y < getHeaderHeight()) {
                // double click in header area
                int columnIndex = getColumnForResize(e.x, e.y);
                resizeColumnOptimal(columnIndex);
                m_ResizeColumnIndex=-1;
                Point cell = getCellForCoordinates(e.x, e.y);
                checkCursorToDisplay(e.x, e.y);
                fireFixedCellDoubleClicked(cell.x, cell.y, e.stateMask);
                return;
            } else {
                Point click = new Point(e.x, e.y);
                Point cell = getCellForCoordinates(e.x, e.y);
                if (isHeaderCell(cell.x, cell.y)) {
                    KTableCellEditor editor = m_Model.getCellEditor(cell.x, cell.y);
                    if ( editor!=null && 
                            (editor.getActivationSignals() & KTableCellEditor.DOUBLECLICK)!=0 && 
                            editor.isApplicable(KTableCellEditor.DOUBLECLICK, this, cell.x, cell.y, click, null, e.stateMask)) {
                        int oldFocusCol = m_FocusCol;
                        int oldFocusRow = m_FocusRow;
                        m_FocusCol=cell.x; m_FocusRow=cell.y;
                        openEditorInFocus();
                        m_FocusCol=oldFocusCol;
                        m_FocusRow=oldFocusRow;
                    }
                    fireFixedCellDoubleClicked(cell.x, cell.y, e.stateMask);
                } else {
                	if (m_FocusCol >= 0 && m_FocusRow >= 0) {
                        KTableCellEditor editor = m_Model.getCellEditor(m_FocusCol, m_FocusRow);
                        Rectangle rect = getCellRect(m_FocusCol, m_FocusRow);
                        if (editor!=null && rect.contains(click) &&
                                (editor.getActivationSignals() & KTableCellEditor.DOUBLECLICK)!=0 && 
                                editor.isApplicable(KTableCellEditor.DOUBLECLICK, this, m_FocusCol, m_FocusRow, click, null, e.stateMask))
                            openEditorInFocus();
                        fireCellDoubleClicked(cell.x, cell.y, e.stateMask);
                	}
                }
            }
        }
    }

    /**
     * Listener Class that implements fake tooltips. The tooltip content
     * is retrieved from the tablemodel.
     */
    class TooltipListener implements Listener {
        IKTableTooltip tt;

        public void handleEvent (final Event event) {
            switch (event.type) {
            case SWT.MouseDoubleClick: {
            	if (tt != null  && !tt.isDisposed ()) {
                	tt.dispose (KTable.this);
                	tt = null;
                }
            	break;
            }
            case SWT.MouseMove: 
            case SWT.Selection: // scrolling
            case SWT.MouseHover: {
            	Point cell = getCellForCoordinates(event.x, event.y);
            	
            	if ((m_tt_cell != null && m_tt_cell.equals(cell)) && (tt != null && !tt.isDisposed())) {
            		return;
            	}
            	
            	if (tt != null && tt.isLocked()) break;
            	
                if (tt != null  && !tt.isDisposed ()) {
                	tt.dispose (KTable.this);
                	tt = null;
                }
                
                IKTableTooltip tooltip = null;
                if (cell.x >= 0 && cell.x < m_Model.getColumnCount() 
                		&& cell.y >= 0 && cell.y < m_Model.getRowCount())
                	tooltip = m_Model.getTooltip(cell.x, cell.y);
                
                // check if there is something to show, and abort otherwise:
                if (((tooltip==null || tooltip.isEmpty()) && 
                        (m_nativTooltip==null || m_nativTooltip.isEmpty())) 
                        || (cell==null || cell.x==-1 || cell.y==-1)) {
                    tt = null;
                    return;
                }
                
                if (tooltip != null) {
                	tt = tooltip;
                } else {
                	tt = m_nativTooltip;
                }
                m_tt_cell = cell;
                
                tt.show(KTable.this, new TooltipAssistant() {
					public Rectangle calcBounds(Point size) {
						// TODO: Correctly position the tooltip below the cursor.
		                int y = 20; // currently the windows default???
		                int x = 0;
		                if (m_defaultCursorSize!=null && 
		                        m_defaultCursorSize.x>=0 && 
		                        m_defaultCursorSize.y>=0) {
		                    y = m_defaultCursorSize.y+1;
		                    x = -m_defaultCursorSize.x;
		                }
		                // place the shell under the mouse, but check that the
		                // bounds of the table are not overlapped.
		                Rectangle tableBounds = KTable.this.getBounds();
		                if (event.x+x+size.x>tableBounds.x+tableBounds.width)
		                    event.x-=event.x+x+size.x-tableBounds.x-tableBounds.width;
		                if (event.y+y+size.y>tableBounds.y+tableBounds.height)
		                    event.y-=event.y+y+size.y-tableBounds.y-tableBounds.height;
		                
		                Point pt = toDisplay (event.x+x, event.y+y);
						return new Rectangle(pt.x, pt.y, size.x, size.y);
					}
				});
                break;
            }
            case SWT.MouseExit: {
                if (tt == null || tt.isLocked()) break;
                tt.dispose(KTable.this);
                tt = null;
                break;
            }
            }
        }
    }

    /**
     * Sets the global tooltip for the whole table.<br>
     * Note that this is only shown if the cell has no tooltip set.
     * For tooltips on cell level (that overwrite this value), look
     * for the method <code>getTooltipText()</code>.
     * @see de.kupzog.ktable.models.KTableModel#getTooltipAt(int, int)
     * @see de.kupzog.ktable.KTable#getToolTipText()
     * @param tooltip The global tooltip for the table.
     */
    public void setToolTipText(String tooltip) {
        m_nativTooltip = new DefaultTooltip(tooltip);
    }
    
    /**
     * Returns the global tooltip for the whole table.<br>
     * Note that this is not shown when there is a non-empty tooltip
     * for the cell.
     * @see de.kupzog.ktable.KTable#setToolTipText(String)
     * @see de.kupzog.ktable.models.KTableModel#getTooltipAt(int, int)
     */
    public String getToolTipText() {
        return m_nativTooltip != null ? m_nativTooltip.toString() : "";
    }
    
    /**
     * Resizes the given column to its optimal width.
     * 
     * Is also called if user doubleclicks in the resize area of a resizable
     * column.
     * 
     * The optimal width is determined by asking the CellRenderers for the
     * visible cells of the column for the optimal with and taking the minimum
     * of the results. Note that the optimal width is only determined for the
     * visible area of the table because otherwise this could take very long
     * time.
     * 
     * @param column
     *            The column to resize
     * @return int The optimal with that was determined or -1, if column out of
     *         range.
     */
    public int resizeColumnOptimal(int column) {
        checkWidget();
        if (column >= 0 && column < m_Model.getColumnCount()) {
            int optWidth = 5;
            GC gc = new GC(this);
			for (int i = 0; i < m_Model.getFixedHeaderRowCount(); i++) {
				Point valid = getValidCell(column, i);
				if (valid.x == column && valid.y == i) {
					int width = m_Model.getCellRenderer(column, i).getOptimalWidth(gc, column, i,
							m_Model.getContentAt(column, i), true, m_Model);
					if (width > optWidth)
						optWidth = width;
				}
			}
			for (int i = m_TopRow; i < m_TopRow + m_RowsVisible; i++) {
				Point valid = getValidCell(column, i);
				if (valid.x == column && valid.y == i) {
					int width = m_Model.getCellRenderer(column, i).getOptimalWidth(gc, column, i,
							m_Model.getContentAt(column, i), true, m_Model);
					if (width > optWidth)
						optWidth = width;
				}
			}
            gc.dispose();
            m_Model.setColumnWidth(column, optWidth);
            fireColumnResize(column, m_Model.getColumnWidth(column));
            redraw();
            return optWidth;
        }
        return -1;        
    }

    /**
     * This method activated the cell editor on the current focus cell, if the
     * table model allows cell editing for this cell.
     */
    public void openEditorInFocus() {
        checkWidget();
        m_CellEditor = m_Model.getCellEditor(m_FocusCol, m_FocusRow);
        if (m_CellEditor != null) {
            scrollToFocus();
            Rectangle r = getCellRect(m_FocusCol, m_FocusRow);
            m_CellEditor.open(this, m_FocusCol, m_FocusRow, r);
        }
    }
    
    /**
     * Checks whether an editor is currently open at the specified position.
     * 
     * @param col The column index.
     * @param row The row index.
     * @return Whether an editor is currently open.
     */
    public boolean isEditorOpen(int col, int row) {
    	Point valid = getValidCell(col, row);
    	return m_CellEditor != null && m_CellEditor.m_Col == valid.x && m_CellEditor.m_Row == valid.y;
    }

    /**
     * Scrolls the table so that the given cell is top left.
     * @param col The column index.
     * @param row The row index.
     */
    public void scroll(int col, int row) {
        if (col<0 || col>=m_Model.getColumnCount() ||
            row<0 || row>=m_Model.getRowCount())
            return;

        m_TopRow = row;
        m_LeftColumn = col;
        redraw();
    }
    
    public void scrollCol(int col) {
        if (col<0 || col>=m_Model.getColumnCount())
            return;

        m_LeftColumn = col;
        redraw();
    }
    
    public void scrollRow(int row) {
        if (row<0 || row>=m_Model.getRowCount())
            return;

        m_TopRow = row;
        redraw();
    }

    protected void scrollToFocus() {
    	scrollToFocus(false);
    }
    protected void scrollToFocus(boolean forceRedraw) {
    	if (m_FocusCol < 0 || m_FocusRow < 0 
    			|| m_FocusCol >= m_Model.getColumnCount() || m_FocusRow >= m_Model.getRowCount()) {
    		return;
    	}

        boolean change = false;

        // vertical scroll allowed?
        if (getVerticalBar() != null) {
            if (m_FocusRow < m_TopRow) {
                if (m_FocusRow < getFixedRowCount()) {
                    m_TopRow = getFixedRowCount();
                } else {
                    m_TopRow = m_FocusRow;
                }
                change = true;
            }

            if (m_FocusRow >= m_TopRow + m_RowsFullyVisible) {
                m_TopRow = Math.min(m_Model.getRowCount() - 1, m_FocusRow - m_RowsFullyVisible + 1);
                change = true;
            }
        }

        // horizontal scroll allowed?
        if (getHorizontalBar() != null) {
            if (!isRowSelectMode() && m_FocusCol < m_LeftColumn) {
                if (m_FocusCol < getFixedColumnCount()) {
                    m_LeftColumn = getFixedColumnCount();
                } else {
                    m_LeftColumn = m_FocusCol;
                }
                change = true;
            }

            if (m_FocusCol >= m_LeftColumn + m_ColumnsFullyVisible) {
                int oldLeftCol = m_LeftColumn;
                Rectangle rect = getClientArea();
                int leftColX = getColumnLeft(m_FocusCol);
                int focusCellWidth = getCellRect(m_FocusCol, m_FocusRow).width;
                while (m_LeftColumn < m_FocusCol && m_LeftColumn < m_Model.getColumnCount()
                		&& ( leftColX<0 || leftColX + focusCellWidth > rect.width + rect.x)) {
                	m_LeftColumn++;
                	leftColX = getColumnLeft(m_FocusCol);
                }
                change |= (oldLeftCol != m_LeftColumn);
            }
        }
        
        if (forceRedraw || change) redraw();
    }

    /**
     * 吾疣犷蜿�耦猁蜩�恹犷疣 �彘觇 �蜞犭桷��筲邃铎脲龛�耠篪囹咫彘 �镳铊珙澍屐 耦猁蜩�
     * @param col
     * @param row
     * @param statemask
     */
    protected void fireCellSelection(int col, int row, int statemask) {
    	Point validCell = getValidCell(col, row);
        for (KTableCellSelectionListener listener: cellSelectionListeners) {
            listener.cellSelected(validCell.x, validCell.y, statemask);
    	}
    }
    private Point[] oldSelections;

    protected void fireCellDoubleClicked(int col, int row, int statemask) {
    	Point validCell = getValidCell(col, row);
        for (int i = 0; i < cellDoubleClickListeners.size(); i++) {
            cellDoubleClickListeners.get(i).cellDoubleClicked(validCell.x, validCell.y, statemask);
        }
    }

    protected void fireFixedCellDoubleClicked(int col, int row, int statemask) {
    	Point validCell = getValidCell(col, row);
        for (int i = 0; i < cellDoubleClickListeners.size(); i++) {
            cellDoubleClickListeners.get(i).fixedCellDoubleClicked(validCell.x, validCell.y, statemask);
        }
    }
    
    protected void fireFixedCellSelection(int col, int row, int statemask) {
    	Point validCell = getValidCell(col, row);
        for (int i = 0; i < cellSelectionListeners.size(); i++) {
            cellSelectionListeners.get(i).fixedCellSelected(validCell.x, validCell.y, statemask);
        }
    }

    protected void fireColumnResize(int col, int newSize) {
        for (int i = 0; i < cellResizeListeners.size(); i++) {
            cellResizeListeners.get(i).columnResized(col, newSize);
        }
    }

    protected void fireRowResize(int row, int newSize) {
        for (int i = 0; i < cellResizeListeners.size(); i++) {
            cellResizeListeners.get(i).rowResized(row, newSize);
        }
    }

    /**
     * Adds a listener that is notified when a cell is selected.
     * 
     * This can happen either by a click on the cell or by arrow keys. Note that
     * the listener is not called for each cell that the user selects in one
     * action using Shift+Click. To get all these cells use the listener and
     * getCellSelecion() or getRowSelection().
     * 
     * @param listener
     */
    public void addCellSelectionListener(KTableCellSelectionListener listener) {
        cellSelectionListeners.add(listener);
    }

    /**
     * Adds a listener that is notified when a cell is resized.
     * This happens when the mouse button is released after a resizing.
     * @param listener
     */
    public void addCellResizeListener(KTableCellResizeListener listener) {
        cellResizeListeners.add(listener);
    }
    
    /**
     * Adds a listener that is notified when a cell is doubleClicked.
     *
     * @param listener
     */
    public void addCellDoubleClickListener(KTableCellDoubleClickListener listener) {
        cellDoubleClickListeners.add(listener);
    }

    /**
     * Removes the listener if present. 
     * Returns true, if found and removed from the list of listeners.
     */
    public boolean removeCellSelectionListener(KTableCellSelectionListener listener) {
        return cellSelectionListeners.remove(listener);
    }

    /**
     * Removes the listener if present. 
     * Returns true, if found and removed from the list of listeners.
     */
    public boolean removeCellResizeListener(KTableCellResizeListener listener) {
        return cellResizeListeners.remove(listener);
    }
    
    /**
     * Removes the listener if present.
     * Returns true, if found and removed from the list of listeners.
     */
    public boolean removeCellDoubleClickListener(KTableCellDoubleClickListener listener) {
        return cellDoubleClickListeners.remove(listener);
    }

    //////////////////////////////////////////////////////////////////////////////
    // SELECTION
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns true if in "Full Selection Mode". 
     * Mode is determined by style bits in the constructor or 
     * by <code>setStyle()</code>. (style: SWT.FULL_SELECTION)
     * @return boolean
     */
    public boolean isRowSelectMode() {
        return (getStyle() & SWT.FULL_SELECTION) == SWT.FULL_SELECTION;
    }

    /**
     * Returns true if in "Multi Selection Mode".
     * Mode is determined by style bits in the constructor (SWT.MULTI)
     * or by <code>getStyle()</code>.
     */
    public boolean isMultiSelectMode() {
        return (getStyle() & SWT.MULTI) == SWT.MULTI;
    }
    
    /**
     * @return Returns true if the selection is kept & shown
     * even if the KTable has no focus.
     * @see #setShowSelectionWithoutFocus(boolean)
     */
    protected boolean isShowSelectionWithoutFocus() {
        return (getStyle() & SWT.HIDE_SELECTION) != SWT.HIDE_SELECTION;
    }
    
    /**
     * @return Returns true if selections are also shown in 
     * the vertical and horizontal table headers.
     * @see #setHighlightSelectionInHeader(boolean)
     */
    protected boolean isHighlightSelectionInHeader() {
        return (getStyle() & SWTX.MARK_FOCUS_HEADERS) == SWTX.MARK_FOCUS_HEADERS;
    }
    
    /**
     * @return Returns wether the celleditor is
     * activated on a (every) keystroke - like in Excel - 
     * and not only by ENTER. Activated by style <code>SWTX.EDIT_ON_KEY</code><p>
     * However, note that every cell editor can specify 
     * the event types it accepts.
     * @see #setEditOnKeyEvent(boolean)
     */
    protected boolean isEditOnKeyEvent() {
    	return (getStyle() & SWTX.EDIT_ON_KEY) == SWTX.EDIT_ON_KEY;
    }

    protected void clearSelectionWithoutRedraw() {
        m_Selection.clear();
    }

    /**
     * Clears the current selection (in all selection modes).
     */
    public void clearSelection() {
        checkWidget();
        clearSelectionWithoutRedraw();
        m_FocusCol = -1;
        m_FocusRow = -1;
        if (isMultiSelectMode())
            redraw();
    }

    /**
     * Works in both modes: Cell and Row Selection.
     * Has no redraw functionality!<p>
     * 
     * Returns true, if added to selection.
     */
    protected boolean toggleSelection(int col, int row) {

        if (isMultiSelectMode()) {
            Object o;
            if (isRowSelectMode()) {
                o = new Integer(row);
            } else {
                o = new Point(col, row);
            }
            if (m_Selection.get(o) != null) {
                m_Selection.remove(o);
                return false;
            } else {
                m_Selection.put(o, o);
                return true;
            }
        }
        return false;
    }

    /**
     * Works in both modes: Cell and Row Selection.
     * Has no redraw functionality!
     */
    protected void addToSelectionWithoutRedraw(int col, int row) {
        if (isMultiSelectMode()) {
            if (isRowSelectMode()) {
                Integer o = new Integer(row);
                m_Selection.put(o, o);
            } else {
                Point o = new Point(col, row);
                m_Selection.put(o, o);
            }
        }
    }

    /**
     * Selects the given cell. If scroll is true, 
     * it scrolls to show this cell if neccessary.
     * In Row Selection Mode, the given row is selected
     * and a scroll to the given column is done.
     * Does nothing if the cell does not exist. <p>
     * Note that if you use a sorted model, don't forget to map the row index!
     * @param col
     * @param row
     * @param scroll
     */
    public void setSelection(int col, int row, boolean scroll) {
        checkWidget();
        if (col < m_Model.getColumnCount()
                && (isRowSelectMode() || col >= m_Model.getFixedHeaderColumnCount())
                && row < m_Model.getRowCount()
                && row >= m_Model.getFixedHeaderRowCount()) {
            focusCell(col, row, 0);
            if (scroll) scrollToFocus();
        }
    }
    
    /**
     * Selects the given cells - Point.x is column index, Point.y is
     * row index. If scroll is true, scrolls so that the first of the 
     * given cell indexes becomes fully visible.<p>
     * Don't forget to map the row index if a sorted model is used.
     * @param selections The selected cells as points. If <code>null</code>
     * or an empty array, clears the selection.
     * @param scroll Wether it is reuqested to scroll to the first cell
     * given as selection.
     * @see KTableSortedModel#mapRowIndexToTable(int)
     */
    public void setSelection(Point[] selections, boolean scroll) {
        checkWidget();
        if (selections==null || selections.length<1) {
            clearSelection();
            return;
            
        } else if (isMultiSelectMode()){
            try {
                this.setRedraw(false);
                for (int i=0; i<selections.length; i++) {
                    int col = selections[i].x; 
                    int row = selections[i].y;
                    if (col < m_Model.getColumnCount()
                            && col >= m_Model.getFixedHeaderColumnCount()
                            && row < m_Model.getRowCount()
                            && row >= m_Model.getFixedHeaderRowCount())
                        if (i==0)
                            focusCell(col, row, SWT.CTRL);
                        else 
                            addToSelectionWithoutRedraw(col, row);
                }
                
                if (scroll) 
                    scrollToFocus();
            } finally {
                this.setRedraw(true);
            }
        } else {
            setSelection(selections[0].x, selections[0].y, scroll);
        }
    }

    /**
     * Returns true, if the given cell is selected.
     * Works also in Row Selection Mode.
     * @param col the column index.
     * @param row the row index.
     * @return boolean Returns true if the given cell is selected.
     */
    public boolean isCellSelected(int col, int row) {
        checkWidget();
        Point v = getValidCell(col, row);
        col = v.x;
        row = v.y;
        if (!isMultiSelectMode()) {
            if (isRowSelectMode())
                return (row == m_FocusRow);
            return (col == m_FocusCol && row == m_FocusRow);
        }

        if (isRowSelectMode())
            return (m_Selection.get(new Integer(row)) != null);
        else
            return (m_Selection.get(new Point(col, row)) != null);
    }

    /**
     * Returns true, if the given row is selected.
     * Returns always false if not in Row Selection Mode!<p>
     * If you use a sorted model, don't forget to map the row index first.
     * @param row The row index as seen by the KTable.
     * @return boolean returns true if the row is selected at the moment.
     */
    public boolean isRowSelected(int row) {
        return (m_Selection.get(new Integer(row)) != null);
    }

    /**
     * Returns an array of the selected row numbers.
     * Returns null if not in Row Selection Mode.
     * Returns an array with one or none element if not in Multi Selection Mode.<p>
     * NOTE: This returns the cell indices as seen by the KTable. If you use a sorting model,
     * don't forget to map the indices properly.
     * @return int[] Returns an array of rows that are selected. 
     * @see KTableSortedModel#mapRowIndexToModel(int)
     */
    public int[] getRowSelection() {
        checkWidget();
        if (!isRowSelectMode()) return null;
        if (!isMultiSelectMode()) {
            if (m_FocusRow < 0) return new int[0];
            int rowCount = m_Model.getRowCount();
            if (m_FocusRow >= rowCount) m_FocusRow = rowCount - 1; // TODO 蒡�玎镫囹赅!
            return new int[] {m_FocusRow};
        }

        Object[] ints = m_Selection.values().toArray();
        int[] erg = new int[ints.length];

        for (int i = 0; i < erg.length; i++) {
            erg[i] = ((Integer) ints[i]).intValue();
        }
        return erg;
    }

    /**
     * Returns an array of the selected cells as Point[].
     * The columns are stored in the x fields, rows in y fields.
     * Returns null if in Row Selection Mode. <br>
     * Returns an array with one or none element if not in Multi Selection Mode.<p>
     * NOTE: This returns the cell indices as seen by the KTable. If you use a sorting model,
     * don't forget to map the indices properly.
     * @return int[] Returns an array of points that are selected. 
     * These are no copies, so don't directly manipulate them...
     * 
     * @see KTableSortedModel#mapRowIndexToModel(int)
     */
    public Point[] getCellSelection() {
        checkWidget();
        if (isRowSelectMode())
            return null;
        if (!isMultiSelectMode()) {
            if (m_Model != null) {
                if (m_FocusCol < 0 && m_LeftColumn < m_Model.getColumnCount()) {
                    m_FocusCol = m_LeftColumn;
                }
                if (m_FocusRow < 0 && m_TopRow < m_Model.getRowCount()) {
                    m_FocusRow = m_TopRow;
                }
            }

            if (m_FocusRow < 0 || m_FocusCol < 0) return new Point[0];
            return new Point[] {new Point(m_FocusCol, m_FocusRow)};
        }

        return (Point[]) m_Selection.values().toArray(new Point[]{});
    }
    
    /**
     * @return Returns the non-fixed cells that are currently visible.
     * The returned rectangle has the columns on the x / width coordinates
     * and the rows at the y / height coordinates.
     */
    public Rectangle getVisibleCells() {
        return new Rectangle(m_LeftColumn, m_TopRow, 
                m_ColumnsVisible, m_RowsVisible);
    }
    
    /**
     * Internal helper method to determine wether the cell at the 
     * given position is to be highlighted because it is a header cell
     * that corresponds to a selected cell.
     * @param col The column index
     * @param row The row index
     * @return true if the cell should be highlighted.
     */
    private boolean highlightSelectedRowCol(int col, int row) {
        if (!isHighlightSelectionInHeader() || !isHeaderCell(col, row))
            return false;
        
        Point[] sel = getCellSelection();
        if (sel!=null) {
            for (int i=0; i<sel.length; i++) {
                if (sel[i].x == col || sel[i].y == row) 
                    return true;
                Point valid = getValidCell(sel[i].x, row);
                if (valid.x == col) return true;
                valid = getValidCell(col, sel[i].y);
                if (valid.y == row) return true;
            }
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////////
    // MODEL
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the table model.
     * The table model provides data to the table.
     * @see de.kupzog.ktable.models.KTableModel for more information.
     * @param model The KTableModel instance that provides the table with all 
     * necessary data!
     */
    public void setModel(KTableModel model) {
        checkWidget();
        m_Model = model;
        m_FocusCol = -1;
        m_FocusRow = -1;
        clearSelectionWithoutRedraw();

        redraw();
    }

    /**
     * returns the current table model
     * @return KTableModel 
     */
    public KTableModel getModel() {
        return m_Model;
    }

    /**
     * Helper method to quickly get the number of fixed columns.
     * @return
     */
    protected int getFixedColumnCount() {
        return m_Model.getFixedHeaderColumnCount() + m_Model.getFixedSelectableColumnCount();
    }

    /**
     * Helper method to quickly get the number of fixed rows.
     * @return
     */
    protected int getFixedRowCount() {
        return m_Model.getFixedHeaderRowCount() + m_Model.getFixedSelectableRowCount();
    }

    /**
     * 蔓麒耠屙桢 磬漕犴铖蜩 �镱腩襦�镳铌痼蜿��铗钺疣驽龛�耜瘥蜩�铐
     * �耠篦噱 礤钺躅滂祛耱�
     */
    protected void updateScrollbarVisibility() {
        Rectangle actualSize = getBounds();
        if ((getStyle() & SWT.BORDER) == SWT.BORDER) {
        	actualSize.height -= getBorderWidth()*2;
        	actualSize.width -= getBorderWidth()*2;
        }

        boolean needVert = needVertBar(actualSize.height);
        if (needVert) actualSize.width -= getVerticalBar().getSize().x;

        boolean needHor = needHorizBar(actualSize.width);
        if (needHor && !needVert) {
        	actualSize.height -= getHorizontalBar().getSize().y;
        	needVert = needVertBar(actualSize.height);
        }

        getVerticalBar().setVisible(needVert);

        if (getHorizontalBar().isVisible() != needHor){
            getHorizontalBar().setVisible(needHor);
            // Scroll the table back to the left
            if (!needHor) {
                getHorizontalBar().setSelection(0);
                m_LeftColumn = 0;
            }
        }            
    }
    private boolean needVertBar(int height) {
        int theoreticalHeight = 0;
        for (int i = 0; i < m_Model.getRowCount(); i++) {
            theoreticalHeight += m_Model.getRowHeight(i);
            if (theoreticalHeight > height) {
                return true;
            }
        }
        return false;
    }
    private boolean needHorizBar(int width) {
        int theoreticalWidth = 0;
        for (int i = 0; i < m_Model.getColumnCount(); i++)
            theoreticalWidth += m_Model.getColumnWidth(i);

        return width < theoreticalWidth;
    }

    /**
     * @param col 皖戾�鲥脲忸�觐腩黻�
     * @return 蒴翦牝桠眢�痂眢 觐腩黻� 喻栩噱蝰� 怅膻鞲�腓 耱桦�蜞犭桷�
     * 镳�觐蝾痤�镱耠邃��觐腩黻�疣聒桊�蝰� 黩钺�玎�螯 怦�铖蜞怿邋� 镳铖蝠囗耱忸.
     */
    private int getColumnWidth(int col) {
        if (col == m_Model.getColumnCount() - 1 && (getStyle() & SWTX.FILL_WITH_LASTCOL) != 0) {
            // expand the width to grab all the remaining space with this last col.
            Rectangle cl = getClientArea();
            int remaining = cl.width - getColumnLeft(col);
            // TODO 项赅 镳铖蝾 玎汶篪桦, �忸钺, 眢骓�漕溴豚螯 DummyColumn
            // return Math.max(remaining, m_Model.getColumnWidth(col));
            return remaining;
        } else
            return m_Model.getColumnWidth(col);
    }

    /**
     *  (non-Javadoc)
     * @see org.eclipse.swt.widgets.Widget#getStyle()
     */
    public int getStyle() {
    	return m_Style;
    }
    /**
     * Overwrites the style bits that determine certain behavior of KTable.
     * Note that not all style bits can be changed after KTable is created.
     * @param style The updated style bits (ORed together). Possibilities:
     * <ul>
     * <li><b>SWTX.FILL_WITH_LASTCOL</b> - Makes the table enlarge the last column to always fill all space.</li>
     * <li><b>SWTX.FILL_WITH_DUMMYCOL</b> - Makes the table fill any remaining space with dummy columns to fill all space.</li>
     * <li><b>SWT.FLAT</b> - Does not paint a dark outer border line.</li>
     * <li><b>SWT.MULTI</b> - Sets the "Multi Selection Mode".
     * In this mode, more than one cell or row can be selected.
     * The user can achieve this by shift-click and ctrl-click.
     * The selected cells/rows can be scattored ofer the complete table.
     * If you pass false, only a single cell or row can be selected.
     * This mode can be combined with the "Row Selection Mode".</li>
     * <li><b>SWT.FULL_SELECTION</b> - Sets the "Full Selection Mode".
     * In the "Full Selection Mode", the table always selects a complete row.
     * Otherwise, each individual cell can be selected.
     * This mode can be combined with the "Multi Selection Mode".</li>
     * <li><b>SWTX.EDIT_ON_KEY</b> - Activates a possibly present cell editor
     * on every keystroke. (Default: only ENTER). However, note that editors
     * can specify which events they accept.</li>
     * <li><b>SWT.HIDE_SELECTION</b> - Hides the selected cells when the KTable
     * looses focus.</li>
     * <li><b>SWTX.MARK_FOCUS_HEADERS</b> - Makes KTable draw left and top header cells
     * in a different style when the focused cell is in their row/column.
     * This mimics the MS Excel behavior that helps find the currently
     * selected cell(s).</li>
     * </ul>
     */
    public void setStyle(int style) {
    	m_Style = style;
    }

	/**
	 * Sets the cursor provider. If no cursor provider was specified or the specified cursor provider is 
	 * <code>null</code>, the default cursor will be displayed. If a cursor provider was specified, however, it will
	 * be asked to provide a cursor for the current mouse hover position every time the mouse is moved.
	 * 
	 * @param cursorProvider The cursor provider to set.
	 */
	public void setCursorProvider(KTableCursorProvider cursorProvider) {
		m_CursorProvider = cursorProvider;
	}

	/**
	 * @return The number of rows visible in the preferred size.
	 */
	public int getNumRowsVisibleInPreferredSize() {
		return m_numRowsVisibleInPreferredSize;
	}

	/**
	 * @return The number of columns visible in the preferred size.
	 */
	public int getNumColsVisibleInPreferredSize() {
		return m_numColsVisibleInPreferredSize;
	}

	/**
	 * Sets the number of rows that are visible in the preferred size of the KTable (as calculated by 
	 * {@link #computeSize(int, int, boolean)}).
	 * 
	 * @param numRowsVisibleInPreferredSize The number of rows visible in the preferred size.
	 */
	public void setNumRowsVisibleInPreferredSize(int numRowsVisibleInPreferredSize) {
		m_numRowsVisibleInPreferredSize = numRowsVisibleInPreferredSize;
	}
	
	/**
	 * Sets the number of columns that are visible in the preferred size of the KTable (as calculated by 
	 * {@link #computeSize(int, int, boolean)}).
	 * 
	 * @param numColsVisibleInPreferredSize The number of columns visible in the preferred size.
	 */
	public void setNumColsVisibleInPreferredSize(int numColsVisibleInPreferredSize) {
		m_numColsVisibleInPreferredSize = numColsVisibleInPreferredSize;
	}

    /**
     * 玉蜞眍怅�滂磬扈麇耜钽�耜痤腚桧汔 镱 忮痱桕嚯�
     * @param fluentScrollVertical 篷腓 篑蜞眍忤螯 珥圜屙桢 false, 蝾 镳�镥疱戾龛�
     * 镱腌箜赅 �镱祛�禧 镥疱躅�磬 眢骓簋 镱玷鲨�狍溴�恹镱腠��蝾朦觐 镱耠�铗矬耜囗�.
     */
    public void setFluentScrollVertical(boolean fluentScrollVertical) {
        this.fluentScrollVertical = fluentScrollVertical;
    }
    /**
     * 玉蜞眍怅�滂磬扈麇耜钽�耜痤腚桧汔 镱 泐痂珙眚嚯�
     * @param fluentScrollHorizontal 篷腓 篑蜞眍忤螯 珥圜屙桢 false, 蝾 镳�镥疱戾龛�
     * 镱腌箜赅 �镱祛�禧 镥疱躅�磬 眢骓簋 镱玷鲨�狍溴�恹镱腠��蝾朦觐 镱耠�铗矬耜囗�.
     */
    public void setFluentScrollHorizontal(boolean fluentScrollHorizontal) {
        this.fluentScrollHorizontal = fluentScrollHorizontal;
    }

	/**
	 * Calculates the preferred size of the KTable. The preferred size of a KTable is calculated such that there is
	 * enough space available to show all header columns and rows, as well as a configurable number of data columns and 
	 * rows.
	 * 
	 * @param wHint The width hint (can be <code>SWT.DEFAULT</code>)
	 * @param hHint The height hint (can be <code>SWT.DEFAULT</code>)
	 * @param changed <code>true</code> if the control's contents have changed, and <code>false</code> otherwise
	 * @return the preferred size of the control.
	 * @see #setNumColsVisibleInPreferredSize(int)
	 * @see #setNumRowsVisibleInPreferredSize(int)
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		// start with margins: 镱 钿眍祗 镨犟咫�磬 忮瘐睨��龛骓, 脲怏��镳噔帼 沭囗桷�
		boolean border = (getStyle() & SWT.BORDER) == SWT.BORDER;
		int height = border ? getBorderWidth()*2 : 0;
		int width = height;

		if (m_Model != null) {
			// Determine height of header rows
			for (int i = 0; i < m_Model.getFixedHeaderRowCount(); i++) {
				height += m_Model.getRowHeight(i);
			}

			// Add height of data rows to display
			for (int i = m_Model.getFixedHeaderRowCount(); i < m_Model.getFixedHeaderRowCount()
					+ m_numRowsVisibleInPreferredSize
					&& i < m_Model.getRowCount(); i++) {
				height += m_Model.getRowHeight(i);
			}

			// Determine width of header columns
			for (int i = 0; i < m_Model.getFixedHeaderColumnCount(); i++) {
				width += m_Model.getColumnWidth(i);
			}

			// Add height of data columns to display
			for (int i = m_Model.getFixedHeaderColumnCount(); i < m_Model.getFixedHeaderColumnCount()
					+ m_numColsVisibleInPreferredSize
					&& i < m_Model.getColumnCount(); i++) {
				width += m_Model.getColumnWidth(i);
			}
		}

		width = Math.max(width, wHint);
		height = Math.max(height, hHint);

		if ((getStyle() & SWTX.AUTO_SCROLL) == SWTX.AUTO_SCROLL) {
			if (needHorizBar(width)) height += getHorizontalBar().getSize().y;
			if (needVertBar(height)) width +=  getVerticalBar().getSize().x;
		} else {
			if (getHorizontalBar() != null) height += getHorizontalBar().getSize().y;
			if (getVerticalBar()   != null) width +=  getVerticalBar().getSize().x;
		}

		return new Point(width, height);
	}
}