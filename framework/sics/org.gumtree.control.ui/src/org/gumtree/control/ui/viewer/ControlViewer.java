package org.gumtree.control.ui.viewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.gumtree.control.core.ICommandController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.ui.viewer.ControlViewerConstants.Column;
import org.gumtree.control.ui.viewer.model.CommandControllerNode;
import org.gumtree.control.ui.viewer.model.DefaultControllerNode;
import org.gumtree.control.ui.viewer.model.DynamicControllerNode;
import org.gumtree.control.ui.viewer.model.INodeSet;
import org.gumtree.control.ui.viewer.model.INodeSet.SetType;
import org.gumtree.control.ui.viewer.parts.ControlViewerLabelProvider;
import org.gumtree.control.ui.viewer.parts.FlatContentProvider;
import org.gumtree.control.ui.viewer.parts.HierarchicalContentProvider;
import org.gumtree.control.ui.viewer.parts.SubTreeContentProvider;
import org.gumtree.control.ui.viewer.parts.TreeDecoratingLabelProvider;
import org.gumtree.ui.util.jface.ITreeNode;
import org.gumtree.ui.util.jface.ITreeViewerColumn;
import org.gumtree.ui.util.jface.TreeViewerColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlViewer {

	private static Logger logger = LoggerFactory.getLogger(ControlViewer.class);

	private TreeViewer treeViewer;

	private List<ITreeViewerColumn> columns;

	private IWorkbenchPartSite site;

	private IAction selectColumnAction;
	
	private IAction refreshSelectionsAction;
	
	private IAction runCommandAction;
	
	private IAction openSpyViewAction;

	public ControlViewer() {
		super();
	}

	public ControlViewer(IWorkbenchPartSite site) {
		this();
		this.site = site;
	}

	public void createPartControl(Composite parent, INodeSet nodeSet) {
		createTreeViewer(parent, nodeSet);
		createContextMenu();
		// Adds listener to sics
//		ISicsManager.INSTANCE.control().instrument().addListener(this);
	}

	public void dispose() {
	}

	public void refresh() {
		if(treeViewer != null && treeViewer.getTree().isDisposed()) {
			treeViewer.refresh(true);
		}
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	private void createTreeViewer(Composite parent, INodeSet nodeSet) {
		treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI) {
			// [Tony] [2009-06-03] We don't want updating of viewer cancel any current editing
			public void cancelEditing() {
				// Do nothing
			}
		};
		if (nodeSet != null && nodeSet.getSetType().equals(SetType.FLAT)) {
			// flat structure
			treeViewer.setContentProvider(new FlatContentProvider(nodeSet));
		} else if (nodeSet != null && nodeSet.getSetType().equals(SetType.SUBTREE)) {
			// Sub tree structure
			treeViewer.setContentProvider(new SubTreeContentProvider(nodeSet));
		} else {
			// default: hierarchical structure
			treeViewer.setContentProvider(new HierarchicalContentProvider(nodeSet));
		}
		ILabelDecorator decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
		treeViewer.setLabelProvider(new TreeDecoratingLabelProvider(new ControlViewerLabelProvider(), decorator));
		
		createColumns(treeViewer, nodeSet);
//		createCellEditors(treeViewer);
		// Set column visibility
		if (nodeSet != null && nodeSet.getColumns() != null) {
			for (ITreeViewerColumn column : getAllColumns()) {
				boolean found = false;
				for (String visibleColumnName : nodeSet.getColumns()) {
					if (column.getColumn().getName().equalsIgnoreCase(visibleColumnName)) {
						found = true;
						break;
					}
				}
				setColumnVisibility(column, found);
			}
			// Note: First column must be always visible regardless the filter setting
			setColumnVisibility(columns.get(0), true);
		}
		
		treeViewer.setInput(SicsManager.getSicsModel());

		// Setup drag source
		DragSource ds = new DragSource(treeViewer.getTree(), DND.DROP_MOVE);
	    ds.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
	    ds.addDragListener(new DragSourceAdapter() {
	    	public void dragFinished(DragSourceEvent event) {
	    		LocalSelectionTransfer.getTransfer().setSelection(null);
	    	}
			public void dragSetData(DragSourceEvent event){
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
					LocalSelectionTransfer.getTransfer().setSelection(treeViewer.getSelection());
				}
			}
	    });
	    
	    // Expand tree to start with
	    if (nodeSet == null || nodeSet.getSetType().equals(SetType.TREE)) {
	    	treeViewer.expandToLevel(2);
	    }
		treeViewer.getTree().addListener(SWT.PaintItem, new ItemPaintListener());
		
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
	}

