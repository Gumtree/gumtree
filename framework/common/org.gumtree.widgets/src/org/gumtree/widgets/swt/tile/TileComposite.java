package org.gumtree.widgets.swt.tile;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.gumtree.widgets.swt.util.UIResources;

public class TileComposite extends ExtendedComposite {

	private UIResourceManager resourceManager;

	private Font fontLarge;

	public TileComposite(Composite parent, int style) {
		super(parent, style);
		setBackgroundMode(SWT.INHERIT_FORCE);
		resourceManager = new UIResourceManager(this);
		fontLarge = resourceManager.createRelativeFont(14, SWT.NONE);
	}

	@PostConstruct
	public void render() {
		if (getLayoutData() == null) {
			return;
		}
		for (Control child : getChildren()) {
			child.dispose();
		}
		int width = ((GridData) getLayoutData()).horizontalSpan;
		int height = ((GridData) getLayoutData()).verticalSpan;

		if (width == 4 && height == 4) {
			GridLayoutFactory.swtDefaults().applyTo(this);
			Label label = getWidgetFactory().createLabel(this, "Medium");
			label.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
			label.setFont(fontLarge);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
					.grab(true, true).applyTo(label);
		} else if (width == 8 && height == 4) {
			GridLayoutFactory.swtDefaults().applyTo(this);
			Label label = getWidgetFactory().createLabel(this, "Wide");
			label.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
			label.setFont(fontLarge);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
					.grab(true, true).applyTo(label);
		} else if (width == 2 && height == 2) {
			GridLayoutFactory.swtDefaults().margins(1, 1).spacing(1, 1).applyTo(this);
			Label iconLabel = getWidgetFactory().createLabel(this, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
					.grab(true, true).applyTo(iconLabel);
			Label textLabel = getWidgetFactory().createLabel(this, "Bean");
			textLabel.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		}
	}

	@Override
	protected void disposeWidget() {
	}

}
