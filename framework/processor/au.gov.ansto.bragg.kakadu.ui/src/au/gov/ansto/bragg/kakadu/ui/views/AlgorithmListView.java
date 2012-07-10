/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.views;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.AlgorithmManager;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.core.UIAlgorithmManager;
import au.gov.ansto.bragg.kakadu.ui.Activator;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.SWTResourceManager;
import au.gov.ansto.bragg.process.exception.NullSignalException;

/**
 * Algorithm List view class.
 * @author Danil Klimontov (dak)
 */
public class AlgorithmListView extends ViewPart {

	private TableViewer viewer;
//	private Button chbx_LoadDataFile;
	private TableColumn tableColumnDescription;
	private TableColumn tableColumnName;
	private Action runAlgorithmAction;
	private Action doubleClickAction;
	private Action loadAlgorithmAction;
	private Algorithm selectedAlgorithm;
	private final List<TableColumn> tableColumns = new ArrayList<TableColumn>();
	private TableViewerSorter tableViewerSorter;
	private final SortSelectionListener sortSelectionListener = new SortSelectionListener();


	/**
	 * The constructor.
	 */
	public AlgorithmListView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		SWTResourceManager.registerResourceUser(parent);

		GridLayout parentLayout = new GridLayout();
		parentLayout.makeColumnsEqualWidth = true;
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parent.setLayout(parentLayout);

		viewer = new TableViewer(parent, SWT.SINGLE
			| SWT.H_SCROLL
			| SWT.V_SCROLL
			| SWT.FULL_SELECTION);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "au.gov.ansto.bragg.kakadu.algorithmListView");

		tableColumnName = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumnName.setText("Name");
		tableColumnName.setWidth(180);
		tableColumnName.setMoveable(true);
		tableColumns.add(tableColumnName);
		tableColumnName.addListener(SWT.Selection, sortSelectionListener);

		tableColumnDescription = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumnDescription.setText("Description");
		tableColumnDescription.setWidth(200);
		tableColumnDescription.setMoveable(true);
		tableColumns.add(tableColumnDescription);
		tableColumnDescription.addListener(SWT.Selection, sortSelectionListener);


		viewer.setContentProvider(new AlgorithmListContentProvider());
		viewer.setLabelProvider(new AlgorithmLabelProvider());
		tableViewerSorter = new TableViewerSorter();
		viewer.setSorter(tableViewerSorter);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		
		
		GridData viewerLData = new GridData();
		viewerLData.horizontalAlignment = GridData.FILL;
		viewerLData.verticalAlignment = GridData.FILL;
		viewerLData.grabExcessHorizontalSpace = true;
		viewerLData.grabExcessVerticalSpace = true;
		viewer.getControl().setLayoutData(viewerLData);

//		chbx_LoadDataFile = new Button(parent, SWT.CHECK | SWT.LEFT);
//		chbx_LoadDataFile.setText("Load Data File");
//		chbx_LoadDataFile.setSelection(true);

		viewer.getTable().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				viewerWidgetSelected(evt);
			}
		});
