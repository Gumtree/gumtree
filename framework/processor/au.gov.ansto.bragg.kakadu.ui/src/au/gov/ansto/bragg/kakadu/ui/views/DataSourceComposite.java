/*******************************************************************************
 * Copyright (c) 2007, 2023 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *     Baha El-Kassaby (ANSTO) - migration to CheckboxTreeViewer for Eclipse 4.x
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.views;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.kakadu.core.DataListener;
import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.core.DataSourceManager.SelectableDataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;
import au.gov.ansto.bragg.kakadu.ui.Activator;
import au.gov.ansto.bragg.kakadu.ui.SWTResourceManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kakadu.ui.widget.CheckboxTableTreeViewer;
import au.gov.ansto.bragg.kakadu.ui.widget.tree.DefaultTreeContentProvider;

/**
 * The class displays and manages data source selection.
 * 
 * @author Danil Klimontov (dak)
 */
public class DataSourceComposite extends Composite {
	private Composite parentComposite;

	protected CheckboxTableTreeViewer fileTableTreeViewer;

	private DefaultMutableTreeNode rootNode;

	protected DataSourceTableLabelProvider dataSourceTableLabelProvider;

	protected DataSourceViewerSorter dataSourceViewerSorter;

	protected Listener sortSelectionListener;

	private DropTarget dropTarget;

	private Action addFileAction;
	private Action addDirectoryAction;
	private Action combineSelectedFilesAction;
	private Action removeFileAction;
	private Action removeAllAction;
	private Action deselectAllAction;
	private Action selectAllAction;

	private IWorkbenchAction dynamicHelpAction;

	public DataSourceComposite(org.eclipse.swt.widgets.Composite parent, int style) {
		super(parent, style);
		SWTResourceManager.registerResourceUser(parent);
		this.parentComposite = parent;
		initialise();
		initListeners();
		createActions();
	}

	protected void initialise() {
		GridLayout thisLayout = new GridLayout();
		thisLayout.marginHeight = 0;
		thisLayout.horizontalSpacing = 3;
		thisLayout.marginWidth = 0;
		thisLayout.verticalSpacing = 3;
		thisLayout.numColumns = 1;
		this.setLayout(thisLayout);

		//init tree viewer
		fileTableTreeViewer = new CheckboxTableTreeViewer(this, SWT.FULL_SELECTION | SWT.BORDER);

		GridData fileTreeViewerLData = new GridData();
		fileTreeViewerLData.horizontalAlignment = GridData.FILL;
		fileTreeViewerLData.verticalAlignment = GridData.FILL;
		fileTreeViewerLData.grabExcessHorizontalSpace = true;
		fileTreeViewerLData.grabExcessVerticalSpace = true;
		fileTreeViewerLData.horizontalSpan = 2;
		fileTableTreeViewer.getControl().setLayoutData(fileTreeViewerLData);

		fileTableTreeViewer.setContentProvider(new DefaultTreeContentProvider());

		dataSourceTableLabelProvider = new DataSourceTableLabelProvider();
		fileTableTreeViewer.setLabelProvider(dataSourceTableLabelProvider);

		dataSourceViewerSorter = new DataSourceViewerSorter();
		fileTableTreeViewer.setSorter(dataSourceViewerSorter);

		fileTableTreeViewer.setInput(getRootNode()); // pass a non-null that will be ignored
		fileTableTreeViewer.setAutoCheckedMode(true);

		final Tree tree = fileTableTreeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		TreeColumn nameColumn = new TreeColumn(tree, SWT.NONE);
		nameColumn.setText("Name");
		nameColumn.setWidth(200);
		nameColumn.setMoveable(true);
		dataSourceTableLabelProvider.addTableColumn(nameColumn);

		sortSelectionListener = new SortSelectionListener();
		nameColumn.addListener(SWT.Selection, sortSelectionListener);

		this.layout();

		createDropTarget();

		initData();
	}

