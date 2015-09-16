package org.gumtree.msw.ui.ktable;

import java.util.List;
import java.util.Objects;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;
import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.renderers.BorderPainter;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;

abstract class ButtonRenderer implements KTableCellRenderer {
    
	private static enum State {
		DEFAULT, MOUSE_OVER, MOUSE_DOWN
	}

	private static class CellIndex {
		// fields
		public final int col;
		public final int row;
		
		// construction
		public CellIndex(int col, int row) {
			this.col = col;
			this.row = row;
		}
		
		// methods
		public boolean equals(int col, int row) {
			return (this.col == col) && (this.row == row);
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CellIndex) {
				CellIndex other = (CellIndex)obj;
				return (other.col == col) && (other.row == row);
			}
			return false;
		}
		@Override
		public int hashCode() {
			return Integer.reverse(col) ^ row;
		}
	}
	
	// fields
	protected final KTable table;
	protected final int optimalWidth;
	protected final int buttonCount;
	protected final Class<?> elementType;
	private final Image[] imagesDefault;
	private final Image[] imagesMouseOver;
    // state
    private State state;
    private CellIndex activeCell;
    private CellIndex hitCell;
    private Point lastMousePosition;
	
	// construction
	public <T> ButtonRenderer(final KTable table, int optimalWidth, Class<?> elementType, List<ButtonInfo<T>> buttons) {
		this.table = table;
		this.optimalWidth = optimalWidth;
		this.buttonCount = buttons.size();
		this.elementType = elementType;

		imagesDefault = new Image[buttonCount];
		imagesMouseOver = new Image[buttonCount];
		for (int i = 0; i != buttonCount; i++) {
			ButtonInfo<?> buttonInfo = buttons.get(i);
			imagesDefault[i] = buttonInfo.getImageDefault();
			imagesMouseOver[i] = buttonInfo.getImageMouseOver();
		}

		state = State.DEFAULT;
		activeCell = null;
		hitCell = null;
		lastMousePosition = new Point(-1, -1);
		
		if (buttonCount == 0)
			return;

		table.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
            	onMouseDown(new Point(e.x, e.y));
            }
            public void mouseUp(MouseEvent e) {
            	onMouseUp(new Point(e.x, e.y));
            }
        });
		table.addMouseMoveListener(new MouseMoveListener() {
			@Override
            public void mouseMove(MouseEvent e) {
				onMouseMove(new Point(e.x, e.y));
            }
        });
		table.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
            public void mouseEnter(MouseEvent e) {
				onMouseEnter(new Point(e.x, e.y));
            }
			@Override
            public void mouseExit(MouseEvent e) {
				onMouseExit(new Point(e.x, e.y));
            }
        });
		table.getVerticalBar().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (lastMousePosition != null)
					onMouseMove(lastMousePosition);					
			}
		});
	}

	// methods
	private CellIndex getCellIndex(int x, int y) {
    	int col = isValidColumn(x, y);
    	int row = isValidRow(x, y);
    	
    	if ((col == -1) || (row == -1))
    		return null;

    	return new CellIndex(col, row);
	}
	private void resetActiveCell() {
		if (activeCell != null) {
			CellIndex oldCell = activeCell;
			activeCell = null;

    		state = State.DEFAULT;
    		redraw(oldCell);
		}
	}
	private void redraw(CellIndex cellIndex) {
		if (cellIndex != null)
			table.redraw(cellIndex.col, cellIndex.row, 0, 0); // (0,0) bug in KTable (redraw only fixedCols and fixedRows)
	}
	// (x,y)->col/row (otherwise: -1)
	protected abstract int isValidColumn(int x, int y);
	protected abstract int isValidRow(int x, int y);
	protected abstract void clicked(int col, int row, int index);
	// event handling
	private void onMouseEnter(Point p) {
		lastMousePosition = p;
	}
	private void onMouseExit(Point p) {
		lastMousePosition = p;

    	CellIndex newCell = null;
		if (!Objects.equals(activeCell, newCell)) {
			resetActiveCell();
    		activeCell = newCell;
		}    		
	}
	private void onMouseMove(Point p) {
		lastMousePosition = p;

    	CellIndex newCell = getCellIndex(p.x, p.y);
		if (!Objects.equals(activeCell, newCell)) {
			if ((hitCell != null) && Objects.equals(activeCell, hitCell)) {
				state = State.MOUSE_OVER;
				redraw(activeCell);
			}
			else
				resetActiveCell();
			
    		activeCell = newCell;
		}
		
		if (newCell != null) {
			if (hitCell != null)
    			if (Objects.equals(hitCell, newCell))
            		state = State.MOUSE_DOWN;
    			else
    				state = State.DEFAULT;
			else
				state = State.MOUSE_OVER;
			
			redraw(activeCell);
    	}
	}
	private void onMouseDown(Point p) {
		lastMousePosition = p;
		
    	CellIndex newCell = getCellIndex(p.x, p.y);
		if (!Objects.equals(activeCell, newCell)) {
			resetActiveCell();
    		activeCell = newCell;
		}
		
    	if (newCell != null) {
    		hitCell = newCell;
    		state = State.MOUSE_DOWN;
    		redraw(activeCell);
    	}
    	else
    		hitCell = null;
	}
	private void onMouseUp(Point p) {
		lastMousePosition = p;
		
    	CellIndex newCell = getCellIndex(p.x, p.y);
		if (!Objects.equals(activeCell, newCell)) {
			resetActiveCell();
    		activeCell = newCell;
		}
		if (hitCell != null) {
			if (Objects.equals(hitCell, newCell)) {
    			int index = -1; // button index
    			
    			if (buttonCount == 1)
    				index = 0;
    			else if (buttonCount > 1) {
        			Rectangle rect = table.getCellRect(hitCell.col, hitCell.row);
        			
        			index = buttonCount * (p.x - rect.x) / rect.width;
        			if (index < 0)
        				index = 0;
        			else if (index >= buttonCount)
        				index = buttonCount - 1;
    			}

    			if (index != -1)
    				clicked(hitCell.col, hitCell.row, index);
    		}
			else {
				redraw(hitCell);
			}
    		hitCell = null;
		}
		
    	if (newCell != null) {
    		state = State.MOUSE_OVER;
    		redraw(activeCell);
    	}
	}
	
	// KTableCellRenderer
	@Override
	public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
		return optimalWidth;
	}
	@Override
	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean header, boolean clicked, KTableModel model) {
		BorderPainter.drawDefaultSolidCellLine(gc, rect, DefaultCellRenderer.COLOR_LINE_DARKGRAY, DefaultCellRenderer.COLOR_LINE_DARKGRAY);
		
		if ((activeCell != null) && activeCell.equals(col, row))
			switch (state) {
			case MOUSE_OVER:
				drawButtonBackground(gc, rect);						
			case MOUSE_DOWN:
				drawButtons(gc, rect, imagesMouseOver);
				return;
				
			default:
				break;
			}
		else if ((hitCell != null) && hitCell.equals(col, row)) {
			drawButtonBackground(gc, rect);
			drawButtons(gc, rect, imagesMouseOver);
			return;
		}
		
		drawButtons(gc, rect, imagesDefault);
	}
	// helpers
    private void drawButtonBackground(GC gc, Rectangle rect) {
        int offset = rect.height / 2;
        
        gc.setBackground(KTableResources.COLOR_GRADIENT_1A);
        gc.setForeground(KTableResources.COLOR_GRADIENT_1B);
        gc.fillGradientRectangle(
        		rect.x,
        		rect.y + offset,
        		rect.width,
        		rect.height - offset,
        		true);

        gc.setBackground(KTableResources.COLOR_GRADIENT_2A);
        gc.setForeground(KTableResources.COLOR_GRADIENT_2B);
        gc.fillGradientRectangle(
        		rect.x,
        		rect.y,
        		rect.width,
        		offset,
        		true);
    }
    private void drawButtons(GC gc, Rectangle rect, Image[] images) {
		// find center
    	int width = -10;
		for (int i = 0; i != images.length; i++) {
			Image image = images[i];
			ImageData imageData = image.getImageData();
			
    		width += imageData.width - 2;
		}
    	
    	// draw buttons
		int offsetX = (optimalWidth - width) / 2;
		int offsetY = 1;
		
		offsetX += rect.x;
		for (int i = 0; i != images.length; i++) {
			Image image = images[i];
			ImageData imageData = image.getImageData();
			
    		gc.drawImage(
    				image,
    				offsetX, 
    				offsetY + rect.y + (rect.height - imageData.height) / 2);

    		offsetX += imageData.width - 4;
		}
    }
}
