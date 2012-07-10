/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.vis.surf3d;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.gumtree.vis.awt.AbstractPlotEditor;
import org.gumtree.vis.hist2d.color.ColorScale;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.ISurf3D;
import org.jfree.layout.LCBLayout;

/**
 * @author nxi
 *
 */
public class Surf3DPlotEditor extends AbstractPlotEditor {


    /**
	 * 
	 */
	private static final long serialVersionUID = -5989624424283148967L;
	private static final String CHANGE_COLOUR_SCALE_COMMAND = "showBaseError";
	private static final String LOGARITHM_SCALE_COMMAND = "useLogarithmScale";
	private JComboBox colourScaleCombo;
	private JCheckBox logarithmScale;
    /**
     * Standard constructor - the property panel is made up of a number of
     * sub-panels that are displayed in the tabbed pane.
     *
     * @param chart  the chart, whichs properties should be changed.
     */
    public Surf3DPlotEditor(IPlot plot) {
        super(plot);
    	JPanel generalPanel = createGeneralPanel();
    	getTabs().add(generalPanel, 0);
//    	JPanel masking = createMaskingPanel(chart);
//    	getTabs().add(masking, 1);
    	getTabs().setSelectedComponent(generalPanel);
    	initialise();
    }


	private JPanel createGeneralPanel() {
		JPanel wrap = new JPanel(new BorderLayout());
		
    	JPanel coordinate = new JPanel(new GridLayout(3, 1));
    	coordinate.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
    	
		//Color scale group
    	JPanel colorScale = new JPanel(new BorderLayout());
    	colorScale.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Color Scale"));

    	JPanel inner = new JPanel(new LCBLayout(6));
		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		inner.add(new JLabel("Select Theme"));
		ColorScale[] scales = ColorScale.values();
		colourScaleCombo = new JComboBox(scales);
//		colourScaleCombo.setMaximumRowCount(7);
		colourScaleCombo.setActionCommand(CHANGE_COLOUR_SCALE_COMMAND);
		colourScaleCombo.addActionListener(this);
		inner.add(colourScaleCombo);
		inner.add(new JLabel());
		
		inner.add(new JLabel("Use Logarithmic Scale"));
		logarithmScale = new JCheckBox();
		logarithmScale.setActionCommand(LOGARITHM_SCALE_COMMAND);
		logarithmScale.addActionListener(this);
		inner.add(logarithmScale);
		inner.add(new JLabel());
		
		colorScale.add(inner, BorderLayout.NORTH);
		coordinate.add(colorScale);
		wrap.setName("Coordinate");
		
		wrap.add(coordinate, BorderLayout.NORTH);
		return wrap;
	}


	private void initialise() {
		colourScaleCombo.setSelectedItem(((ISurf3D) getPlot()).getColorScale());
		logarithmScale.setSelected(getPlot().isLogarithmEnabled());
	}


	@Override
	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updatePlot() {
		updateColourScale();
		updateLogarithmScale();
		getPlot().updatePlot();
	}


	private void updateLogarithmScale() {
		if (logarithmScale.isSelected() != getPlot().isLogarithmEnabled()) {
			getPlot().setLogarithmEnabled(logarithmScale.isSelected());
		}
	}


	private void updateColourScale() {
		ColorScale scale = (ColorScale) colourScaleCombo.getSelectedItem();
		if (scale != ((ISurf3D) getPlot()).getColorScale()) {
			((ISurf3D) getPlot()).setColorScale(scale);
		}
	}

}
