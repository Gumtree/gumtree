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
package org.jfree.chart.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.title.Title;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.layout.LCBLayout;
import org.jfree.ui.PaintSample;

/**
 * @author nxi
 *
 */
public class ExposedChartEditor extends JPanel implements ActionListener, ChartEditor {

    /** A panel for displaying/editing the properties of the title. */
    private DefaultTitleEditor titleEditor;

    /** A panel for displaying/editing the properties of the plot. */
    private DefaultPlotEditor plotEditor;

    /**
     * A checkbox indicating whether or not the chart is drawn with
     * anti-aliasing.
     */
    private JCheckBox antialias;
    private JTabbedPane tabs;
    private JPanel interior;
    
    /** The chart background color. */
    private PaintSample background;

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
    public ExposedChartEditor(JFreeChart chart) {
        setLayout(new BorderLayout());

        JPanel other = new JPanel(new BorderLayout());
        other.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JPanel general = new JPanel(new BorderLayout());
        general.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            localizationResources.getString("General")));

        interior = new JPanel(new LCBLayout(6));
        interior.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        this.antialias = new JCheckBox(localizationResources.getString(
                "Draw_anti-aliased"));
        this.antialias.setSelected(chart.getAntiAlias());
        interior.add(this.antialias);
        interior.add(new JLabel(""));
        interior.add(new JLabel(""));
        interior.add(new JLabel(localizationResources.getString(
                "Background_paint")));
        this.background = new PaintSample(chart.getBackgroundPaint());
        interior.add(this.background);
        JButton button = new JButton(localizationResources.getString(
                "Select..."));
        button.setActionCommand("BackgroundPaint");
        button.addActionListener(this);
        interior.add(button);

//        interior.add(new JLabel(localizationResources.getString(
//                "Series_Paint")));
//        JTextField info = new JTextField(localizationResources.getString(
//                "No_editor_implemented"));
//        info.setEnabled(false);
//        interior.add(info);
//        button = new JButton(localizationResources.getString("Edit..."));
//        button.setEnabled(false);
//        interior.add(button);
//
//        interior.add(new JLabel(localizationResources.getString(
//                "Series_Stroke")));
//        info = new JTextField(localizationResources.getString(
//                "No_editor_implemented"));
//        info.setEnabled(false);
//        interior.add(info);
//        button = new JButton(localizationResources.getString("Edit..."));
//        button.setEnabled(false);
//        interior.add(button);
//
//        interior.add(new JLabel(localizationResources.getString(
//                "Series_Outline_Paint")));
//        info = new JTextField(localizationResources.getString(
//                "No_editor_implemented"));
//        info.setEnabled(false);
//        interior.add(info);
//        button = new JButton(localizationResources.getString("Edit..."));
//        button.setEnabled(false);
//        interior.add(button);
//
//        interior.add(new JLabel(localizationResources.getString(
//                "Series_Outline_Stroke")));
//        info = new JTextField(localizationResources.getString(
//                "No_editor_implemented"));
//        info.setEnabled(false);
//        interior.add(info);
//        button = new JButton(localizationResources.getString("Edit..."));
//        button.setEnabled(false);
//        interior.add(button);

        general.add(interior, BorderLayout.NORTH);
        other.add(general, BorderLayout.NORTH);

        JPanel parts = new JPanel(new BorderLayout());

        Title title = chart.getTitle();
        Plot plot = chart.getPlot();

        tabs = new JTabbedPane();

        this.titleEditor = new DefaultTitleEditor(title);
        this.titleEditor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        tabs.addTab(localizationResources.getString("Title"), this.titleEditor);

        this.plotEditor = new DefaultPlotEditor(plot);
        this.plotEditor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        tabs.addTab(localizationResources.getString("Plot"), this.plotEditor);

        tabs.add(localizationResources.getString("Other"), other);
        parts.add(tabs, BorderLayout.NORTH);
        add(parts);
    }

    /**
     * Returns a reference to the title editor.
     *
     * @return A panel for editing the title.
     */
    public DefaultTitleEditor getTitleEditor() {
      return this.titleEditor;
    }

    /**
     * Returns a reference to the plot property sub-panel.
     *
     * @return A panel for editing the plot properties.
     */
    public DefaultPlotEditor getPlotEditor() {
        return this.plotEditor;
    }

    /**
     * Returns the current setting of the anti-alias flag.
     *
     * @return <code>true</code> if anti-aliasing is enabled.
     */
    public boolean getAntiAlias() {
        return this.antialias.isSelected();
    }

    /**
     * Returns the current background paint.
     *
     * @return The current background paint.
     */
    public Paint getBackgroundPaint() {
        return this.background.getPaint();
    }

    /**
     * Handles user interactions with the panel.
     *
     * @param event  a BackgroundPaint action.
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals("BackgroundPaint")) {
            attemptModifyBackgroundPaint();
        }
    }

    /**
     * Allows the user the opportunity to select a new background paint.  Uses
     * JColorChooser, so we are only allowing a subset of all Paint objects to
     * be selected (fix later).
     */
    private void attemptModifyBackgroundPaint() {
        Color c;
        c = JColorChooser.showDialog(this, localizationResources.getString(
                "Background_Color"), Color.blue);
        if (c != null) {
            this.background.setPaint(c);
        }
    }

    /**
     * Updates the properties of a chart to match the properties defined on the
     * panel.
     *
     * @param chart  the chart.
     */
    public void updateChart(JFreeChart chart) {

        this.titleEditor.setTitleProperties(chart);
        this.plotEditor.updatePlotProperties(chart.getPlot());

        chart.setAntiAlias(getAntiAlias());
        chart.setBackgroundPaint(getBackgroundPaint());
    }

    public JTabbedPane getTabs(){
    	return tabs;
    }
    
    public JPanel getGeneralPanel(){
    	return interior;
    }
}
