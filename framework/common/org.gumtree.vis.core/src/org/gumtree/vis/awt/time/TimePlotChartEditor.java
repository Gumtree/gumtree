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
package org.gumtree.vis.awt.time;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.gumtree.vis.core.internal.ImageComboRender;
import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.ITimeSeriesSet;
import org.gumtree.vis.plot1d.LogarithmizableAxis;
import org.gumtree.vis.plot1d.MarkerShape;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.editor.ExposedChartEditor;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.layout.LCBLayout;
import org.jfree.ui.PaintSample;
import org.jfree.ui.StrokeSample;

/**
 * @author nxi
 *
 */
public class TimePlotChartEditor extends ExposedChartEditor{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4452369559130703413L;
	private static final String CURVE_COLOR_COMMAND = "changeCurveColour";
	private static final String CURVE_STROCK_COMMAND = "changeCurveStrock";
	private static final String MARKER_SHAPE_COMMAND = "changeMarkerShape";
	private static final String MARKER_FILLED_COMMAND = "changeMarkerFilled";
	private static final String CURVE_VISIBLE_COMMAND = "changeCurveVisibility";
	private static final String CHANGE_CURVE_COMMAND = "changeCurveSelection";
	private static final String SHOW_MARKER_COMMAND = "showBaseMarker";
	private static final String SHOW_ERROR_COMMAND = "showBaseError";
	private static final String SHOW_ALL_HISTORY_COMMAND = "showAllHistory";
	private static final String FIXED_LENGTH_HISTORY_COMMAND = "showPartHistory";
	private static final String FLIP_X_AXIS_COMMAND = "flipXAxis";
	private static final String FLIP_Y_AXIS_COMMAND = "flipYAxis";
	

	private static String currentSeriesKey;
	private IDataset currentDataset;
	private int currentSeriesIndex;
	private boolean isPropertiesChanged = false;
	private MarkerShape currentShape;
	private JFreeChart chart;
//	private boolean initialLogarithmX = false;
//	private boolean initialLogarithmY = false;
//	private boolean initialFlipX = false;
//	private boolean initialFlipY = false;
	
	private JComboBox seriesCombo;
	private JComboBox shapeCombo;
	private JComboBox strokeCombo;
	private PaintSample curveColorPaint;
	private StrokeSample curveStrokeSample;
	private JCheckBox showMarker;
	private JRadioButton showAllHistory;
	private JRadioButton showPartHistory;
	private JTextField timeField;
//	private JCheckBox flipX;
//	private JCheckBox flipY;
	private JCheckBox markerFilled;
	private JCheckBox curveVisable;
	private JLabel shapeLabel;
	private ImageComboRender comboRender;

	public TimePlotChartEditor(JFreeChart chart) {
		super(chart);
		this.chart = chart;
		
		JPanel curves = createCurvesPanel();
		getTabs().add(curves, 0);
		
		JPanel coordinate = createCordinatePanel();
		getTabs().add(coordinate, 1);

		if (currentDataset != null && currentSeriesIndex >= 0) {
			initialise(currentDataset, currentSeriesIndex);
		}
		
		getTabs().setSelectedComponent(curves);
	}

    private JPanel createCurvesPanel() {
    	JPanel wrap = new JPanel(new BorderLayout());
		JPanel curves = new JPanel(new BorderLayout());
		curves.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

//		JPanel general = new JPanel(new BorderLayout());
//		general.setBorder(BorderFactory.createTitledBorder(
//				BorderFactory.createEtchedBorder(), "General"));
//
//		JPanel inner = new JPanel(new LCBLayout(6));
//		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
//
//		inner.add(new JLabel("Show Marker"));
//		showMarker = new JCheckBox();
//		showMarker.setActionCommand(SHOW_MARKER_COMMAND);
//		showMarker.addActionListener(this);
//		inner.add(showMarker);
//		inner.add(new JLabel());

//		inner.add(new JLabel("Show Error"));
//		showError = new JCheckBox();
//		showError.setActionCommand(SHOW_ERROR_COMMAND);
//		showError.addActionListener(this);
//		inner.add(showError);
//		inner.add(new JLabel());
//
//		general.add(inner, BorderLayout.NORTH);
//		curves.add(general, BorderLayout.NORTH);

		JPanel individual = new JPanel(new BorderLayout());
		individual.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Individual Curve"));

		JPanel interior = new JPanel(new LCBLayout(7));
		interior.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		int numberOfDataset = chart.getXYPlot().getDatasetCount();
		currentDataset = null;
		currentSeriesIndex = -1;
		List<String> seriesNames = new ArrayList<String>();
		for (int i = 0; i < numberOfDataset; i++) {
			XYDataset dataset = chart.getXYPlot().getDataset(i);
			if (dataset != null && dataset instanceof ITimeSeriesSet) {
				int numberOfSeries = dataset.getSeriesCount();
				for (int j = 0; j < numberOfSeries; j++) {
					String seriesName = (String) dataset.getSeriesKey(j);
					seriesNames.add(seriesName);
					if (seriesName.equals(currentSeriesKey)) {
						currentDataset = (IDataset) dataset;
						currentSeriesIndex = j;
					}
				}
			}
		}
		if ((currentDataset == null || currentSeriesIndex < 0) && seriesNames.size() > 0) {
			for (int i = 0; i < numberOfDataset; i++) {
				XYDataset dataset = chart.getXYPlot().getDataset(i);
				if (dataset != null && dataset instanceof ITimeSeriesSet
						&& dataset.getSeriesCount() > 0) {
					currentDataset = (IDataset) dataset;
					currentSeriesIndex = 0;
					currentSeriesKey = (String) dataset.getSeriesKey(currentSeriesIndex);
					break;
				}
			}
		}
		
		//Select curve combo
		this.seriesCombo = new JComboBox(seriesNames.toArray());
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

		interior.add(new JLabel("Marker Visible"));
		showMarker = new JCheckBox();
		showMarker.setActionCommand(SHOW_MARKER_COMMAND);
		showMarker.addActionListener(this);
		interior.add(showMarker);
		interior.add(new JLabel());
		
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
		curves.add(individual, BorderLayout.NORTH);
		curves.setName("Curves");
		wrap.setName("Curves");
		wrap.add(curves, BorderLayout.NORTH);
		return wrap;
	}

