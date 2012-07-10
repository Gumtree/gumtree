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
package org.gumtree.vis.plot1d;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.gumtree.vis.awt.HelpObject;
import org.gumtree.vis.awt.JChartPanel;
import org.gumtree.vis.core.internal.ImageComboRender;
import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.interfaces.IHelpProvider;
import org.gumtree.vis.mask.RangeMask;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.editor.ExposedChartEditor;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.layout.LCBLayout;
import org.jfree.ui.PaintSample;
import org.jfree.ui.StrokeSample;

/**
 * @author nxi
 *
 */
public class Plot1DChartEditor extends ExposedChartEditor implements KeyListener{


	/**
	 * 
	 */
	private static final long serialVersionUID = 3163030155024979836L;
	private static final String CURVE_COLOR_COMMAND = "changeCurveColour";
	private static final String CURVE_STROCK_COMMAND = "changeCurveStrock";
	private static final String MARKER_SHAPE_COMMAND = "changeMarkerShape";
	private static final String MARKER_FILLED_COMMAND = "changeMarkerFilled";
	private static final String CURVE_VISIBLE_COMMAND = "changeCurveVisibility";
	private static final String CHANGE_CURVE_COMMAND = "changeCurveSelection";
	private static final String SHOW_MARKER_COMMAND = "showBaseMarker";
	private static final String SHOW_ERROR_COMMAND = "showBaseError";
	private static final String LOGARITHM_X_AXIS_COMMAND = "logarithmXAxis";
	private static final String LOGARITHM_Y_AXIS_COMMAND = "logarithmYAxis";
	private static final String FLIP_X_AXIS_COMMAND = "flipXAxis";
	private static final String FLIP_Y_AXIS_COMMAND = "flipYAxis";
	
	private static final String CHANGE_ROI_COMMAND = "changeROI";
	private static final String CREATE_NEW_RECT_REGION_COMMAND = "newRectangleRegion";
	private static final String USE_INCLUSIVE_COMMAND = "inclusiveType";
	private static final String USE_EXCLUSIVE_COMMAND = "exclusiveType";
	private static final String APPLY_CHANGE_ACTION = "applyChange";
	private static final String CHANGE_ROI_NAME_COMMAND = "roiName";
	private static final String REMOVE_CHANGE_ACTION = "removeRegion";
	private static final String X_AXIS_MARGIN_COMMAND = "xAxisMargin";

	private static String currentSeriesKey;
	private int currentSeriesIndex;
	private boolean isPropertiesChanged = false;
	private MarkerShape currentShape;
	private JFreeChart chart;
	private boolean initialLogarithmX = false;
	private boolean initialLogarithmY = false;
	private boolean initialFlipX = false;
	private boolean initialFlipY = false;
	private float initialMargin = 0.05f;
	
	private JComboBox seriesCombo;
	private JComboBox shapeCombo;
	private JComboBox strokeCombo;
	private PaintSample curveColorPaint;
	private StrokeSample curveStrokeSample;
	private JCheckBox showMarker;
	private JCheckBox showError;
	private JCheckBox logarithmX;
	private JCheckBox logarithmY;
	private JCheckBox flipX;
	private JCheckBox flipY;
	private JCheckBox markerFilled;
	private JCheckBox curveVisable;
	private JTextField horizontalMargin;
	private JLabel shapeLabel;
	private ImageComboRender comboRender;
	
	private JChartPanel panel;
	private JComboBox roiCombo;
	private JTextField roiName;
	private JRadioButton inclusiveRadio;
	private JRadioButton exclusiveRadio;
//	private JRadioButton rectangleRadio;
//	private JRadioButton ellipseRadio;
	private JButton newRectangleButton;
	private JButton applyButton;
	private JButton deleteButton;
	private JTextField xMin;
	private JTextField xMax;

	private RangeMask newMask = null;
	private List<RangeMask> toDeleteMasks = new ArrayList<RangeMask>();

