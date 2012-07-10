package org.gumtree.gumnix.sics.internal.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.nebula.widgets.pgroup.SimpleGroupStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.gumnix.sics.ui.widgets.SicsInterruptGadget;
import org.gumtree.gumnix.sics.ui.widgets.SicsStatusGadget;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;
import org.gumtree.ui.util.resource.UIResourceManager;

public class SicsStatusPageWidget extends AbstractCruisePageWidget {

	public SicsStatusPageWidget(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().applyTo(this);

		Composite holder = getWidgetFactory().createComposite(this);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL)
				.grab(true, true).hint(180, SWT.DEFAULT).applyTo(holder);
		GridLayoutFactory.swtDefaults().applyTo(holder);
		render(holder);
	}

	public void render(Composite parent) {
		UIResourceManager resourceManager = new UIResourceManager(
				Activator.PLUGIN_ID, this);
		Font titleFont = resourceManager.createRelativeFont(1, SWT.BOLD);

		// Status
		PGroup statusGroup = new PGroup(parent, SWT.NONE);
		statusGroup.setStrategy(new SimpleGroupStrategy());
		statusGroup.setToggleRenderer(null);
		statusGroup.setText("Server Status");
		statusGroup.setFont(titleFont);
		statusGroup.setLayout(new FillLayout());
		statusGroup.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		statusGroup.setLinePosition(SWT.CENTER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(statusGroup);

		SicsStatusGadget statusGadget = new SicsStatusGadget(statusGroup,
				SWT.NONE);
//		statusGadget.afterParametersSet();

		Label separator = getWidgetFactory().createLabel(parent, "",
				SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setVisible(false);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(separator);

		// Connection
		PGroup connectionGroup = new PGroup(parent, SWT.NONE);
		connectionGroup.setStrategy(new SimpleGroupStrategy());
		connectionGroup.setToggleRenderer(null);
		connectionGroup.setText("Server Connection");
		connectionGroup.setFont(titleFont);
		connectionGroup.setForeground(getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		connectionGroup.setLinePosition(SWT.CENTER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(connectionGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(connectionGroup);

		ISicsConnectionContext connectionContext = SicsCore.getDefaultProxy()
				.getConnectionContext();
		Label label = getWidgetFactory().createLabel(connectionGroup, "Host: ");
		label.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		label = getWidgetFactory().createLabel(connectionGroup, "--");
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		if (connectionContext != null) {
			label.setText(connectionContext.getHost());
		}
		label = getWidgetFactory().createLabel(connectionGroup, "Port: ");
		label = getWidgetFactory().createLabel(connectionGroup, "--");
		label.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		if (connectionContext != null) {
			label.setText(connectionContext.getPort() + "");
		}

		separator = getWidgetFactory().createLabel(parent, "",
				SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setVisible(false);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(separator);

		// Interrupt
		PGroup interruptGroup = new PGroup(parent, SWT.NONE);
		interruptGroup.setStrategy(new SimpleGroupStrategy());
		interruptGroup.setToggleRenderer(null);
		interruptGroup.setText("Interrupt");
		interruptGroup.setFont(titleFont);
		interruptGroup.setForeground(getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		interruptGroup.setLinePosition(SWT.CENTER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(interruptGroup);
		GridLayoutFactory.swtDefaults().applyTo(interruptGroup);

		SicsInterruptGadget interruptGadget = new SicsInterruptGadget(
				interruptGroup, SWT.NONE);
		interruptGadget.afterParametersSet();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(interruptGadget);
	}

	@Override
	protected void disposeWidget() {
	}

}
