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
package org.gumtree.vis.core.internal;

import java.awt.Cursor;
import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.gumtree.vis.hist2d.color.ColorScale;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.util.ShapeUtilities;

/**
 * @author nxi
 *
 */
public class StaticValues {
	
    public final static int PANEL_WIDTH = 800;
    public final static int PANEL_HEIGHT = 600;
    public final static int PANEL_MINIMUM_DRAW_WIDTH = 100;
    public final static int PANEL_MINIMUM_DRAW_HEIGHT = 50;
    public final static int PANEL_MAXIMUM_DRAW_WIDTH = 1280 * 3;
    public final static int PANEL_MAXIMUM_DRAW_HEIGHT = 1200;
    public final static double SMALLEST_POSITIVE_VALUE = 1e-8;
	public final static float defaultZoomInFactor = 0.85f;
	public final static float defaultZoomOutFactor = 1 / defaultZoomInFactor;
	public final static int DOMAIN_MASK_SHIFT_RESOLUTION = 200;
    public final static int NUMBER_OF_MASK_COLORS = 8;
    public final static ColorScale DEFAULT_COLOR_SCALE = ColorScale.Nature;
	public static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	public static final Cursor defaultCursor = Cursor.getDefaultCursor();
	public static final String SYSTEM_SAVE_PATH_LABEL = "SYSTEM_SAVE_PATH";
	public static final String SHOW_COLORSCALE_PROPERTY = "hist2d.showSubtitle";
	public static final String HORIZONTAL_MARGIN_PROPERTY = "plot1D.horizontalMarginPercentage";
	public static final String DEFAULT_IMAGE_FILE_EXTENSION = "jpg";
    
//    public static Shape[] SHAPE_SERIES_10 = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE;
	public static Shape[] LOCAL_SHAPE_SERIES = createLocalMarkerShape();

	public static Shape[] createLocalMarkerShape() {
		Shape[] shapeSeries = new Shape[13];
		shapeSeries[0] = null;
		shapeSeries[1] = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[0];
		shapeSeries[2] = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[1];
		shapeSeries[3] = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[2];
		shapeSeries[4] = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[3];
		shapeSeries[5] = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[4];
		shapeSeries[6] = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[5];
		shapeSeries[7] = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[6];
		shapeSeries[8] = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[7];
		shapeSeries[9] = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[8];
		shapeSeries[10] = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[9];
		shapeSeries[11] = ShapeUtilities.createDiagonalCross(3f, 0.5f);
		shapeSeries[12] = ShapeUtilities.createRegularCross(3f, 0.5f);
		return shapeSeries;
	}
	
	private static Map<String, Image> imageStorage;
	
	public static Image getImage(String path) {
		synchronized(StaticValues.class){
			if (imageStorage == null) {
				imageStorage = new HashMap<String, Image>();
			}
		}
		if (imageStorage.containsKey(path)) {
			return imageStorage.get(path);
		}
		Image newImage = new Image(Display.getDefault(), StaticValues.class.getClassLoader().getResourceAsStream(path));
		imageStorage.put(path, newImage);
		return newImage;
	}
}