	public Plot1DChartEditor(JFreeChart chart, JChartPanel panel) {
		super(chart);
		this.chart = chart;
		this.panel = panel;
		JPanel curves = createCurvesPanel();
		getTabs().add(curves, 0);
		
		JPanel coordinate = createCordinatePanel();
		getTabs().add(coordinate, 1);

    	JPanel masking = createMaskingPanel(chart);
    	getTabs().add(masking, 2);

    	JPanel help = createHelpPanel(chart);
    	getTabs().add(help);
    	
    	if (currentSeriesIndex >= 0) {
			initialise(currentSeriesIndex);
		}
		
    	if (panel.getSelectedMask() != null) {
    		roiCombo.setSelectedItem(panel.getSelectedMask());
    	}
		getTabs().setSelectedComponent(curves);
	}

	private JPanel createHelpPanel(JFreeChart chart) {
		JPanel wrap = new JPanel(new GridLayout(1, 1));
		
    	JPanel helpPanel = new JPanel(new GridLayout(1, 1));
    	helpPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//    	helpPanel.setBorder(BorderFactory.createTitledBorder(
//				BorderFactory.createEtchedBorder(), "Help Topics"));
		
    	SpringLayout spring = new SpringLayout(); 
		JPanel inner = new JPanel(spring);
		inner.setBorder(BorderFactory.createEmptyBorder());

		final IHelpProvider provider = new Plot1DHelpProvider();
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
	
    private JPanel createCurvesPanel() {
    	JPanel wrap = new JPanel(new BorderLayout());
		JPanel curves = new JPanel(new BorderLayout());
		curves.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		JPanel general = new JPanel(new BorderLayout());
		general.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "General"));

		JPanel inner = new JPanel(new LCBLayout(6));
		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		inner.add(new JLabel("Show Marker"));
		showMarker = new JCheckBox();
		showMarker.setActionCommand(SHOW_MARKER_COMMAND);
		showMarker.addActionListener(this);
		inner.add(showMarker);
		inner.add(new JLabel());

		inner.add(new JLabel("Show Error"));
		showError = new JCheckBox();
		showError.setActionCommand(SHOW_ERROR_COMMAND);
		showError.addActionListener(this);
		inner.add(showError);
		inner.add(new JLabel());

		general.add(inner, BorderLayout.NORTH);
		curves.add(general, BorderLayout.NORTH);

		JPanel individual = new JPanel(new BorderLayout());
		individual.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Individual"));

		JPanel interior = new JPanel(new LCBLayout(6));
		interior.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		XYDataset dataset = chart.getXYPlot().getDataset();
		int numberOfSeries = dataset.getSeriesCount();
		String[] seriesNames = new String[numberOfSeries];
		currentSeriesIndex = -1;
		for (int i = 0; i < numberOfSeries; i++) {
			seriesNames[i] = (String) dataset.getSeriesKey(i);
			if (seriesNames[i].equals(currentSeriesKey)) {
				currentSeriesIndex = i;
			}
		}
		if (currentSeriesIndex < 0 && seriesNames.length > 0) {
			currentSeriesIndex = 0;
			currentSeriesKey = seriesNames[currentSeriesIndex];
		}
		
		//Select curve combo
		this.seriesCombo = new JComboBox(seriesNames);
		seriesCombo.setActionCommand(CHANGE_CURVE_COMMAND);
		seriesCombo.addActionListener(this);
		interior.add(new JLabel("Select Curve"));
		interior.add(seriesCombo);
		interior.add(new JLabel(""));

		interior.add(new JLabel("Curve Stroke"));
		curveStrokeSample = new StrokeSample(new BasicStroke());
		curveStrokeSample.setEnabled(false);
		interior.add(curveStrokeSample);
//		JButton button = new JButton(localizationResources.getString("Edit..."));
		Float[] strokes = new Float[]{0f, 0.2f, 0.5f, 1f, 1.5f, 2f, 3f};
		strokeCombo = new JComboBox(strokes);
		strokeCombo.setActionCommand(CURVE_STROCK_COMMAND);
		strokeCombo.addActionListener(this);
		interior.add(strokeCombo);

		interior.add(new JLabel("Curve Colour"));
		curveColorPaint = new PaintSample(chart.getBackgroundPaint());
		interior.add(curveColorPaint);
		JButton button = new JButton(localizationResources.getString("Edit..."));
		button.setActionCommand(CURVE_COLOR_COMMAND);
		button.addActionListener(this);
		interior.add(button);