	protected void initData() {

		//initialise by existed files in DataSourceManager
		//the case will work when View has been closed and then opened again
		for (Iterator<DataSourceFile> filesIterator = DataSourceManager.getInstance().getFiles(); filesIterator
				.hasNext();) {
			DataSourceFile file = filesIterator.next();

			//create DataItem nodes
			DefaultMutableTreeNode fileNode = createNode(file);

			//add nodes to view
			addFileNode(fileNode);

			//update checked state
			for (Enumeration<?> e = fileNode.children(); e.hasMoreElements();) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) e.nextElement();
				final Object userObject = childNode.getUserObject();
				if (userObject instanceof SelectableDataItem) {
					SelectableDataItem selectableDataItem = (SelectableDataItem) userObject;
					if (selectableDataItem.isSelected()) {
						fileTableTreeViewer.setChecked(childNode, true);
					}
				}
			}
		}
	}

	/**
	 * Adds not existed columns to table.
	 * @param columnNames
	 */
	private void updateTableColumns(List<String> columnNames) {
		for (String columnName : columnNames) {
			if (!dataSourceTableLabelProvider.isColumnExist(columnName)) {
				final Tree tree = fileTableTreeViewer.getTree();
				TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);
				treeColumn.setText(columnName);
				treeColumn.setWidth(200);
				treeColumn.setMoveable(true);
				treeColumn.addListener(SWT.Selection, sortSelectionListener);
				dataSourceTableLabelProvider.addTableColumn(treeColumn);
			}
		}

	}

	protected DefaultMutableTreeNode getRootNode() {
		if (rootNode == null) {
			rootNode = new DefaultMutableTreeNode();
		}
		return rootNode;
	}

	/**
	 * Adds data files from the directory to the list of source files.
	 * @param directoryName path to the directory with data files.
	 */
	public void addDirectory(String directoryName) {
		File directory = new File(directoryName);
		if (directory.isDirectory()) {
			File[] directoryFiles = directory.listFiles(new FileFilter() {
				public boolean accept(File file) {
					//filter for only known formats
					String name = file.getName();
					int lastDotPosition = name.lastIndexOf(".");
					if (lastDotPosition == -1 || lastDotPosition + 1 >= name.length()) {
						return false;
					}
					String extention = name.substring(lastDotPosition + 1).toLowerCase();
					return Util.getSupportedFileExtentions().contains(extention);
				}

			});

			for (File file : directoryFiles) {
				addFile(file.getAbsolutePath());
			}

			adjustColumnSize();
		} else {
			new IllegalArgumentException("The path '" + directoryName + "' is not a directory.");
		}
	}

	/**
	 * Adds data file to the list of source files. 
	 * @param filePath absolute file path to the file.
	 */
	public DataSourceFile addFile(String filePath) {

		//check duplications
		DataSourceFile dataSourceFile = DataSourceManager.getInstance().getDataSourceFile(filePath);
		if (dataSourceFile != null) {
			if (confirmFileReplace(filePath)) {
				DataSourceManager.getInstance().removeFile(dataSourceFile);

				//update UI
				removeFileNode(dataSourceFile);
			} else {
				return dataSourceFile;
			}
		}

		//add file to DataSourceManager
		try {
			dataSourceFile = DataSourceManager.getInstance().addFile(filePath);
		} catch (IOException e) {
			Util.handleException(getShell(), e);
			return null;
		} catch (Exception e) {
			Util.handleException(getShell(), e);
			return null;
		}

		//create DataItem nodes
		DefaultMutableTreeNode fileNode = createNode(dataSourceFile);

		//add nodes to view
		addFileNode(fileNode);

		//update expanded/checked state
		fileTableTreeViewer.setExpandedState(fileNode, true);
		fileTableTreeViewer.setSubtreeChecked(fileNode, true);
		return dataSourceFile;
	}

	/**
	 * @param dataSourceFile
	 * @return
	 */
	protected DefaultMutableTreeNode createNode(DataSourceFile dataSourceFile) {
		DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(dataSourceFile);
		for (DataItem dataItem : dataSourceFile.getDataItems()) {
			fileNode.add(new DefaultMutableTreeNode(dataItem));

			//add TreeColumns if necessary
			updateTableColumns(dataItem.getArrributeNames());
		}
		return fileNode;
	}

	private void adjustColumnSize() {
		//adjust column size
		for (TreeColumn treeColumn : fileTableTreeViewer.getTree().getColumns()) {
			treeColumn.pack();
		}
	}

	protected void addFileNode(DefaultMutableTreeNode fileNode) {
		fileTableTreeViewer.add(getRootNode(), fileNode);
		getRootNode().add(fileNode);
	}

	protected void removeFileNode(DataSourceFile dataSourceFile) {
		DefaultMutableTreeNode fileNode = getFileNode(dataSourceFile);
		removeFileNode(fileNode);
	}

	private void removeFileNode(DefaultMutableTreeNode fileNode) {
		if (fileNode != null) {
			getRootNode().remove(fileNode);
			fileTableTreeViewer.remove(fileNode);
		}
	}

	private void removeAllNodes() {
		List<DefaultMutableTreeNode> childrenNodes = new ArrayList<>();
		for (Enumeration<?> children = getRootNode().children(); children.hasMoreElements();) {
			childrenNodes.add((DefaultMutableTreeNode) children.nextElement());
		}
		getRootNode().removeAllChildren();
		fileTableTreeViewer.remove(childrenNodes.toArray());
	}

	private DefaultMutableTreeNode getFileNode(DataSourceFile dataSourceFile) {
		for (Enumeration<?> children = getRootNode().children(); children.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
			if (node.getUserObject() == dataSourceFile) {
				return node;
			}
		}
		return null;
	}

	protected boolean confirmFileReplace(String fileName) {
		return MessageDialog.openQuestion(getShell(), "Data Source View - Add data file",
				"The file '" + fileName + "' already exists.\nDo you want to replace and reload it?");
	}

	private boolean confirmFileRemove(String fileName) {
		return MessageDialog.openQuestion(getShell(), "Data Source View - Remove data file",
				"Are you sure you want to remove file '" + fileName + "'?");
	}

	private boolean confirmRemoveAll() {
		return MessageDialog.openQuestion(getShell(), "Data Source View - Remove all",
				"Are you sure you want to remove all files from the list?");
	}

	private void initListeners() {
		fileTableTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				//transfer events from view to model
				Object element = event.getElement();
				if (element instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) element;
					Object userObject = treeNode.getUserObject();
					if (userObject instanceof SelectableDataItem) {
						SelectableDataItem selectableDataItem = (SelectableDataItem) userObject;
						selectableDataItem.setSelected(event.getChecked());
					}
				}
			}
		});
		fileTableTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				removeFileAction.setEnabled(!event.getSelection().isEmpty());
			}
		});

		DataSourceManager.getInstance().addDataListener(new DataListener<DataSourceFile>() {
			public void dataAdded(DataSourceFile addedData) {
				updateButtons();
			}

			public void dataRemoved(DataSourceFile removedData) {
				updateButtons();
			}

			public void dataUpdated(DataSourceFile updatedData) {
				updateButtons();
			}

			public void allDataRemoved(DataSourceFile removedData) {
				updateButtons();
			}
		});
	}

	/**
	 * Update button sate. 
	 */
	private void updateButtons() {
		boolean dataExiests = DataSourceManager.getInstance().getSourceDataFileCount() > 0;
		removeAllAction.setEnabled(dataExiests);
		selectAllAction.setEnabled(dataExiests);
		deselectAllAction.setEnabled(dataExiests);
	}

	protected void createActions() {
		addFileAction = new Action() {
			public void run() {
				String[] selectedFiles = Util.selectFilesFromShell(parentComposite.getShell(), "*.hdf",
						"HDF data file");
				for (String selectedFileName : selectedFiles) {
					addFile(selectedFileName);
				}
				adjustColumnSize();
			}
		};
		addFileAction.setText("Add File(s)");
		addFileAction.setToolTipText("Add data file(s) to view");
		addFileAction.setImageDescriptor(Activator.getImageDescriptor("icons/add_item.gif"));
		addFileAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/add_item_dis.gif"));

		addDirectoryAction = new Action() {
			public void run() {
				String selectedDirectory = Util.selectDirectoryFromShell(parentComposite.getShell());
				if (selectedDirectory != null) {
					addDirectory(selectedDirectory);
				}
			}
		};
		addDirectoryAction.setText("Add Directory");
		addDirectoryAction.setToolTipText("Add data files from a directory to view");
		addDirectoryAction.setImageDescriptor(Activator.getImageDescriptor("icons/add_dir.gif"));
		addDirectoryAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/add_dir_dis.gif"));

		combineSelectedFilesAction = new Action() {
			public void run() {
				try {
					combineSelectedFiles();
				} catch (Exception e) {
					Util.handleException(getShell(), e);
				}
			}
		};
		combineSelectedFilesAction.setText("Combine Data");
		combineSelectedFilesAction.setToolTipText("Combine selected data to a group");
		combineSelectedFilesAction.setImageDescriptor(Activator.getImageDescriptor("icons/combine_data.gif"));
		combineSelectedFilesAction
				.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/combine_data_dis.gif"));

		removeFileAction = new Action() {
			public void run() {
				removeSelectedFile();
			}
		};
		removeFileAction.setText("Remove File");
		removeFileAction.setToolTipText("Remove selected data file from view");
		removeFileAction.setImageDescriptor(Activator.getImageDescriptor("icons/rem_item.gif"));
		removeFileAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/rem_item_dis.gif"));
		removeFileAction.setEnabled(false);

		removeAllAction = new Action() {
			public void run() {
				removeAll();
				removeAllAction.setEnabled(false);
			}
		};
		removeAllAction.setText("Remove All");
		removeAllAction.setToolTipText("Remove All data file(s) from view");
		removeAllAction.setImageDescriptor(Activator.getImageDescriptor("icons/rem_all_items.gif"));
		removeAllAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/rem_all_items_dis.gif"));

		selectAllAction = new Action() {
			public void run() {
				fileTableTreeViewer.setAllChecked(true);
			}
		};
		selectAllAction.setText("Select All");
		selectAllAction.setToolTipText("Select all data items");
		selectAllAction.setImageDescriptor(Activator.getImageDescriptor("icons/select_all.gif"));

		deselectAllAction = new Action() {
			public void run() {
				fileTableTreeViewer.setAllChecked(false);
			}
		};
		deselectAllAction.setText("Deselect All");
		deselectAllAction.setToolTipText("Deselect all data items");
		deselectAllAction.setImageDescriptor(Activator.getImageDescriptor("icons/deselect_all.gif"));

		dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		dynamicHelpAction.setImageDescriptor(ActionFactory.HELP_CONTENTS
				.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()).getImageDescriptor());

		updateButtons();
	}

	/**
	 * Gets all actions for the composite.
	 * The method can be used to contribute to view bar/menu actions. 
	 * @return
	 */
	public List<Object> getActionList() {
		final ArrayList<Object> result = new ArrayList<>();
		result.add(addFileAction);
		result.add(addDirectoryAction);
		result.add(combineSelectedFilesAction);
		result.add(removeFileAction);
		result.add(removeAllAction);
		result.add(new Separator());
		result.add(selectAllAction);
		result.add(deselectAllAction);
		result.add(dynamicHelpAction);

		return result;
	}

	protected void createDropTarget() {
		if (dropTarget != null)
			dropTarget.dispose();
		int dropOperation = DND.DROP_COPY;
		final int dropDefaultOperation = DND.DROP_COPY;
		final int dropFeedback = DND.FEEDBACK_INSERT_AFTER | DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL
				| DND.FEEDBACK_SELECT;

		dropTarget = new DropTarget(fileTableTreeViewer.getTree(), dropOperation);
		Transfer[] dropTypes = new Transfer[] { TextTransfer.getInstance(), FileTransfer.getInstance() };
		dropTarget.setTransfer(dropTypes);
		dropTarget.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = dropDefaultOperation;
				}
				if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_COPY;
				}
				event.feedback = dropFeedback;
			}

			public void dragLeave(DropTargetEvent event) {
			}

			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = dropDefaultOperation;
				}
				event.feedback = dropFeedback;
			}

			public void dragOver(DropTargetEvent event) {
				event.feedback = dropFeedback;
			}

			public void drop(DropTargetEvent event) {
				String[] strings = null;
				if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					strings = (String[]) event.data;
				}
				if (strings == null || strings.length == 0) {
					System.out.println("!!Invalid data dropped");
					return;
				}

				//load droped files
				for (String fileName : strings) {
					File file = new File(fileName);
					if (file.isDirectory()) {
						addDirectory(fileName);
					} else {
						addFile(fileName);
					}
				}

			}

			public void dropAccept(DropTargetEvent event) {
			}
		});
	}

	protected void combineSelectedFiles() throws Exception {
		IDataset dataset = Factory.createEmptyDatasetInstance();
		IGroup rootGroup = dataset.getRootGroup();
		IGroup newEntry = Factory.createGroup(rootGroup, "CombinedEntry", true);
		au.gov.ansto.bragg.datastructures.core.Util.setDataStructure(newEntry, DataStructureType.combined);
		Object[] selection = fileTableTreeViewer.getCheckedElements();
		for (Object itemData : selection) {
			if (itemData instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) itemData;
				Object userObject = treeNode.getUserObject();

				DefaultMutableTreeNode treeNodeToRemove = null;
				DataSourceFile dataSourceFile = null;

				if (userObject instanceof DataItem) {
					treeNodeToRemove = (DefaultMutableTreeNode) treeNode.getParent();
					dataSourceFile = (DataSourceFile) treeNodeToRemove.getUserObject();
					if (rootGroup.findDictionary() == null || rootGroup.findDictionary().getAllKeys().size() <= 1)
						rootGroup
								.setDictionary(((DataItem) userObject).getDataObject().getRootGroup().findDictionary());
					newEntry.addSubgroup(((DataItem) userObject).getDataObject());
				} else if (userObject instanceof DataSourceFile) {
					dataSourceFile = (DataSourceFile) userObject;
					treeNodeToRemove = treeNode;
				}
				//confirm file removing
				if (treeNodeToRemove != null && dataSourceFile != null) {
					DataSourceManager.getInstance().removeFile(dataSourceFile);
					try {
						removeFileNode(treeNodeToRemove);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		DataSourceFile dataSourceFile = null;
		try {
			dataSourceFile = DataSourceManager.getInstance().addDataset(dataset);
		} catch (IOException e) {
			Util.handleException(getShell(), e);
		} catch (Exception e) {
			Util.handleException(getShell(), e);
		}

		//create DataItem nodes
		DefaultMutableTreeNode fileNode = createNode(dataSourceFile);

		//add nodes to view
		addFileNode(fileNode);

		//update expanded/checked state
		fileTableTreeViewer.setExpandedState(fileNode, true);
		fileTableTreeViewer.setSubtreeChecked(fileNode, true);
	}

	protected void removeSelectedFile() {
		IStructuredSelection selection = fileTableTreeViewer.getStructuredSelection();
		for (Object itemData : selection.toList()) {
			if (itemData instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) itemData;
				Object userObject = treeNode.getUserObject();

				DefaultMutableTreeNode treeNodeToRemove = null;
				DataSourceFile dataSourceFile = null;

				if (userObject instanceof DataItem) {
					treeNodeToRemove = (DefaultMutableTreeNode) treeNode.getParent();
					dataSourceFile = (DataSourceFile) treeNodeToRemove.getUserObject();

				} else if (userObject instanceof DataSourceFile) {
					dataSourceFile = (DataSourceFile) userObject;
					treeNodeToRemove = treeNode;
				}
				//confirm file removing
				if (treeNodeToRemove != null && dataSourceFile != null && confirmFileRemove(dataSourceFile.getName())) {
					DataSourceManager.getInstance().removeFile(dataSourceFile);
					removeFileNode(treeNodeToRemove);
				}
			}
		}
	}

	public void removeAll() {
		if (fileTableTreeViewer.getTree().getItemCount() > 0 && confirmRemoveAll()) {
			DataSourceManager.getInstance().removeAll();
			removeAllNodes();
		}
	}

	public final class SortSelectionListener implements Listener {
		public void handleEvent(Event event) {
			//store checked nodes for check state restoring
			Object[] checkedElements = fileTableTreeViewer.getCheckedElements();

			//apply sorting
			dataSourceViewerSorter.setSortColumn((TreeColumn) event.widget);
			fileTableTreeViewer.refresh(false);

			//clear checked sate
			fileTableTreeViewer.setAllChecked(false);

			//compose list of only DataItem nodes
			ArrayList<DefaultMutableTreeNode> checkedDataNodes = new ArrayList<>();
			for (int i = 0; i < checkedElements.length; i++) {
				Object element = checkedElements[i];
				if (element instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) element;
					Object userObject = node.getUserObject();
					if (userObject instanceof DataItem) {
						checkedDataNodes.add(node);
					}
				}

			}

			//update checked state for all DataItem naodes.
			//All file nodes will be updated automaticaly
			fileTableTreeViewer.setCheckedElements(checkedDataNodes.toArray());
		}
	}

	public class DataSourceViewerSorter extends ViewerSorter {

		private TreeColumn column;

		private int direction = SWT.NONE;

		/**
		 * Does the sort. If it's a different column from the previous sort, do an
		 * ascending sort. If it's the same column as the last sort, toggle the sort
		 * direction.
		 *
		 * @param column
		 */
		public void setSortColumn(TreeColumn column) {
			final Tree tree = fileTableTreeViewer.getTree();
			if (column == this.column) {
				// Same column as last sort; toggle the direction
				switch (direction) {
				case SWT.DOWN:
					direction = SWT.UP;
					break;
				default:
					direction = SWT.DOWN;
					break;
				}

			} else {
				// New column; do an ascending sort
				this.column = column;
				direction = SWT.DOWN;

				tree.setSortColumn(column);
			}

			tree.setSortDirection(direction);
		}

		/**
		 * Compares the object for sorting
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (direction == SWT.NONE) {
				//not sorted result
				return 0;
			}
			String name1;
			String name2;

			if (viewer == null || !(viewer instanceof ContentViewer)) {
				name1 = e1.toString();
				name2 = e2.toString();
			} else {
				IBaseLabelProvider prov = ((ContentViewer) viewer).getLabelProvider();
				if (prov instanceof ITableLabelProvider) {
					ITableLabelProvider lprov = (ITableLabelProvider) prov;
					int columnIndex = getColumnIndex(column);

					name1 = lprov.getColumnText(e1, columnIndex);
					name2 = lprov.getColumnText(e2, columnIndex);
				} else {
					name1 = e1.toString();
					name2 = e2.toString();
				}
			}
			if (name1 == null) {
				name1 = "";//$NON-NLS-1$
			}
			if (name2 == null) {
				name2 = "";//$NON-NLS-1$
			}

			// use the comparator to compare the strings
			int result = getComparator().compare(name1, name2);

			if (direction == SWT.UP) {
				//opposite direction
				result = -result;
			}
			return result;
		}
	}

	public int getColumnIndex(TreeColumn column) {
		return dataSourceTableLabelProvider.getColumnIndex(column);
	}

	public void setSelectionAll(boolean flag) {
		fileTableTreeViewer.setAllChecked(flag);
	}

	public void selectDataSourceItem(URI fileUri, String entryName) {
		// TODO Auto-generated method stub

	}

}
