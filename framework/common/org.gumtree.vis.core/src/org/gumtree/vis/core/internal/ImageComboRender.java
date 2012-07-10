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

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.Shape;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.gumtree.vis.plot1d.MarkerShape;

/**
 * @author nxi
 *
 */
public class ImageComboRender extends JLabel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 663301478117534865L;
	
	private Shape[] shapes;
	private Paint paint = Color.DARK_GRAY;
	private boolean isShapeFilled = true;

    /**
	 * @param shapes the shapes to set
	 */
	public void setShapes(Shape[] shapes) {
		this.shapes = shapes;
	}

	public ImageComboRender() {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
    }

    /*
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     */
    public Component getListCellRendererComponent(
                                       JList list,
                                       Object value,
                                       int index,
                                       boolean isSelected,
                                       boolean cellHasFocus) {
        //Get the selected index. (The index param isn't
        //always valid, so just use the value.)
        int selectedIndex = ((Integer)value).intValue();

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        //Set the icon and text.  If icon was null, say so.
//        ImageIcon icon = images[selectedIndex];
        if (shapes[selectedIndex] != null) {
        	ImageIcon icon = MarkerShape.createIcon(shapes[selectedIndex], 
        			paint, isShapeFilled);
        	setIcon(icon);
        	setText(null);
        } else {
        	setIcon(null);
        	setText("None");
        }
        return this;
    }

    public void setPaint (Paint paint){
    	this.paint = paint;
    }

	public void setFilled(boolean isFilled) {
		this.isShapeFilled = isFilled;
	}
}
