package org.gumtree.data.ui.viewers;

import java.io.IOException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;

public class DataItemViewer extends ExtendedComposite {

	private IDataItem dataItem;
	
	public DataItemViewer(Composite parent, int style) {
		super(parent, style);
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dataItem = null;
			}
		});
	}
	
	public void setDataItem(IDataItem dataItem) {
		// Update only if input is changed
		if (dataItem != null && (dataItem != this.dataItem || !dataItem.equals(this.dataItem))) {
			this.dataItem = dataItem;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateViewer();
				}
			});
		}
	}

	public IDataItem getDataItem() {
		return dataItem;
	}
	
	private void updateViewer() {
		// Dispose old controls
		for (Control child : getChildren()) {
			child.dispose();
		}
		
		// Prepare new controls
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		// Top bar
		Composite topBarComposite = getWidgetFactory().createComposite(this);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(topBarComposite);
		createTopBar(topBarComposite);
		
		// Data area
		Composite dataAreaComposite = getWidgetFactory().createComposite(this);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(dataAreaComposite);
		dataAreaComposite.setLayout(new FillLayout());
		createDataArea(dataAreaComposite);
		
		getParent().layout(true, true);
	}

	private void createTopBar(Composite parent) {
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(parent);
		
		// 1
		Label label = getWidgetFactory().createLabel(parent, "Data item: ");
		
		// 2
		label = getWidgetFactory().createLabel(parent, getDataItem().getName());
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().grab(true, false).applyTo(label);
		
		// 3
		// Layout
	}
	
	private void createDataArea(Composite parent) {
		// Clean old controls
		for (Control child : parent.getChildren()) {
			child.dispose();
		}
		parent.setLayout(new FillLayout());
		
		// Load data
		IArray array = null;
		try {
			array = getDataItem().getData();
		} catch (IOException e) {
			createErrorDataView(parent, e);
			getParent().layout(true, true);
			return;
		}
		
		if (getDataItem().getType().equals(char.class)) {
			// String
			if (getDataItem().getRank() == 2) {
				// String
				StringViewer stringViewer = new StringViewer(parent, SWT.NONE);
				stringViewer.setString(array.toString());
			}
		} else if (getDataItem().getType().equals(String.class)) {
			// String
			StringViewer stringViewer = new StringViewer(parent, SWT.NONE);
			stringViewer.setString(array.toString());
		} else {
			// Numeric
			if (getDataItem().getRank() == 0) {
				create1DDataView(parent, array);
			} else if (getDataItem().getRank() == 1) {
				create1DDataView(parent, array);
			} else if (getDataItem().getRank() == 2) {
				create2DDataView(parent, array);
			} else if (getDataItem().getRank() == 3) {
				create3DDataView(parent, array);
			} else if (getDataItem().getRank() == 4) {
				create4DDataView(parent, array);
			}
		}
		
		// Update
		getParent().layout(true, true);
	}
	
	private void create1DDataView(Composite parent, IArray array) {
		Group dataGroup = getWidgetFactory().createGroup(parent, "Data");
		dataGroup.setLayout(new FillLayout());
		
		ArrayViewer arrayViewer = createArrayViewer(dataGroup, SWT.NONE);
		arrayViewer.setArray(array, 0, 0, dataItem.getShortName());
	}
	
	private void create2DDataView(Composite parent, IArray array) {
		Group dataGroup = getWidgetFactory().createGroup(parent, "Data");
		dataGroup.setLayout(new FillLayout());
		
		ArrayViewer arrayViewer = createArrayViewer(dataGroup, SWT.NONE);
		arrayViewer.setArray(array, 0, 0, dataItem.getShortName());
	}

	private void create3DDataView(Composite parent, final IArray array) {
		int col = 5;
		GridLayoutFactory.swtDefaults().numColumns(col).spacing(5, 0).margins(0, 0).applyTo(parent);
		
		int numberOfLayer = array.getShape()[0];
		Label label = getWidgetFactory().createLabel(parent, "Layer: ");
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		Button layerBackwardButton = getWidgetFactory().createButton(parent, "<", SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(layerBackwardButton);
		final ComboViewer layerSelectorCombo = new ComboViewer(parent, SWT.READ_ONLY);
		layerSelectorCombo.setContentProvider(new ArrayContentProvider());
		layerSelectorCombo.setInput(generateList(numberOfLayer));
		layerSelectorCombo.getCombo().select(0);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(layerSelectorCombo.getControl());
		Label layerLabel = getWidgetFactory().createLabel(parent, " / " + numberOfLayer);
		GridDataFactory.swtDefaults().applyTo(layerLabel);
		Button layerForewardButton = getWidgetFactory().createButton(parent, ">", SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(layerForewardButton);
		
		final Group dataGroup = getWidgetFactory().createGroup(parent, "Data");
		dataGroup.setLayout(new FillLayout());
		GridDataFactory.fillDefaults().grab(true, true).span(col, 0).applyTo(dataGroup);
		
		ArrayViewer arrayViewer = createArrayViewer(dataGroup, SWT.NONE);
		arrayViewer.setArray(array, 0, 0, dataItem.getShortName());
		
		layerBackwardButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int currentSelection = layerSelectorCombo.getCombo().getSelectionIndex();
				int newSelection = currentSelection - 1;
				if (newSelection >= 0) {
					layerSelectorCombo.setSelection(new StructuredSelection(newSelection + 1 + ""));
				}
			}
		});
		
		layerForewardButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int currentSelection = layerSelectorCombo.getCombo().getSelectionIndex();
				int newSelection = currentSelection + 1;
				if (newSelection < layerSelectorCombo.getCombo().getItemCount()) {
					layerSelectorCombo.setSelection(new StructuredSelection(newSelection + 1 + ""));
				}
			}
		});
		
		layerSelectorCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = ((IStructuredSelection) layerSelectorCombo.getSelection()).getFirstElement();
				int layer = Integer.parseInt(((String) selection)) - 1;
				for (Control child : dataGroup.getChildren()) {
					child.dispose();
				}
				dataGroup.setLayout(new FillLayout());
				ArrayViewer arrayViewer = createArrayViewer(dataGroup, SWT.NONE);
				arrayViewer.setArray(array, 0, layer, dataItem.getShortName());
				dataGroup.layout(true, true);
			}
		});
	}

	private void create4DDataView(Composite parent, final IArray array) {
		int col = 5;
		GridLayoutFactory.swtDefaults().numColumns(col).spacing(5, 0).margins(0, 0).applyTo(parent);
		
		int numberOfFrame = array.getShape()[0];
		Label label = getWidgetFactory().createLabel(parent, "Frame: ");
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		Button frameBackwardButton = getWidgetFactory().createButton(parent, "<", SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(frameBackwardButton);
		final ComboViewer frameSelectorCombo = new ComboViewer(parent, SWT.READ_ONLY);
		frameSelectorCombo.setContentProvider(new ArrayContentProvider());
		frameSelectorCombo.setInput(generateList(numberOfFrame));
		frameSelectorCombo.getCombo().select(0);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(frameSelectorCombo.getControl());
		Label frameLabel = getWidgetFactory().createLabel(parent, " / " + numberOfFrame);
		GridDataFactory.swtDefaults().applyTo(frameLabel);
		Button frameForewardButton = getWidgetFactory().createButton(parent, ">", SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(frameForewardButton);

		int numberOfLayer = array.getShape()[1];
		label = getWidgetFactory().createLabel(parent, "Layer: ");
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		Button layerBackwardButton = getWidgetFactory().createButton(parent, "<", SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(layerBackwardButton);
		final ComboViewer layerSelectorCombo = new ComboViewer(parent, SWT.READ_ONLY);
		layerSelectorCombo.setContentProvider(new ArrayContentProvider());
		layerSelectorCombo.setInput(generateList(numberOfLayer));
		layerSelectorCombo.getCombo().select(0);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(layerSelectorCombo.getControl());
		Label layerLabel = getWidgetFactory().createLabel(parent, " / " + numberOfLayer);
		GridDataFactory.swtDefaults().applyTo(layerLabel);
		Button layerForewardButton = getWidgetFactory().createButton(parent, ">", SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(layerForewardButton);
		
		final Group dataGroup = getWidgetFactory().createGroup(parent, "Data");
		GridDataFactory.fillDefaults().grab(true, true).span(col, 0).applyTo(dataGroup);
		dataGroup.setLayout(new FillLayout());
		ArrayViewer arrayViewer = createArrayViewer(dataGroup, SWT.NONE);
		arrayViewer.setArray(array, 0, 0, dataItem.getShortName());
		dataGroup.layout(true, true);
		
		frameBackwardButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int currentSelection = frameSelectorCombo.getCombo().getSelectionIndex();
				int newSelection = currentSelection - 1;
				if (newSelection >= 0) {
					frameSelectorCombo.setSelection(new StructuredSelection(newSelection + 1 + ""));
				}
			}
		});
		
		frameForewardButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int currentSelection = frameSelectorCombo.getCombo().getSelectionIndex();
				int newSelection = currentSelection + 1;
				if (newSelection < frameSelectorCombo.getCombo().getItemCount()) {
					frameSelectorCombo.setSelection(new StructuredSelection(newSelection + 1 + ""));
				}
			}
		});
		
		frameSelectorCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = ((IStructuredSelection) frameSelectorCombo.getSelection()).getFirstElement();
				int frame = Integer.parseInt(((String) selection)) - 1;
				selection = ((IStructuredSelection) layerSelectorCombo.getSelection()).getFirstElement();
				int layer = Integer.parseInt(((String) selection)) - 1;
				for (Control child : dataGroup.getChildren()) {
					child.dispose();
				}
				dataGroup.setLayout(new FillLayout());
				ArrayViewer arrayViewer = createArrayViewer(dataGroup, SWT.NONE);
				arrayViewer.setArray(array, frame, layer, dataItem.getShortName());
				dataGroup.layout(true, true);
			}
		});
		
		layerBackwardButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int currentSelection = layerSelectorCombo.getCombo().getSelectionIndex();
				int newSelection = currentSelection - 1;
				if (newSelection >= 0) {
					layerSelectorCombo.setSelection(new StructuredSelection(newSelection + 1 + ""));
				}
			}
		});
		
		layerForewardButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int currentSelection = layerSelectorCombo.getCombo().getSelectionIndex();
				int newSelection = currentSelection + 1;
				if (newSelection < layerSelectorCombo.getCombo().getItemCount()) {
					layerSelectorCombo.setSelection(new StructuredSelection(newSelection + 1 + ""));
				}
			}
		});
		
		layerSelectorCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = ((IStructuredSelection) frameSelectorCombo.getSelection()).getFirstElement();
				int frame = Integer.parseInt(((String) selection)) - 1;
				selection = ((IStructuredSelection) layerSelectorCombo.getSelection()).getFirstElement();
				int layer = Integer.parseInt(((String) selection)) - 1;
				for (Control child : dataGroup.getChildren()) {
					child.dispose();
				}
				dataGroup.setLayout(new FillLayout());
				ArrayViewer arrayViewer = createArrayViewer(dataGroup, SWT.NONE);
				arrayViewer.setArray(array, frame, layer, dataItem.getShortName());
				dataGroup.layout(true, true);
			}
		});
	}
	
	private void createErrorDataView(Composite parent, Exception e) {
		getWidgetFactory().createText(parent,
				"Failed to load data " + getDataItem().getName() + ".\n" + e,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
	}
	
	private String[] generateList(int number) {
		String[] list = new String[number];
		for (int i = 1; i <= number; i++) {
			list[i - 1] = i + "";
		}
		return list;
	}
	
	@Override
	protected void disposeWidget() {
		dataItem = null;
	}
	
	public void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				updateViewer();
			}
		});
	}
	
	public void clear() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				for (Control child : getChildren()) {
					child.dispose();
				}
				dataItem = null;
			}
		});
	}
	
	protected ArrayViewer createArrayViewer(Composite parent, int style) {
		return new ArrayViewer(parent, style);
	}
}
