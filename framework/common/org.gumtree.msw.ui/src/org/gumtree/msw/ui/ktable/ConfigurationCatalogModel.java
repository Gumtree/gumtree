package org.gumtree.msw.ui.ktable;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import java.util.TimeZone;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.IModelListener;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementPropertyListener;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.internal.IElementAdapter;
import org.gumtree.msw.ui.ktable.internal.TableCellEditorListener;
import org.gumtree.msw.ui.ktable.internal.TextModifyListener;

import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;
import org.gumtree.msw.ui.ktable.KTableDefaultModel;
import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

public class ConfigurationCatalogModel extends KTableDefaultModel {
	// finals
	private static final int RESOURCE_NAME_COLUMN_SPAN = 20;
	private static final int DATE_COLUMNSPAN = 6;
	private static final String XML_EXTENSION = ".xml";
	
	// fields
	private final List<IListener> listeners = new ArrayList<>();
	// nodes
	private FolderInfo rootNode;
	private final List<ResourceInfo> rows;
	//
	private int dataColumnSpan;
	// collapsed
    private final List<Integer> vr2dr;	// visual-row -> data-row
    private final List<Integer> dr2vr;	// data-row -> visual-row
    private final Set<ResourceInfo> collapsedNodes;
    private final List<IFile> checkedNodes; // list is used to keep track of order of selection
    // used to update text background
    private final TextModifyListener textModifyListener;
	// table
	private final KTable table;
    private final KTableCellRenderer columnHeaderRenderer;
    private final KTableCellRenderer settingsCellRenderer;
    private final Map<Class<? extends ResourceInfo>, KTableCellRenderer> rowHeaderRenderers;

    private final KTableCellRenderer collapsableCellRenderer;
    private final KTableCellRenderer folderCellRenderer;
    private final KTableCellRenderer fileCellRenderer;
    private final KTableCellRenderer checkableCellRenderer;
    private final KTableCellEditor checkableCellEditor;
    private final DefaultCellRenderer clearCellRenderer;
    
    private final TextCellRenderer textCellRenderer;
    private final KTableCellEditor textCellEditor;
    private final KTableCellEditor readonlyTextCellEditor;
	
    // construction
    public ConfigurationCatalogModel(final KTable table, IFolder rootFolder, final Menu menu) {
        initialize();

        // final
        this.table = table;
        this.table.setNumColsVisibleInPreferredSize(getColumnCount());
        this.table.setPreferredSizeDefaultRowHeight(KTableResources.ROW_HEIGHT);

    	final int firstColumnWidth = Resources.IMAGE_SETTINGS_DROPDOWN.getBounds().width;
    	
    	columnHeaderRenderer = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
    	rowHeaderRenderers = new HashMap<>();
    	rowHeaderRenderers.put(FolderInfo.class, createFolderRowButtonRenderer(firstColumnWidth));
    	rowHeaderRenderers.put(FileInfo.class, createFileRowButtonRenderer(firstColumnWidth));

    	settingsCellRenderer = new SettingsCellRenderer(table, firstColumnWidth, menu);
    	textCellRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS);
    	
