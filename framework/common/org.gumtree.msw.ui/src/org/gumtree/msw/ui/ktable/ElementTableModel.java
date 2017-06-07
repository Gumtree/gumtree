package org.gumtree.msw.ui.ktable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListListener;
import org.gumtree.msw.elements.IElementListener;
import org.gumtree.msw.ui.IModelValueConverter;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.internal.ListElementAdapter;
import org.gumtree.msw.ui.ktable.internal.TableCellEditorListener;
import org.gumtree.msw.ui.ktable.internal.TextModifyListener;
import org.gumtree.msw.ui.observable.ProxyElement;

import org.gumtree.msw.ui.ktable.ITableCellEditorListener;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;
import org.gumtree.msw.ui.ktable.KTableCellSelectionAdapter;
import org.gumtree.msw.ui.ktable.KTableDefaultModel;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;

public final class ElementTableModel<TElementList extends ElementList<TListElement>, TListElement extends Element> extends KTableDefaultModel {
    // fields
	private TElementList elementList;
	private final IElementListener disposedListener;
	private final IElementListListener<TListElement> listListener;
	// visible
    private final List<ColumnDefinition> columns; // e.g. INDEX, ENABLED, NAME, PHONE, EMAIL
    private final String buttonHeader;
    // if name was changed enabled is set to true
    private final int nameColumnIndex;
    private final IDependencyProperty nameProperty;
    private final IDependencyProperty enabledProperty;
    // used to update text background
    private final TextModifyListener textModifyListener;
    // content
    private final List<TListElement> elements;
    private final Map<TListElement, ElementListener> elementListeners;
    private final ProxyElement<TListElement> selectedElement;
	// table
	private final KTable table;
    private final KTableCellRenderer columnHeaderRenderer;
    private final KTableCellRenderer rowHeaderRenderer;
    private final KTableCellRenderer settingsCellRenderer;

