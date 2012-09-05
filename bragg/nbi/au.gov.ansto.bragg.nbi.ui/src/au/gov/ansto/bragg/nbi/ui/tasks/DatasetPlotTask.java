/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.nbi.ui.tasks;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;
import org.gumtree.workflow.ui.models.SingleDataModel;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.kakadu.dom.PlotDOM;

public class DatasetPlotTask extends AbstractTask {

	@Override
	protected Object createModelInstance() {
		return new SingleDataModel(DataDimensionType.map);
	}

	@Override
	protected ITaskView createViewInstance() {
		return new DatasetPlotTaskView();
	}

	@Override
	protected Object run(Object input) throws WorkflowException {
		if (input instanceof IDataset) {
			try {
				IGroup rootGroup = ((IDataset) input).getRootGroup();
				List<IGroup> entryList = NexusUtils.getNexusEntryList(rootGroup);
				if (entryList.size() == 0) {
					throw new WorkflowException("can not find data entry in the target URI");
				}
				IGroup data = NexusUtils.getNexusData(entryList.get(0));
				Plot plot = (Plot) PlotFactory.copyToPlot(data, "Data", getType());
				PlotDOM.plot(plot);
			} catch (Exception e) {
				throw new WorkflowException("Failed to plot data", e);
			}
			return input;
		}
		return null;
	}

	public DataDimensionType getType() {
		return (DataDimensionType) ((SingleDataModel) getDataModel()).getData();
	}
	
	public void setType(DataDimensionType type) {
		((SingleDataModel) getDataModel()).setData(type);
	}
	
	public Class<?>[] getInputTypes() {
		return new Class[] { IDataset.class };
	}
	
	public Class<?>[] getOutputTypes() {
		return new Class[] { IDataset.class };
	}
	
	private class DatasetPlotTaskView extends AbstractTaskView {

		@Override
		public void createPartControl(Composite parent) {
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(parent);
			
			Label label = getToolkit().createLabel(parent, "Show as: ");
			label.setFont(UIResources.getDefaultFont(SWT.BOLD));
			GridDataFactory.swtDefaults().applyTo(label);
			
			ComboViewer comboViewer = new ComboViewer(parent, SWT.READ_ONLY);
			comboViewer.setContentProvider(new ArrayContentProvider());
			comboViewer.setLabelProvider(new LabelProvider());
			comboViewer.setSorter(new ViewerSorter());
			comboViewer.setInput(DataDimensionType.values());
			comboViewer.setSelection(new StructuredSelection(getType()));
			GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(comboViewer.getControl());
			
			comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					Object object = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (object instanceof DataDimensionType) {
						setType((DataDimensionType) object);
					}
				}				
			});
		}
		
	}
	
}
