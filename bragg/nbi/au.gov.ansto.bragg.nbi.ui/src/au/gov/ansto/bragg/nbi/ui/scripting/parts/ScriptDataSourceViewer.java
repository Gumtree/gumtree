/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting.parts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.utils.FactoryManager;

import au.gov.ansto.bragg.nbi.ui.internal.InternalImage;


/**
 * @author nxi
 *
 */
public class ScriptDataSourceViewer extends Composite {

	private final static String[] COLUMN_TITLES = new String[]{"ID/Name", "Location"};
	private final static int[] COLUMN_BOUNDS = { 60, 200};
	private static FactoryManager factoryManager = new FactoryManager();
	public static String fileDialogPath;
	private List<IActivityListener> activityListeners = 
			new ArrayList<ScriptDataSourceViewer.IActivityListener>();
	private CoolBar coolbar;
	private CoolItem coolItem;
	private ToolBar controlToolBar;
	private ToolItem openFileToolItem;
	private ToolItem removeFileToolItem;
	private TableViewer tableViewer;
	private List<DatasetInfo> datasetList;
	private ColumnSorter comparator;
	private List<DatasetInfo> selectedDatasets;

	/**
	 * @param parent
	 * @param style
	 */
	public ScriptDataSourceViewer(Composite parent, int style) {
		super(parent, style);
		datasetList = new ArrayList<DatasetInfo>();
		setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridLayoutFactory.fillDefaults().spacing(0, 0).margins(0, 0).applyTo(this);
		createToolboxComposite(this);
		createFileTableComposite(this);
	}

	private void createToolboxComposite(Composite parent) {
		coolbar = new CoolBar(parent, SWT.BORDER | SWT.FLAT);
		coolbar.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(coolbar);
		
		coolItem = new CoolItem(coolbar, SWT.DROP_DOWN);
		controlToolBar = new ToolBar(coolbar, SWT.FLAT);
		coolItem.setControl(controlToolBar);
		
		openFileToolItem = new ToolItem(controlToolBar, SWT.PUSH);
		openFileToolItem.setToolTipText("Open file");
		openFileToolItem.setImage(InternalImage.ADD_ITEM.getImage());
		
		removeFileToolItem = new ToolItem(controlToolBar, SWT.PUSH);
		removeFileToolItem.setToolTipText("Remove file");
		removeFileToolItem.setImage(InternalImage.REMOVE_ITEM.getImage());
		
		addListeners();
	    resizeCoolBar();
	}

