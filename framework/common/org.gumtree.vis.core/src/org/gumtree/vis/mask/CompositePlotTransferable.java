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
package org.gumtree.vis.mask;

import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.gumtree.vis.awt.CompositePanel;

/**
 * @author nxi
 *
 */
public class CompositePlotTransferable implements Transferable {

	/**
	 * 
	 */
	final DataFlavor imageFlavor = new DataFlavor(
            "image/x-java-image; class=java.awt.Image", "Image");  
	private CompositePanel panel;
	
	public CompositePlotTransferable() {
	}

	public CompositePlotTransferable(CompositePanel panel){
		this.panel = panel;
	}
	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (panel != null) {
			BufferedImage image; 
	        if (this.imageFlavor.equals(flavor)) {
	        	image = panel.getImage();
	        }
	        else {
	            throw new UnsupportedFlavorException(flavor);
	        }
//	        Graphics2D g2 = image.createGraphics();
//	        g2.drawImage(image, 0, 0, null);
//	        drawMasksInDataArea(g2, dataArea != null ? dataArea : 
//	        	new Rectangle2D.Double(0, 0, width, height), masks, chart);
//	        g2.dispose();
	        return image;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {this.imageFlavor};
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		 return this.imageFlavor.equals(flavor);
	}

}
