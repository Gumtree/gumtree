package org.gumtree.msw.ui.ktable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListener;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.internal.IElementAdapter;
import org.gumtree.msw.ui.ktable.internal.TableCellEditorListener;
import org.gumtree.msw.ui.ktable.internal.TextModifyListener;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;
import org.gumtree.msw.ui.util.AlphanumericComparator;

public class FilesystemModel extends KTableDefaultModel {
	// finals
	private static final int NAME_COLUMN_SPAN = 20;
	private static final int DATE_COLUMN_SPAN = 6;
	// date to string
	private static final DateFormat FORMATTER;
	// allowed characters
	private static final Pattern VALID_NAME = Pattern.compile("[\\w\\(\\)\\[\\],;!#%$=\\+\\-]([\\w\\(\\)\\[\\],;!#%$=\\+\\-\\s.]*[\\w\\(\\)\\[\\],;!#%$=\\+\\-])?");

	private static enum SortState {
		NONE,
		ASCENDING,
		DESCENDING
	}
	
	// fields
	private final Pattern fileFilter;
	private final String newFileName;
	private final String newFileExt;
	private final byte[] newFileContent; // content of a new file
	private final List<IListener> listeners = new ArrayList<>();
	// content
	private FolderInfo rootNode;
	private final List<ResourceInfo> rows;
	// listening
	private final FileInfo.IListener fileListener;
	private final FolderInfo.IListener folderListener;
	private final ResourceInfo.IVisitor resourceTreeInstaller;
	private final ResourceInfo.IVisitor resourceTreeUninstaller;
	//
	private int dataColumnSpan;
	// collapsed
    private final List<Integer> vr2dr;	// visual-row -> data-row
    private final List<Integer> dr2vr;	// data-row -> visual-row
    private final Set<FolderInfo> collapsedNodes;
    private final List<FileInfo> checkedNodes; // list is used to keep track of order of selection
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
    private final TextCellRenderer folderDateCellRenderer;
    private final KTableCellEditor textCellEditor;
    private final KTableCellEditor readonlyTextCellEditor;
	
