package org.gumtree.app.workbench.apps;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.app.workbench.internal.InternalImage;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.gumtree.widgets.swt.tile.TileColor;
import org.gumtree.widgets.swt.tile.TileDataFactory;
import org.gumtree.widgets.swt.tile.TileLayoutFactory;
import org.gumtree.widgets.swt.util.UIResources;

@SuppressWarnings("restriction")
public class IconTile extends ExtendedComposite {

	private Image icon;

	private String text;

	@Inject
	public IconTile(Composite parent, @Optional int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		disposeChildren();
		GridLayoutFactory.swtDefaults().applyTo(this);
		Label iconLabel = getWidgetFactory().createLabel(this, getIcon());
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.END)
				.grab(true, true).applyTo(iconLabel);
		Label textLabel = getWidgetFactory().createLabel(this, getText());
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.END)
				.grab(true, true).applyTo(textLabel);
	}

	@Override
	protected void disposeWidget() {
	}

	/*************************************************************************
	 * Properties
	 *************************************************************************/

	public Image getIcon() {
		return icon;
	}

	public void setIcon(Image icon) {
		this.icon = icon;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		TileLayoutFactory.create(8).applyTo(shell);
		shell.setBackground(UIResources.getSystemColor(SWT.COLOR_BLACK));
		shell.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));

		IconTile app = new IconTile(shell, SWT.NONE);
		app.setBackground(TileColor.CYAN.getColor());
		app.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		app.setIcon(InternalImage.APPLICATION.getImage());
		app.setText("Text Here");
		TileDataFactory.create().size(2, 2).applyTo(app);
		app.render();

		app = new IconTile(shell, SWT.NONE);
		app.setBackground(TileColor.CYAN.getColor());
		app.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		app.setIcon(InternalImage.APPLICATION.getImage());
		app.setText("Text Here");
		TileDataFactory.create().size(4, 4).applyTo(app);
		app.render();

		app = new IconTile(shell, SWT.NONE);
		app.setBackground(TileColor.CYAN.getColor());
		app.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		app.setIcon(InternalImage.APPLICATION.getImage());
		app.setText("Text Here");
		TileDataFactory.create().size(8, 4).applyTo(app);
		app.render();

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