	private JPanel createCordinatePanel() {
		JPanel wrap = new JPanel(new BorderLayout());
    	JPanel coordinate = new JPanel(new GridLayout(2, 1));
    	coordinate.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
    	//Horizontal group
    	JPanel horizontal = new JPanel(new BorderLayout());
		horizontal.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Time Axis"));

		JPanel inner = new JPanel(new LCBLayout(6));
		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		ButtonGroup axisControlGroup = new ButtonGroup();
		
		showAllHistory = new JRadioButton("Show all history");
		showAllHistory.setActionCommand(SHOW_ALL_HISTORY_COMMAND);
		showAllHistory.addActionListener(this);
		inner.add(showAllHistory);
		inner.add(new JLabel());
		inner.add(new JLabel());
//
		showPartHistory = new JRadioButton("Fix time range");
		showPartHistory.setActionCommand(FIXED_LENGTH_HISTORY_COMMAND);
		showPartHistory.addActionListener(this);
		inner.add(showPartHistory);
		timeField = new JTextField(5);
		inner.add(timeField);
		inner.add(new JLabel("seconds"));
		axisControlGroup.add(showAllHistory);
		axisControlGroup.add(showPartHistory);
		
		horizontal.add(inner, BorderLayout.NORTH);

		//Vertical group
//    	JPanel vertical = new JPanel(new BorderLayout());
//    	vertical.setBorder(BorderFactory.createTitledBorder(
//				BorderFactory.createEtchedBorder(), "Vertical Axis"));
//
//    	inner = new JPanel(new LCBLayout(6));
//		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

//		inner.add(new JLabel("Logarithm Y Axis"));
//		logarithmY = new JCheckBox();
//		logarithmY.setActionCommand(LOGARITHM_Y_AXIS_COMMAND);
//		logarithmY.addActionListener(this);
//		inner.add(logarithmY);
//		inner.add(new JLabel());
//
//		inner.add(new JLabel("Flip Y Axis"));
//		flipY = new JCheckBox();
//		flipY.setActionCommand(FLIP_Y_AXIS_COMMAND);
//		flipY.addActionListener(this);
//		inner.add(flipY);
//		inner.add(new JLabel());

//    	vertical.add(inner, BorderLayout.NORTH);
    	
    	
		coordinate.add(horizontal, BorderLayout.NORTH);
//		coordinate.add(vertical);
		wrap.setName("Coordinate");
		wrap.add(coordinate, BorderLayout.NORTH);
		return wrap;
	}