//		viewer.setInput(getViewSite());
		// quick hack
		viewer.setInput(new Object());
		
		//adjust column size
		for (TableColumn tableColumn : tableColumns) {
			tableColumn.pack();
		}
		
		initInstrumentList();
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}
	
	public int getColumnIndex(TableColumn column) {
		return tableColumns.indexOf(column);
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AlgorithmListView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
//		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
//		IActionBars bars = getViewSite().getActionBars();
//		fillLocalPullDown(bars.getMenuManager());
//		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(loadAlgorithmAction);
		manager.add(runAlgorithmAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(loadAlgorithmAction);
		manager.add(runAlgorithmAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(loadAlgorithmAction);
		manager.add(runAlgorithmAction);
	}

	private void makeActions() {
		runAlgorithmAction = new Action() {
			public void run() {
				runSelectedAlgorithm();
			}
		};
		runAlgorithmAction.setText("Run Algorithm");
		runAlgorithmAction.setToolTipText("Run selected algorithm");
		runAlgorithmAction.setImageDescriptor(Activator.getImageDescriptor("icons/run_tool.gif"));
		runAlgorithmAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/run_tool_dis.gif"));
		runAlgorithmAction.setEnabled(false);
		
		loadAlgorithmAction = new Action() {
			public void run() {
				loadSelectedAlgorithm();
			}
		};
		loadAlgorithmAction.setText("Load Algorithm");
		loadAlgorithmAction.setToolTipText("Load selected algorithm");
		loadAlgorithmAction.setImageDescriptor(Activator.getImageDescriptor("icons/loadAlgorithm.gif"));
		loadAlgorithmAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/loadAlgorithm_dis.gif"));
		loadAlgorithmAction.setEnabled(false);
		
		doubleClickAction = new Action() {
			public void run() {
				loadSelectedAlgorithm();
//				runSelectedAlgorithm();
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});

	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Algorithm List",
			message);
	}

	private void showErrorMessage(String message) {
		MessageDialog.openError(
			viewer.getControl().getShell(),
			"Algorithm List",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	private void viewerWidgetSelected(SelectionEvent evt) {
//		System.out.println("viewer.widgetSelected, event=" + evt);
		
		selectedAlgorithm = (Algorithm)((TableItem)evt.item).getData();
		runAlgorithmAction.setEnabled(true);
		loadAlgorithmAction.setEnabled(true);
	}
	
	protected void runSelectedAlgorithm() {
		if (selectedAlgorithm != null) {
			if (selectedAlgorithm.hasInPort())
				if (DataSourceManager.getInstance().getSelectedDataItems().size() <= 0) {
					showMessage("Add and Select at least one Data Item in Data Source section prior to run this Algorithm.");
					return;
				}

			System.out.println("Run algorithm '" + selectedAlgorithm.getName() +"'...");
			
//			OperationControlManager.runAlgorithm(selectedAlgorithm, chbx_LoadDataFile.getSelection());
			try {
				ProjectManager.runAlgorithm(selectedAlgorithm
//						, chbx_LoadDataFile.getSelection()
						);
			} catch (Exception e) {
				e.printStackTrace();
				showErrorMessage(e.getMessage());
			} 
		} else {
			showMessage("Select an algorithm first.");
		}
		
	}
	
	protected void loadSelectedAlgorithm() {
		if (selectedAlgorithm != null) {
			if (selectedAlgorithm.hasInPort())
				if (DataSourceManager.getInstance().getSelectedDataItems().size() <= 0) {
					showMessage("Add and Select at least one Data Item in Data Source section prior to run this Algorithm.");
					return;
				}

			System.out.println("Load algorithm '" + selectedAlgorithm.getName() +"'...");
			
//			OperationControlManager.runAlgorithm(selectedAlgorithm, chbx_LoadDataFile.getSelection());
			try {
				ProjectManager.loadAlgorithm(selectedAlgorithm
//						, chbx_LoadDataFile.getSelection()
						);
			} catch (Exception e) {
				e.printStackTrace();
				showErrorMessage(e.getMessage());
			} 
		} else {
			showMessage("Select an algorithm first.");
		}
		
	}
	
	private void initInstrumentList() {
        IWorkbenchWindow workbenchWindow = 
            Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
       	final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();

		
	}


	public static File[] getInstrumentDirectory(String folderName){
		File folder = new File(folderName);
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {

				return file.isDirectory() && isInstrumentFolder(file);
			}
		};	    	    
		return folder.listFiles(fileFilter);
	}

	static boolean isInstrumentFolder(File instrumentFolder){
		File[] fileList = instrumentFolder.listFiles();
		for (int i = 0; i < fileList.length; i ++){
			if (fileList[i].getName().equals("instrument")) 
				return true;
		}
		return false;
	}

	
	
	class AlgorithmListContentProvider implements IStructuredContentProvider {
		private Algorithm[] algorithmArray;
		
		public AlgorithmListContentProvider() {
			AlgorithmManager algorithmManager = UIAlgorithmManager.getAlgorithmManager();
			algorithmArray = algorithmManager.getAnalysisAlgorithms();
//			for (int i = 0; i < algorithmArray.length; i++){
//				algorithmArray[i];
//			}

		}
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return algorithmArray;
		}
	}
	class AlgorithmLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			Algorithm algorithm = (Algorithm) obj;
			switch (index) {
			case 0:
				return algorithm.getName();
			case 1:
				return algorithm.getShortDescription();
			default:
				return "";
			}
		}
		public Image getColumnImage(Object obj, int index) {
			Algorithm algorithm = (Algorithm) obj;
//			Image icon = new Image(this.getShell().getDisplay(),  parent.getParent().getParent().getWorkspaceDir() + "/" + algorithm.getIcon());
			switch (index) {
			case 1:
				return null;
			case 0:
			default:
				return PlatformUI.getWorkbench().
				getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			}
//			return getImage(algorithm.getIcon());
		}
//		public Image getImage(Object obj) {
//			return PlatformUI.getWorkbench().
//					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
//		}
	}
	
	private final class TableViewerSorter extends ViewerSorter {

		private TableColumn column;

		private int direction = SWT.NONE;

		/**
		 * Does the sort. If it's a different column from the previous sort, do an
		 * ascending sort. If it's the same column as the last sort, toggle the sort
		 * direction.
		 *
		 * @param column
		 */
		public void setSortColumn(TableColumn column) {
			final Table table = viewer.getTable();
			if (column == this.column) {
				// Same column as last sort; toggle the direction
				switch (direction) {
				case SWT.DOWN:
					direction = SWT.UP;
					break;
//				case SWT.UP:
//					direction = SWT.NONE;
//					break;

				default:
					direction = SWT.DOWN;
					break;
				}

			} else {
				// New column; do an ascending sort
				this.column = column;
				direction = SWT.DOWN;

				table.setSortColumn(column);
			}
			
			table.setSortDirection(direction);
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
	            IBaseLabelProvider prov = ((ContentViewer) viewer)
	                    .getLabelProvider();
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

	private final class SortSelectionListener implements Listener {
		public void handleEvent(Event event) {
			//apply sorting
			tableViewerSorter.setSortColumn((TableColumn)event.widget);
			viewer.refresh(false);
		}
	}

}