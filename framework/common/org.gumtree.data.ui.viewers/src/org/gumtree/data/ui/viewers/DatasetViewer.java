package org.gumtree.data.ui.viewers;

import java.net.URI;

import javax.swing.UIManager;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.ui.viewers.internal.InternalImage;

public class DatasetViewer extends ExtendedComposite {
	
	private DatasetBrowser datasetBrowser;
	
	private DataItemViewer dataItemViewer;

	private AttributeViewer attributeViewer;
	
	private DictionaryViewer dictionaryViewer;

	public DatasetViewer(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		SashForm mainSashForm = getWidgetFactory().createSashForm(this, SWT.HORIZONTAL);

		// Left: dataset browser
		datasetBrowser = createDatasetBrowser(mainSashForm);
		datasetBrowser.getTreeViewer().addSelectionChangedListener(
				new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						Object selection = ((IStructuredSelection) event
								.getSelection()).getFirstElement();
						performSelectionChanged(selection);
					}
				});
		
		// Right top: object viewer
		SashForm rightSashForm = getWidgetFactory().createSashForm(mainSashForm, SWT.VERTICAL);
		dataItemViewer = createDataItemViewer(rightSashForm);
		
		// Right bottom 1: attribute viewer
		CTabFolder tabFolder = new CTabFolder(rightSashForm, SWT.BORDER | SWT.BOTTOM | SWT.FLAT);
		CTabItem attributeTab = new CTabItem(tabFolder, SWT.NONE);
		attributeTab.setText("Attribute");
		attributeViewer = new AttributeViewer(tabFolder, SWT.NONE);
		attributeTab.setControl(attributeViewer);
		tabFolder.setSelection(attributeTab);
		
		CTabItem dictionaryTab = new CTabItem(tabFolder, SWT.NONE);
		dictionaryTab.setText("Dictionary");
		dictionaryViewer = new DictionaryViewer(tabFolder, SWT.NONE);
		dictionaryViewer.setDatasetBrowser(datasetBrowser);
		dictionaryTab.setControl(dictionaryViewer);
		
		rightSashForm.setWeights(new int[] { 3, 1 });

		mainSashForm.setWeights(new int[] { 1, 3 });
	}
	
	protected void performSelectionChanged(Object selection) {
		if (selection instanceof IDataset) {
			dictionaryViewer.setDataset(((IDataset) selection));	
		}
		if (selection instanceof IContainer) {
			attributeViewer.setContainer((IContainer) selection);
			dictionaryViewer.setDataset(((IContainer) selection).getDataset());
		}
		if (selection instanceof IDataItem) {
			dataItemViewer.setDataItem((IDataItem) selection);
		} else if (selection != null) {
			dataItemViewer.clear();
		}
	}
	
	protected DatasetBrowser createDatasetBrowser(Composite parent) {
		DatasetBrowser datasetBrowser = new DatasetBrowser(parent,
				SWT.BORDER);
		return datasetBrowser;
	}
	
	protected DataItemViewer createDataItemViewer(Composite parent) {
		return new DataItemViewer(parent, SWT.NONE);
	}
	
	public void addDataset(URI uri) {
		getDatasetBrowser().addDataset(uri);
	}
	
	public DatasetBrowser getDatasetBrowser() {
		return datasetBrowser;
	}
	
	@Override
	protected void disposeWidget() {
		datasetBrowser = null;
		dataItemViewer = null;
		attributeViewer = null;
		dictionaryViewer = null;
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setSize(800, 640);

		// Form version
//		FormComposite formComposite = new FormComposite(shell, SWT.NONE);		
//		formComposite.setLayout(new FillLayout());
//		new DatasetViewer(formComposite, SWT.NONE);

		// SWT version
		new DatasetViewer(shell, SWT.NONE);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		InternalImage.dispose();
		display.dispose();
	}

	public void refresh(Object object) {
		datasetBrowser.refreshDataset((IDataset) object);
		dataItemViewer.clear();
		performSelectionChanged(object);
//		dataItemViewer.refresh();
//		attributeViewer.refresh();
		
	}
	
	public void refresh() {
		datasetBrowser.refresh();
		dataItemViewer.clear();
		attributeViewer.clear();
		dictionaryViewer.clear();
	}

	/**
	 * @return the dataItemViewer
	 */
	public DataItemViewer getDataItemViewer() {
		return dataItemViewer;
	}

	/**
	 * @return the attributeViewer
	 */
	public AttributeViewer getAttributeViewer() {
		return attributeViewer;
	}

	public DictionaryViewer getDictionaryViewer() {
		return dictionaryViewer;
	}
	
}
