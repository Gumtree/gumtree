package org.gumtree.gumnix.sics.internal.ui.controlview;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.gumtree.gumnix.sics.ui.util.ControlViewerConstants.Column;

/**
 * Reference: http://wiki.eclipse.org/index.php/FAQ_How_to_decorate_a_TableViewer_or_TreeViewer_with_Columns%3F
 * 
 * @since 1.0
 */
public class TreeDecoratingLabelProvider extends DecoratingLabelProvider implements ITableLabelProvider, ITableColorProvider {

	private ITableLabelProvider labelProvider;
	
	private ITableColorProvider colorProvider;
	
	private ILabelDecorator decorator;
	
	public TreeDecoratingLabelProvider (ILabelProvider provider, ILabelDecorator decorator) {
		super(provider, decorator);
		if (provider instanceof ITableLabelProvider) {
			labelProvider = (ITableLabelProvider) provider;	
		}
		if (provider instanceof ITableColorProvider) {
			colorProvider = (ITableColorProvider) provider;
		}
        this.decorator = decorator;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if (labelProvider == null) {
			return null;
		}
		Image image = labelProvider.getColumnImage(element, columnIndex);
        if (decorator != null && columnIndex == Column.NODE.getIndex()) {
            Image decorated = decorator.decorateImage(image, element);
            if (decorated != null) {
                return decorated;
            }
        }
        return image;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (labelProvider == null) {
			return null;
		}
		String text = labelProvider.getColumnText(element, columnIndex);
        if (decorator != null && columnIndex == Column.NODE.getIndex()) {
            String decorated = decorator.decorateText(text, element);
            if (decorated != null) {
                return decorated;
            }
        }
        return text;
	}

	public Color getBackground(Object element, int columnIndex) {
		if (colorProvider == null) {
			return null;
		}
		return colorProvider.getBackground(element, columnIndex);
	}

	public Color getForeground(Object element, int columnIndex) {
		if (colorProvider == null) {
			return null;
		}
		return colorProvider.getForeground(element, columnIndex);
	}

}
	