//	 TODO: auto width adjustment
	private void createColumns(final TreeViewer treeViewer, INodeSet nodeSet) {
		Tree tree = treeViewer.getTree();
		columns = new ArrayList<ITreeViewerColumn>();
		

		TreeColumn nodeColumn = new TreeColumn(tree, SWT.LEFT);
		nodeColumn.setText(Column.NODE.getLabel());
		nodeColumn.setMoveable(true);
		if (nodeSet != null) {
			nodeColumn.setWidth(nodeSet.getLabelColumnWidth());
		} else {
			nodeColumn.setWidth(250);
		}
		columns.add(new TreeViewerColumn(Column.NODE, nodeColumn));

		TreeColumn deviceColumn = new TreeColumn(tree, SWT.LEFT);
		deviceColumn.setText(Column.DEVICE.getLabel());
		deviceColumn.setWidth(65);
		deviceColumn.setMoveable(true);
		columns.add(new TreeViewerColumn(Column.DEVICE, deviceColumn));
		
		
		org.eclipse.jface.viewers.TreeViewerColumn targetColumnViewer = new org.eclipse.jface.viewers.TreeViewerColumn(treeViewer, SWT.LEFT);
		targetColumnViewer.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof ITreeNode) {
					return ((ITreeNode) element).getColumnText(Column.TARGET.getIndex());
				}
				return "";
			}
		});
		final TargetEditingSupport editingSupport = new TargetEditingSupport(treeViewer, Column.TARGET.getIndex());
		targetColumnViewer.setEditingSupport(editingSupport);
		
		// Allow edit on double click only
		treeViewer.getTree().addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				editingSupport.setEnabled(true);
			}
		});
		
		// Allow edit on double click only
