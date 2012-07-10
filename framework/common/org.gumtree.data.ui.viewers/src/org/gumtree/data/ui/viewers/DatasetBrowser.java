package org.gumtree.data.ui.viewers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IDataset;

public class DatasetBrowser extends ExtendedComposite {

	private TreeViewer treeViewer;
	
	private List<IDataset> datasets;
	
	private List<DatasetChangeListener> datasetListeners;
	
	public DatasetBrowser(Composite parent, int style) {
		super(parent, style);
		datasets = new ArrayList<IDataset>();
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(this);
		
		// Tree viewer
		treeViewer = new TreeViewer(this, getOriginalStyle());
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(treeViewer.getControl());
		treeViewer.setContentProvider(makeContentProvider());
		treeViewer.setLabelProvider(makeLabelProvider());
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				TreeSelection selection = (TreeSelection) event.getSelection();
				if (selection.getFirstElement() == null) {
					return;
				}
				// Use path instead of object to avoid direct object comparison
				treeViewer.setExpandedState(selection.getPaths()[0],
						!treeViewer.getExpandedState(selection.getPaths()[0]));
			}
		});
		treeViewer.setInput(datasets);
		
		// DnD support
		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		DropTarget dropTarget = new DropTarget(treeViewer.getTree(), operations);
		dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (FileTransfer.getInstance().isSupportedType(
						event.currentDataType)) {
					String[] files = (String[]) event.data;
					for (String file : files) {
						addDataset(new File(file).toURI());
					}
				}
			}
		});
		
		// Open button
		Button openButton = getWidgetFactory().createButton(this, "Open", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(openButton);
//		openButton.setImage(InternalImage.OPEN.getImage());
		openButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
				String filename = dialog.open();
				if (filename != null) {
					String path = dialog.getFilterPath();
					for (String name : dialog.getFileNames()) {
						File file = new File(path + "/" + name);
						addDatasetFromDialog(file.toURI());
					}
				}
			}
		});
		
		// Remove button
		Button removeButton = getWidgetFactory().createButton(this, "Remove", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(removeButton);
//		removeButton.setImage(InternalImage.REMOVE.getImage());
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Object selection = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
				if (selection instanceof IDataset) {
//					datasets.remove(selection);
//					treeViewer.remove(selection);
					removeDataset((IDataset) selection);
					for (DatasetChangeListener listener : getDatasetChangeListeners()) {
						listener.datasetRemoved((IDataset) selection);
					}
				}
			}
		});
	}

	protected IBaseLabelProvider makeLabelProvider() {
		return new DatasetBrowserLabelProvider();
	}

	protected void addDatasetFromDialog(final URI uri) {
		Display.getDefault().asyncExec(new Runnable() {			
			@Override
			public void run() {
				try {
					IDataset dataset = makeDataset(uri);
					// Check duplication
					for (IDataset ds : datasets) {
						if (dataset.getLocation().equals(ds.getLocation())) {
							dataset.close();
							return;
						}
					}
					// If not duplicated, open it
					if (!dataset.isOpen()) {
						dataset.open();
					}
					datasets.add(dataset);
					treeViewer.add(datasets, dataset);
					for (DatasetChangeListener listener : getDatasetChangeListeners()) {
						listener.datasetAdded(dataset);
					}
//					treeViewer.expandToLevel(dataset, 0);
				} catch (Exception e1) {
					// TODO: error handling
					e1.printStackTrace();
				}
			}
		});
	}
	
	public void addDataset(final URI uri) {
		Display.getDefault().asyncExec(new Runnable() {			
			@Override
			public void run() {
				try {
					IDataset dataset = makeDataset(uri);
					// Check duplication
					for (IDataset ds : datasets) {
						if (dataset.getLocation().equals(ds.getLocation())) {
							dataset.close();
							return;
						}
					}
					// If not duplicated, open it
					if (!dataset.isOpen()) {
						dataset.open();
					}
					datasets.add(dataset);
					treeViewer.add(datasets, dataset);
//					treeViewer.expandToLevel(dataset, 0);
				} catch (Exception e1) {
					// TODO: error handling
					e1.printStackTrace();
				}
			}
		});
	}
	
	public void addDataset(final IDataset dataset) {
		Display.getDefault().asyncExec(new Runnable() {			
			@Override
			public void run() {
				try {
					// Check duplication
//					for (IDataset ds : datasets) {
//						if (dataset.getLocation().equals(ds.getLocation())) {
//							return;
//						}
//					}
					if (datasets.contains(dataset)) {
						return;
					}
					// If not duplicated, open it
//					dataset.open();
					datasets.add(dataset);
					treeViewer.add(datasets, dataset);
//					for (DatasetChangeListener listener : datasetListeners) {
//						listener.datasetAdded(dataset);
//					}
					treeViewer.expandToLevel(dataset, 1);
				} catch (Exception e1) {
					// TODO: error handling
					e1.printStackTrace();
				}
			}
		});
	}
	
	public void removeDataset(final IDataset dataset) {
		Display.getDefault().asyncExec(new Runnable() {			
			@Override
			public void run() {
				try {
					if (!datasets.contains(dataset)) {
						return;
					}
					treeViewer.remove(datasets, datasets.indexOf(dataset));
					datasets.remove(dataset);
					try {
						dataset.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
//					treeViewer.expandToLevel(dataset, 0);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	}
	
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
	
	protected void disposeWidget() {
		if (datasets != null) {
			for (IDataset dataset : datasets) {
				try {
					dataset.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			datasets.clear();
			datasets = null;
		}
		treeViewer = null;
	}

	public List<IDataset> getDatasets() {
		return datasets;
	}
	
	protected ITreeContentProvider makeContentProvider() {
		return new DatasetBrowserContentProvider();
	}
	
	protected IDataset makeDataset(final URI uri) throws Exception {
		return Factory.createDatasetInstance(uri);
	}
	
	public void refreshDataset(final IDataset dataset) {
		Display.getDefault().asyncExec(new Runnable() {			
			@Override
			public void run() {
				try {
					treeViewer.refresh(dataset);
					treeViewer.setExpandedState(dataset, false);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	}
	
	public void refresh() {
		Display.getDefault().asyncExec(new Runnable() {			
			@Override
			public void run() {
				try {
					for (IDataset dataset : datasets) {
						treeViewer.refresh(dataset);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	}
	
	public void setSelection(final Object selection) {
		Display.getDefault().asyncExec(new Runnable() {			
			@Override
			public void run() {
				try {
					treeViewer.setSelection(new StructuredSelection(selection), true);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	}
	
	private List<DatasetChangeListener> getDatasetChangeListeners() {
		if (datasetListeners == null) {
			datasetListeners = new ArrayList<DatasetChangeListener>();
		}
		return datasetListeners;
	}

	public void addDatasetChangeListener(DatasetChangeListener listener) {
		getDatasetChangeListeners().add(listener);
	}
	
	public void removeDatasetChangeListener(DatasetChangeListener listener) {
		getDatasetChangeListeners().remove(listener);
	}
}
