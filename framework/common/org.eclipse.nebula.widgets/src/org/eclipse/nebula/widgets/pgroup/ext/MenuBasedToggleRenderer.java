/*******************************************************************************
 * Copyright (c) 2006 Chris Gross. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html Contributors: schtoo@schtoo.com
 * (Chris Gross) - initial API and implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.pgroup.ext;

import org.eclipse.nebula.widgets.pgroup.AbstractRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;

/**
 * This toggle strategy appears as a triangle pointing to the right when
 * collapsed and pointing downwards when expanded.
 * 
 * @author chris
 * @author Tony Lam (copied from TwisteToggleRender)
 */
public class MenuBasedToggleRenderer extends AbstractRenderer
{

    /**
     * 
     */
    public MenuBasedToggleRenderer()
    {
        super();
        setSize(new Point(10, 10));
    }

    public void paint(GC gc, Object value)
    {
        Transform transform = new Transform(gc.getDevice());
        transform.translate(getBounds().x, getBounds().y);
        // [Tony]
        transform.scale(1.3f, 1.3f);
        gc.setTransform(transform);

//        Color back = gc.getBackground();
//        Color fore = gc.getForeground();

        if (!isHover())
        {
            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        }
        else
        {
            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_LIST_SELECTION));
        }

        gc.setBackground(gc.getForeground());
        // [Tony]
//        if (isExpanded())
//        {
            gc.drawPolygon(new int[] {1, 3, 4, 6, 5, 6, 8, 3 });
            gc.fillPolygon(new int[] {1, 3, 4, 6, 5, 6, 8, 3 });
//        }
//        else
//        {
//            gc.drawPolygon(new int[] {3, 1, 6, 4, 6, 5, 3, 8 });
//            gc.fillPolygon(new int[] {3, 1, 6, 4, 6, 5, 3, 8 });
//        }

//        if (isFocus())
//        {
//            gc.setBackground(back);
//            gc.setForeground(fore);
//            gc.drawFocus(-1, -1, 12, 12);
//        }

        gc.setTransform(null);
        transform.dispose();
    }

    public Point computeSize(GC gc, int wHint, int hHint, Object value)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
