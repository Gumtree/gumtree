package org.gumtree.gumnix.sics.ui.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.internal.ui.InternalImage;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.resource.UIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsInterruptWidget extends AbstractSicsComposite {

	private static final Logger logger = LoggerFactory
			.getLogger(SicsInterruptWidget.class);

	private Label interruptButton;

	private Image buttonImage;

	public SicsInterruptWidget(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void handleSicsConnect() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (interruptButton != null) {
					interruptButton.setEnabled(true);
				};
			}
		});
	}

	@Override
	protected void handleSicsDisconnect() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (interruptButton != null) {
					interruptButton.setEnabled(false);
				};
			}
		});
	}

	@Override
	protected void handleRender() {
		GridLayoutFactory.swtDefaults().applyTo(this);
		interruptButton = getWidgetFactory().createLabel(this, "");
		interruptButton.setEnabled(false);
		interruptButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		if (getButtonImage() != null) {
			interruptButton.setImage(getButtonImage());
		}
		interruptButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				try {
					getSicsManager().control().getSicsController().interrupt();
				} catch (SicsIOException e1) {
					logger.error("Failed to send interrupt.", e);
				}
			}
		});
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, true).applyTo(interruptButton);
	}

	@Override
	protected void disposeWidget() {
		interruptButton = null;
		buttonImage = null;
		super.disposeWidget();
	}

	@Override
	public void setBackgroundImage(Image image) {
		super.setBackgroundImage(image);
		if (interruptButton != null) {
			interruptButton.setBackgroundImage(image);
		}
	}

	/**************************************************************************
	 * Properties
	 **************************************************************************/

	public Image getButtonImage() {
		if (buttonImage == null) {
			buttonImage = InternalImage.STOP_128.getImage();
		}
		return buttonImage;
	}

	public void setButtonImage(Image buttonImage) {
		this.buttonImage = buttonImage;
	}

}