	private void addListeners() {
		openFileToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
 				if (fileDialogPath == null){
 					IWorkspace workspace= ResourcesPlugin.getWorkspace();
 					IWorkspaceRoot root = workspace.getRoot();
 					dialog.setFilterPath(root.getLocation().toOSString());
 				} else {
 					dialog.setFilterPath(fileDialogPath);
 				}
 				dialog.setFilterExtensions(new String[]{"*.hdf"});
 				dialog.open();
 				if (dialog.getFileName() == null) {
 					return;
 				}
				String filePath = dialog.getFilterPath() + File.separator + dialog.getFileName();
				File pickedFile = new File(filePath);
				if (!pickedFile.exists() || !pickedFile.isFile())
					return;
				fileDialogPath = pickedFile.getParent();
				if (filePath != null) {
					try {
						String[] filenames = dialog.getFileNames();
						if (filenames.length == 0) {
							return;
						}
						if (filenames.length == 1) {
							addDataset(dialog.getFilterPath() + File.separator + filenames[0], true);
						} else {
							for (int i = 0; i < filenames.length; i++) {
								filenames[i] = dialog.getFilterPath() + File.separator + filenames[i];
							}
							addDatasets(filenames);
						}
					} catch (Exception error) {
						MessageDialog.openError(getShell(), "Error", "Cannot open file " 
								+ filePath + ": " + error.getLocalizedMessage());
						error.printStackTrace();
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		removeFileToolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedDataset();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		

	}

	public void removeSelectedDataset() {
		ISelection selection = tableViewer.getSelection();
		if (!selection.isEmpty()) {
			Object[] objs = ((StructuredSelection) selection).toArray();
			for (Object obj : objs) {
				if (obj instanceof DatasetInfo) {
					try {
						((DatasetInfo) obj).getDataset().close();
					} catch (IOException e1) {
					}
					datasetList.remove(obj);
				}
			}
			tableViewer.refresh(false, true);
		}
	}
	
	public void addDataset(String filePath, final boolean notifyListener) throws FileAccessException, IOException {
		DatasetInfo datasetInfo = getDataset(filePath);
		if (datasetInfo == null) {
			URI fileURI = new File(filePath).toURI();
			IDataset dataset = factoryManager.getFactory().openDataset(fileURI);
			datasetInfo = new DatasetInfo(dataset);
			dataset.close();
			datasetList.add(datasetInfo);
//			Display.getDefault().asyncExec(new Runnable() {
//
//				@Override
//				public void run() {
//					tableViewer.refresh(false, true);
//					tableViewer.setSelection(new StructuredSelection(new Object[]{obj}), true);
//					if (notifyListener) {
//						fireActivityEvent(obj);
//					}
//				}
//			});
		}
		final DatasetInfo info = datasetInfo;
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				tableViewer.refresh(false, true);
				tableViewer.setSelection(new StructuredSelection(new Object[]{info}), true);
				if (notifyListener) {
					fireDatasetAddEvent(new DatasetInfo[]{info});
				}
			}
		});			
	}
	
	public DatasetInfo getDataset(String filePath) {
		for (DatasetInfo dataset : datasetList) {
			if (dataset.getLocation().equals(filePath)){
				return dataset;
			}
		}
		return null;
	}
	
	public void addDatasets(String[] filePaths) throws FileAccessException, IOException {
		boolean refresh = false;
		for (String filename : filePaths) {
			if (findDataset(filename) == null) {
				URI fileURI = new File(filename).toURI();
				IDataset dataset = factoryManager.getFactory().openDataset(fileURI);
				DatasetInfo obj = new DatasetInfo(dataset);
				dataset.close();
				datasetList.add(obj);
				refresh = true;
			}
		}
		if (refresh) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					tableViewer.refresh(false, true);
				}
			});
		}
	}
	
	public DatasetInfo findDataset(String filePath) {
		for (DatasetInfo dataset : datasetList) {
			if (filePath != null && filePath.equalsIgnoreCase(dataset.getLocation())) {
				return dataset;
			}
		}
		return null;
	}
	
	private void createFileTableComposite(
			Composite parent) {
		Composite fileTableComposite = new Composite(parent, SWT.BORDER);
		fileTableComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridLayoutFactory.fillDefaults().applyTo(fileTableComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fileTableComposite);
		tableViewer = new TableViewer(fileTableComposite, SWT.MULTI 
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
//		GridLayoutFactory.fillDefaults().applyTo(tableViewer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableViewer.getControl());
//		tableViewer.setBackgroundMode(SWT.INHERIT_DEFAULT);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(datasetList);
		comparator = new ColumnSorter(new AlphanumComparator());
		comparator.setSortDirection(ColumnSorter.SORT_DESCENDING);
		tableViewer.setComparator(comparator);
		createColumns(fileTableComposite);
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					Object obj = ((StructuredSelection) selection).getFirstElement();
					if (obj instanceof DatasetInfo) {
						fireRunSelectedEvent();
					}
				}
			}
		});
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				setSelectedDatasets(getSelectedDatasetList());
			}
		});
//		final Menu tableMenu = new Menu(table);
//		MenuItem removeItem = new MenuItem(tableMenu, SWT.PUSH);
//		removeItem.setText("Remove");
//		
//		MenuItem runItem = new MenuItem(tableMenu, SWT.PUSH);
//		runItem.setText("Run");
//		
//		tableViewer.getTable().addMenuDetectListener(new MenuDetectListener() {
//			
//			@Override
//			public void menuDetected(MenuDetectEvent e) {
//				tableMenu.setVisible(true);
//			}
//		});
		
	    MenuManager popupMenu = new MenuManager();
	    IAction runAction = new Action("Run") {
	    	@Override
	    	public void run() {
	    		ISelection selection = tableViewer.getSelection();
	    		if (!selection.isEmpty()) {
//	    			Object[] objs = ((StructuredSelection) selection).toArray();
//	    			DatasetInfo[] datasets = new DatasetInfo[objs.length];
//	    			System.arraycopy(objs, 0, datasets, 0, objs.length);
	    			fireRunSelectedEvent();
	    		} 		
	    	}
		};
		runAction.setImageDescriptor(InternalImage.PLAY_16.getDescriptor());
		popupMenu.add(runAction);
