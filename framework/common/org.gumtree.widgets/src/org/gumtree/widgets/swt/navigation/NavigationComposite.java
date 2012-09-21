package org.gumtree.widgets.swt.navigation;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.gumtree.widgets.swt.IControlFactory;
import org.gumtree.widgets.swt.util.SharedImages;
import org.gumtree.widgets.swt.util.UIResources;

public class NavigationComposite extends ExtendedComposite {

	private UIContext context;

	public NavigationComposite(Composite parent, int style) {
		super(parent, style);
		setBackgroundMode(SWT.INHERIT_FORCE);
	}

	// @PostConstruct
	// public void render() {
	// context = new UIContext();
	// GridLayoutFactory.swtDefaults().margins(0, 0).spacing(5, 0)
	// .numColumns(4).applyTo(this);
	//
	// Label label = getWidgetFactory().createLabel(this, "");
	// // label.setImage(InternalImage.WINDOW_16.getImage());
	// // GridDataFactory.swtDefaults().align(SWT.BEGINNING,
	// SWT.BEGINNING).applyTo(label);
	// // label = getWidgetFactory().createLabel(this, "");
	// // label.setImage(InternalImage.HOME_16.getImage());
	// // label = getWidgetFactory().createLabel(this, "");
	// label.setImage(InternalImage.BACK_16.getImage());
	// GridDataFactory.swtDefaults().align(SWT.BEGINNING,
	// SWT.BEGINNING).applyTo(label);
	//
	// // Link
	// context.link = new Link(this, SWT.NONE);
	// GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
	// .grab(true, false).applyTo(context.link);
	// context.link
	// .setText("<a>Home</a> > <a>A</a> > <a>B</a> > <a>C</a> > This is long");
	//
	// // Content
	// context.content = getWidgetFactory().createComposite(this);
	// GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
	// .grab(true, true).span(4, 1).applyTo(context.content);
	//
	// // Stack Layout
	// context.stackLayout = new StackLayout();
	// context.content.setLayout(context.stackLayout);
	//
	// }

	@PostConstruct
	public void render() {
		context = new UIContext();
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0)
				.applyTo(this);
		createLinkArea(this);
		createContentArea(this);
	}

	private void createLinkArea(Composite parent) {
		context.linkArea = getWidgetFactory().createComposite(parent);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(context.linkArea);
		RowLayoutFactory.swtDefaults().wrap(true).applyTo(context.linkArea);

		Label label = getWidgetFactory().createLabel(context.linkArea,
				"Experiment");
		label.setForeground(getForeground());

		label = getWidgetFactory().createLabel(context.linkArea, " > ");
		label.setForeground(getForeground());

		label = getWidgetFactory().createLabel(context.linkArea,
				"Multi Sample Workflow");
		label.setForeground(getForeground());
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));

		label = getWidgetFactory().createLabel(context.linkArea, " > ");
		label.setForeground(getForeground());

		label = getWidgetFactory().createLabel(context.linkArea, "User Info");
		label.setForeground(getForeground());
	}

	private void createContentArea(Composite parent) {
		context.content = getWidgetFactory().createComposite(parent);
		context.stackLayout = new StackLayout();
		context.content.setLayout(context.stackLayout);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(context.content);
	}

	@Override
	protected void disposeWidget() {
		context = null;
	}

	/*************************************************************************
	 * API
	 *************************************************************************/

	public void addPage(String id, String label, IControlFactory factory) {
		if (context == null) {
			return;
		}
		context.stackLayout.topControl = factory.createControl(context.content);
		layout(true, true);
	}

	// public void setPage(int index) {
	//
	// }
	//
	// public void setPage(String path) {
	//
	// }

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	private class UIContext {
		private Composite linkArea;
		private Composite content;
		private StackLayout stackLayout;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		NavigationComposite composite = new NavigationComposite(shell, SWT.NONE);
		composite.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		composite.setBackgroundImage(SharedImages.BG_TEXTURED.getImage());
		composite.render();

		shell.setSize(300, 800);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