    // construction
    public ElementTableModel(final KTable table, final Menu menu, String buttonHeader, final List<ButtonInfo<TListElement>> buttons, Iterable<ColumnDefinition> columns) {
        initialize();

        this.elementList = null;
        this.columns = new ArrayList<ColumnDefinition>();
        this.buttonHeader = buttonHeader;

        int nameColumnIndex = -1;
        IDependencyProperty nameProperty = null;	
        IDependencyProperty enabledProperty = null;
        
        textModifyListener = new TextModifyListener();
        ITableCellEditorListener cellEditorListener = new TableCellEditorListener(textModifyListener);
        Set<KTableCellEditor> editors = new HashSet<>(); // editors being listened to
        
        for (ColumnDefinition column : columns) {
        	for (CellDefinition cell : column.getCellDefinitions()) {
            	IDependencyProperty cellProperty = cell.getProperty();
            	if (cellProperty != null) {
                	if (cellProperty.matches("name", String.class)) {
                		nameColumnIndex = this.columns.size() + getFixedColumnCount();
                		nameProperty = cellProperty;
                	}
                	if (cellProperty.matches("enabled", Boolean.class))
                		enabledProperty = cellProperty;
            	}
            	
            	KTableCellEditor editor = cell.getCellEditor();
    			if (TableCellEditorListener.isValidEditor(editor) && !editors.contains(editor)) {
            		editor.addListener(cellEditorListener);
            		editors.add(editor);
        		}
        	}
        	this.columns.add(column);
        }
        
        this.nameColumnIndex = nameColumnIndex;
        this.nameProperty = nameProperty;
        this.enabledProperty = enabledProperty;
        
        // content
        this.elements = new ArrayList<>();
        this.elementListeners = new HashMap<>();
        this.selectedElement = new ProxyElement<>();
        
        // final
        this.table = table;
        this.table.setNumColsVisibleInPreferredSize(getColumnCount());
        this.table.setPreferredSizeDefaultRowHeight(KTableResources.ROW_HEIGHT);

    	final int firstColumnWidth = Resources.IMAGE_SETTINGS_DROPDOWN.getBounds().width;
    	
    	columnHeaderRenderer = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
    	rowHeaderRenderer = new ButtonRenderer(
    			table,
    			firstColumnWidth,
    			buttons) {
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
			@Override
			protected void clicked(int col, int row, int index) {
				IButtonListener<TListElement> listener = buttons.get(index).getListener();
				if (listener != null)
					listener.onClicked(col, row, toElement(row));
			}
    	};
    	settingsCellRenderer = new SettingsCellRenderer(table, firstColumnWidth, menu);
    	
    	table.addCellSelectionListener(new KTableCellSelectionAdapter() {
			@Override
			public void cellSelected(int col, int row, int statemask) {
				selectedElement.setTarget(toElement(row));
				table.redraw();
			}
		});

        // update model
    	disposedListener = new IElementListener() {
    		// methods
			@Override
			public void onDisposed() {
				updateSource(null);
			}
			@Override
			public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
				// ignore
			}
    	};
    	listListener = new ElementListListener();
    	updateSource(null);
    }
    
    // properties
    public ProxyElement<TListElement> getSelectedElement() {
    	return selectedElement;
    }
    
    // methods
    public void updateSource(TElementList elementList) {
    	if (this.elementList != null) {
    		if (this.elementList.isValid()) {
        		this.elementList.removeElementListener(disposedListener);
    	    	this.elementList.removeListListener(listListener);
    		}
    		this.elementList = null;
    	}

    	// remove all element listeners
    	for (ElementListener listener : elementListeners.values())
    		listener.getElement().removeElementListener(listener);

		elements.clear();
    	elementListeners.clear();
		selectedElement.setTarget(null);
		table.clearSelection();
		
		if (elementList != null) {
			table.setBackground(Resources.COLOR_DEFAULT);
			table.setEnabled(true);
			table.redraw();
			
			this.elementList = elementList;
			this.elementList.addElementListener(disposedListener);
			this.elementList.addListListener(listListener);
		}
		else {
			table.setBackground(Resources.COLOR_DISABLED);
			table.setEnabled(false);
			table.redraw();
		}

		selectedElement.setTarget(null);
		table.clearSelection();
    }
    @Override
    public int doGetRowCount() {
        return getFixedRowCount() + elements.size();
    }
    @Override
    public int doGetColumnCount() {
        return getFixedColumnCount() + columns.size();
    }
    @Override
    public Object doGetContentAt(int col, int row) {
    	if (!isValid(col, row))
    		return null;
    	if (col == 0)
    		return null;
    	if (row == 0)
    		return columns.get(toPropertyIndex(col)).getHeader();

    	int index = toElementIndex(row);
    	if (index < elements.size()) {
    		TListElement element = elements.get(index);
    		ColumnDefinition column = columns.get(toPropertyIndex(col));
    		CellDefinition cell = column.getCellDefinition(element.getClass());
    		
    		return cell.getValue(element);
    	}

    	return null;
    }
    @Override
    public void doSetContentAt(int col, int row, Object value) {
    	if (!isValid(col, row))
    		return;
    	if ((col == 0) || (row == 0))
    		return;
    	
    	int index = toElementIndex(row);
    	if (index < elements.size()) {
    		TListElement element = elements.get(index);
    		ColumnDefinition column = columns.get(toPropertyIndex(col));
    		CellDefinition cell = column.getCellDefinition(element.getClass());
			try {
		    	if (Objects.equals(cell.getValue(element), value))
		    		return;
		    	
		    	cell.setValue(element, value);

	    		// if name was changed set enabled to true
	    		if ((cell.getProperty() == nameProperty) && (enabledProperty != null))
	    			element.set(enabledProperty, true);
			}
			catch (Exception e) {
				// ignore cast exceptions
			}
    	}
    }
    @Override
    public KTableCellEditor doGetCellEditor(int col, int row) {
    	if (!isValid(col, row))
    		return null;
    	if ((col == 0) || (row == 0))
    		return null;

    	int index = toElementIndex(row);
    	if (index < elements.size()) {
    		TListElement element = elements.get(index);
        	ColumnDefinition column = columns.get(toPropertyIndex(col));
    		CellDefinition cell = column.getCellDefinition(element.getClass());
    		
    		textModifyListener.update(cell, new ListElementAdapter<>(element));
    		return cell.getCellEditor();
    	}

    	return null;
    }
    @Override
    public KTableCellRenderer doGetCellRenderer(int col, int row) {
    	if (!isValid(col, row))
    		return null;
    	if ((row == 0) && (col == 0))
    		return settingsCellRenderer;
        if (row == 0)
            return columnHeaderRenderer;
    	if (col == 0)
    		return rowHeaderRenderer;
    	
    	int index = toElementIndex(row);
    	if (index < elements.size()) {
    		TListElement element = elements.get(index);
        	ColumnDefinition column = columns.get(toPropertyIndex(col));
    		CellDefinition cell = column.getCellDefinition(element.getClass());
    		
    		return cell.getCellRenderer();
    	}
    	return null;

    	/*
    	int index = toElementIndex(row);
    	if ((index < elements.size()) && Objects.equals(false, elements.get(index).get(enabledProperty)))
			cellRenderer.setForeground(KTableResources.COLOR_DISABLED);
    	else
    		cellRenderer.setForeground(null);
    	*/
    }
    @Override
    public String doGetTooltipAt(int col, int row) {
    	if ((row == 0) && (col == 0))
    		return "click to expand menu";
    	if (col == 0)
    		return buttonHeader;
    	
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
        return KTableResources.ROW_HEIGHT;
    }
    @Override
    public int getInitialColumnWidth(int col) {
    	int propertyIndex = col - 1;
    	
    	if (col < getFixedColumnCount())
    		return KTableResources.ROW_HEADER_WIDTH;

    	return columns.get(propertyIndex).getWidth();
    }
    @Override
    public int getInitialRowHeight(int row) {
    	if (row < getFixedHeaderRowCount())
    		return KTableResources.COLUMN_HEADER_HEIGHT;
    	
    	return KTableResources.ROW_HEIGHT;
    }
    @Override
    public boolean isColumnResizable(int col) {
        return false;
    }
    @Override
    public boolean isRowResizable(int row) {
        return false;
    }
    // helpers
    public int toPropertyIndex(int col) {
    	int n = getFixedHeaderColumnCount();
    	if (col < n)
    		return -1;
    	
    	return col - n;
    }
    public int toRowIndex(TListElement element) {
    	int i = elements.indexOf(element);
    	if (i != -1)
    		i += getFixedHeaderRowCount();
    	
    	return i;
    }
    public int toElementIndex(int row) {
    	int n = getFixedHeaderRowCount();
    	if (row < n)
    		return -1;
    	
    	return row - n;
    }
    public TListElement toElement(int row) {
    	int index = toElementIndex(row);
    	if (index == -1)
    		return null;
    	
    	return elements.get(index);
    }

    // bug check
    private boolean isValid(int col, int row) {
    	return
    			(col >= 0) && (col < getColumnCount()) &&
    			(row >= 0) && (row < getRowCount());
    }
    
    // element/element-list listener
    private class ElementListener implements IElementListener {
    	// fields
    	private final TListElement element;
    	private final Map<IDependencyProperty, Integer> columnIndices;
    	
    	// construction
    	public ElementListener(TListElement element) {
    		this.element = element;
    		
    		columnIndices = new HashMap<>();
    		int index = getFixedColumnCount();
    		Class<? extends Element> elementType = element.getClass();
    		for (ColumnDefinition column : columns) {
    			CellDefinition cell = column.getCellDefinition(elementType);
    			columnIndices.put(cell.getProperty(), index++);
    		}
    	}
    	
    	// properties
		public TListElement getElement() {
			return element;
		}

		// methods
		@Override
		public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
			if (property == Element.INDEX) {
				Collections.sort(elements, Element.INDEX_COMPARATOR);
				table.redraw();
			}
			else if (columnIndices.containsKey(property)) {
				table.updateCell(
						columnIndices.get(property),
						toRowIndex(element));
			}
		}
		@Override
		public void onDisposed() {
			// ignored (look at elementList-Listener)
		}
    }
    private class ElementListListener implements IElementListListener<TListElement> {
		// methods
		@Override
		public void onAddedListElement(TListElement element) {
			ElementListener listener = new ElementListener(element);
			elements.add(element);
			elementListeners.put(element, listener);
			
			element.addElementListener(listener);
			
			Collections.sort(elements, Element.INDEX_COMPARATOR);
			
			if (table.getModel() == null)
				return;

			int col = Math.max(0, nameColumnIndex);
			int row = toRowIndex(element);
			selectedElement.setTarget(element);
			table.setSelection(col, row, true);
			table.redraw();
		}
		@Override
		public void onDeletedListElement(TListElement element) {
			TListElement target = selectedElement.getTarget();
			
			int i0 = elements.indexOf(target);
			int i1 = elements.indexOf(element);

			elements.remove(element);
			ElementListener listener = elementListeners.get(element);
			if (listener != null)
				listener.getElement().removeElementListener(listener);
			
			if (i1 == i0) {
				selectedElement.setTarget(null);
				table.clearSelection();
			}
			else if (i1 < i0) {
				int col, row;
				
				Point[] cellSelection = table.getCellSelection();
				if (cellSelection.length > 0)
					col = cellSelection[0].x;
				else
					col = Math.max(0, nameColumnIndex);
					
				row = toRowIndex(target);
				
				table.setSelection(col, row, true);
			}
			
			table.redraw();
		}
    }

    // definitions
    public static class CellDefinition implements IModelCellDefinition {
    	// fields
    	private final IDependencyProperty property;
    	private final DefaultCellRenderer cellRenderer;
    	private final KTableCellEditor cellEditor;
    	// converter
    	private final IModelValueConverter<?, ?> converter;

    	// construction
    	public <TElement extends Element, TModel>
    	CellDefinition(DependencyProperty<TElement, TModel> property, DefaultCellRenderer cellRenderer) {
    		this(property, cellRenderer, null, null);
    	}
    	public <TElement extends Element, TModel>
    	CellDefinition(DependencyProperty<TElement, TModel> property, DefaultCellRenderer cellRenderer, KTableCellEditor cellEditor) {
    		this(property, cellRenderer, cellEditor, null);
    	}
    	public <TElement extends Element, TModel>
    	CellDefinition(DependencyProperty<TElement, TModel> property, DefaultCellRenderer cellRenderer, KTableCellEditor cellEditor, IModelValueConverter<TModel, ?> converter) {
        	this.property = property;
        	this.cellRenderer = cellRenderer;
        	this.cellEditor = cellEditor;
    		this.converter = converter;
    	}

    	// properties
    	@Override
    	public IDependencyProperty getProperty() {
        	return property;
    	}
    	public DefaultCellRenderer getCellRenderer() {
        	return cellRenderer;
    	}
    	public KTableCellEditor getCellEditor() {
        	return cellEditor;
    	}

    	// methods
    	@Override
    	@SuppressWarnings("unchecked")
    	public Object convertFromModel(Object value) {
    		if (converter == null)
    			return value;
    		
    		return ((IModelValueConverter<Object, Object>)converter).fromModelValue(
    				converter.getModelValueType().cast(value));
    	}
    	@Override
    	@SuppressWarnings("unchecked")
    	public Object convertToModelValue(Object value) {
    		if (converter == null)
    			return value;
    		
    		return ((IModelValueConverter<Object, Object>)converter).toModelValue(
    				converter.getTargetValueType().cast(value));
    	}
    	// helpers
    	public <TElement extends Element>
    	Object getValue(TElement element) {
    		return convertFromModel(element.get(property));
    	}
    	public <TElement extends Element>
    	void setValue(TElement element, Object value) {
    		element.set(property, convertToModelValue(value));
    	}
    }
    
    public static class ColumnDefinition {
    	// fields
    	private final String header;
    	private final int width;
    	// cell definitions
    	private final Map<Class<? extends Element>, CellDefinition> cellDefinitions;
    	
    	// construction
    	public <TElement extends Element, TModel>
    	ColumnDefinition(String header, int width, DependencyProperty<TElement, TModel> property, DefaultCellRenderer cellRenderer) {
    		this(header, width, property, cellRenderer, null, null);
    	}
    	public <TElement extends Element, TModel>
    	ColumnDefinition(String header, int width, DependencyProperty<TElement, TModel> property, DefaultCellRenderer cellRenderer, KTableCellEditor cellEditor) {
    		this(header, width, property, cellRenderer, cellEditor, null);
    	}
    	public <TElement extends Element, TModel>
    	ColumnDefinition(String header, int width, DependencyProperty<TElement, TModel> property, DefaultCellRenderer cellRenderer, KTableCellEditor cellEditor, IModelValueConverter<TModel, ?> converter) {
        	this.header = header;
        	this.width = width;
        	this.cellDefinitions = new HashMap<>();
        	this.cellDefinitions.put(null, new CellDefinition(property, cellRenderer, cellEditor, converter));
    	}
    	public ColumnDefinition(String header, int width, Map<Class<? extends Element>, CellDefinition> cellDefinitions) {
        	this.header = header;
        	this.width = width;
        	this.cellDefinitions = new HashMap<>(cellDefinitions);
    	}
    	
    	// properties
    	public String getHeader() {
        	return header;
    	}
    	public int getWidth() {
        	return width;
    	}
    	public Iterable<CellDefinition> getCellDefinitions() {
    		return cellDefinitions.values();
    	}
    	public CellDefinition getCellDefinition(Class<? extends Element> elementType) {
    		CellDefinition result = cellDefinitions.get(elementType);
    		if (result == null)
    			return cellDefinitions.get(null);
    		
    		return result;
    	}
    }
}
