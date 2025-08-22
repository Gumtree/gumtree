/**
 * 
 */
package org.gumtree.control.ui.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.ui.viewer.InternalImage;
import org.gumtree.widgets.swt.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NXI
 *
 */
public class InterruptWidget extends ExtendedWidgetComposite {

	private static final Logger logger = LoggerFactory
			.getLogger(InterruptWidget.class);

	private static final String GUMTREE_USE_LARGE_STOP_BUTTON = "gumtree.sics.useLargeStopButton";

	private Label interruptButton;

	private Image buttonImage;

	public InterruptWidget(Composite parent, int style) {
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
	protected void handleModelUpdate() {
	}
	
	@Override
	protected void handleStatusUpdate(ServerStatus status) {
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
					SicsManager.getSicsProxy().interrupt();
				} catch (Exception e1) {
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
			String useLargeStopButton = System.getProperty(GUMTREE_USE_LARGE_STOP_BUTTON, "true");
			if (Boolean.parseBoolean(useLargeStopButton)) {
				buttonImage = InternalImage.STOP_128.getImage();
			} else {
				buttonImage = InternalImage.STOP_64.getImage();
			}
		}
		return buttonImage;
	}

	public void setButtonImage(Image buttonImage) {
		this.buttonImage = buttonImage;
	}

}
