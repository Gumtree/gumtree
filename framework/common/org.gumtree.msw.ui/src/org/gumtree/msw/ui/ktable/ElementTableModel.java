package org.gumtree.msw.ui.ktable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import org.gumtree.msw.elements.IElementPropertyListener;
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
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;

public final class ElementTableModel<TElementList extends ElementList<TListElement>, TListElement extends Element> extends KTableDefaultModel {
    // fields
    private final List<ColumnDefinition> columns; // e.g. INDEX, ENABLED, NAME, PHONE, EMAIL
    private final String buttonHeader;
    // if name was changed enabled is set to true
    private final IDependencyProperty nameProperty;
    private final IDependencyProperty enabledProperty;
    // used to update text background
    private final TextModifyListener textModifyListener;
    // content
    private final List<TListElement> elements;
    private final ProxyElement<TListElement> selectedElement;
	// table
	private final KTable table;
    private final KTableCellRenderer columnHeaderRenderer;
    private final KTableCellRenderer rowHeaderRenderer;
    private final KTableCellRenderer settingsCellRenderer;

    // construction
    public ElementTableModel(final KTable table, TElementList elementList, final Menu menu, String buttonHeader, final List<ButtonInfo<TListElement>> buttons, Iterable<ColumnDefinition> columns) {
        initialize();

        this.columns = new ArrayList<ColumnDefinition>();
        this.buttonHeader = buttonHeader;

        IDependencyProperty nameProperty = null;	
        IDependencyProperty enabledProperty = null;
        
        textModifyListener = new TextModifyListener();
        ITableCellEditorListener cellEditorListener = new TableCellEditorListener(textModifyListener);
        Set<KTableCellEditor> editors = new HashSet<>(); // editors being listened to
        
        for (ColumnDefinition column : columns) {
        	this.columns.add(column);
        	
        	IDependencyProperty columnProperty = column.getProperty();
        	if (columnProperty.matches("name", String.class))
        		nameProperty = columnProperty;
        	if (columnProperty.matches("enabled", Boolean.class))
        		enabledProperty = columnProperty;
        	
        	KTableCellEditor editor = column.getCellEditor();
			if (TableCellEditorListener.isValidEditor(editor) && !editors.contains(editor)) {
        		editor.addListener(cellEditorListener);
        		editors.add(editor);
    		}
        }
        
        this.nameProperty = nameProperty;
        this.enabledProperty = enabledProperty;
        
        // content
        this.elements = new ArrayList<TListElement>();
        this.selectedElement = new ProxyElement<TListElement>();
        
        // final
        this.table = table;
        this.table.setNumColsVisibleInPreferredSize(getColumnCount());
        this.table.setPreferredSizeDefaultRowHeight(KTableResources.ROW_HEIGHT);

    	final int firstColumnWidth = Resources.IMAGE_SETTINGS_DROPDOWN.getBounds().width;
    	
    	columnHeaderRenderer = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
    	rowHeaderRenderer = new ButtonRenderer(
    			table,
    			firstColumnWidth,
    			null,
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
        elementList.addListListener(new ElementListListener());
    }

    // properties
    public ProxyElement<TListElement> getSelectedElement() {
    	return selectedElement;
    }
    
    // methods
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
    		ColumnDefinition column = columns.get(toPropertyIndex(col));
    		IDependencyProperty property = column.getProperty();
    		
    		Object value = elements.get(index).get(property);
    		return column.convertFromModel(value);
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
    		ColumnDefinition column = columns.get(toPropertyIndex(col));
    		IDependencyProperty property = column.getProperty();
			try {
		    	if (Objects.equals(doGetContentAt(col, row), value))
		    		return;

	    		TListElement element = elements.get(index);
	    		element.set(property, column.convertToModelValue(value));

	    		// if name was changed set enabled to true
	    		if ((property == nameProperty) && (enabledProperty != null))
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

    	ColumnDefinition column = columns.get(toPropertyIndex(col));
    	textModifyListener.update(column, new ListElementAdapter<>(toElement(row)));
    	return column.getCellEditor();
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

    	return columns.get(toPropertyIndex(col)).getCellRenderer();
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
    public int toColumnIndex(IDependencyProperty property) {
    	for (int i = 0, n = columns.size(); i != n; i++)
    		if (columns.get(i).getProperty() == property)
    			return getFixedHeaderColumnCount() + i;
    	
    	return -1;
    }
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
    			(col >= 0) || (col < getColumnCount()) ||
    			(row >= 0) || (row < getRowCount());
    }
    
    // element/element-list listener
    private class ElementListener implements IElementPropertyListener {
    	// fields
    	private final TListElement element;
    	
    	// construction
    	public ElementListener(TListElement element) {
    		this.element = element;
    	}
    	
		// methods
		@Override
		public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
			if (property != Element.INDEX)
				table.updateCell(
						toColumnIndex(property),
						toRowIndex(element));
			else {
				Collections.sort(elements, Element.INDEX_COMPARATOR);
				table.redraw();
			}
		}
    }
    private class ElementListListener implements IElementListListener<TListElement> {
		// methods
		@Override
		public void onAddedListElement(TListElement element) {
			elements.add(element);
			element.addPropertyListener(new ElementListener(element));
			
			Collections.sort(elements, Element.INDEX_COMPARATOR);
			
			if (table.getModel() == null)
				return;

			int col = Math.max(0, toColumnIndex(nameProperty));
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
					col = Math.max(0, toColumnIndex(nameProperty));
					
				row = toRowIndex(target);
				
				table.setSelection(col, row, true);
			}
			
			table.redraw();
		}
    }

    // definitions
    public static class ColumnDefinition implements IModelCellDefinition {
    	// fields
    	private final IDependencyProperty property;
    	private final String header;
    	private final int width;
    	// cell
    	private final KTableCellRenderer cellRenderer;
    	private final KTableCellEditor cellEditor;
    	// converter
    	private final IModelValueConverter<?, ?> converter;
    	
    	// construction
    	public <TElement extends Element, TModel>
    	ColumnDefinition(DependencyProperty<TElement, TModel> property, String header, int width, KTableCellRenderer cellRenderer) {
    		this(property, header, width, cellRenderer, null, null);
    	}
    	public <TElement extends Element, TModel>
    	ColumnDefinition(DependencyProperty<TElement, TModel> property, String header, int width, KTableCellRenderer cellRenderer, KTableCellEditor cellEditor) {
    		this(property, header, width, cellRenderer, cellEditor, null);
    	}
    	public <TElement extends Element, TModel>
    	ColumnDefinition(DependencyProperty<TElement, TModel> property, String header, int width, KTableCellRenderer cellRenderer, KTableCellEditor cellEditor, IModelValueConverter<TModel, ?> converter) {
        	this.property = property;
        	this.header = header;
        	this.width = width;
        	this.cellRenderer = cellRenderer;
        	this.cellEditor = cellEditor;
    		this.converter = converter;
    	}
    	
    	// properties
    	public String getHeader() {
        	return header;
    	}
    	public int getWidth() {
        	return width;
    	}
    	@Override
    	public IDependencyProperty getProperty() {
        	return property;
    	}
    	@Override
    	public KTableCellRenderer getCellRenderer() {
        	return cellRenderer;
    	}
    	@Override
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
    }
}
