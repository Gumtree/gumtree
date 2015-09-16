package org.gumtree.msw.ui.ktable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListListener;
import org.gumtree.msw.elements.IElementPropertyListener;
import org.gumtree.msw.elements.IElementVisitor;
import org.gumtree.msw.ui.Resources;

import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;
import org.gumtree.msw.ui.ktable.KTableDefaultModel;
import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.renderers.BorderPainter;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

public final class AcquisitionTableModel extends KTableDefaultModel {
	
	public static final Comparator<ElementNode> INDEX_COMPARATOR = new NodeIndexComparator();
	
	// finals
	private static Color GRADIENT_COLOR_1A = SWTResourceManager.getColor(240, 240, 240);
	private static Color GRADIENT_COLOR_1B = SWTResourceManager.getColor(218, 218, 218);
	private static Color GRADIENT_COLOR_2A = SWTResourceManager.getColor(232, 232, 232);
	private static Color GRADIENT_COLOR_2B = SWTResourceManager.getColor(251, 251, 251);
	
	// fixed
	private static final int preferredSizeRowHeaderWidth = 41;
	private static final int preferredSizeColumnHeaderHeight = 22;
	private static final int preferredSizeDefaultRowHeight = 18;

	// fields
	private final List<ElementNode> linkNodes; // linkNodes.get(0) -> root node
	private final Map<Element, ElementNode> lookup;
	private final Map<Class<?>, RowDefinition> rowDefinitions;
    // state
    private final Set<String> collapsedNodes;
    private final Set<String> disabledNodes;
	// table
	private final KTable table;
    private final KTableCellRenderer columnHeaderRenderer;
    private final KTableCellRenderer rowHeaderRenderer;
    private final KTableCellRenderer settingsCellRenderer;
    private final TextCellRenderer textCellRenderer;
    private final KTableCellRenderer collapsableCellRenderer;
    private final KTableCellRenderer checkableCellRenderer;
    private final KTableCellRenderer clearCellRenderer;
    private final KTableCellRenderer endOfLineCellRenderer;
    private final KTableCellEditor checkableCellEditor;
    // helpers
    private final ElementNodeCounter DEFAULT_COUNTER = new ElementNodeCounter();
    private final ElementNodeFinder DEFAULT_FINDER = new ElementNodeFinder();
    
	// construction
	public AcquisitionTableModel(final KTable table, ElementList<? extends Element> loopHierarchy, final Menu menu, Iterable<RowDefinition> rowDefinitions) {
        initialize();
        
        this.rowDefinitions = new HashMap<>();
        for (RowDefinition rowDefinition : rowDefinitions)
        	this.rowDefinitions.put(rowDefinition.getElementType(), rowDefinition);

        collapsedNodes = new HashSet<>();
        disabledNodes = new HashSet<>();

        // final
        this.table = table;
        this.table.setNumColsVisibleInPreferredSize(getColumnCount());
        this.table.setPreferredSizeDefaultRowHeight(preferredSizeDefaultRowHeight);

    	final int firstColumnWidth = Resources.IMAGE_SETTINGS_DROPDOWN.getBounds().width;
    	IButtonListener settingsButtonListener = new IButtonListener() {
			@Override
			public void onClicked(int col, int row) {
				if (menu != null) {
	            	Point point = table.toDisplay(0, preferredSizeColumnHeaderHeight);
					menu.setLocation(point.x, point.y);
					menu.setVisible(true);
				}
			}
    	};

    	columnHeaderRenderer = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
    	rowHeaderRenderer = new ButtonRenderer(
    			table,
    			firstColumnWidth,
    			Arrays.asList(
		    			new ButtonInfo(Resources.IMAGE_PLUS_SMALL_GRAY, Resources.IMAGE_PLUS_SMALL_GRAY, null),
		    			new ButtonInfo(Resources.IMAGE_COPY_SMALL_GRAY, Resources.IMAGE_COPY_SMALL_GRAY, null),
		    			new ButtonInfo(Resources.IMAGE_MINUS_SMALL_GRAY, Resources.IMAGE_MINUS_SMALL_GRAY, null))) {
			@Override
			protected int isValidColumn(int x, int y) {
				Rectangle rect = table.getCellRect(0, 0);
				return (0 <= x) && (x < rect.width) ? 0 : -1;
			}
			@Override
			protected int isValidRow(int x, int y) {
				int row = table.getRowForY(y);
				return row > 0 ? row : -1;
			}
    	};
    	settingsCellRenderer = new ButtonRenderer(
    			table,
    			firstColumnWidth,
    			Arrays.asList(new ButtonInfo(Resources.IMAGE_SETTINGS_DROPDOWN, Resources.IMAGE_SETTINGS_DROPDOWN, settingsButtonListener))) {
			@Override
			protected int isValidColumn(int x, int y) {
				Rectangle rect = table.getCellRect(0, 0);
				return (0 <= x) && (x < rect.width) ? 0 : -1;
			}
			@Override
			protected int isValidRow(int x, int y) {
				int row = table.getRowForY(y);
				return row == 0 ? row : -1;
			}
    	};
    	textCellRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);
    	
