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
package org.gumtree.vis.hist2d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.gumtree.vis.hist2d.color.ColorScale;
import org.gumtree.vis.interfaces.IHelpProvider;
import org.gumtree.vis.mask.Abstract2DMask;
import org.gumtree.vis.mask.EllipseMask;
import org.gumtree.vis.mask.RectangleMask;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.editor.ExposedChartEditor;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.layout.LCBLayout;

/**
 * @author nxi
 *
 */
public class Hist2DChartEditor extends ExposedChartEditor implements KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1626220960806011268L;
	private static final String CHANGE_ROI_COMMAND = "changeROI";
	private static final String SHOW_SCALE_COMMAND = "showColorScale";
	private static final String CHANGE_COLOUR_SCALE_COMMAND = "showBaseError";
	private static final String LOGARITHM_SCALE_COMMAND = "useLogarithmScale";
	private static final String CREATE_NEW_RECT_REGION_COMMAND = "newRectangleRegion";
	private static final String USE_INCLUSIVE_COMMAND = "inclusiveType";
	private static final String USE_EXCLUSIVE_COMMAND = "exclusiveType";
	private static final String FLIP_X_AXIS_COMMAND = "flipXAxis";
	private static final String FLIP_Y_AXIS_COMMAND = "flipYAxis";
	private static final String APPLY_CHANGE_ACTION = "applyChange";
	private static final String RECTANGLE_SHAPE_LABEL = "rectangle";
	private static final String ELLIPSE_SHAPE_LABEL = "ellipse";
	private static final String CHANGE_ROI_NAME_COMMAND = "roiName";
	private static final String CHANGE_XMIN_COMMAND = "xMin";
	private static final String CHANGE_XMAX_COMMAND = "xMax";
	private static final String CHANGE_YMIN_COMMAND = "yMin";
	private static final String CHANGE_YMAX_COMMAND = "yMax";
	private static final String CREATE_NEW_ELLIPSE_REGION_COMMAND = "newEllipseRegion";
	private static final String REMOVE_CHANGE_ACTION = "removeRegion";

	private JChartPanel panel;
	private JComboBox roiCombo;
	private JTextField roiName;
	private JRadioButton inclusiveRadio;
	private JRadioButton exclusiveRadio;
