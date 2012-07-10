/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Norman Xiong (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.vis.plot1d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Transparency;

import javax.swing.ImageIcon;

import org.gumtree.vis.core.internal.StaticValues;

public enum MarkerShape {

	NONE (StaticValues.LOCAL_SHAPE_SERIES[0]), 
	Square (StaticValues.LOCAL_SHAPE_SERIES[1]),
	Circle (StaticValues.LOCAL_SHAPE_SERIES[2]),
	UpTriangle (StaticValues.LOCAL_SHAPE_SERIES[3]),
	Diamond (StaticValues.LOCAL_SHAPE_SERIES[4]), 
	HorizontalRectangle (StaticValues.LOCAL_SHAPE_SERIES[5]),
	DownTriangle (StaticValues.LOCAL_SHAPE_SERIES[6]),
    HorizontalEllipse (StaticValues.LOCAL_SHAPE_SERIES[7]), 
    RightTriangle (StaticValues.LOCAL_SHAPE_SERIES[8]),
    VerticalRectangle (StaticValues.LOCAL_SHAPE_SERIES[9]),
    LeftTriangle (StaticValues.LOCAL_SHAPE_SERIES[10]),
	DiagonalCross (StaticValues.LOCAL_SHAPE_SERIES[11]),
	RegularCross (StaticValues.LOCAL_SHAPE_SERIES[12]);

	public final  static int size = 13;
	private final static String DEFAULT_MARKER_SHAPE = "vis.defaultMarkerShape";
	
	private Shape shape;	
		
	MarkerShape(Shape shape){
		this.shape = shape;
	}
	
	public Shape getShape(){
		return shape;
	}
	
	public static MarkerShape getDefaultMarkerShape(){
		try{
			String defaultMarker = System.getProperty(
					DEFAULT_MARKER_SHAPE);
			return MarkerShape.valueOf(defaultMarker);
		}catch (Exception e) {
		}
		return MarkerShape.NONE;
	}
	
	public static ImageIcon createIcon(Shape shape, Paint paint, boolean isFilled) {
		if (shape == null) {
			return null;
		}
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
    	GraphicsDevice gs = ge.getDefaultScreenDevice(); 
    	GraphicsConfiguration gc = gs.getDefaultConfiguration(); 
    	Image image = gc.createCompatibleVolatileImage(16, 16, Transparency.TRANSLUCENT);
    	Graphics2D g2 = (Graphics2D) image.getGraphics();
    	g2.setBackground(new Color(255, 255, 255, 10));
    	g2.translate(7, 7);
    	g2.setPaint(paint);
    	g2.setStroke(new BasicStroke(1));
    	g2.draw(shape);
    	if (isFilled) {
    		g2.fill(shape);
    	}
    	g2.dispose();
    	return new ImageIcon(image);
	}
	
	public static MarkerShape findMarkerShape(Shape shape) {
		for (int i = 0; i < StaticValues.LOCAL_SHAPE_SERIES.length; i++) {
			if (shape == StaticValues.LOCAL_SHAPE_SERIES[i]) {
				return getMarkerShape(i);
			}
		}
		return getMarkerShape(0);
	}
	
	public static MarkerShape getMarkerShape(int index) {
		switch (index) {
		case 0:
			return MarkerShape.NONE;
		case 1:
			return MarkerShape.Square;
		case 2:
			return MarkerShape.Circle;
		case 3:
			return MarkerShape.UpTriangle;
		case 4:
			return MarkerShape.Diamond;
		case 5:
			return MarkerShape.HorizontalRectangle;
		case 6:
			return MarkerShape.DownTriangle;
		case 7:
			return MarkerShape.HorizontalEllipse;
		case 8:
			return MarkerShape.RightTriangle;
		case 9:
			return MarkerShape.VerticalRectangle;
		case 10:
			return MarkerShape.LeftTriangle;
		case 11:
			return MarkerShape.DiagonalCross;
		case 12:
			return MarkerShape.RegularCross;
		default:
			return MarkerShape.NONE;
		}
	}
	
}