    	Color backgroundColor = SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND);
    	collapsableCellRenderer = new CheckableCellRenderer(
    			CheckableCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE,
    			Resources.IMAGE_EXPANDED,
    			Resources.IMAGE_COLLAPSED,
    			backgroundColor,
    			backgroundColor);
    	checkableCellRenderer = new CheckableCellRenderer(
    			CheckableCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE,
    			backgroundColor,
    			backgroundColor);
    	clearCellRenderer = new ClearCellRenderer();
    	endOfLineCellRenderer = new EndOfLineRenderer();
    	
    	checkableCellEditor = new KTableCellEditorCheckbox();
    	
    	linkNodes = new ArrayList<>();
    	lookup = new HashMap<>();
    	
    	loopHierarchy.addListListener(new IElementListListener<Element>() {
    		// fields
        	private final IElementPropertyListener indexListener = new IElementPropertyListener() {
    			@Override
    			public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
    				if ((property == Element.INDEX) || property.matches("enabled", Boolean.class))
    					resetLinks();
    			}
    		};
    		
    		// methods
			@Override
			public void onAddedListElement(Element element) {
				ElementNode linkNode = new ElementNode(element);
				
				element.addPropertyListener(indexListener);
				
				linkNodes.add(linkNode);
				lookup.put(element, linkNode);
				resetLinks();
			}
			@Override
			public void onDeletedListElement(Element element) {
				ElementNode linkNode = lookup.remove(element);
				if (linkNode != null) {
					element.removePropertyListener(indexListener);
					
					linkNode.resetLink();
					linkNodes.remove(linkNode);
					resetLinks();
				}
			}
		});
	}
	
	private ElementNode getRootNode() {
		for (ElementNode linkNode : linkNodes)
			if (linkNode.getEnabled())
				return linkNode;
		
		return null;
	}
	private void resetLinks() {
		int i = 0;
		int n = linkNodes.size();
		if (n > 0) {
			Collections.sort(linkNodes, INDEX_COMPARATOR);
			
			ElementNode last = linkNodes.get(0);
			while (++i != n) {
				ElementNode next = linkNodes.get(i);
				last.setLink(next);
				last = next;
			}
			last.resetLink();
		}
		table.redraw();
	}
	
	// methods
    @Override
    public int doGetRowCount() {
    	ElementNodeCounter counter = DEFAULT_COUNTER;
    	counter.count(getRootNode());
        return getFixedRowCount() + counter.getCount();
    }
    @Override
    public int doGetColumnCount() {
        return getFixedColumnCount() + 35;
    }
    @Override
    public Point doBelongsToCell(int col, int row) {
    	if (col <= 0)
    		return new Point(col, row);
    	if (row <= 0)
    		return new Point(1, row);

    	int index = row - 1; // toElementIndex(row);
    	ElementNodeFinder finder = DEFAULT_FINDER;
    	finder.find(getRootNode(), index);

		int colTarget = col;
    	if (finder.successful())
    		if ((col -= finder.getDepth()) > 0) {
				switch (col) {
				case 1:
					// Collapsed
					return new Point(colTarget, row);
				case 2:
					// Enabled
					if (finder.getNode().canBeDisabled())
						return new Point(colTarget, row);
					
					break;
					
				default:
					if (finder.getNode().canBeDisabled())
						col--; // enabled button
						
					break;
				}

				// custom buttons, expandable button
				col -= 2;
				
				if (col > 0) { // (col >= 0)
					Element element = finder.getNode().getElement();
					RowDefinition rowDefinition = rowDefinitions.get(element.getClass());
					if (rowDefinition != null)
						for (CellDefinition cell : rowDefinition.getCells()) {
							int span = cell.getColumnSpan();
							if (col < span) {
								colTarget -= col;
								break;
							}
							col -= span;
						}
				}
    		}

        return new Point(colTarget, row);
    }
	@Override
	public Object doGetContentAt(int col, int row) {
    	if ((col <= 0) || (row < 0))
    		return null;
    	if (row == 0)
			return "Acquisition Tree";

    	int index = row - 1; // toElementIndex(row);
    	ElementNodeFinder finder = DEFAULT_FINDER;
    	finder.find(getRootNode(), index);
    	
    	if (finder.successful())
    		if ((col -= finder.getDepth()) > 0) {
				switch (col) {
				case 1:
					// Collapsed
					if (!finder.getNode().isLeaf())
						return !collapsedNodes.contains(finder.getPath());
					else
						return null;
				case 2:
					// Enabled
					if (finder.getNode().canBeDisabled())
						return !disabledNodes.contains(finder.getPath());
					
					break;
					
				default:
					if (finder.getNode().canBeDisabled())
						col--; // enabled button
						
					break;
				}

				// custom buttons, expandable button
				col -= 2;
				
				Element element = finder.getNode().getElement();
				RowDefinition rowDefinition = rowDefinitions.get(element.getClass());
				if (rowDefinition != null)
					for (CellDefinition cell : rowDefinition.getCells())
						if (col < 0)
							break;
						else if (col == 0)
							return cell.getValue(element);
						else
							col -= cell.getColumnSpan();

				if (col >= 0)
					return "";
				
				return "?";
    		}
    	
    	return null;
	}
	@Override
	public void doSetContentAt(int col, int row, Object value) {
    	if ((col <= 0) || (row <= 0))
    		return;

    	int index = row - 1; // toElementIndex(row);
    	ElementNodeFinder finder = DEFAULT_FINDER;
    	finder.find(getRootNode(), index);
    	
    	if (finder.successful())
			switch (col - finder.getDepth()) {
			case 1:
				// Collapsed
				if (Objects.equals(Boolean.TRUE, value))
					collapsedNodes.remove(finder.getPath());
				else
					collapsedNodes.add(finder.getPath());
				
				table.redraw();
				break;
			case 2:
				// Enabled
				if (Objects.equals(Boolean.TRUE, value))
					disabledNodes.remove(finder.getPath());
				else
					disabledNodes.add(finder.getPath());

				//table.redraw();
				break;
			}
	}
	@Override
	public KTableCellEditor doGetCellEditor(int col, int row) {
    	if ((col <= 0) || (row <= 0))
    		return null;

    	int index = row - 1; // toElementIndex(row);
    	ElementNodeFinder finder = DEFAULT_FINDER;
    	finder.find(getRootNode(), index);

    	if (finder.successful())
			switch (col - finder.getDepth()) {
			case 1:
				// Collapsed
				if (!finder.getNode().isLeaf())
					return checkableCellEditor;
				else
					return null;
			case 2:
				// Enabled
				if (finder.getNode().canBeDisabled())
					return checkableCellEditor;
			}
		
		return null;
	}
	@Override
	public KTableCellRenderer doGetCellRenderer(int col, int row) {
    	if ((row == 0) && (col == 0))
    		return settingsCellRenderer;
        if (row == 0)
            return columnHeaderRenderer;
    	if (col == 0)
    		return rowHeaderRenderer;

    	int index = row - 1; // toElementIndex(row);
    	ElementNodeFinder finder = DEFAULT_FINDER;
    	finder.find(getRootNode(), index);
    	
    	if (finder.successful())
    		if ((col -= finder.getDepth()) > 0) {
				switch (col) {
				case 1:
					// Collapsed
					if (!finder.getNode().isLeaf())
						return collapsableCellRenderer;
					else
						return clearCellRenderer;
				case 2:
					// Enabled
					if (finder.getNode().canBeDisabled())
						return checkableCellRenderer;
					
					break;
					
				default:
					if (finder.getNode().canBeDisabled())
						col--; // enabled button
						
					break;
				}

				// custom buttons, expandable button
				col -= 2;
				if (col < 0)
					return clearCellRenderer;
				
				Element element = finder.getNode().getElement();
				RowDefinition rowDefinition = rowDefinitions.get(element.getClass());
				if (rowDefinition != null)
					for (CellDefinition cell : rowDefinition.getCells())
						if (col < 0)
							break;
						else if (col == 0) {
							textCellRenderer.setBackground(rowDefinition.getColor());
							return textCellRenderer;
						}
						else
							col -= cell.getColumnSpan();

				if (col >= 0)
					return endOfLineCellRenderer;
	    	}

		return clearCellRenderer;
	}
    @Override
    public String doGetTooltipAt(int col, int row) {
    	if ((row == 0) && (col == 0))
    		return "click to expand menu";
    	if (col == 0)
    		return "unknown";
    	
        return null;
    }
	// fixed
    @Override
    public int getFixedHeaderColumnCount() {
        return 1;
    }
    @Override
    public int getFixedSelectableColumnCount() {
        return 0;
    }
    @Override
    public int getFixedHeaderRowCount() {
        return 1;
    }
    @Override
    public int getFixedSelectableRowCount() {
        return 0;
    }
	// heights/widths
	@Override
	public int getRowHeightMinimum() {
        return preferredSizeDefaultRowHeight;
	}
	@Override
	public int getInitialColumnWidth(int col) {
    	if (col < getFixedColumnCount())
    		return preferredSizeRowHeaderWidth;

    	return preferredSizeDefaultRowHeight;
	}
	@Override
	public int getInitialRowHeight(int row) {
    	if (row < getFixedHeaderRowCount())
    		return preferredSizeColumnHeaderHeight;
    	
    	return preferredSizeDefaultRowHeight;
	}
    @Override
    public boolean isColumnResizable(int col) {
        return false;
    }
    @Override
    public boolean isRowResizable(int row) {
        return false;
    }
	
    // button renderer
    private static abstract class ButtonRenderer implements KTableCellRenderer {
    	// fields
    	protected final KTable table;
    	private final int optimalWidth;
    	private final int buttonCount;
    	private final List<IButtonListener> listeners;
    	private final Image[] imagesDefault;
    	private final Image[] imagesMouseOver;
        // state
        private State state;
        private CellIndex activeCell;
        private CellIndex hitCell;
        private Point lastMousePosition;
    	
    	// construction
    	public ButtonRenderer(final KTable table, int optimalWidth, List<ButtonInfo> buttons) {
    		this.table = table;
    		this.optimalWidth = optimalWidth;

    		buttonCount = buttons.size();
    		listeners = new ArrayList<>(buttonCount);
    		imagesDefault = new Image[buttonCount];
    		imagesMouseOver = new Image[buttonCount];
    		for (int i = 0; i != buttonCount; i++) {
    			ButtonInfo buttonInfo = buttons.get(i);
    			listeners.add(buttonInfo.listener);
    			imagesDefault[i] = buttonInfo.imageDefault;
    			imagesMouseOver[i] = buttonInfo.imageMouseOver;
    		}

    		state = State.DEFAULT;
    		activeCell = null;
    		hitCell = null;
    		lastMousePosition = new Point(-1, -1);

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
        			IButtonListener listener = null;
        			
        			if (buttonCount == 1)
        				listener = listeners.get(0);
        			else if (buttonCount > 1) {
            			Rectangle rect = table.getCellRect(hitCell.col, hitCell.row);
            			
            			int index = buttonCount * (p.x - rect.x) / rect.width;
            			if (index < 0)
            				index = 0;
            			else if (index >= buttonCount)
            				index = buttonCount - 1;
        				
            			listener = listeners.get(index);
        			}

        			if (listener != null)
        				listener.onClicked(hitCell.col, hitCell.row);
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
            
            gc.setBackground(GRADIENT_COLOR_1A);
            gc.setForeground(GRADIENT_COLOR_1B);
            gc.fillGradientRectangle(
            		rect.x,
            		rect.y + offset,
            		rect.width,
            		rect.height - offset,
            		true);

            gc.setBackground(GRADIENT_COLOR_2A);
            gc.setForeground(GRADIENT_COLOR_2B);
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

	// state
	private static enum State {
		DEFAULT, MOUSE_OVER, MOUSE_DOWN
	}
	
	// key
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
	
	// classes
	public static class ButtonInfo {
    	// fields
    	public final Image imageDefault;
    	public final Image imageMouseOver;
    	public final IButtonListener listener;
    	
    	// construction
		public ButtonInfo(Image imageDefault, Image imageMouseOver, IButtonListener listener) {
			this.imageDefault = imageDefault;
			this.imageMouseOver = imageMouseOver;
			this.listener = listener;
		}
	}
	public static interface IButtonListener {
		public void onClicked(int col, int row);
	}

	// tree-node
	private class ElementNode {
		// fields
		private final Element element;
		private final boolean isBare;		// e.g. ConfigurationList and SampleList won't show up in AcquisitionTable
		private List<ElementNode> subNodes;	// if subNodes == null: it's a leaf node
		private Map<Element, ElementNode> lookup;
		private final List<ElementNode> unmodifiableList;
	    // element properties
	    private IDependencyProperty enabledProperty;
		private int index;
		private boolean enabled;
		// link
		private ElementNode link;
		
		// construction
		public ElementNode(Element element) {
			this.element = element;
			
			Set<IDependencyProperty> properties = element.getProperties();
			if (properties.isEmpty())
				isBare = true;
			else {
				boolean foundIndexProperty = false;
				for (IDependencyProperty property : properties)
					if (property == Element.INDEX)
						foundIndexProperty = true;
		        	else if (property.matches("enabled", Boolean.class))
		        		enabledProperty = property;
				
				isBare = foundIndexProperty && (properties.size() == 1);
			}

			index = element.getIndex();
			enabled = (enabledProperty == null) || (boolean)element.get(enabledProperty);
			
			element.addPropertyListener(new IElementPropertyListener() {
				@Override
				public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
					if (property == Element.INDEX)
						index = (int)newValue;
					else if (property == enabledProperty)
						enabled = (boolean)newValue;
					
					table.redraw(); // !!! only redraw necessary cell
				}
			});
			
			element.accept(new IElementVisitor() {
				@Override
				public <TElement extends Element> void visit(TElement element) {
					// won't have any subNodes
					subNodes = null;
					lookup = null;
				}
				@Override
				public <TElementList extends ElementList<TListElement>, TListElement extends Element> void visit(TElementList elementList) {
					subNodes = new ArrayList<>();
					lookup = new HashMap<>();
					
					elementList.addListListener(new IElementListListener<TListElement>() {
						@Override
						public void onAddedListElement(TListElement element) {
							ElementNode subNode = new ElementNode(element);
							subNode.setLink(link);
							
							subNodes.add(subNode);
							lookup.put(element, subNode);

							table.redraw();
						}
						@Override
						public void onDeletedListElement(TListElement element) {
							ElementNode subNode = lookup.remove(element);
							if (subNode != null) {
								subNode.resetLink();
								subNodes.remove(subNode);

								table.redraw();
							}
						}
					});
				}
			});
			
			unmodifiableList = subNodes == null ? null : Collections.unmodifiableList(subNodes);
		}
		
		// properties
		public Element getElement() {
			return element;
		}
		public boolean isLeaf() {
			return (subNodes == null) && (link == null);
		}
		public boolean isBare() {
			return isBare;
		}
		public List<ElementNode> getSubNodes() {
			if (subNodes != null)
				Collections.sort(subNodes, INDEX_COMPARATOR);
			return unmodifiableList;
		}
		public ElementNode getLink() {
			return link;
		}
		// element properties
		public String getElementName() {
			return element.getPath().getElementName();
		}
		public int getIndex() {
			//if (index != element.getIndex())
			//	System.out.print("index out of sync\r\n");
				
			//return element.getIndex();
			return index;
		}
		public boolean getEnabled() {
			return enabled;
		}
		public boolean canBeDisabled() {
			return enabledProperty != null;
		}
		
		// methods
		public void setLink(ElementNode link) {
			this.link = link;
			
			if (subNodes != null)
				for (ElementNode subNode : subNodes)
					subNode.setLink(link);
		}
		public void resetLink() {
			this.link = null;
			
			if (subNodes != null)
				for (ElementNode subNode : subNodes)
					subNode.resetLink();
		}
	}

	private class ElementNodeCounter {
		// fields
		private int count;
		
		// construction
		public ElementNodeCounter() {
			count = 0;
		}

		// properties
		private int getCount() {
			return count;
		}

		// methods
		public void count(ElementNode node) {
			count = 0;
			count("", node);
		}
		public void count(String root, ElementNode subNode) {
			count = 0;
			count(subNode, root);
		}
		// helpers
		private void count(ElementNode node, String root) {
			String path = root + "/" + node.getElementName();
			
			if (!node.isBare())
				count++;

			// collapsed node is still visible, but not its children
			if (collapsedNodes.contains(path))
				return;
			
			List<ElementNode> subNodes = node.getSubNodes();
			if (subNodes != null) {
				for (ElementNode subNode : subNodes)
					if (subNode.getEnabled())
						count(subNode, path);
			}
			else if (node.getLink() != null)
				count(node.getLink(), path);
		}
	}
	
	private class ElementNodeFinder {
		// fields
		private int index;
		// result
		private ElementNode node;
		private String path;
		private int depth;
		
		// construction
		public ElementNodeFinder() {
			find(null, -1);
		}
		
		// properties
		private boolean successful() {
			return node != null;
		}
		private ElementNode getNode() {
			return node;
		}
		private String getPath() {
			return path;
		}
		private int getDepth() {
			return depth;
		}
		
		// methods
		public void find(ElementNode node, int index) {
			this.index = index;
			this.node = null;
			path = null;
			depth = -1;
			
			if (index >= 0)
				search(node, "", 0);
		}
		// helpers
		private void search(ElementNode node, String root, int depth) {
			String path = root + "/" + node.getElementName();
			
			if (!node.isBare()) {
				if (index == 0) {
					this.node = node;
					this.path = path;
					this.depth = depth;
					return;
				}

				index--;
				depth++;
			}
			
			List<ElementNode> subNodes = node.getSubNodes();
			if (subNodes != null) {
				for (ElementNode subNode : subNodes)
					if (subNode.getEnabled()) {
						ElementNodeCounter counter = DEFAULT_COUNTER;
						counter.count(path, subNode);
						if (index < counter.getCount()) {
							search(subNode, path, depth);
							return;
						}
						else
							index -= counter.getCount();
					}
			}
			else if (node.getLink() != null)
				search(node.getLink(), path, depth);
		}
	}
	
	// comparator for element indices
    private static class NodeIndexComparator implements Comparator<ElementNode> {
    	// methods
		@Override
		public int compare(ElementNode node1, ElementNode node2) {
			return Integer.compare(node1.getIndex(), node2.getIndex());
		}
    }
    
    private static class ClearCellRenderer extends DefaultCellRenderer {
        // construction
    	public ClearCellRenderer() {
    		super(SWT.NONE);
    	}
    	
    	// methods
    	@Override
    	public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
    		return 1;
    	}
    	@Override
    	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean header, boolean clicked, KTableModel model) {
            gc.setBackground(getBackground());
            gc.fillRectangle(new Rectangle(rect.x, rect.y, rect.width + 1, rect.height + 1));
    	}
    }
    private static class EndOfLineRenderer extends DefaultCellRenderer {
        // construction
    	public EndOfLineRenderer() {
    		super(SWT.NONE);
    	}
    	
    	// methods
    	@Override
    	public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
    		return 1;
    	}
    	@Override
    	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean header, boolean clicked, KTableModel model) {
            gc.setBackground(getBackground());
            gc.fillRectangle(new Rectangle(rect.x, rect.y, rect.width + 1, rect.height));
    	}
    }
    
    public static class RowDefinition {
    	// fields
    	private final Class<? extends Element> elementType;
    	private final Color color;
    	private final List<CellDefinition> cells;
    	
    	// construction
    	public RowDefinition(Class<? extends Element> elementType, Color color, CellDefinition ... cells) {
    		this.elementType = elementType;
    		this.color = color;
    		this.cells = Collections.unmodifiableList(Arrays.asList(cells));
    	}
    	
    	// properties
    	public Class<? extends Element> getElementType() {
    		return elementType;
    	}
    	public Color getColor() {
    		return color;
    	}
    	public List<CellDefinition> getCells() {
    		return cells;
    	}
    }
    public static abstract class CellDefinition {
    	// fields
    	private int columnSpan;
    	
    	// construction
    	protected CellDefinition(int columnSpan) {
    		this.columnSpan = columnSpan;
    	}
    	
    	// properties
    	public int getColumnSpan() {
    		return columnSpan;
    	}
    	
    	// methods
    	public abstract String getValue(Element element);
    }
    public static class PropertyCellDefinition extends CellDefinition {
		// fields
    	private final IDependencyProperty property;

    	// construction
    	public PropertyCellDefinition(IDependencyProperty property, int columnSpan) {
			super(columnSpan);
			this.property = property;
		}
    	
    	// methods
		@Override
		public String getValue(Element element) {
			return String.valueOf(element.get(property));
		}
    }
    public static class FixedCellDefinition extends CellDefinition {
		// fields
    	private final String value;

    	// construction
    	public FixedCellDefinition(String value, int columnSpan) {
			super(columnSpan);
			this.value = value;
		}
    	
    	// methods
		@Override
		public String getValue(Element element) {
			return value;
		}
    }
}
