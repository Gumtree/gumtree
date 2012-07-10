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
package org.gumtree.vis.awt;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.gumtree.vis.interfaces.IHelpProvider;
import org.gumtree.vis.interfaces.IPlot;
import org.jfree.chart.util.ResourceBundleWrapper;

/**
 * @author nxi
 *
 */
public abstract class AbstractPlotEditor extends JPanel implements ActionListener {


    /**
	 * 
	 */
	private static final long serialVersionUID = 988634288987529784L;
	/**
     * A checkbox indicating whether or not the chart is drawn with
     * anti-aliasing.
     */
	private IPlot plot;
    private JTabbedPane tabs;
    private JPanel interior;
    private JPanel helpPanel;
    
    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.chart.editor.LocalizationBundle");

    /**
     * Standard constructor - the property panel is made up of a number of
     * sub-panels that are displayed in the tabbed pane.
     *
     * @param chart  the chart, whichs properties should be changed.
     */
    public AbstractPlotEditor(IPlot plot) {
        setLayout(new BorderLayout());
        this.plot = plot;
        JPanel parts = new JPanel(new BorderLayout());
        tabs = new JTabbedPane();
        
        helpPanel = createHelpPanel();
        helpPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        tabs.addTab("Help", helpPanel);
        
        parts.add(tabs, BorderLayout.NORTH);
        add(parts);
    }


    private JPanel createHelpPanel() {
		JPanel wrap = new JPanel(new GridLayout(1, 1));
		
    	JPanel helpPanel = new JPanel(new GridLayout(1, 1));
    	helpPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//    	helpPanel.setBorder(BorderFactory.createTitledBorder(
//				BorderFactory.createEtchedBorder(), "Help Topics"));
		
    	SpringLayout spring = new SpringLayout(); 
		JPanel inner = new JPanel(spring);
		inner.setBorder(BorderFactory.createEmptyBorder());

		final IHelpProvider provider = plot.getHelpProvider();
		final JList list = new JList(provider.getHelpMap().keySet().toArray());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		list.setBorder(BorderFactory.createEtchedBorder());
		JScrollPane listPane1 = new JScrollPane(list);
		inner.add(listPane1);
		listPane1.setMaximumSize(new Dimension(140, 0));
		listPane1.setMinimumSize(new Dimension(70, 0));
//		JPanel contentPanel = new JPanel(new GridLayout(2, 1));
//		inner.add(list);
		final JTextField keyField = new JTextField();
		keyField.setMaximumSize(new Dimension(400, 20));
		keyField.setEditable(false);
//		keyField.setMaximumSize();
//		keyArea.setLineWrap(true);
//		keyArea.setWrapStyleWord(true);
//		keyArea.setBorder(BorderFactory.createEtchedBorder());
		inner.add(keyField);
//		contentPanel.add(new JLabel());
//		contentPanel.add(new JLabel());
		final JTextArea helpArea = new JTextArea();
		JScrollPane areaPane = new JScrollPane(helpArea);
		helpArea.setEditable(false);
		helpArea.setLineWrap(true);
		helpArea.setWrapStyleWord(true);
//		helpArea.setBorder(BorderFactory.createEtchedBorder());
		inner.add(areaPane);
//		contentPanel.add(new JLabel());
//		contentPanel.add(new JLabel());
//		inner.add(contentPanel);
		spring.putConstraint(SpringLayout.WEST, listPane1, 2, SpringLayout.WEST, inner);
		spring.putConstraint(SpringLayout.NORTH, listPane1, 2, SpringLayout.NORTH, inner);
		spring.putConstraint(SpringLayout.WEST, keyField, 4, SpringLayout.EAST, listPane1);
		spring.putConstraint(SpringLayout.NORTH, keyField, 2, SpringLayout.NORTH, inner);
		spring.putConstraint(SpringLayout.EAST, inner, 2, SpringLayout.EAST, keyField);
		spring.putConstraint(SpringLayout.WEST, areaPane, 4, SpringLayout.EAST, listPane1);
		spring.putConstraint(SpringLayout.NORTH, areaPane, 4, SpringLayout.SOUTH, keyField);
		spring.putConstraint(SpringLayout.EAST, areaPane, -2, SpringLayout.EAST, inner);
		spring.putConstraint(SpringLayout.SOUTH, inner, 2, SpringLayout.SOUTH, areaPane);
		spring.putConstraint(SpringLayout.SOUTH, listPane1, -2, SpringLayout.SOUTH, inner);

		list.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				Object[] selected = list.getSelectedValues();
				if (selected.length >= 0) {
					HelpObject help = provider.getHelpMap().get(selected[0]);
					if (help != null) {
						keyField.setText(help.getKey());
						helpArea.setText(help.getDiscription());
					}
				}
			}
		});

		helpPanel.add(inner, BorderLayout.NORTH);
		wrap.setName("Help");
		
		wrap.add(helpPanel, BorderLayout.NORTH);
		return wrap;
	}


	/**
     * Handles user interactions with the panel.
     *
     * @param event  a BackgroundPaint action.
     */
    public abstract void actionPerformed(ActionEvent event);

    /**
     * Updates the properties of a chart to match the properties defined on the
     * panel.
     *
     * @param chart  the chart.
     */
    public abstract void updatePlot();

    public JTabbedPane getTabs(){
    	return tabs;
    }
    
    public JPanel getGeneralPanel(){
    	return interior;
    }
    
    public IPlot getPlot() {
    	return plot;
    }
    
    public void showHelpPanel() {
    	tabs.setSelectedComponent(helpPanel);
    }
}