    	Color backgroundColor = SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND);
    	collapsableCellRenderer = new CheckableCellRenderer(
    			CheckableCellRenderer.INDICATION_FOCUS,
    			Resources.IMAGE_EXPANDED,
    			Resources.IMAGE_COLLAPSED,
    			backgroundColor,
    			DefaultCellRenderer.COLOR_LINE_LIGHTGRAY);
    	checkableCellRenderer = new CheckableCellRenderer(
    			CheckableCellRenderer.INDICATION_FOCUS,
    			backgroundColor,
    			DefaultCellRenderer.COLOR_LINE_LIGHTGRAY);

    	folderCellRenderer = new CheckableCellRenderer(
    			CheckableCellRenderer.INDICATION_FOCUS,
    			Resources.IMAGE_FOLDER,
    			Resources.IMAGE_FOLDER,
    			backgroundColor,
    			DefaultCellRenderer.COLOR_LINE_LIGHTGRAY);
    	
    	fileCellRenderer = new CheckableCellRenderer(
    			CheckableCellRenderer.INDICATION_FOCUS,
    			Resources.IMAGE_CONFIGURATION,
    			Resources.IMAGE_CONFIGURATION,
    			backgroundColor,
    			DefaultCellRenderer.COLOR_LINE_LIGHTGRAY);
    	
    	clearCellRenderer = new ClearCellRenderer();
    	
    	checkableCellEditor = new KTableCellEditorCheckbox();
    	readonlyTextCellEditor = new KTableCellEditorText2(SWT.READ_ONLY);

        textModifyListener = new TextModifyListener();
		textCellEditor = new KTableCellEditorText2();
		textCellEditor.addListener(new TableCellEditorListener(textModifyListener));
		
    	// load nodes
    	rows = new ArrayList<>();
    	
        vr2dr = new ArrayList<>();
        dr2vr = new ArrayList<>();
        
        checkedNodes = new ArrayList<>();
        collapsedNodes = new HashSet<>();
        
        setRoot(rootFolder);
    }
    private ButtonRenderer createFolderRowButtonRenderer(int optimalWidth) {
    	IButtonListener<FolderInfo> addButtonListener = new IButtonListener<FolderInfo>() {
			@Override
			public void onClicked(int col, int row, FolderInfo resource) {
			}
		};
    	IButtonListener<FolderInfo> duplicateButtonListener = new IButtonListener<FolderInfo>() {
			@Override
			public void onClicked(int col, int row, FolderInfo resource) {
			}
		};
    	IButtonListener<FolderInfo> deleteButtonListener = new IButtonListener<FolderInfo>() {
			@Override
			public void onClicked(int col, int row, FolderInfo resource) {
				MessageBox dialog = new MessageBox(table.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
				dialog.setText("Warning");
				dialog.setMessage("Do you really want to delete this folder and all its content?");
				if (dialog.open() == SWT.YES)
					resource.remove();
			}
		};
		
    	return createRowButtonRenderer(
    			optimalWidth,
    			FolderInfo.class,
    			Arrays.asList(
    	    			new ButtonInfo<FolderInfo>(Resources.IMAGE_PLUS_SMALL_GRAY, Resources.IMAGE_PLUS_SMALL, addButtonListener),
    	    			new ButtonInfo<FolderInfo>(Resources.IMAGE_COPY_SMALL_GRAY, Resources.IMAGE_COPY_SMALL, duplicateButtonListener),
    	    			new ButtonInfo<FolderInfo>(Resources.IMAGE_MINUS_SMALL_GRAY, Resources.IMAGE_MINUS_SMALL, deleteButtonListener)));
    }
    private ButtonRenderer createFileRowButtonRenderer(int optimalWidth) {
    	IButtonListener<FileInfo> addButtonListener = new IButtonListener<FileInfo>() {
			@Override
			public void onClicked(int col, int row, FileInfo resource) {
			}
		};
    	IButtonListener<FileInfo> duplicateButtonListener = new IButtonListener<FileInfo>() {
			@Override
			public void onClicked(int col, int row, FileInfo resource) {
			}
		};
    	IButtonListener<FileInfo> deleteButtonListener = new IButtonListener<FileInfo>() {
			@Override
			public void onClicked(int col, int row, FileInfo resource) {
				MessageBox dialog = new MessageBox(table.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
				dialog.setText("Warning");
				dialog.setMessage("Do you really want to delete this configuration?");
				if (dialog.open() == SWT.YES)
					resource.remove();
			}
		};
    	
    	return createRowButtonRenderer(
    			optimalWidth,
    			FileInfo.class,
    			Arrays.asList(
    	    			new ButtonInfo<FileInfo>(Resources.IMAGE_PLUS_SMALL_GRAY, Resources.IMAGE_PLUS_SMALL, addButtonListener),
    	    			new ButtonInfo<FileInfo>(Resources.IMAGE_COPY_SMALL_GRAY, Resources.IMAGE_COPY_SMALL, duplicateButtonListener),
    	    			new ButtonInfo<FileInfo>(Resources.IMAGE_MINUS_SMALL_GRAY, Resources.IMAGE_MINUS_SMALL, deleteButtonListener)));
    }
    private <T extends ResourceInfo>
    ButtonRenderer createRowButtonRenderer(int optimalWidth, Class<T> elementType, final List<ButtonInfo<T>> buttons) {
    	return new ButtonRenderer(
				table,
				optimalWidth,
				elementType,
				buttons) {
			@Override
			protected int isValidColumn(int x, int y) {
				Rectangle rect = table.getCellRect(0, 0);
				return (0 <= x) && (x < rect.width) ? 0 : -1;
			}
			@Override
			protected int isValidRow(int x, int y) {
				int row = table.getRowForY(y);
				if ((row > 0) && (elementType == getResourceInfo(row).getClass()))
					return row;

				return -1;
			}
			@Override
			@SuppressWarnings("unchecked")
			protected void clicked(int col, int row, int index) {
				if (col != 0)
					return;
				
				ResourceInfo resourceInfo = getResourceInfo(row);
				if (elementType == resourceInfo.getClass()) {
					buttons.get(index).getListener().onClicked(
							col,
							row,
							(T)resourceInfo);
				}
			}
    	};
    }

    // properties
    public Iterable<IFile> getSelected() {
    	return checkedNodes;
    }
    public void setRoot(IFolder root) {
    	checkedNodes.clear();
    	collapsedNodes.clear();
    	
    	IFolderInfoListener listener = new IFolderInfoListener() {
			@Override
			public void onResourceRemoved(ResourceInfo resource) {
				removeReferences(resource);
				rebuildIndicesAndCounts();
			}
			
			// helper
			private void removeReferences(ResourceInfo resource) {
				collapsedNodes.remove(resource);
				checkedNodes.remove(resource);
				
				if (resource instanceof FolderInfo) {
					FolderInfo folder = (FolderInfo)resource;
					
					for (FolderInfo folderInfo : folder.getFolders())
						removeReferences(folderInfo);
					
					for (FileInfo fileInfo : folder.getFiles())
						removeReferences(fileInfo);
				}
			}
    	};
    	
    	if (root != null) {
    		rootNode = new FolderInfo(root);
    		
    		addFolderListener(rootNode, listener);
        	collapseFolders(rootNode.getFolders());
    	}
    	else
    		rootNode = null;
    	
    	rebuildIndicesAndCounts();
    	table.redraw();
	}
    private void addFolderListener(FolderInfo folder, IFolderInfoListener listener) {
    	folder.addListener(listener);
    	for (FolderInfo subFolder : folder.getFolders())
    		addFolderListener(subFolder, listener);
    }
    private void collapseFolders(Iterable<FolderInfo> folders) {
    	for (FolderInfo folder : folders) {
    		collapsedNodes.add(folder);
    		collapseFolders(folder.getFolders());
    	}
    }
    
	// determine depth, index and count for each node
	private void rebuildIndicesAndCounts() {
		rows.clear();
		
		updateNodes(0, 0, rootNode);
		cleanCollapsedNodes();
		
		// determine how many columns are needed
		int columnSpan = 0;
		for (ResourceInfo resourceInfo : rows)
			columnSpan = Math.max(columnSpan, resourceInfo.getDepth() + RESOURCE_NAME_COLUMN_SPAN);
			
		if (dataColumnSpan != columnSpan) {
			dataColumnSpan = columnSpan;
			table.setNumColsVisibleInPreferredSize(getColumnCount());
		}
	}
	private int updateNodes(int index, int depth, FolderInfo folder) {
		int nodeIndex = index;
		if (index == rows.size())
			rows.add(folder);
		else
			rows.set(index, folder);
		index++;
		
		// sort any sub nodes
		folder.sort(ResourceNameComparator.DEFAULT, ResourceNameComparator.DEFAULT);
		
		int nextDepth = folder.getOwner() != null ? depth + 1 : depth;
		for (FolderInfo subFolder : folder.getFolders())
			index += updateNodes(index, nextDepth, subFolder);
		for (FileInfo subFile : folder.getFiles())
			index += updateNodes(index, nextDepth, subFile);

		int totalCount = index - nodeIndex;
		int ancestorCount = totalCount - 1;
		folder.update(depth, nodeIndex, ancestorCount);
		return totalCount;
	}
	private int updateNodes(int index, int depth, FileInfo file) {
		int nodeIndex = index;
		if (index == rows.size())
			rows.add(file);
		else
			rows.set(index, file);
		index++;
		
		int totalCount = index - nodeIndex;
		int ancestorCount = totalCount - 1;
		file.update(depth, nodeIndex, ancestorCount);
		return totalCount;
	}
	private void cleanCollapsedNodes() {
        // rebuild map: visual-row -> data-row (hide invisible nodes)
        vr2dr.clear();
        dr2vr.clear();
        
        while (dr2vr.size() != rows.size()) {
        	ResourceInfo row = rows.get(dr2vr.size());
    		if (row.getIndex() != dr2vr.size())
    			throw new Error("inconsistent row index");

    		if (row.getOwner() != null) {
        		dr2vr.add(vr2dr.size());
        		vr2dr.add(row.getIndex());
    		}
    		else {
        		// don't show root folder
        		dr2vr.add(-1);
    		}
        }

		if (collapsedNodes.isEmpty())
			return;

		// find existing and collapsed nodes
		TreeMap<Integer, ResourceInfo> newCollapsedNodes = new TreeMap<>();
    	for (ResourceInfo node : collapsedNodes)
    		if (rows.contains(node))
    			newCollapsedNodes.put(node.getIndex(), node);
        
        // collapse nodes (top-down to accelerate process)
        collapsedNodes.clear();
    	for (ResourceInfo node : newCollapsedNodes.values())
    		collapsedNode(node, false);
	}
	
    private ResourceInfo getResourceInfo(int row) {
    	int index = row - getFixedRowCount();
    	return rows.get(vr2dr.get(index));
    }
	private void collapsedNode(ResourceInfo node, boolean refresh) {
		if (collapsedNodes.add(node)) {
			int row = node.getIndex();
			int anc = node.getAncestorCount();
			
			// check if node has ancestors to collapse
			if (anc == 0)
				return;
			
			int vr = dr2vr.get(row);

			// check if node is visible
			if (vr == -1)
				return;

			int ancFirst = row + 1;
			int ancEnd  = ancFirst + anc;
			if (ancEnd > dr2vr.size())
				throw new Error();

			int vrFirst = vr + 1;
			int vrEnd   = vrFirst + anc;
			if (vrEnd > vr2dr.size())
				vrEnd = vr2dr.size();
			
			for (int i = vrFirst; i < vrEnd; i++)
				if (vr2dr.get(i) >= ancEnd) {
					vrEnd = i;
					break;
				}
			
			// should never happen
			if (vrFirst >= vrEnd)
				return;
			
			// update dr2vr
			int n = vrEnd - vrFirst;
			for (int i = ancFirst; i < ancEnd; i++)
				dr2vr.set(i, -1);
			for (int i = ancEnd; i < dr2vr.size(); i++) {
				int oldIndex = dr2vr.get(i);
				if (oldIndex != -1)
					dr2vr.set(i, oldIndex - n);
			}
			
			// update vr2dr
			vr2dr.subList(vrFirst, vrEnd).clear();
			
			// System.out.println("collapsedNode");
			if (refresh)
				table.redraw();
		}
	}
	private void expandNode(ResourceInfo node, boolean refresh) {
		if (collapsedNodes.remove(node)) {
			cleanCollapsedNodes();
			if (refresh)
				table.redraw();
		}
	}
	
	// methods
    @Override
    public int doGetRowCount() {
        return getFixedRowCount() + vr2dr.size();
    }
    @Override
    public int doGetColumnCount() {
        return
        		getFixedColumnCount()
        		+ 2						// collapsed + icon
        		+ dataColumnSpan
        		+ DATE_COLUMNSPAN;
    }
    @Override
    public Point doBelongsToCell(int col, int row) {
    	if (!isValid(col, row))
    		return new Point(col, row);

    	int dateColumn = doGetColumnCount() - DATE_COLUMNSPAN;
    	
    	if (col == 0)
    		return new Point(0, row);
    	if (col >= dateColumn)
    		return new Point(dateColumn, row);
    	if (row == 0)
    		return new Point(1, row);

    	ResourceInfo nodeInfo = getResourceInfo(row);

		int colTarget = col;
		if ((col -= nodeInfo.getDepth()) > 0) {
			switch (col) {
			case 1: // Collapsed/Checked
			case 2: // Icon
				return new Point(colTarget, row);
			}

			// custom buttons, expandable, icon button
			col -= 3;
			
			if (col >= 0)
		        return new Point(colTarget - col, row);
		}

        return new Point(colTarget, row);
    }
	@Override
	public Object doGetContentAt(int col, int row) {
    	if (!isValid(col, row))
    		return null;

    	if (col == 0)
    		return null;
    	if (row == 0) {
        	int dateColumn = doGetColumnCount() - DATE_COLUMNSPAN;
        	if (col >= dateColumn)
        		return "Date";
        	else
        		return "Name";
    	}

    	ResourceInfo nodeInfo = getResourceInfo(row);
    	
    	if (col == doGetColumnCount() - DATE_COLUMNSPAN)
    		return nodeInfo.getDate();
	
		if ((col -= nodeInfo.getDepth()) > 0) {
			switch (col) {
			case 1: // Collapsed/Checked
			case 2: // Icon
				if (nodeInfo instanceof FolderInfo) {
					if (!nodeInfo.isLeaf())
						return !collapsedNodes.contains(nodeInfo);
					else
						return null;
				}
				
				if (nodeInfo instanceof FileInfo)
					return checkedNodes.contains(nodeInfo.getResource());
			}

			String name = nodeInfo.getName();
			if ((nodeInfo instanceof FileInfo) && (name.toLowerCase().endsWith(XML_EXTENSION)))
				name = name.substring(0, name.length() - XML_EXTENSION.length());
			
			return name;
		}
    	
    	return null;
	}
	@Override
	public void doSetContentAt(int col, int row, Object value) {
    	if (!isValid(col, row))
    		return;
    	if ((col == 0) || (row == 0))
    		return;

    	ResourceInfo nodeInfo = getResourceInfo(row);

		if ((col -= nodeInfo.getDepth()) > 0) {
			switch (col) {
			case 1: // Collapsed/Checked
			case 2: // Icon
				if (nodeInfo instanceof FolderInfo) {
					if (Objects.equals(Boolean.TRUE, value))
						expandNode(nodeInfo, true);
					else
						collapsedNode(nodeInfo, true);
				}
				if (nodeInfo instanceof FileInfo) {
					FileInfo fileInfo = (FileInfo)nodeInfo;
					if (Objects.equals(Boolean.TRUE, value))
						checkedNodes.add(fileInfo.getResource());
					else
						checkedNodes.remove(fileInfo.getResource());
					
					for (IListener listener : listeners)
						listener.onSelectionChanged(checkedNodes.size());
				}
				return;
			}

			// custom buttons, expandable, icon button
			col -= 3;
			
			if ((col == 0) && (value instanceof String))
				//nodeInfo.setName((String)value);
				return;
		}
	}
	@Override
	public KTableCellEditor doGetCellEditor(int col, int row) {
    	if (!isValid(col, row))
    		return null;
    	if ((col == 0) || (row == 0))
    		return null;

    	final ResourceInfo nodeInfo = getResourceInfo(row);

    	if (col == doGetColumnCount() - DATE_COLUMNSPAN)
    		return readonlyTextCellEditor;
    	
		if ((col -= nodeInfo.getDepth()) > 0) {
			switch (col) {
			case 1: // Collapsed/Checked
				if (nodeInfo instanceof FileInfo) {
					return checkableCellEditor;
				}
				// break; // intentionally
			case 2: // Icon
				if (nodeInfo instanceof FolderInfo) {
					if (!nodeInfo.isLeaf())
						return checkableCellEditor;
					else
						return null;
				}
			}

			// custom buttons, expandable, icon button
			col -= 3;
	    	
			if (col == 0) {
		    	textModifyListener.update(
		    			new IModelCellDefinition() {
							@Override
							public IDependencyProperty getProperty() {
								return null;
							}
							@Override
							public KTableCellRenderer getCellRenderer() {
								return null;
							}
							@Override
							public KTableCellEditor getCellEditor() {
								return null;
							}

							@Override
							public Object convertFromModel(Object value) {
								if ((nodeInfo instanceof FileInfo) && (value instanceof String)) {
									String str = (String)value;
									if (str.toLowerCase().endsWith(XML_EXTENSION))
										return str.substring(0, str.length() - XML_EXTENSION.length());
								}
								
								return value;
							}
							@Override
							public Object convertToModelValue(Object value) {
								if ((nodeInfo instanceof FileInfo) && (value instanceof String)) {
									String str = (String)value;
									if (!str.toLowerCase().endsWith(XML_EXTENSION))
										return str + XML_EXTENSION;
								}
								
								return value;
							}
		    			},
		    			new IElementAdapter() {
							@Override
							public Object get(IDependencyProperty property) {
								return nodeInfo.getName();
							}
							@Override
							public void addPropertyListener(IElementPropertyListener listener) {
							}
							@Override
							public boolean removePropertyListener(IElementPropertyListener listener) {
								return false;
							}
		    			});
		    	
				//return textCellEditor;
		    	return readonlyTextCellEditor;
			}
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

    	ResourceInfo nodeInfo = getResourceInfo(row);
    	
    	if (col == 0)
    		return rowHeaderRenderers.get(nodeInfo.getClass());
    	if (col == doGetColumnCount() - DATE_COLUMNSPAN)
    		return textCellRenderer;
	
		if ((col -= nodeInfo.getDepth()) > 0) {
			switch (col) {
			case 1:
				// Collapsed/Checked
				if (nodeInfo instanceof FolderInfo) {
					if (!nodeInfo.isLeaf())
						return collapsableCellRenderer;
					else
						return clearCellRenderer;
				}
				if (nodeInfo instanceof FileInfo) {
					return checkableCellRenderer;
				}
				
			case 2:
				// Icon
				if (nodeInfo instanceof FolderInfo)
					return folderCellRenderer;
				if (nodeInfo instanceof FileInfo)
					return fileCellRenderer;
				
				return clearCellRenderer;
			}

			// custom buttons, expandable, icon button
			col -= 3;

			if (col == 0)
				return textCellRenderer;
    	}

		return clearCellRenderer;
	}
    @Override
    public String doGetTooltipAt(int col, int row) {
    	if ((row == 0) && (col == 0))
    		return "click to expand menu";
    	if (col == 0) {
        	ResourceInfo nodeInfo = getResourceInfo(row);
        	if (nodeInfo instanceof FolderInfo)
        		return "add, duplicate or delete folder";
        	if (nodeInfo instanceof FileInfo)
        		return "add, duplicate or delete configuration";
    	}
    	
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
    	if (col < getFixedColumnCount())
    		return KTableResources.ROW_HEADER_WIDTH;

    	return KTableResources.ROW_HEIGHT + 2;
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
	// listeners
	public synchronized void addListener(IListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
	}
	public synchronized boolean removeListener(IListener listener) {
		return listeners.remove(listener);
	}
	
    // bug check
    private boolean isValid(int col, int row) {
    	return
    			(col >= 0) || (col < getColumnCount()) ||
    			(row >= 0) || (row < getRowCount());
    }
    
	//////////////////////////////////////////////
    
	private static abstract class ResourceInfo {
		// fields
		private final FolderInfo owner;
		private final IResource resource;
		// cached
		private int depth;
		private int index;
		private int ancestorCount; // count of all ancestors
		
		// construction
		public ResourceInfo(FolderInfo owner, IResource resource) {
			this.owner = owner;
			this.resource = resource;
		}

		// properties
		public FolderInfo getOwner() {
			return owner;
		}
		public IResource getResource() {
			return resource;
		}
		// depth and count
		public int getDepth() {
			return depth;
		}
		public int getIndex() {
			return index;
		}
		public int getAncestorCount() {
			return ancestorCount;
		}
		public boolean isLeaf() {
			return ancestorCount == 0;
		}
		// info
		public String getName() {
			return resource.getName();
		}
		public String getDate() {
			return "";
		}
		
		// methods
		public void update(int depth, int index, int ancestorCount) {
			this.depth = depth;
			this.index = index;
			this.ancestorCount = ancestorCount;
		}
		public void remove() {
			try {
				resource.delete(true, new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
				return;
			}
			
			if (owner != null)
				owner.remove(this);
		}
		protected void onRemovedAncestor() {
			ancestorCount--;
		}
	}
	
	private static class FolderInfo extends ResourceInfo {
		// fields
		private final List<FolderInfo> folders;
		private final List<FileInfo> files;
		private final List<IFolderInfoListener> listeners;
		
		// construction
		public FolderInfo(IFolder resource) {
			this(null, resource);
		}
		private FolderInfo(FolderInfo owner, IFolder resource) {
			super(owner, resource);
			
			folders = new ArrayList<>();
			files = new ArrayList<>();
			listeners = new ArrayList<>();
			
			try {
				for (IResource member : resource.members())
					if (member instanceof IFolder)
						folders.add(new FolderInfo(this, (IFolder)member));
					else if ((member instanceof IFile) && member.getName().endsWith(XML_EXTENSION))
						files.add(new FileInfo(this, (IFile)member));
			} catch (CoreException e) {
			}
		}

		// properties
		public Iterable<FolderInfo> getFolders() {
			return folders;
		}
		public Iterable<FileInfo> getFiles() {
			return files;
		}

		// methods
		public boolean sort(Comparator<ResourceInfo> folderComparator, Comparator<ResourceInfo> fileComparator) {
			Collections.sort(folders, folderComparator);
			Collections.sort(files, fileComparator);
			
			for (FolderInfo folder : folders)
				folder.sort(folderComparator, fileComparator);
			
			return true;
		}
		protected void remove(ResourceInfo resource) {
			boolean removed = false;
			if (resource instanceof FolderInfo)
				removed = folders.remove(resource);
			else if (resource instanceof FileInfo)
				removed = files.remove(resource);
			
			if (!removed)
				throw new Error("node not found");

			onRemovedAncestor();
			for (IFolderInfoListener listener : listeners)
				listener.onResourceRemoved(resource);
		}
		// listeners
		public void addListener(IFolderInfoListener listener) {
			if (listeners.contains(listener))
				throw new Error("listener already exists");
			
			listeners.add(listener);	
		}
		public boolean removeListener(IFolderInfoListener listener) {
			return listeners.remove(listener);
		}
	}
	
	private static class FileInfo extends ResourceInfo {
		// finals
		private static final DateFormat FORMATTER;
		
		// fields
		//private final long creationTime;
		private final long lastModifiedTime;
		private final String lastModifiedTimeString;
		
		// construction
		static {
			FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			FORMATTER.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
		}
		private FileInfo(FolderInfo owner, IFile resource) {
			super(owner, resource);
			
			File file = new File(resource.getLocationURI());
			lastModifiedTime = file.lastModified();
			
			lastModifiedTimeString = FORMATTER.format(lastModifiedTime);
				
			/*
			BasicFileAttributes attributes = Files.readAttributes(
					Paths.get(resource.getLocationURI()),
					BasicFileAttributes.class);

			creationTime = attributes.creationTime();
			lastModifiedTime = attributes.lastModifiedTime();
			*/
		}

		// properties
		@Override
		public IFile getResource() {
			return (IFile)super.getResource();
		}
		//public FileTime getCreationTime() {
		//	return creationTime;
		//}
		public long getLastModifiedTime() {
			return lastModifiedTime;
		}
		// info
		@Override
		public String getDate() {
			return lastModifiedTimeString;
		}
	}
	
	private static interface IFolderInfoListener {
    	// methods
    	public void onResourceRemoved(ResourceInfo resource);
	}
	
	private static class ResourceNameComparator implements Comparator<ResourceInfo> {
		// finals
		public static final Comparator<ResourceInfo> DEFAULT = new ResourceNameComparator();
		
		// methods
		@Override
		public int compare(ResourceInfo a, ResourceInfo b) {
			return a.getName().compareToIgnoreCase(b.getName());
		}
	}
	private static class FileDateComparator implements Comparator<FileInfo> {
		// finals
		public static final Comparator<FileInfo> DEFAULT = new FileDateComparator();
		
		// methods
		@Override
		public int compare(FileInfo a, FileInfo b) {
			return Long.compare(a.getLastModifiedTime(), b.getLastModifiedTime());
		}
	}
	
	//////////////////////////////////////////////

    // cell renderer
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
            gc.fillRectangle(new Rectangle(rect.x, rect.y, rect.width + 1, rect.height));
    	}
    }

    //
    public static interface IListener {
    	// methods
    	public void onSelectionChanged(int count);
    }
}