//	private JRadioButton rectangleRadio;
//	private JRadioButton ellipseRadio;
	private JLabel shapeLabel;
	private JButton newRectangleButton;
	private JButton newEllipseButton;
	private JButton applyButton;
	private JButton deleteButton;
	private JTextField xMin;
	private JTextField xMax;
	private JTextField yMin;
	private JTextField yMax;
	private JComboBox colourScaleCombo;
	private JCheckBox logarithmScale;
	private JCheckBox flipX;
	private JCheckBox flipY;
	private JCheckBox showColorScale;
	private boolean initialFlipX = false;
	private boolean initialFlipY = false;

	private Abstract2DMask newMask = null;
	private List<Abstract2DMask> toDeleteMasks = new ArrayList<Abstract2DMask>();
	
	/**
	 * @param chart
	 */
	public Hist2DChartEditor(JFreeChart chart, JChartPanel panel) {
		super(chart);
		this.panel = panel;
//		JPanel general = getGeneralPanel();
    	JPanel coordinate = createCordinatePanel();
    	getTabs().add(coordinate, 0);
    	JPanel masking = createMaskingPanel(chart);
    	getTabs().add(masking, 1);
    	JPanel help = createHelpPanel(chart);
    	getTabs().add(help);
    	getTabs().setSelectedComponent(coordinate);
		initialise(chart);
		if (panel.getSelectedMask() != null) {
    		roiCombo.setSelectedItem(panel.getSelectedMask());
    	}
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

		final IHelpProvider provider = new Hist2DHelpProvider();
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
		roiCombo.setActionCommand(CHANGE_ROI_COMMAND);
		roiCombo.addActionListener(this);
		roiCombo.setSelectedIndex(-1);
		inner.add(roiCombo);
		inner.add(new JLabel());

		inner.add(new JLabel("or create"));
		JPanel createNewPanel = new JPanel(new GridLayout(1, 2));
		newRectangleButton = new JButton("New Rectangle");
		newRectangleButton.setActionCommand(CREATE_NEW_RECT_REGION_COMMAND);
		newRectangleButton.addActionListener(this);
		newEllipseButton = new JButton("New Ellipse");
		newEllipseButton.setActionCommand(CREATE_NEW_ELLIPSE_REGION_COMMAND);
		newEllipseButton.addActionListener(this);
		createNewPanel.add(newRectangleButton);
		createNewPanel.add(newEllipseButton);
		inner.add(createNewPanel);
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
		
		editInner.add(new JLabel("Shape"));
//		ButtonGroup shapeGroup = new ButtonGroup();
//		JPanel shapePanel = new JPanel(new GridLayout(1, 2));
//		rectangleRadio = new JRadioButton("rectangle");
//		rectangleRadio.setActionCommand(CHOOSE_RECTANGLE_SHAPE_COMMAND);
//		rectangleRadio.addActionListener(this);
//		shapeGroup.add(rectangleRadio);
//		shapePanel.add(rectangleRadio);
//		ellipseRadio = new JRadioButton("ellipse");
//		ellipseRadio.setActionCommand(CHOOSE_ELLIPSE_SHAPE_COMMAND);
//		ellipseRadio.addActionListener(this);
//		shapeGroup.add(ellipseRadio);
//		shapePanel.add(ellipseRadio);
//		editInner.add(shapePanel);
		shapeLabel = new JLabel();
		editInner.add(shapeLabel);
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

		editInner.add(new JLabel("Y Range"));
		JPanel yRangePanel = new JPanel(new GridLayout(1, 2));
		JPanel yMinPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		yMinPanel.add(new JLabel("min: "));
		yMin = new JTextField(10);
//		yMin.setActionCommand(CHANGE_YMIN_COMMAND);
//		yMin.addActionListener(this);
		yMin.addKeyListener(this);
		yMinPanel.add(yMin);
		yRangePanel.add(yMinPanel);
		JPanel yMaxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		yMaxPanel.add(new JLabel("max: "));
		yMax = new JTextField(10);
//		yMax.setActionCommand(CHANGE_YMAX_COMMAND);
//		yMax.addActionListener(this);
		yMax.addKeyListener(this);
		yMaxPanel.add(yMax);
		yRangePanel.add(yMaxPanel);
		editInner.add(yRangePanel);
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
		
    	JPanel coordinate = new JPanel(new GridLayout(3, 1));
    	coordinate.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
    	//Horizontal group
    	JPanel horizontal = new JPanel(new BorderLayout());
		horizontal.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Horizontal Axis"));

		JPanel inner = new JPanel(new LCBLayout(6));
		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		inner.add(new JLabel("Flip X Axis"));
		flipX = new JCheckBox();
		flipX.setActionCommand(FLIP_X_AXIS_COMMAND);
		flipX.addActionListener(this);
		inner.add(flipX);
		inner.add(new JLabel());

		horizontal.add(inner, BorderLayout.NORTH);

		//Vertical group
    	JPanel vertical = new JPanel(new BorderLayout());
    	vertical.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Vertical Axis"));

    	inner = new JPanel(new LCBLayout(6));
		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		inner.add(new JLabel("Flip Y Axis"));
		flipY = new JCheckBox();
		flipY.setActionCommand(FLIP_Y_AXIS_COMMAND);
		flipY.addActionListener(this);
		inner.add(flipY);
		inner.add(new JLabel());

    	vertical.add(inner, BorderLayout.NORTH);
    	
		//Color scale group
    	JPanel colorScale = new JPanel(new BorderLayout());
    	colorScale.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Color Scale"));

    	inner = new JPanel(new LCBLayout(6));
		inner.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		inner.add(new JLabel("Show Color Scale"));
		showColorScale = new JCheckBox();
		showColorScale.setActionCommand(SHOW_SCALE_COMMAND);
		showColorScale.addActionListener(this);
		inner.add(showColorScale);
		inner.add(new JLabel());

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

		coordinate.add(horizontal);
		coordinate.add(vertical);
		coordinate.add(colorScale);
		wrap.setName("Coordinate");
		
		wrap.add(coordinate, BorderLayout.NORTH);
		return wrap;
	}
	
	private void initialise(JFreeChart chart) {
		showColorScale.setSelected(chart.isShowSubtitle());
		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		if (renderer instanceof XYBlockRenderer) {
			PaintScale scale = ((XYBlockRenderer) renderer).getPaintScale();
			if (scale instanceof ColorPaintScale) {
				colourScaleCombo.setSelectedItem(((ColorPaintScale) scale).getColorScale());
				logarithmScale.setSelected(((ColorPaintScale) scale).isLogScale());
			}
		}
		ValueAxis axis = chart.getXYPlot().getDomainAxis();
		initialFlipX = axis.isInverted();
		flipX.setSelected(initialFlipX);
		
		axis = chart.getXYPlot().getRangeAxis();
		initialFlipY = axis.isInverted();
		flipY.setSelected(initialFlipY);

	}
	
	private void chooseROI(Abstract2DMask mask) {
		if (mask != null) {
			roiName.setText(mask.getName());
			if (mask instanceof EllipseMask) {
				shapeLabel.setText(ELLIPSE_SHAPE_LABEL);
			} else {
				shapeLabel.setText(RECTANGLE_SHAPE_LABEL);
			}
			if (mask.isInclusive()) {
				inclusiveRadio.setSelected(true);
			} else {
				exclusiveRadio.setSelected(true);
			}
			Rectangle2D bounds = mask.getRectangleFrame();
			xMin.setText(String.valueOf(bounds.getMinX()));
			xMax.setText(String.valueOf(bounds.getMaxX()));
			yMin.setText(String.valueOf(bounds.getMinY()));
			yMax.setText(String.valueOf(bounds.getMaxY()));
//			xMin.setText(String.format("%.2f", bounds.getMinX()));
//			xMax.setText(String.format("%.2f", bounds.getMaxX()));
//			yMin.setText(String.format("%.2f", bounds.getMinY()));
//			yMax.setText(String.format("%.2f", bounds.getMaxY()));
			deleteButton.setEnabled(true);
		} else {
			roiName.setText(null);
			shapeLabel.setText(null);
			inclusiveRadio.setSelected(false);
			exclusiveRadio.setSelected(false);
			xMin.setText(null);
			xMax.setText(null);
			yMin.setText(null);
			yMax.setText(null);
		}
		applyButton.setEnabled(false);
	}
	
	private void createNewRectangleROI() {
		newMask = new RectangleMask(true, 0, 0, 0, 0);
		newMask.setName("new mask");
		chooseROI(newMask);
		applyButton.setEnabled(true);
		roiName.requestFocus();
		roiName.selectAll();
	}

	private void createNewEllipseROI() {
		newMask = new EllipseMask(true, 0, 0, 0, 0);
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
			if (obj instanceof Abstract2DMask) {
				toDeleteMasks.add((Abstract2DMask) obj);
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
			Abstract2DMask mask = newMask;
			roiCombo.addItem(newMask);
			newMask = mask;
			panel.addMask(newMask);
		} else {
			Object obj = roiCombo.getSelectedItem();
			if (obj instanceof Abstract2DMask) {
				changeMask((Abstract2DMask) obj);
			}
		}
		if (toDeleteMasks.size() > 0) {
			for (Abstract2DMask mask : toDeleteMasks) {
				panel.removeMask(mask);
			}
			toDeleteMasks.clear();
		}
	}
	
	private void changeMask(Abstract2DMask mask) {
		mask.setName(roiName.getText());
		if (inclusiveRadio.isSelected()) {
			mask.setInclusive(true);
		} else if (exclusiveRadio.isSelected()) {
			mask.setInclusive(false);
		}
		double minX = Double.valueOf(xMin.getText());
		double maxX = Double.valueOf(xMax.getText());
		double minY = Double.valueOf(yMin.getText());
		double maxY = Double.valueOf(yMax.getText());
		mask.setRectangleFrame(new Rectangle2D.Double(Math.min(minX, maxX), Math.min(minY, maxY), 
				Math.abs(maxX - minX), Math.abs(maxY - minY)));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
        if (command.equals(CHANGE_ROI_COMMAND)) {
        	Object obj = roiCombo.getSelectedItem();
        	if (obj instanceof Abstract2DMask) {
        		newMask = null;
        		chooseROI((Abstract2DMask) obj);
        	}
        } else if (command.equals(CREATE_NEW_RECT_REGION_COMMAND)) {
            createNewRectangleROI();
        } else if (command.equals(CREATE_NEW_ELLIPSE_REGION_COMMAND)) {
            createNewEllipseROI();
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
        } else if (command.equals(CHANGE_XMIN_COMMAND)) {
        	applyButton.setEnabled(true);
        } else if (command.equals(CHANGE_XMAX_COMMAND)) {
        	applyButton.setEnabled(true);
        } else if (command.equals(CHANGE_YMIN_COMMAND)) {
        	applyButton.setEnabled(true);
        } else if (command.equals(CHANGE_YMAX_COMMAND)) {
        	applyButton.setEnabled(true);
        } 
	}
	
	@Override
	public void updateChart(JFreeChart chart) {
		chart.setShowSubtitle(showColorScale.isSelected());
		for (Object object : chart.getSubtitles()) {
			if (object instanceof PaintScaleLegend) {
				PaintScale scale = ((PaintScaleLegend) object).getScale();
				if (scale instanceof ColorPaintScale) {
					((ColorPaintScale) scale).setLogScale(logarithmScale.isSelected());
					((ColorPaintScale) scale).setColorScale(
							(ColorScale) colourScaleCombo.getSelectedItem());
					XYItemRenderer renderer = chart.getXYPlot().getRenderer();
					if (renderer instanceof XYBlockRenderer) {
						((XYBlockRenderer) renderer).setPaintScale(scale);
					}
				}
			}
		}
		
		ValueAxis axis = chart.getXYPlot().getDomainAxis();
		if (flipX.isSelected() != initialFlipX) {
			axis.setInverted(flipX.isSelected());
		}

		axis = chart.getXYPlot().getRangeAxis();
		if (flipY.isSelected() != initialFlipY) {
			axis.setInverted(flipY.isSelected());
		}
		
		applyROIChange();
		super.updateChart(chart);	
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