		interior.add(new JLabel("Marker Shape"));
		shapeLabel = new JLabel();
		interior.add(shapeLabel);
		Integer[] shapeIndex = new Integer[MarkerShape.size];
		for (int i = 0; i < shapeIndex.length; i++) {
			shapeIndex[i] = i;
		}
		shapeCombo = new JComboBox(shapeIndex);
		comboRender = new ImageComboRender();
		comboRender.setShapes(StaticValues.LOCAL_SHAPE_SERIES);
		shapeCombo.setRenderer(comboRender);
		shapeCombo.setMaximumRowCount(7);
		shapeCombo.setActionCommand(MARKER_SHAPE_COMMAND);
		shapeCombo.addActionListener(this);
		interior.add(shapeCombo);
		
		interior.add(new JLabel("Marker Filled"));
		markerFilled = new JCheckBox();
		markerFilled.setActionCommand(MARKER_FILLED_COMMAND);
		markerFilled.addActionListener(this);
		interior.add(markerFilled);
		interior.add(new JLabel());

		interior.add(new JLabel("Curve Visable"));
		curveVisable = new JCheckBox();
		curveVisable.setActionCommand(CURVE_VISIBLE_COMMAND);
		curveVisable.addActionListener(this);
		interior.add(curveVisable);
		interior.add(new JLabel());