	private void initialise(IDataset dataset, int seriesIndex) {
    	if (dataset != null && seriesIndex >= 0) {
    		XYItemRenderer renderer = chart.getXYPlot().getRendererForDataset(dataset);
    		if (renderer instanceof XYLineAndShapeRenderer) {
    			//Update show marker field
//    			showMarker.setSelected(((XYLineAndShapeRenderer) renderer).
//    					getBaseShapesVisible());
    			
    			//Update series selection combo field
    			seriesCombo.setSelectedItem(dataset.getSeriesKey(seriesIndex));
    			
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
    			
    			//Update marker visible field
    			Boolean isMarkerVisible = ((XYLineAndShapeRenderer) renderer).
    					getSeriesShapesVisible(currentSeriesIndex);
    			if (isMarkerVisible == null) {
    				isMarkerVisible = ((XYLineAndShapeRenderer) renderer).
					getBaseShapesVisible();
    				if (isMarkerVisible == null) {
    					isMarkerVisible = false;
    				}
    			}
    			showMarker.setSelected(isMarkerVisible);

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
    			
    			ValueAxis timeAxis = chart.getXYPlot().getDomainAxis();
    			double fixedTime = timeAxis.getFixedAutoRange();
    			if (fixedTime <= 0) {
    				showAllHistory.setSelected(true);
    			} else {
    				showPartHistory.setSelected(true);
    				timeField.setText(String.valueOf(fixedTime / 1000));
    			}
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
        if (command.equals(SHOW_ERROR_COMMAND)) {
            modifyBaseError();
        } else if (command.equals(CHANGE_CURVE_COMMAND)) {
        	if (isPropertiesChanged) {
        		updateChart(chart);
        	}
        	currentSeriesKey = (String) seriesCombo.getSelectedItem();
        	for (int i = 0; i < chart.getXYPlot().getDatasetCount(); i++) {
        		XYDataset dataset = chart.getXYPlot().getDataset(i);
        		if (dataset != null && dataset instanceof IDataset && dataset.getSeriesCount() > 0) {
        			int indexOfKey = dataset.indexOf(currentSeriesKey);
        			if (indexOfKey >= 0) {
        				currentDataset = (IDataset) dataset;
        				currentSeriesIndex = indexOfKey;
        	            initialise(currentDataset, currentSeriesIndex);
        	            isPropertiesChanged = false;
        	            break;
        			}
        		}
        	}
        } else if (command.equals(CURVE_COLOR_COMMAND)) {
            modifyCurvePaint();
        } else if (command.equals(CURVE_STROCK_COMMAND)) {
        	modifyCurveStroke();
        } else if (command.equals(SHOW_MARKER_COMMAND)) {
        	modifyMarkerVisible();
        } else 
        	if (command.equals(MARKER_SHAPE_COMMAND)) {
            modifyMarkerShape();
        } else if (command.equals(MARKER_FILLED_COMMAND)) {
            modifyMarkerFilled();
        } else if (command.equals(CURVE_VISIBLE_COMMAND)) {
            modifyCurveVisibility();
        } else {
        	super.actionPerformed(event);
        }
    	isPropertiesChanged = true;
    }

    private void modifyMarkerVisible() {
    	if (showMarker.isSelected()) {
    		updateMarkerShape();
    	}
	}

    private void modifyBaseError() {
	}
    
	private void updateMarkerShape() {
		XYItemRenderer renderer = chart.getXYPlot().getRendererForDataset(currentDataset);
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
    
    @Override
    public void updateChart(JFreeChart chart) {
    	super.updateChart(chart);
    	if (currentDataset != null && currentSeriesIndex >= 0) {
    		XYItemRenderer renderer = chart.getXYPlot().getRendererForDataset(currentDataset);
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

//    			if (!isMarkerVisible) {
//    				for (int i = 0; i < chart.getXYPlot().getSeriesCount(); i++) {
//    					((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(i, 
//    							isMarkerVisible);
//    				}
//    			}
//    			((XYLineAndShapeRenderer) renderer).setBaseShapesVisible(
//    					isMarkerVisible);
    			
    			((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(
						currentSeriesIndex, showMarker.isSelected());
    			
    			Shape shape = currentShape.getShape();
    			if (shape == null) {
    				((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(
    						currentSeriesIndex, false);
    			} else {
//    				((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(
//    						currentSeriesIndex,  true);
    				renderer.setSeriesShape(currentSeriesIndex, shape);
    			}
    			
    			((XYLineAndShapeRenderer) renderer).setSeriesShapesFilled(
    					currentSeriesIndex, markerFilled.isSelected());
    			
    			renderer.setSeriesVisible(currentSeriesIndex, 
    					curveVisable.isSelected());
    			
    			//Update logarithm X filed
    			ValueAxis axis = chart.getXYPlot().getDomainAxis();
    			if (showAllHistory.isSelected()) {
    				axis.setFixedAutoRange(0d);
    			}
    			if (showPartHistory.isSelected()) {
    				double fixedTimeSec = 0;
    				try{
    					fixedTimeSec = Double.valueOf(timeField.getText());
    				}catch (Exception e) {
						// TODO: handle exception
					}
    				if (fixedTimeSec <= 0) {
    					fixedTimeSec = 60;
    				}
    				axis.setFixedAutoRange(fixedTimeSec * 1000);
    			}
    			
    			//Update logarithm Y filed
    			axis = chart.getXYPlot().getRangeAxis();
    			if (axis instanceof LogarithmizableAxis) {
//    				if (logarithmY.isSelected() != initialLogarithmY) {
//    					((LogarithmizableAxis) axis).setLogarithmic(
//    							logarithmY.isSelected());
//    					((LogarithmizableAxis) axis).autoAdjustRange();
//    				}
    			}
    			
    		}
    	}
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
}
