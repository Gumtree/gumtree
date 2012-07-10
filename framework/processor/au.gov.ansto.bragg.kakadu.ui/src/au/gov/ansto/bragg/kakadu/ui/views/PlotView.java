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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import au.gov.ansto.bragg.kakadu.ui.plot.Plot;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotPropertyChangeListener;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotType;

/**
 * The view is used to display data results in a plot.
 * 
 * @author Danil Klimontov (dak)
 */
public class PlotView extends ViewPart {

	private Plot plotComposite;
	/**PlotView ID.*/ 
	public static final String PLOT_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.PlotView";

	/**
	 * Creates new view instance. 
	 */
	public PlotView() {
		setPartName("Plot");

	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		PlotManager.removePlotView(this);
		System.out.println("Plot View Disposed");
		plotComposite.dispose();
	}


	/**
	 * Creates all UI components of the view.
	 */
	public void createPartControl(Composite parent) {
		plotComposite = new Plot(parent, SWT.NONE);
		
		parent.redraw();
		
		plotComposite.addPlotPropertyChangeListener(new PlotPropertyChangeListener() {
			public void propertyChanged(int propertyId, Object oldValue,
					Object newValue) {
				
				switch (propertyId) {
				case PlotPropertyChangeListener.TITLE_PROPERTY_ID:
					setTitleToolTip((String)newValue);
					break;
					
				case PlotPropertyChangeListener.PLOT_TYPE_PROPERTY_ID:
					setPartName(((PlotType)newValue).toString() + 
							"[" + plotComposite.getId() + "]");
					break;
					
				case PlotPropertyChangeListener.DATA_ITEM_INDEX_PROPERTY_ID:
//					setPartName("Plot T"+plotComposite.getAlgorithmTask().getId() 
//							+ ".O" + plotComposite.getAlgorithmTask()
//									.getOperationManager(plotComposite.getDataItemIndex())
//									.getOperation(plotComposite.getOperationName()).getID()
//							+ ".D" + plotComposite.getDataItemIndex() 
//							);
					break;
				default:
					break;
				}
			}
		});
	}
	
	public Plot getPlotComposite() {
		return plotComposite;
	}

	public void setFocus() {
		plotComposite.setFocus();
	}

	
}