//		new MenuItem(popupMenu.getMenu(), SWT.SEPARATOR);
		popupMenu.add(new Separator());
		IAction removeAction = new Action("Remove") {
	    	@Override
	    	public void run() {
	    		removeSelectedDataset();    		
	    	}
		};
		removeAction.setImageDescriptor(InternalImage.REMOVE_ITEM.getDescriptor());
	    popupMenu.add(removeAction);


	    Menu menu = popupMenu.createContextMenu(table);
	    table.setMenu(menu);
	}

	// This will create the columns for the table
	private void createColumns(final Composite parent) {

		// First column is for the first name
		TableViewerColumn col = createTableViewerColumn(COLUMN_TITLES[0], COLUMN_BOUNDS[0], 0);
		ColumnLabelProvider labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DatasetInfo obj = (DatasetInfo) element;
				return obj.getFileID();
			}
			
		};
		col.setLabelProvider(labelProvider);

		// Second column is for the last name
		col = createTableViewerColumn(COLUMN_TITLES[1], COLUMN_BOUNDS[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DatasetInfo obj = (DatasetInfo) element;
				return obj.getLocation();
			}
		});

	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = tableViewer.getTable().indexOf(column);
				if (comparator.getColumnIndex() == index) {
					comparator.setSortDirection(1 - comparator.getSortDirection());
				}
				comparator.setColumnIndex(index);
				tableViewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		return viewerColumn;
	}
	
	private void resizeCoolBar() {
	    Point toolBar1Size = controlToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    Point coolBar1Size = coolItem.computeSize(toolBar1Size.x,
	        toolBar1Size.y);
	    coolItem.setSize(coolBar1Size);
	}

	/**
	 * @return the datasetList
	 */
	public List<DatasetInfo> getDatasetList() {
		return datasetList;
	}

	public List<DatasetInfo> getSelectedDatasetList() {
		List<DatasetInfo> datasets = new ArrayList<DatasetInfo>();
		ISelection selection = tableViewer.getSelection();
		if (!selection.isEmpty()) {
			Object[] objs = ((StructuredSelection) selection).toArray();
			for (Object obj : objs) {
				if (obj instanceof DatasetInfo) {
					datasets.add((DatasetInfo) obj);
				}
			}
		}
		return datasets;
	}
	
	public interface IActivityListener {
		public void datasetAdded(DatasetInfo[] datasets);
		public void runSelected();
	}
	
	public void addActivityListener(IActivityListener listener) {
		activityListeners.add(listener);
	}
	
	public void removeActivityListener(IActivityListener listener) {
		activityListeners.remove(listener);
	}
	
	public void fireDatasetAddEvent(DatasetInfo[] datasets) {
		for (IActivityListener listener : activityListeners) {
			listener.datasetAdded(datasets);
		}
	}
	
	public void fireRunSelectedEvent() {
		for (IActivityListener listener : activityListeners) {
			listener.runSelected();
		}
	}

	public void setSelectedDatasets(List<DatasetInfo> selectedDatasets) {
		this.selectedDatasets = selectedDatasets;
	}
	
	public List<DatasetInfo> getSelectedDatasets() {
		if (selectedDatasets == null){
			return new ArrayList<DatasetInfo>();
		} else {
			return selectedDatasets;
		}
	}
//	public void addSelectionChangeListener(ISelectionChangedListener listener) {
//		tableViewer.addSelectionChangedListener(listener);
//		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
//			
//			@Override
//			public void doubleClick(DoubleClickEvent event) {
//				event.getSelection()
//			}
//		});
//	}
//	
//	public void removeSelectionChangeListener(ISelectionChangedListener listener) {
//		tableViewer.removeSelectionChangedListener(listener);
//	}
	

}