		individual.add(interior, BorderLayout.NORTH);
		curves.add(individual);
		curves.setName("Curves");
		wrap.setName("Curves");
		wrap.add(curves, BorderLayout.NORTH);
		return wrap;
	}

	private JPanel createMaskingPanel(JFreeChart chart) {
		JPanel wrap = new JPanel(new BorderLayout());
		
    	JPanel maskingPanel = new JPanel(new GridLayout(1, 1));
    	maskingPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
    	//Horizontal group
    	JPanel managePanel = new JPanel(new BorderLayout());
		managePanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Region of Interests"));

		JPanel inner = new JPanel(new LCBLayout(6));
		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		inner.add(new JLabel("Select Region"));
		roiCombo = new JComboBox(panel.getMasks().toArray());
//		colourScaleCombo.setMaximumRowCount(7);
		roiCombo.setSelectedIndex(-1);
		roiCombo.setActionCommand(CHANGE_ROI_COMMAND);
		roiCombo.addActionListener(this);
		inner.add(roiCombo);
		inner.add(new JLabel());

		inner.add(new JLabel("or create"));
		newRectangleButton = new JButton("New Region of Interests");
		newRectangleButton.setActionCommand(CREATE_NEW_RECT_REGION_COMMAND);
		newRectangleButton.addActionListener(this);
		inner.add(newRectangleButton);
		inner.add(new JLabel());

		JPanel editPanel = new JPanel(new BorderLayout());
		editPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Edit the Region"));
		
		JPanel editInner = new JPanel(new LCBLayout(6));
		editInner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		editInner.add(new JLabel("Name"));
		roiName = new JTextField(10);
//		colourScaleCombo.setMaximumRowCount(7);
		roiName.setActionCommand(CHANGE_ROI_NAME_COMMAND);
		roiName.addActionListener(this);
		editInner.add(roiName);
		editInner.add(new JLabel());
		
		editInner.add(new JLabel("Usage"));
		ButtonGroup buttonGroup = new ButtonGroup();
		JPanel radioPanel = new JPanel(new GridLayout(1, 2));
		inclusiveRadio = new JRadioButton("inclusive");
		inclusiveRadio.setActionCommand(USE_INCLUSIVE_COMMAND);
		inclusiveRadio.addActionListener(this);
		buttonGroup.add(inclusiveRadio);
		radioPanel.add(inclusiveRadio);
		exclusiveRadio = new JRadioButton("exclusive");
		exclusiveRadio.setActionCommand(USE_EXCLUSIVE_COMMAND);
		exclusiveRadio.addActionListener(this);
		buttonGroup.add(exclusiveRadio);
		radioPanel.add(exclusiveRadio);
		editInner.add(radioPanel);
		editInner.add(new JLabel());
		
		editInner.add(new JLabel("X Range"));
		JPanel xRangePanel = new JPanel(new GridLayout(1, 2));
		JPanel xMinPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		xMinPanel.add(new JLabel("min: "));
		xMin = new JTextField(10);
//		xMin.setActionCommand(CHANGE_XMIN_COMMAND);
//		xMin.addActionListener(this);
		xMin.addKeyListener(this);
		xMinPanel.add(xMin);
		xRangePanel.add(xMinPanel);
		JPanel xMaxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		xMaxPanel.add(new JLabel("max: "));
		xMax = new JTextField(10);
//		xMax.setActionCommand(CHANGE_XMAX_COMMAND);
		xMax.addKeyListener(this);
		xMaxPanel.add(xMax);
		xRangePanel.add(xMaxPanel);
		editInner.add(xRangePanel);
		editInner.add(new JLabel());

		editInner.add(new JLabel());
		JPanel applyPanel = new JPanel(new GridLayout(1, 2));
		deleteButton = new JButton("Remove");
		deleteButton.setEnabled(false);
		deleteButton.setActionCommand(REMOVE_CHANGE_ACTION);
		deleteButton.addActionListener(this);
		applyPanel.add(deleteButton);
		applyPanel.add(new JLabel());
		applyButton = new JButton("Apply");
		applyButton.setEnabled(false);
		applyButton.setActionCommand(APPLY_CHANGE_ACTION);
		applyButton.addActionListener(this);
		applyPanel.add(applyButton);
		editInner.add(applyPanel);
		editInner.add(new JLabel());
//		inner.add(new JLabel("X Range"));
//		inner.add(inclusiveRadio);
//		exclusiveRadio = new JRadioButton("exclusive");
//		exclusiveRadio.setActionCommand(USE_EXCLUSIVE_COMMAND);
//		exclusiveRadio.addActionListener(this);
//		inner.add(exclusiveRadio);
		editPanel.add(editInner, BorderLayout.NORTH);
		managePanel.add(editPanel, BorderLayout.SOUTH);
		managePanel.add(inner, BorderLayout.NORTH);
		maskingPanel.add(managePanel);
		wrap.setName("ROI");
		
		wrap.add(maskingPanel, BorderLayout.NORTH);
		return wrap;
	}
	
	private JPanel createCordinatePanel() {
		JPanel wrap = new JPanel(new BorderLayout());
    	JPanel coordinate = new JPanel(new GridLayout(2, 1));
    	coordinate.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
    	//Horizontal group
    	JPanel horizontal = new JPanel(new BorderLayout());
		horizontal.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Horizontal Axis"));

		JPanel inner = new JPanel(new LCBLayout(6));
		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		inner.add(new JLabel("Logarithm X Axis"));
		logarithmX = new JCheckBox();
		logarithmX.setActionCommand(LOGARITHM_X_AXIS_COMMAND);
		logarithmX.addActionListener(this);
		inner.add(logarithmX);
		inner.add(new JLabel());

		inner.add(new JLabel("Flip X Axis"));
		flipX = new JCheckBox();
		flipX.setActionCommand(FLIP_X_AXIS_COMMAND);
		flipX.addActionListener(this);
		inner.add(flipX);
		inner.add(new JLabel());

		inner.add(new JLabel("X Axis Margin Percentage"));
		horizontalMargin = new JTextField();
		horizontalMargin.setActionCommand(X_AXIS_MARGIN_COMMAND);
		horizontalMargin.addActionListener(this);
		inner.add(horizontalMargin);
		inner.add(new JLabel());
		
		horizontal.add(inner, BorderLayout.NORTH);

		//Vertical group
    	JPanel vertical = new JPanel(new BorderLayout());
    	vertical.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Vertical Axis"));

    	inner = new JPanel(new LCBLayout(6));
		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		inner.add(new JLabel("Logarithm Y Axis"));
		logarithmY = new JCheckBox();
		logarithmY.setActionCommand(LOGARITHM_Y_AXIS_COMMAND);
		logarithmY.addActionListener(this);
		inner.add(logarithmY);
		inner.add(new JLabel());

		inner.add(new JLabel("Flip Y Axis"));
		flipY = new JCheckBox();
		flipY.setActionCommand(FLIP_Y_AXIS_COMMAND);
		flipY.addActionListener(this);
		inner.add(flipY);
		inner.add(new JLabel());

    	vertical.add(inner, BorderLayout.NORTH);
    	
    	
		coordinate.add(horizontal, BorderLayout.NORTH);
		coordinate.add(vertical);
		wrap.setName("Coordinate");
		wrap.add(coordinate, BorderLayout.NORTH);
		return wrap;
	}

	private void initialise(int seriesIndex) {
    	if (seriesIndex >= 0) {
    		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
    		if (renderer instanceof XYErrorRenderer) {
    			//Update show marker field
    			showMarker.setSelected(((XYLineAndShapeRenderer) renderer).
    					getBaseShapesVisible());
    			
    			//Update show error field
    			showError.setSelected(((XYErrorRenderer) renderer).getDrawYError());
    			
    			//Update logarithm X filed
    			ValueAxis axis = chart.getXYPlot().getDomainAxis();
    			if (axis instanceof LogarithmizableAxis) {
    				initialLogarithmX = ((LogarithmizableAxis) axis).isLogarithmic();
        			logarithmX.setSelected(initialLogarithmX);
    			} else {
    				logarithmX.setEnabled(false);
    			}
    			initialFlipX = axis.isInverted();
    			flipX.setSelected(initialFlipX);
    			
    			initialMargin = (float) axis.getLowerMargin();
    			horizontalMargin.setText(String.valueOf(initialMargin));
    			//Update logarithm Y filed
    			axis = chart.getXYPlot().getRangeAxis();
    			if (axis instanceof LogarithmizableAxis) {
    				initialLogarithmY = ((LogarithmizableAxis) axis).isLogarithmic();
        			logarithmY.setSelected(initialLogarithmY);
    			} else {
    				logarithmY.setEnabled(false);
    			}
    			initialFlipY = axis.isInverted();
    			flipY.setSelected(initialFlipY);
    			
    			//Update series selection combo field
    			seriesCombo.setSelectedIndex(seriesIndex);
    			
    			//Update curve stroke fields
    			Boolean isLineVisible = ((XYLineAndShapeRenderer) renderer).
    				getSeriesLinesVisible(seriesIndex);
    			if (isLineVisible == null) {
    				isLineVisible = ((XYLineAndShapeRenderer) renderer).
    					getBaseLinesVisible();
    			}
    			if (isLineVisible) {
    				Stroke stroke = renderer.getSeriesStroke(seriesIndex);
    				curveStrokeSample.setStroke(stroke);
    				if (stroke instanceof BasicStroke) {
    					strokeCombo.setSelectedItem(
    							new Float(((BasicStroke) stroke).getLineWidth()));
    				}
    			} else {
    				curveStrokeSample.setStroke(null);
    				strokeCombo.setSelectedIndex(0);
    			}
    			
    			//Update curve colour fields
    			Paint paint = renderer.getSeriesPaint(seriesIndex);
    			curveColorPaint.setPaint(paint);
    			
    			//Update marker fill field
    			Boolean seriesShapeFilled = ((XYLineAndShapeRenderer) 
    					renderer).getSeriesShapesFilled(seriesIndex);
    			if (seriesShapeFilled == null) {
    				seriesShapeFilled = ((XYLineAndShapeRenderer) 
        					renderer).getBaseShapesFilled();
    			}
    			if (seriesShapeFilled != null) {
    				markerFilled.setSelected(seriesShapeFilled);
    			}
    			
    			//Update marker shape fields
    			Boolean isShapeVisible = ((XYLineAndShapeRenderer) renderer).
    					getSeriesShapesVisible(currentSeriesIndex);
    			if (isShapeVisible == null) {
    				isShapeVisible = true;
    			}
    			if (isShapeVisible) {
    				Shape serieseShape = renderer.getSeriesShape(seriesIndex);
    				currentShape = MarkerShape.findMarkerShape(serieseShape);
    				shapeLabel.setIcon(MarkerShape.createIcon(serieseShape, paint,
    						markerFilled.isSelected()));
    				shapeLabel.setText(null);
    			} else {
    				currentShape = MarkerShape.NONE;
    				shapeLabel.setIcon(null);
    				shapeLabel.setText("None");
    			}
				updateComborender(paint, markerFilled.isSelected());

				//Update curve visibility field
				Boolean isCurveVisible = ((XYLineAndShapeRenderer) renderer).
    				isSeriesVisible(seriesIndex);
    			if (isCurveVisible == null) {
    				isCurveVisible = ((XYLineAndShapeRenderer) renderer).
    					getBaseSeriesVisible();
    			}
    			curveVisable.setSelected(isCurveVisible);
    		}
    	}
	}

	private void updateComborender(Paint paint, boolean isFilled) {
		if (paint != null) {
			comboRender.setPaint(paint);
		}
		comboRender.setFilled(isFilled);
		shapeCombo.updateUI();
	}

	@Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals(SHOW_MARKER_COMMAND)) {
        	modifyBaseMarker();
        } else if (command.equals(SHOW_ERROR_COMMAND)) {
            modifyBaseError();
        } else if (command.equals(CHANGE_CURVE_COMMAND)) {
        	if (isPropertiesChanged) {
        		updateChart(chart);
        	}
            currentSeriesIndex = seriesCombo.getSelectedIndex();
            XYDataset dataset = chart.getXYPlot().getDataset();
            currentSeriesKey = (String) dataset.getSeriesKey(currentSeriesIndex);
            initialise(currentSeriesIndex);
            isPropertiesChanged = false;
        } else if (command.equals(CURVE_COLOR_COMMAND)) {
            modifyCurvePaint();
        } else if (command.equals(CURVE_STROCK_COMMAND)) {
        	modifyCurveStroke();
        } else if (command.equals(MARKER_SHAPE_COMMAND)) {
            modifyMarkerShape();
        } else if (command.equals(MARKER_FILLED_COMMAND)) {
            modifyMarkerFilled();
        } else if (command.equals(CURVE_VISIBLE_COMMAND)) {
            modifyCurveVisibility();
        } else if (command.equals(CHANGE_ROI_COMMAND)) {
        	Object obj = roiCombo.getSelectedItem();
        	if (obj instanceof RangeMask) {
        		newMask = null;
        		chooseROI((RangeMask) obj);
        	}
        } else if (command.equals(CREATE_NEW_RECT_REGION_COMMAND)) {
            createNewRectangleROI();
        } else if (command.equals(REMOVE_CHANGE_ACTION)) {
        	removeROI();
        } else if (command.equals(APPLY_CHANGE_ACTION)) {
        	applyROIChange();
        	applyButton.setEnabled(false);
        } else if (command.equals(CHANGE_ROI_NAME_COMMAND)) {
        	applyButton.setEnabled(true);
        } else if (command.equals(USE_INCLUSIVE_COMMAND)) {
        	applyButton.setEnabled(true);
        } else if (command.equals(USE_EXCLUSIVE_COMMAND)) {
        	applyButton.setEnabled(true);
        } else {
        	super.actionPerformed(event);
        }
        if (!command.equals(CHANGE_CURVE_COMMAND)) {
        	isPropertiesChanged = true;
        }
    }

    private void modifyBaseMarker() {
    	if (showMarker.isSelected()) {
    		updateMarkerShape();
    	}
	}

    private void modifyBaseError() {
	}
    
	private void updateMarkerShape() {
		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		if (renderer instanceof XYLineAndShapeRenderer) {
			//Update marker shape fields

			Shape serieseShape = renderer.getSeriesShape(currentSeriesIndex);
			currentShape = MarkerShape.findMarkerShape(serieseShape);
			shapeLabel.setIcon(MarkerShape.createIcon(serieseShape, 
					curveColorPaint.getPaint(), markerFilled.isSelected()));
			shapeLabel.setText(null);
		}
	}

	private void modifyCurveVisibility() {
    	
	}

	private void modifyMarkerFilled() {
    	updateMarkerShapeLabel(curveColorPaint.getPaint());
    	updateComborender(null, markerFilled.isSelected());
	}

	private void modifyMarkerShape() {
    	int selectionIndex = shapeCombo.getSelectedIndex();
    	if (selectionIndex == 0) {
    		shapeLabel.setIcon(null);
    		shapeLabel.setText("None");
    	} else {
    		shapeLabel.setIcon(MarkerShape.createIcon(
    				StaticValues.LOCAL_SHAPE_SERIES[selectionIndex], 
    				curveColorPaint.getPaint(), markerFilled.isSelected()));
    		shapeLabel.setText(null);
    	}
		currentShape = MarkerShape.getMarkerShape(selectionIndex);
	}

	/**
     * Allows the user the opportunity to select a new background paint.  Uses
     * JColorChooser, so we are only allowing a subset of all Paint objects to
     * be selected (fix later).
     */
    private void modifyCurvePaint() {
        Color c;
        c = JColorChooser.showDialog(this, "Curve Color", Color.blue);
        if (c != null) {
            curveColorPaint.setPaint(c);
            updateMarkerShapeLabel(c);
            comboRender.setPaint(c);
            shapeCombo.updateUI();
        }
    }
    
    /**
     * Allow the user to change the outline stroke.
     */
    private void modifyCurveStroke() {
//        StrokeChooserPanel panel = new StrokeChooserPanel(
//                this.curveStrokeSample, createStrokeItems());
//        int result = JOptionPane.showConfirmDialog(this, panel,
//                localizationResources.getString("Stroke_Selection"),
//                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//
//        if (result == JOptionPane.OK_OPTION) {
//            this.curveStrokeSample.setStroke(panel.getSelectedStroke());
//        }
    	Float value = (Float) strokeCombo.getSelectedItem();
    	Stroke stroke = null;
    	if (value > 0) {
    		stroke = new BasicStroke(value.floatValue());
    	}
    	curveStrokeSample.setStroke(stroke);
    }
    
	private void chooseROI(RangeMask mask) {
		if (mask != null) {
			roiName.setText(mask.getName());
			if (mask.isInclusive()) {
				inclusiveRadio.setSelected(true);
			} else {
				exclusiveRadio.setSelected(true);
			}
			Range bounds = mask.getRange();
			xMin.setText(String.valueOf(bounds.getLowerBound()));
			xMax.setText(String.valueOf(bounds.getUpperBound()));
			deleteButton.setEnabled(true);
		} else {
			roiName.setText(null);
			inclusiveRadio.setSelected(false);
			exclusiveRadio.setSelected(false);
			xMin.setText(null);
			xMax.setText(null);
		}
		applyButton.setEnabled(false);
	}
	
	private void createNewRectangleROI() {
		newMask = new RangeMask(true);
		newMask.setName("new mask");
		chooseROI(newMask);
		applyButton.setEnabled(true);
		roiName.requestFocus();
		roiName.selectAll();
	}

	private void removeROI() {
		if (newMask != null) {
			newMask = null;
		} else {
			Object obj = roiCombo.getSelectedItem();
			if (obj instanceof RangeMask) {
				toDeleteMasks.add((RangeMask) obj);
				roiCombo.removeItem(obj);
				roiCombo.setSelectedItem(null);
			}
		}
		chooseROI(null);
		deleteButton.setEnabled(false);
	}
	
	private void applyROIChange(){
		if (newMask != null) {
			changeMask(newMask);
			RangeMask mask = newMask;
			roiCombo.addItem(newMask);
			newMask = mask;
			panel.addMask(newMask);
		} else {
			Object obj = roiCombo.getSelectedItem();
			if (obj instanceof RangeMask) {
				changeMask((RangeMask) obj);
			}
		}
		if (toDeleteMasks.size() > 0) {
			for (RangeMask mask : toDeleteMasks) {
				panel.removeMask(mask);
			}
			toDeleteMasks.clear();
		}
	}
	
	private void changeMask(RangeMask mask) {
		mask.setName(roiName.getText());
		if (inclusiveRadio.isSelected()) {
			mask.setInclusive(true);
		} else if (exclusiveRadio.isSelected()) {
			mask.setInclusive(false);
		}
		double minX = Double.valueOf(xMin.getText());
		double maxX = Double.valueOf(xMax.getText());
		mask.setBoundary(Math.min(minX, maxX), Math.max(minX, maxX));
	}
	
    @Override
    public void updateChart(JFreeChart chart) {
    	super.updateChart(chart);
    	if (currentSeriesIndex >= 0) {
    		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
    		if (renderer instanceof XYLineAndShapeRenderer) {
    			renderer.setSeriesPaint(currentSeriesIndex, 
    					curveColorPaint.getPaint());
    			Stroke stroke = curveStrokeSample.getStroke();
    			if (stroke == null) {
    				((XYLineAndShapeRenderer) renderer).setSeriesLinesVisible(
    						currentSeriesIndex, false);
    			} else {
    				((XYLineAndShapeRenderer) renderer).setSeriesLinesVisible(
    						currentSeriesIndex, true);
    				renderer.setSeriesStroke(currentSeriesIndex, stroke);
    			}

    			((XYErrorRenderer) renderer).setDrawYError(showError.isSelected());
    			
    			boolean isMarkerVisible = showMarker.isSelected();
    			if (!isMarkerVisible) {
    				for (int i = 0; i < chart.getXYPlot().getSeriesCount(); i++) {
    					((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(i, 
    							isMarkerVisible);
    				}
    			}
    			((XYLineAndShapeRenderer) renderer).setBaseShapesVisible(
    					isMarkerVisible);
    			
    			Shape shape = currentShape.getShape();
    			if (shape == null) {
    				((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(
    						currentSeriesIndex, false);
    			} else {
    				((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(
    						currentSeriesIndex,  isMarkerVisible);
    				renderer.setSeriesShape(currentSeriesIndex, shape);
    			}
    			
    			((XYLineAndShapeRenderer) renderer).setSeriesShapesFilled(
    					currentSeriesIndex, markerFilled.isSelected());
    			
    			renderer.setSeriesVisible(currentSeriesIndex, 
    					curveVisable.isSelected());
    			
    			//Update logarithm X filed
    			ValueAxis axis = chart.getXYPlot().getDomainAxis();
    			if (axis instanceof LogarithmizableAxis) {
    				if (logarithmX.isSelected() != initialLogarithmX) {
    					((LogarithmizableAxis) axis).setLogarithmic(
    							logarithmX.isSelected());
    					((LogarithmizableAxis) axis).autoAdjustRange();
    				}
    			}
    			if (flipX.isSelected() != initialFlipX) {
    				axis.setInverted(flipX.isSelected());
    			}
    			
    			float newMargin = initialMargin;
    			try {
    				newMargin = Float.valueOf(horizontalMargin.getText());
    				if (newMargin < 0) {
    					newMargin = 0;
    				} else if (newMargin > 0.10) {
    					newMargin = 0.10f;
    				}
				} catch (Exception e) {
				}
    			if (newMargin != initialMargin) {
    				axis.setLowerMargin(newMargin);
    				axis.setUpperMargin(newMargin);
    			}
    			
    			//Update logarithm Y filed
    			axis = chart.getXYPlot().getRangeAxis();
    			if (axis instanceof LogarithmizableAxis) {
    				if (logarithmY.isSelected() != initialLogarithmY) {
    					((LogarithmizableAxis) axis).setLogarithmic(
    							logarithmY.isSelected());
    					((LogarithmizableAxis) axis).autoAdjustRange();
    				}
    			}
    			if (flipY.isSelected() != initialFlipY) {
    				axis.setInverted(flipY.isSelected());
    			}
    		}
    	}
    	applyROIChange();
    }
    
    public static void setSuggestedSeriesKey(String key) {
    	currentSeriesKey = key;
    }
    
//    public static StrokeSample[] createStrokeItems() {
//    	StrokeSample[] availableStrokeSamples = new StrokeSample[5];
//    	availableStrokeSamples[0] = new StrokeSample(null);
//        availableStrokeSamples[1] = new StrokeSample(
//        		new BasicStroke(0.1f));
//        availableStrokeSamples[2] = new StrokeSample(
//                new BasicStroke(1.0f));
//        availableStrokeSamples[3] = new StrokeSample(
//                new BasicStroke(2.0f));
//        availableStrokeSamples[4] = new StrokeSample(
//                new BasicStroke(3.0f));
//        return availableStrokeSamples;
//    }
//    
    private void updateMarkerShapeLabel(Paint paint) {
    	shapeLabel.setIcon(MarkerShape.createIcon(
				currentShape.getShape(), paint, markerFilled.isSelected()));
    }

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		applyButton.setEnabled(true);
	}
}