//		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
//			public void doubleClick(DoubleClickEvent event) {
//				editingSupport.setEnabled(true);
//				Object selectedObject = ((IStructuredSelection)event.getSelection()).getFirstElement();
//				// Fix for [GUMTREE-44]
//				// both double click and enter key will trigger the "doubleClick" method
//				// and activate edit on the target value (if editable)
//				treeViewer.editElement(selectedObject, Column.TARGET.getIndex());
//				editingSupport.setEnabled(false);
////				boolean previousExpandedState = treeViewer.getExpandedState(selectedObject);
////				treeViewer.setExpandedState(selectedObject, !previousExpandedState);
//			}
//		});
		
		TreeColumn targetColumn = targetColumnViewer.getColumn();
		targetColumn.setText(Column.TARGET.getLabel());
		targetColumn.setWidth(80);
		targetColumn.setMoveable(true);
		columns.add(new TreeViewerColumn(Column.TARGET, targetColumn));
		
		TreeColumn currentColumn = new TreeColumn(tree, SWT.CENTER);
		currentColumn.setText(Column.CURRENT.getLabel());
		currentColumn.setWidth(80);
		currentColumn.setMoveable(true);
		columns.add(new TreeViewerColumn(Column.CURRENT, currentColumn));
		
		TreeColumn statusColumn = new TreeColumn(tree, SWT.CENTER);
		statusColumn.setText(Column.STATUS.getLabel());
		statusColumn.setWidth(65);
		statusColumn.setMoveable(true);
		columns.add(new TreeViewerColumn(Column.STATUS, statusColumn));
		
		TreeColumn messageColumn = new TreeColumn(tree, SWT.LEFT);
		messageColumn.setText(Column.MESSAGE.getLabel());
		messageColumn.setWidth(200);
		messageColumn.setMoveable(true);
		columns.add(new TreeViewerColumn(Column.MESSAGE, messageColumn));

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
	}

	protected ITreeViewerColumn[] getAllColumns() {
		return columns.toArray(new ITreeViewerColumn[columns.size()]);
	}

	protected ITreeViewerColumn[] getVisibleColumns() {
		List<ITreeViewerColumn> buffer = new ArrayList<ITreeViewerColumn>();
		if(columns != null) {
			for(ITreeViewerColumn column : columns) {
				if(column.isVisible()) {
					buffer.add(column);
				}
			}
		}
		return buffer.toArray(new ITreeViewerColumn[buffer.size()]);
	}

	protected void setColumnVisibility(ITreeViewerColumn column, final boolean visible) {
		if(columns != null) {
			for(ITreeViewerColumn viewerColumn : columns) {
				if(viewerColumn.equals(column)) {
					viewerColumn.setVisible(visible);
				}
			}
		}
	}

	private void createContextMenu() {
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		  mgr.addMenuListener(new IMenuListener() {
			  public void menuAboutToShow(IMenuManager manager) {
				  fillContextMenu(manager);
			  }
		  });
		  Menu menu = mgr.createContextMenu(treeViewer.getControl());
		  treeViewer.getControl().setMenu(menu);
		  if(getSite() != null) {
			  getSite().registerContextMenu(mgr, treeViewer);
		  }
	}

	private void fillContextMenu(IMenuManager manager) {
		if(refreshSelectionsAction == null) {
			refreshSelectionsAction = new Action("Refresh selected node(s)", InternalImage.REFRESH.getDescriptor()) {
				public void run() {
					IStructuredSelection selections = (IStructuredSelection)treeViewer.getSelection();
					for(Object selection : selections.toList()) {
						if(selection instanceof DynamicControllerNode) {
							((DynamicControllerNode)selection).refreshNode();
						}
					}
				}
			};
		}
		
		if (selectColumnAction == null) {
			selectColumnAction = new Action("Select columns", InternalImage.COLUMN.getDescriptor()) {
				public void run() {
					ListSelectionDialog dialog = new ListSelectionDialog(
							treeViewer.getControl().getShell(),
							getAllColumns(),
							new ArrayContentProvider(), new LabelProvider(),
							"Select columns for display");
					dialog.setHelpAvailable(true);
					dialog.setInitialSelections(getVisibleColumns());
					dialog.open();
					if(dialog.getResult() != null) {
						final Object[] selecton = dialog.getResult();
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								for(ITreeViewerColumn column : getAllColumns()) {
									setColumnVisibility(column, false);
								}
								for(Object column : selecton) {
									setColumnVisibility((ITreeViewerColumn)column, true);
								}
							}
						});
					}

				}
			};
		}

		if (openSpyViewAction == null) {
			openSpyViewAction = new Action("Open spy view", InternalImage.SPY.getDescriptor()) {
				public void run() {
					IStructuredSelection selections = (IStructuredSelection)treeViewer.getSelection();
					if(selections.size() == 1 && selections.getFirstElement() instanceof DynamicControllerNode) {
//						try {
//							// The Spy view is currently disabled in GumTree 1.5
//							ISpyDashboardViewer viewer = DashboardUI.openNewSpyView();
//							viewer.addWidget(selections.getFirstElement());
//						} catch (PartInitException e) {
//							// TODO: UI error handling
//							logger.error("Failed to open spy view from SICS control viewer", e);
//						}
					}
				}
			};
		}
			
		if(runCommandAction == null) {
			runCommandAction = new Action("Run selected command", InternalImage.RUN.getDescriptor()) {
				public void run() {
					IStructuredSelection selections = (IStructuredSelection)treeViewer.getSelection();
					if(selections.size() == 1 && selections.getFirstElement() instanceof DefaultControllerNode) {
						DefaultControllerNode node = (DefaultControllerNode) selections.getFirstElement();
						ICommandController commandController = null;
						if (selections.getFirstElement() instanceof CommandControllerNode) {
							commandController = (ICommandController) node.getController();
						} else if (node instanceof DynamicControllerNode && 
								SicsManager.getSicsModel().findParentController(node.getController()) instanceof ICommandController) {
							commandController = (ICommandController) SicsManager.getSicsModel().findParentController(node.getController());
						}
						try {
							commandController.run(null);
						} catch (SicsException e) {
							MessageDialog.openError(getSite().getShell(), "Error", "An error has occurred on starting a SICS command");
							logger.error("An error has occuried on starting a SICS command", e);
						}
					}
				}
			};
		}
		
		IStructuredSelection selections = (IStructuredSelection)treeViewer.getSelection();
		if (selections.size() == 1 && selections.getFirstElement() instanceof DefaultControllerNode) {
			DefaultControllerNode node = (DefaultControllerNode) selections.getFirstElement();
			if (node instanceof CommandControllerNode ||
					(node instanceof DynamicControllerNode && 
							SicsManager.getSicsModel().findParentController(node.getController()) instanceof ICommandController)) {
				manager.add(runCommandAction);
				manager.add(new Separator());
			}
		}
		
		manager.add(refreshSelectionsAction);
		manager.add(selectColumnAction);
		
		// Spy view is only available in RCP mode (ie workbench != null)
		if (selections.size() == 1 && selections.getFirstElement() instanceof DynamicControllerNode && PlatformUI.getWorkbench() != null) {
			manager.add(new Separator());
			manager.add(openSpyViewAction);
		}
	}

	public IWorkbenchPartSite getSite() {
		return site;
	}

}