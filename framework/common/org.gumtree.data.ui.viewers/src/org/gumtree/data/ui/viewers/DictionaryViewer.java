package org.gumtree.data.ui.viewers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IKey;

public class DictionaryViewer extends ExtendedComposite {

	private IDataset dataset;

	private Map<String, IKey> keyMap;

	private Combo combo;

	private TableViewer pathsTableViewer;
	
	private PathLabelProvider pathLabelProvider;
	
	private DatasetBrowser datasetBrowser;

	 private TableViewer parametersTableViewer;

	public DictionaryViewer(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		Label label = getWidgetFactory().createLabel(this, "Key: ");
		GridDataFactory.swtDefaults().applyTo(label);

		combo = getWidgetFactory().createCombo(this);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String keyName = combo.getItem(combo.getSelectionIndex());
				handleKeySelected(keyName);
			}
		});
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER)
				.hint(300, SWT.DEFAULT).applyTo(combo);

		CTabFolder tabFolder = new CTabFolder(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).span(2, 1).applyTo(tabFolder);

		CTabItem pathsItem = new CTabItem(tabFolder, SWT.NONE);
		pathsItem.setText("Paths");
		tabFolder.setSelection(pathsItem);
		pathsTableViewer = new TableViewer(tabFolder, SWT.NONE);
		pathsTableViewer.getTable().setLinesVisible(true);
		pathsTableViewer.setContentProvider(new ArrayContentProvider());
		pathLabelProvider = new PathLabelProvider();
		pathsTableViewer.setLabelProvider(pathLabelProvider);
		pathsTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IPath path = (IPath) ((IStructuredSelection) event.getSelection())
						.getFirstElement();
				IContainer container = dataset.getRootGroup()
						.findObjectByPath(path);
				if (datasetBrowser != null
						&& !datasetBrowser.isDisposed()) {
					datasetBrowser.setSelection(container);
				}
			}
		});
		pathsItem.setControl(pathsTableViewer.getControl());

		// TODO: To be implemented
		 CTabItem paramaetersItem = new CTabItem(tabFolder, SWT.NONE);
		 paramaetersItem.setText("Parameters");
		 parametersTableViewer = new TableViewer(tabFolder, SWT.NONE);
		 paramaetersItem.setControl(parametersTableViewer.getControl());
		
		 TableViewerColumn column = new
		 TableViewerColumn(parametersTableViewer, SWT.NONE);
		 column.getColumn().setText("Name");
		 column.getColumn().setWidth(100);
		 column.getColumn().setResizable(true);
		 column = new TableViewerColumn(parametersTableViewer, SWT.NONE);
		 column.getColumn().setText("Type");
		 column.getColumn().setWidth(200);
		 column.getColumn().setResizable(true);
		 column = new TableViewerColumn(parametersTableViewer, SWT.NONE);
		 column.getColumn().setText("Value");
		 column.getColumn().setWidth(300);
		 column.getColumn().setResizable(true);
	}

	public void setDataset(IDataset dataset) {
		// Update only if input is changed
		if (dataset != null
				&& (dataset != this.dataset || !dataset.equals(this.dataset))) {
			this.dataset = dataset;
			pathLabelProvider.setDataset(dataset);
			try {
				// Prepare keys
				List<IKey> keyList = dataset.getRootGroup().findDictionary()
						.getAllKeys();
				keyMap = new HashMap<String, IKey>();
				String[] keys = new String[keyList.size()];
				for (int i = 0; i < keys.length; i++) {
					keys[i] = keyList.get(i).getName();
					keyMap.put(keys[i], keyList.get(i));
				}
				// Set keys
				combo.setItems(keys);
				// Pre select
				if (keys.length > 0) {
					combo.select(0);
					handleKeySelected(keys[0]);
				}
				// Auto completion
				SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(
						keys);
				proposalProvider.setFiltering(true);
				ContentProposalAdapter adapter = new ContentProposalAdapter(
						combo, new ComboContentAdapter(), proposalProvider,
						null, null);
				adapter.setPropagateKeys(true);
				adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
				adapter.addContentProposalListener(new IContentProposalListener() {
					@Override
					public void proposalAccepted(IContentProposal proposal) {
						handleKeySelected(proposal.getContent());
					}
				});
			} catch (Exception e) {
			}
		}
	}

	public void setDatasetBrowser(DatasetBrowser datasetBrowser) {
		this.datasetBrowser = datasetBrowser;
	}
	
	@Override
	protected void disposeWidget() {
		combo = null;
		dataset = null;
		pathsTableViewer = null;
		datasetBrowser = null;
		pathLabelProvider = null;
		parametersTableViewer = null;
		if (keyMap != null) {
			keyMap.clear();
			keyMap = null;
		}
	}

	public void clear() {

	}

	private void handleKeySelected(String keyName) {
		if (keyMap != null) {
			final IKey key = keyMap.get(keyName);
			if (key != null) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (pathsTableViewer != null
								&& !pathsTableViewer.getControl().isDisposed()) {
							List<IPath> pathList = dataset.getRootGroup()
									.findDictionary().getAllPaths(key);
							pathsTableViewer.setInput(pathList
									.toArray(new IPath[pathList.size()]));
						}
					}
				});
			}
		}
	}

}