    // construction
	static {
		FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		FORMATTER.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
	}
    public FilesystemModel(final KTable table, Pattern fileFilter, String newFileName, String newFileExt, byte[] newFileContent) {
    	this.fileFilter = fileFilter;
    	this.newFileName = newFileName;
    	this.newFileExt = newFileExt;
    	this.newFileContent = newFileContent;
    	
        initialize();
        
        // final
        this.table = table;
        this.table.setNumColsVisibleInPreferredSize(getColumnCount());
        this.table.setPreferredSizeDefaultRowHeight(KTableResources.ROW_HEIGHT);

    	final int firstColumnWidth = Resources.IMAGE_SETTINGS_DROPDOWN.getBounds().width;
    	
    	Menu menu = createMenu(table.getParent());
    	
    	columnHeaderRenderer = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
    	rowHeaderRenderers = new HashMap<>();
    	rowHeaderRenderers.put(FolderInfo.class, createFolderRowButtonRenderer(firstColumnWidth));
    	rowHeaderRenderers.put(FileInfo.class, createFileRowButtonRenderer(firstColumnWidth));

    	settingsCellRenderer = new SettingsCellRenderer(table, firstColumnWidth, menu);
    	textCellRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS);
		folderDateCellRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS);
		folderDateCellRenderer.setForeground(DefaultCellRenderer.COLOR_LINE_DARKGRAY);
    	
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
        
        resourceTreeInstaller = new ResourceInfo.IVisitor() {
			@Override
			public void visit(FileInfo info) {
				info.addListener(fileListener);
			}
			@Override
			public void visit(FolderInfo info) {
				info.addListener(folderListener);
				
				for (FolderInfo folderInfo : info.getFolders())
					folderInfo.accept(this);
				
				for (FileInfo fileInfo : info.getFiles())
					fileInfo.accept(this);
			}
		};
		resourceTreeUninstaller = new ResourceInfo.IVisitor() {
			@Override
			public void visit(FileInfo info) {
				removeReferences(info);
				info.removeListener(fileListener);
			}
			@Override
			public void visit(FolderInfo info) {
				removeReferences(info);
				info.removeListener(folderListener);
				
				for (FolderInfo folderInfo : info.getFolders())
					folderInfo.accept(this);
				
				for (FileInfo fileInfo : info.getFiles())
					fileInfo.accept(this);
			}
			// helper
			private void removeReferences(ResourceInfo info) {
				collapsedNodes.remove(info);
				checkedNodes.remove(info);
			}
		};
		
		fileListener = new FileInfo.IListener() {
			@Override
			public void onNameChanged(FileInfo sender) {
				redrawResource(sender);
			}
			@Override
			public void onExtensionChanged(FileInfo sender) {
				redrawResource(sender);
			}
			@Override
			public void onDateChanged(FileInfo sender) {
				redrawResource(sender);
			}
		};
		folderListener = new FolderInfo.IListener() {
			@Override
			public void onNameChanged(FolderInfo sender) {
				redrawResource(sender);
			}
			@Override
			public void onDateChanged(FolderInfo sender) {
				redrawResource(sender);
			}
			@Override
			public void onInserted(FolderInfo sender, ResourceInfo info) {
				info.accept(resourceTreeInstaller);
				rebuildIndicesAndCounts();

				rootNode.updateDates();
				table.redraw();
			}
			@Override
			public void onRemoved(FolderInfo sender, ResourceInfo info) {
				info.accept(resourceTreeUninstaller);
				rebuildIndicesAndCounts();

				rootNode.updateDates();
				table.redraw();
			}
			@Override
			public void onRefreshed(FolderInfo sender, Collection<ResourceInfo> insertedResources, Collection<ResourceInfo> removedResources) {
				for (ResourceInfo info : insertedResources)
					info.accept(resourceTreeInstaller);
				for (ResourceInfo info : removedResources)
					info.accept(resourceTreeUninstaller);
			}
		};
    }
    private Menu createMenu(Control parent) {
		Menu menu = new Menu(parent);
	    MenuItem menuItem;

	    // add new
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Add File"); 
	    menuItem.setImage(Resources.IMAGE_PLUS);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (rootNode != null)
					selectResource(rootNode.newFile(newFileName, newFileExt, newFileContent));
			}
		});
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Add Folder"); 
	    menuItem.setImage(Resources.IMAGE_FOLDER);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (rootNode != null)
					selectResource(rootNode.newFolder(newFileName));
			}
		});
		
	    // selection
	    new MenuItem(menu, SWT.SEPARATOR);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Select All"); 
	    menuItem.setImage(Resources.IMAGE_BOX_CHECKED);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				selectAll();
			}
		});
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Clear Selection"); 
	    menuItem.setImage(Resources.IMAGE_BOX_UNCHECKED);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				selectNone();
			}
		});

	    // sort
	    new MenuItem(menu, SWT.SEPARATOR);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Sort by Name"); 
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (rootNode != null) {
					rootNode.sortByName();
			    	rebuildIndicesAndCounts();
			    	table.redraw();
				}		
			}
		});
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Sort by Date"); 
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (rootNode != null) {
					rootNode.sortByDate();
			    	rebuildIndicesAndCounts();
			    	table.redraw();
				}		
			}
		});
		
		// refresh
	    new MenuItem(menu, SWT.SEPARATOR);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Refresh"); 
	    menuItem.setImage(Resources.IMAGE_REFRESH);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (rootNode != null) {
					rootNode.refresh();
			    	rebuildIndicesAndCounts();
			    	cleanCheckedNodes();
			    	table.redraw();
				}		
			}
		});
		
    	return menu;
    }
    private ButtonRenderer createFolderRowButtonRenderer(int optimalWidth) {
    	IButtonListener<FolderInfo> addButtonListener = new IButtonListener<FolderInfo>() {
			@Override
			public void onClicked(int col, int row, FolderInfo resource) {
				NewResourceDialog dialog = new NewResourceDialog(table.getShell());
				switch (dialog.open()) {
				case NewResourceDialog.FILE_ID:
					selectResource(resource.newFile(newFileName, newFileExt, newFileContent));
					break;
				case NewResourceDialog.FOLDER_ID:
					selectResource(resource.newFolder(newFileName));
					break;
				}
			}
		};
    	IButtonListener<FolderInfo> duplicateButtonListener = new IButtonListener<FolderInfo>() {
			@Override
			public void onClicked(int col, int row, FolderInfo resource) {
				selectResource(resource.duplicate());
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
				NewResourceDialog dialog = new NewResourceDialog(table.getShell());
				switch (dialog.open()) {
				case NewResourceDialog.FILE_ID:
					selectResource(resource.getOwner().newFile(newFileName, newFileExt, newFileContent, resource.getOwner().indexOf(resource)));
					break;
				case NewResourceDialog.FOLDER_ID:
					selectResource(resource.getOwner().newFolder(newFileName));
					break;
				}
			}
		};
    	IButtonListener<FileInfo> duplicateButtonListener = new IButtonListener<FileInfo>() {
			@Override
			public void onClicked(int col, int row, FileInfo resource) {
				selectResource(resource.duplicate());
			}
		};
    	IButtonListener<FileInfo> deleteButtonListener = new IButtonListener<FileInfo>() {
			@Override
			public void onClicked(int col, int row, FileInfo resource) {
				MessageBox dialog = new MessageBox(table.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
				dialog.setText("Warning");
				dialog.setMessage("Do you really want to delete this file?");
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
    ButtonRenderer createRowButtonRenderer(int optimalWidth, final Class<T> elementType, final List<ButtonInfo<T>> buttons) {
    	return new ButtonRenderer(
				table,
				optimalWidth,
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
    public int getSelectedCount() {
    	return checkedNodes.size();
    }
    public Iterable<Path> getSelectedFiles() {
    	if (checkedNodes.isEmpty())
    		return Collections.emptyList();
    	
    	List<Path> selected = new ArrayList<>(checkedNodes.size());
    	for (FileInfo file : checkedNodes)
    		selected.add(rootNode.getPath().relativize(file.getPath()));
    	
    	return selected;
    }
    public void setRoot(Path root) {
    	collapsedNodes.clear();
    	checkedNodes.clear();
    	
    	if (rootNode != null)
    		rootNode.accept(resourceTreeUninstaller);
    	
    	if (root != null) {
    		rootNode = new FolderInfo(root, fileFilter);
    		rootNode.updateDates();
    		rootNode.sort();
    		
    		// collapse all folders
    		rootNode.accept(new ResourceInfo.IVisitor() {
				@Override
				public void visit(FolderInfo info) {
			    	for (FolderInfo f : info.getFolders()) {
			    		collapsedNodes.add(f);
			    		f.accept(this);
			    	}
				}
				@Override
				public void visit(FileInfo info) {
					// ignore
				}
			});
    		rootNode.accept(resourceTreeInstaller);
    	}
    	else
    		rootNode = null;
    	
    	rebuildIndicesAndCounts();
    	table.redraw();
	}
    
    // determine depth, index and count for each node
	private void rebuildIndicesAndCounts() {
		rows.clear();
		
		if (rootNode != null)
			updateNodes(0, 0, rootNode);
		
		cleanCollapsedNodes();
		
		// determine how many columns are needed
		int columnSpan = 0;
		for (ResourceInfo resourceInfo : rows)
			columnSpan = Math.max(columnSpan, resourceInfo.getDepth() + NAME_COLUMN_SPAN);
			
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
		
		int totalCount = index - nodeIndex; // = 1;
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
		TreeMap<Integer, FolderInfo> newCollapsedNodes = new TreeMap<>();
    	for (FolderInfo node : collapsedNodes)
    		if (rows.contains(node))
    			newCollapsedNodes.put(node.getIndex(), node);
        
        // collapse nodes (top-down to accelerate process)
        collapsedNodes.clear();
    	for (FolderInfo node : newCollapsedNodes.values())
    		collapsedNode(node, false);
	}
	private void cleanCheckedNodes() {
		for (int i = checkedNodes.size() - 1; i >= 0; i--)
			if (!rows.contains(checkedNodes.get(i)))
				checkedNodes.remove(i);
	}
	
    private ResourceInfo getResourceInfo(int row) {
    	int index = row - getFixedRowCount();
    	return rows.get(vr2dr.get(index));
    }
    private void redrawResource(ResourceInfo info) {
    	int index = info.getIndex();
    	if ((index >= 0) && (index < dr2vr.size()))
    		table.redraw(0, getFixedRowCount() + dr2vr.get(index), doGetColumnCount(), 1);
    }
    private void selectResource(ResourceInfo info) {
    	if (info == null)
    		table.clearSelection();
    	else {
    		// ensure that selection is visible
    		FolderInfo owner = info.getOwner();
    		while (owner != null) {
    			collapsedNodes.remove(owner);
    			owner = owner.getOwner();
    		}
    		
    		rebuildIndicesAndCounts();
    		
	    	int index = info.getIndex();
	    	if ((index >= 0) && (index < dr2vr.size()))
				table.setSelection(info.getDepth() + 3, getFixedRowCount() + dr2vr.get(index), true);
    	}
    }
	private void collapsedNode(FolderInfo node, boolean refresh) {
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
	private void expandNode(FolderInfo node, boolean refresh) {
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
        		+ DATE_COLUMN_SPAN;
    }
    @Override
    public Point doBelongsToCell(int col, int row) {
    	if (!isValid(col, row))
    		return new Point(col, row);

    	int dateColumn = doGetColumnCount() - DATE_COLUMN_SPAN;
    	
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
        	int dateColumn = doGetColumnCount() - DATE_COLUMN_SPAN;
        	if (col < dateColumn)
        		return "Name";
        	else
        		return "Date";
    	}

    	ResourceInfo nodeInfo = getResourceInfo(row);
    	
    	if (col == doGetColumnCount() - DATE_COLUMN_SPAN)
    		if (nodeInfo.getDate() == 0)
    			return "";
    		else
    			return FORMATTER.format(nodeInfo.getDate());
	
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
					return checkedNodes.contains(nodeInfo);
				
				return null;
			}

			return nodeInfo.getName();
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
				if (nodeInfo instanceof FileInfo) {
					FileInfo fileInfo = (FileInfo)nodeInfo;
					if (Objects.equals(Boolean.TRUE, value))
						checkedNodes.add(fileInfo);
					else
						checkedNodes.remove(fileInfo);
					
					for (IListener listener : listeners)
						listener.onSelectionChanged(checkedNodes.size());
				}
				// break; // intentionally
			case 2: // Icon
				if (nodeInfo instanceof FolderInfo) {
					if (Objects.equals(Boolean.TRUE, value))
						expandNode((FolderInfo)nodeInfo, true);
					else
						collapsedNode((FolderInfo)nodeInfo, true);
				}
				return;
			}

			// custom buttons, expandable, icon button
			col -= 3;
			
			if ((col == 0) && (value instanceof String))
				nodeInfo.setName((String)value);
				return;
		}
	}
	@Override
	public KTableCellEditor doGetCellEditor(int col, int row) {
    	if (!isValid(col, row))
    		return null;

    	if (col == 0)
    		return null;
    	if (row == 0) {
    		if (rootNode != null) {
            	int dateColumn = doGetColumnCount() - DATE_COLUMN_SPAN;
            	if (col < dateColumn)
            		rootNode.sortByName();
            	else
            		rootNode.sortByDate();
            	
		    	rebuildIndicesAndCounts();
		    	table.redraw();
    		}
    		return null;
    	}
    	
    	final ResourceInfo nodeInfo = getResourceInfo(row);

    	if (col == doGetColumnCount() - DATE_COLUMN_SPAN)
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
						return checkableCellEditor; // clicking on Collapsed/Checked or on Folder icon
				}
				return null;
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
							public Object convertFromModel(Object value) {
								return value;
							}
							@Override
							public Object convertToModelValue(Object value) {
								return value;
							}
		    			},
		    			new IElementAdapter() {
							@Override
							public Object get(IDependencyProperty property) {
								return nodeInfo.getName();
							}
							@Override
							public boolean validate(IDependencyProperty property, Object newValue) {
								if (newValue instanceof String)
									return VALID_NAME.matcher(((String)newValue).trim()).matches();
								
								return false;
							}
							@Override
							public void addPropertyListener(IElementListener listener) {
							}
							@Override
							public boolean removePropertyListener(IElementListener listener) {
								return false;
							}
		    			});
		    	
				return textCellEditor;
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
    	if (col == doGetColumnCount() - DATE_COLUMN_SPAN)
    		// resource date
    		if (nodeInfo instanceof FolderInfo)
        		return folderDateCellRenderer;
    		else
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
				return clearCellRenderer;
				
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
				return textCellRenderer; // resource name
    	}

		return clearCellRenderer;
	}
    @Override
    public String doGetTooltipAt(int col, int row) {
    	if ((row == 0) && (col == 0))
    		return "click to expand menu";
    	if (col == 0) {
        	ResourceInfo nodeInfo = getResourceInfo(row);
        	if (nodeInfo instanceof FileInfo)
        		return "add, duplicate or delete file";
        	if (nodeInfo instanceof FolderInfo)
        		return "add, duplicate or delete folder";
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
	// custom
    public void selectAll() {
        checkedNodes.clear();
        
        for (ResourceInfo resource : rows)
        	if (resource instanceof FileInfo)
        		checkedNodes.add((FileInfo)resource);

		for (IListener listener : listeners)
			listener.onSelectionChanged(checkedNodes.size());

    	table.redraw();
    }
    public void selectNone() {
    	if (!checkedNodes.isEmpty()) {
            checkedNodes.clear();

    		for (IListener listener : listeners)
    			listener.onSelectionChanged(checkedNodes.size());

        	table.redraw();
    	}
    }
    
    // listeners
	public void addListener(IListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
	}
	public boolean removeListener(IListener listener) {
		return listeners.remove(listener);
	}
	
    // bug check
    private boolean isValid(int col, int row) {
    	return
    			(col >= 0) && (col < getColumnCount()) &&
    			(row >= 0) && (row < getRowCount());
    }
    
	//////////////////////////////////////////////

	private static abstract class ResourceInfo {
		// fields
		private final FolderInfo owner;
		private Path path;				// changes when this resource or an owner is renamed
		// cached (for display only)
		private int depth;
		private int index;
		private int ancestorCount; // count of all ancestors
		
		// construction
		public ResourceInfo(FolderInfo owner) {
			this.owner = owner;
		}
		public void refresh() {
			refresh(path);
		}
		protected void refresh(Path path) {
			this.path = path;
		}
		
		// properties
		public FolderInfo getOwner() {
			return owner;
		}
		public Path getPath() {
			return path;
		}
		public abstract String getName();	// may hide file extension
		public void setName(String value) {
			if ((owner != null) && (value != null) && !Objects.equals(getName(), value))
				owner.rename(this, value);
		}
		public abstract long getDate(); // folder's date is equivalent to latest file date (or zero if there are no files)
		// cached (for display only)
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
		
		// methods
		public ResourceInfo duplicate() {
			if (owner != null)
				return owner.duplicate(this);
			
			return null;
		}
		public void remove() {
			if (owner != null)
				owner.remove(this);
		}
		// internal
		public abstract void accept(IVisitor visitor);
		public void update(int depth, int index, int ancestorCount) {
			this.depth = depth;
			this.index = index;
			this.ancestorCount = ancestorCount;
		}
	
		// visitor pattern
		public static interface IVisitor {
			// methods
			void visit(FileInfo info);
			void visit(FolderInfo info);
		}
	}
	private static final class FileInfo extends ResourceInfo {
		// fields
		private String name;
		private String extension;
		private long date;
		// 
		private final List<IListener> listeners = new ArrayList<>();
		
		// construction
		public FileInfo(FolderInfo owner, Path path) {
			super(owner);
			refresh(path);
		}
		@Override
		protected void refresh(Path path) {
			File file = path.toFile();
			String newName = getNameWithoutExtension(file);
			String newExtension = getExtension(file);
			long newDate = file.lastModified();
			
			// assign new content
			super.refresh(path);

			boolean nameChanged = !Objects.equals(name, newName);
			boolean extensionChanged = !Objects.equals(extension, newExtension);
			boolean dateChanged = date != newDate;

			name = newName;
			extension = newExtension;
			date = newDate;
			
			if (nameChanged)
				for (IListener listener : listeners)
					listener.onNameChanged(this);

			if (extensionChanged)
				for (IListener listener : listeners)
					listener.onExtensionChanged(this);
			
			if (dateChanged)
				for (IListener listener : listeners)
					listener.onDateChanged(this);
		}

		// properties
		@Override
		public String getName() {
			return name;
		}
		public String getExtension() {
			return extension;
		}
		@Override
		public long getDate() {
			return date;
		}
		
		// visitor pattern
		@Override
		public void accept(IVisitor visitor) {
			visitor.visit(this);
		}
	    // listeners
		public void addListener(IListener listener) {
			if (listeners.contains(listener))
				throw new Error("listener already exists");
			
			listeners.add(listener);
		}
		public boolean removeListener(IListener listener) {
			return listeners.remove(listener);
		}
		
		// helper
		private static String getNameWithoutExtension(File file) {
			String name = file.getName();
			int i = name.lastIndexOf('.');
			if (i != -1)
				return name.substring(0, i);
			
			return name;
		}
		private static String getExtension(File file) {
			String name = file.getName();
			int i = name.lastIndexOf('.');
			if (i != -1)
				return name.substring(i);
			
			return "";
		}

		// listener interface
		public static interface IListener {
			// methods
			void onNameChanged(FileInfo sender);
			void onExtensionChanged(FileInfo sender);
			void onDateChanged(FileInfo sender);
		}
	}
	private static final class FolderInfo extends ResourceInfo {
		// fields
		private String name;
		private long date; // buffered
		private final Pattern fileFilter;
		// content
		private List<FileInfo> files;
		private List<FolderInfo> folders;
		// raw-name to resource
		private Map<String, FileInfo> fileLookup;
		private Map<String, FolderInfo> folderLookup;
		// 
		private final List<IListener> listeners = new ArrayList<>();
		
		// construction
		public FolderInfo(Path path, Pattern fileFilter) {
			this(null, path, fileFilter);
		}
		public FolderInfo(FolderInfo owner, Path path, Pattern fileFilter) {
			super(owner);
			this.fileFilter = fileFilter;
			
			files = new ArrayList<>();
			folders = new ArrayList<>();
			fileLookup = new HashMap<>();
			folderLookup = new HashMap<>();

			refresh(path);
		}
		@Override
		public void refresh(Path path) {
			String newName = path.getFileName().toString();

			SortStateHelper stateHelper = determineSortState();
			
			List<FileInfo> newFiles = new ArrayList<>();
			List<FolderInfo> newFolders = new ArrayList<>();
			Map<String, FileInfo> newFileLookup = new HashMap<>();
			Map<String, FolderInfo> newFolderLookup = new HashMap<>();
			
			Collection<ResourceInfo> insertedResources = new ArrayList<>();

	        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
	            for (Path p : stream) {
	            	String s = p.getFileName().toString();
	            	
	            	if (Files.isDirectory(p)) {
	            		// directory
	            		if (!newFolderLookup.containsKey(s)) {
							FolderInfo info = folderLookup.remove(s);
							if (info != null)
								info.refresh(p);
							else {
								info = new FolderInfo(this, p, fileFilter);
								insertedResources.add(info);
							}
							
							newFolders.add(info);
							newFolderLookup.put(s, info);
	            		}
	            	}
	            	else if (Files.isRegularFile(p) && fileFilter.matcher(s).matches()) {
	            		// file
	            		if (!newFileLookup.containsKey(s)) {
		            		FileInfo info = fileLookup.remove(s);
							if (info != null) 
								info.refresh(p);
							else {
								info = new FileInfo(this, p);
								insertedResources.add(info);
							}
							
							newFiles.add(info);
							newFileLookup.put(s, info);
	            		}
	            	}
	            }
	        }
	        catch (IOException ex) {
	        	// ignore
	        }
			
			// assign new content
			super.refresh(path);
			
			// all remaining file- and folder-resources are discarded
			Collection<ResourceInfo> removedResources = new ArrayList<>();
			removedResources.addAll(fileLookup.values());
			removedResources.addAll(folderLookup.values());
			
			files = newFiles;
			folders = newFolders;
			fileLookup = newFileLookup;
			folderLookup = newFolderLookup;
			
			// restore sort state
			if (stateHelper.isSorted())
				switch (stateHelper.getNameSortState()) {
				case ASCENDING:
					sort(ResourceNameComparator.DEFAULT_ASCENDING);
					break;
				case DESCENDING:
					sort(ResourceNameComparator.DEFAULT_DESCENDING);
					break;
				default:
					switch (stateHelper.getDateSortState()) {
					case ASCENDING:
						sort(ResourceDateComparator.DEFAULT_ASCENDING);
						break;
					case DESCENDING:
						sort(ResourceDateComparator.DEFAULT_DESCENDING);
						break;
					default:
						// ignore
						break;
					}
				}

			if (!Objects.equals(this.name, newName)) {
				name = newName;
				for (IListener listener : listeners)
					listener.onNameChanged(this);
			}

			for (IListener listener : listeners)
				listener.onRefreshed(this, insertedResources, removedResources);
		}
		
		// properties
		@Override
		public String getName() {
			return name;
		}
		@Override
		public long getDate() {
			return date;
		}
		// sub resources
		public Iterable<FileInfo> getFiles() {
			return files;
		}
		public Iterable<FolderInfo> getFolders() {
			return folders;
		}
		
		// helpers
		public void updateDates() {
			long newDate = 0;
			for (FolderInfo folder : folders) {
				folder.updateDates();
				newDate = Math.max(newDate, folder.getDate());
			}
			for (FileInfo file : files) {
				newDate = Math.max(newDate, file.getDate());
			}
			
			if (date != newDate) {
				date = newDate;

				for (IListener listener : listeners)
					listener.onDateChanged(this);
			}
		}
		
		// methods
		public int indexOf(FileInfo file) {
			return files.indexOf(file);
		}
		@SuppressWarnings("unused")
		public int indexOf(FolderInfo folder) {
			return folders.indexOf(folder);
		}
		// content
		public FileInfo newFile(String name, String extension, byte[] content) {
			return newFile(name, extension, content, files.size());
		}
		public FileInfo newFile(String name, String extension, byte[] content, int index) {
			String validName = findValidName(name, files);
			if (validName != null)
				try {
					Path path = getPath().resolve(String.format("%s%s", validName, extension));
					try (OutputStream outStream = new FileOutputStream(path.toFile())) {
					    outStream.write(content);
					}

					FileInfo info = new FileInfo(this, path);
					files.add(index, info);
					fileLookup.put(path.getFileName().toString(), info);
					
					for (IListener listener : listeners)
						listener.onInserted(this, info);
					
					return info;
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			
			return null;
		}
		public FolderInfo newFolder(String name) {
			return newFolder(name, folders.size());
		}
		public FolderInfo newFolder(String name, int index) {
			String validName = findValidName(name, folders);
			if (validName != null)
				try {
					Path path = getPath().resolve(validName);
					Files.createDirectories(path);
					
					FolderInfo info = new FolderInfo(this, path, fileFilter);
					folders.add(index, info);
					folderLookup.put(path.getFileName().toString(), info);

					for (IListener listener : listeners)
						listener.onInserted(this, info);
					
					return info;
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			
			return null;
		}
		public void rename(ResourceInfo resourceInfo, final String name) {
			if (Objects.equals(resourceInfo.getName(), name.trim()))
				return;

			final String sourceKey = resourceInfo.getPath().getFileName().toString();
			resourceInfo.accept(new IVisitor() {
				@Override
				public void visit(FileInfo info) {
					if (!fileLookup.containsKey(sourceKey))
						return;

					if (fileLookup.get(sourceKey) != info)
						throw new Error();

					String validName = findValidName(name, files);
					if (validName != null) {
						Path path = getPath().resolve(String.format("%s%s", validName, info.getExtension()));
						String targetKey = path.getFileName().toString();

						if (info.getPath().toFile().renameTo(path.toFile())) {
							fileLookup.put(targetKey, fileLookup.remove(sourceKey));
							info.refresh(path);
						}
					}
				}
				@Override
				public void visit(FolderInfo info) {
					if (!folderLookup.containsKey(sourceKey))
						return;

					if (folderLookup.get(sourceKey) != info)
						throw new Error();

					String validName = findValidName(name, folders);
					if (validName != null) {
						Path path = getPath().resolve(validName);
						String targetKey = path.getFileName().toString();

						if (info.getPath().toFile().renameTo(path.toFile())) {
							folderLookup.put(targetKey, folderLookup.remove(sourceKey));
							info.refresh(path);
						}
					}
				}
			});
		}
		public ResourceInfo duplicate(ResourceInfo resourceInfo) {
			final FolderInfo root = this;
			final String sourceKey = resourceInfo.getPath().getFileName().toString();
			final ResourceInfo[] result = new ResourceInfo[] { null };
			resourceInfo.accept(new IVisitor() {
				@Override
				public void visit(FileInfo sourceInfo) {
					if (!fileLookup.containsKey(sourceKey))
						return;

					if (fileLookup.get(sourceKey) != sourceInfo)
						throw new Error();

					String validName = findValidName(sourceInfo.getName(), files);
					if (validName != null) {
						Path targetPath = getPath().resolve(String.format("%s%s", validName, sourceInfo.getExtension()));
						String targetKey = targetPath.getFileName().toString();

						try {
							FileUtils.copyFile(sourceInfo.getPath().toFile(), targetPath.toFile(), true); // preserveFileDate
						}
						catch (IOException e) {
							e.printStackTrace();
							return;
						}
						
						FileInfo newInfo = new FileInfo(root, targetPath);
						files.add(files.indexOf(sourceInfo) + 1, newInfo);
						fileLookup.put(targetKey, newInfo);
						result[0] = newInfo;
						
						for (IListener listener : listeners)
							listener.onInserted(root, newInfo);
					}
				}
				@Override
				public void visit(FolderInfo sourceInfo) {
					if (!folderLookup.containsKey(sourceKey))
						return;

					if (folderLookup.get(sourceKey) != sourceInfo)
						throw new Error();

					String validName = findValidName(sourceInfo.getName(), folders);
					if (validName != null) {
						Path targetPath = getPath().resolve(validName);
						String targetKey = targetPath.getFileName().toString();

						try {
							FileUtils.copyDirectory(sourceInfo.getPath().toFile(), targetPath.toFile(), true); // preserveFileDate
						}
						catch (IOException e) {
							e.printStackTrace();
							return;
						}
						
						FolderInfo newInfo = new FolderInfo(root, targetPath, fileFilter);
						folders.add(folders.indexOf(sourceInfo) + 1, newInfo);
						folderLookup.put(targetKey, newInfo);
						result[0] = newInfo;

						for (IListener listener : listeners)
							listener.onInserted(root, newInfo);
					}
				}
			});
			return result[0];
		}
		public void remove(ResourceInfo resourceInfo) {
			final FolderInfo root = this;
			final String key = resourceInfo.getPath().getFileName().toString();
			resourceInfo.accept(new IVisitor() {
				@Override
				public void visit(FileInfo info) {
					if (!fileLookup.containsKey(key))
						return;

					if (fileLookup.get(key) != info)
						throw new Error();

					if (FileUtils.deleteQuietly(info.getPath().toFile())) {
						files.remove(info);
						fileLookup.remove(key);
					}
					
					for (IListener listener : listeners)
						listener.onRemoved(root, info);
				}
				@Override
				public void visit(FolderInfo info) {
					if (!folderLookup.containsKey(key))
						return;

					if (folderLookup.get(key) != info)
						throw new Error();

					if (FileUtils.deleteQuietly(info.getPath().toFile())) {
						folders.remove(info);
						folderLookup.remove(key);
					}
					
					for (IListener listener : listeners)
						listener.onRemoved(root, info);
				}
			});
		}
		// visitor pattern
		@Override
		public void accept(IVisitor visitor) {
			visitor.visit(this);
		}
	    // listeners
		public void addListener(IListener listener) {
			if (listeners.contains(listener))
				throw new Error("listener already exists");
			
			listeners.add(listener);
		}
		public boolean removeListener(IListener listener) {
			return listeners.remove(listener);
		}
		
		// helpers
		private static String findValidName(String name, List<? extends ResourceInfo> resources) {
			if (name == null)
				return null;
			
			name = name.trim();			
			if (!VALID_NAME.matcher(name).matches())
				return null;
			
			int index = 1;
			String finalName = name;

			boolean restart = true;
			while (restart) {
				restart = false;
				for (ResourceInfo resource : resources)
					if (Objects.equals(resource.getName(), finalName)) {
						finalName = String.format("%s (%d)", name, ++index);
						restart = true;
						break;
					}
			}
			
			return finalName;
		}
		
		// sorting
		public void sort() {
			sort(ResourceNameComparator.DEFAULT_ASCENDING);
		}
		public void sortByName() {
			SortStateHelper stateHelper = determineSortState();
			switch (stateHelper.getNameSortState()) {
			case ASCENDING:
				sort(ResourceNameComparator.DEFAULT_DESCENDING);
				break;
			default:
				sort(ResourceNameComparator.DEFAULT_ASCENDING);
				break;
			}
		}
		public void sortByDate() {
			SortStateHelper stateHelper = determineSortState();
			switch (stateHelper.getDateSortState()) {
			case ASCENDING:
				sort(ResourceDateComparator.DEFAULT_DESCENDING);
				break;
			default:
				sort(ResourceDateComparator.DEFAULT_ASCENDING);
				break;
			}
		}
		// helpers
		private SortStateHelper determineSortState() {
			return determineSortState(new SortStateHelper(), this);
		}
		private SortStateHelper determineSortState(SortStateHelper stateHelper, FolderInfo folder) {
			stateHelper.update(folder.files);
			stateHelper.update(folder.folders);
			
			for (FolderInfo subFolder : folder.folders)
				if (stateHelper.isSorted())
					determineSortState(stateHelper, subFolder);
				else
					break;
			
			return stateHelper;
		}
		private void sort(Comparator<ResourceInfo> comparator) {
			Collections.sort(files, comparator);
			Collections.sort(folders, comparator);
			
			for (FolderInfo subFolder : folders)
				subFolder.sort(comparator);
		}
		
		private static class SortStateHelper {
			// fields
			private boolean nameAscending;
			private boolean nameDescending;
			private boolean dateAscending;
			private boolean dateDescending;
			
			// construction
			public SortStateHelper() {
				nameAscending = true;
				nameDescending = true;
				dateAscending = true;
				dateDescending = true;
			}
			
			// properties
			public boolean isSorted() {
				return
						nameAscending || nameDescending ||
						dateAscending || dateDescending;
			}
			public SortState getNameSortState() {
				if (nameAscending)
					return SortState.ASCENDING;
				if (nameDescending)
					return SortState.DESCENDING;
				
				return SortState.NONE;
			}
			public SortState getDateSortState() {
				if (dateAscending)
					return SortState.ASCENDING;
				if (dateDescending)
					return SortState.DESCENDING;
				
				return SortState.NONE;
			}
			
			// methods
			public void update(List<? extends ResourceInfo> resources) {
				if (isSorted() && (resources.size() > 1)) {
					ResourceInfo reference = resources.get(0);
					
					for (int i = 1, n = resources.size(); i < n; i++) {
						ResourceInfo resource = resources.get(i);

						int nameDelta = ResourceNameComparator.DEFAULT_ASCENDING.compare(reference, resource);
						int dateDelta = ResourceDateComparator.DEFAULT_ASCENDING.compare(reference, resource);
						
						if (nameDelta < 0)
							nameDescending = false;
						else if (nameDelta > 0)
							nameAscending = false;

						if (dateDelta < 0)
							dateDescending = false;
						else if (dateDelta > 0)
							dateAscending = false;
						
						reference = resource;
					}
				}
			}
		}

		// listener interface
		public static interface IListener {
			// methods
			void onNameChanged(FolderInfo sender);
			void onDateChanged(FolderInfo sender);
			void onInserted(FolderInfo sender, ResourceInfo info);
			void onRemoved(FolderInfo sender, ResourceInfo info);
			void onRefreshed(FolderInfo sender, Collection<ResourceInfo> insertedResources, Collection<ResourceInfo> removedResources);
		}
	}
	
	private static class ResourceNameComparator implements Comparator<ResourceInfo> {
		// finals
		public static final Comparator<ResourceInfo> DEFAULT_ASCENDING = new ResourceNameComparator();
		public static final Comparator<ResourceInfo> DEFAULT_DESCENDING = new InvertedComparator<>(DEFAULT_ASCENDING);
		
		// methods
		@Override
		public int compare(ResourceInfo a, ResourceInfo b) {
			return AlphanumericComparator.DEFAULT.compare(a.getName(), b.getName());
		}
	}
	private static class ResourceDateComparator implements Comparator<ResourceInfo> {
		// finals
		public static final Comparator<ResourceInfo> DEFAULT_ASCENDING = new ResourceDateComparator();
		public static final Comparator<ResourceInfo> DEFAULT_DESCENDING = new InvertedComparator<>(DEFAULT_ASCENDING);
		
		// methods
		@Override
		public int compare(ResourceInfo a, ResourceInfo b) {
			return Long.compare(a.getDate(), b.getDate());
		}
	}
	private static class InvertedComparator<T> implements Comparator<T> {
		// fields
		private final Comparator<T> comparator;
		
		// construction
		public InvertedComparator(Comparator<T> comparator) {
			this.comparator = comparator;
		}
		
		// methods
		@Override
		public int compare(T a, T b) {
			return -comparator.compare(a, b);
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
	
	//////////////////////////////////////////////
    
    // new dialog
    private static class NewResourceDialog extends Dialog {
    	// finals
    	public static final int FILE_ID = IDialogConstants.YES_ID;
    	public static final int FOLDER_ID = IDialogConstants.NO_ID;
    	public static final int CANCEL_ID = IDialogConstants.CANCEL_ID;

    	// construction
		public NewResourceDialog(Shell parentShell) {
			super(parentShell);
		}
		
		// methods
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Question");
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite)super.createDialogArea(parent);

			GridLayout layout = new GridLayout();
			layout.marginHeight = 20;
			layout.marginWidth = 20;
			container.setLayout(layout);

			Label text = new Label(container, SWT.NONE);
			text.setText("Please select what you would like to create:");

			return container;
		}
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, FILE_ID, "File", false);
			createButton(parent, FOLDER_ID, "Folder", false);
			createButton(parent, CANCEL_ID, "Cancel", true);
		}
		@Override
		protected void buttonPressed(int buttonId) {
			switch (buttonId) {
			case FILE_ID:
				setReturnCode(FILE_ID);
				close();
				break;

			case FOLDER_ID:
				setReturnCode(FOLDER_ID);
				close();
				break;

			case CANCEL_ID:
				cancelPressed();
				break;
			}
		}
    }
	
	//////////////////////////////////////////////

    //
    public static interface IListener {
    	// methods
    	public void onSelectionChanged(int count);
    }
}
