package org.gumtree.ui.scripting.tools;

import javax.script.ScriptContext;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.gumtree.scripting.IObservableComponent;
import org.gumtree.ui.scripting.tools.AttributeContentProvider.EngineAttribute;
import org.gumtree.ui.util.SafeUIRunner;

public class AttributeViewer extends AbstractCommandLineTool {

	private TableViewer attributeTableViewer;
	
	private Text attributeText;
	
	public void createControl(Composite parent) {
		parent.setLayout(new FillLayout());
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		
		attributeTableViewer = new TableViewer(sashForm, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		attributeTableViewer.setContentProvider(new AttributeContentProvider());
		attributeTableViewer.setLabelProvider(new AttributeLabelProvider());
		attributeTableViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((EngineAttribute) e1).getName().compareTo(((EngineAttribute) e2).getName());
			}
		});
		TableColumn nameColumn = new TableColumn(attributeTableViewer.getTable(), SWT.LEFT);
		nameColumn.setText("name");
		nameColumn.setWidth(180);
		TableColumn typeColumn = new TableColumn(attributeTableViewer.getTable(), SWT.LEFT);
		typeColumn.setText("type");
		typeColumn.setWidth(150);
		attributeTableViewer.getTable().setHeaderVisible(true);
		attributeTableViewer.getTable().setLinesVisible(true);
		// TODO: re-enable auto update light later
		attributeTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object selectedObject = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (selectedObject instanceof EngineAttribute) {
					final Object value = ((EngineAttribute) selectedObject).getValue();
					if (attributeText != null && !attributeText.isDisposed()) {
						SafeUIRunner.asyncExec(new SafeRunnable() {
							@Override
							public void run() throws Exception {
								if (value != null) {
									attributeText.setText(value.toString());
								} else {
									attributeText.setText("null");
								}
							}							
						});
					}
				}
			}			
		});
		
		ScriptContext scriptContext = getScriptExecutor().getEngine().getContext();
		if (scriptContext instanceof IObservableComponent) {
			attributeTableViewer.setInput(scriptContext);
		}
		
		attributeText = new Text(sashForm, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		attributeText.setEditable(false);
		attributeText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		sashForm.setWeights(new int[] {4, 1});
		
	}

	public void dispose() {
		attributeTableViewer = null;
		attributeText = null;
	}

}
