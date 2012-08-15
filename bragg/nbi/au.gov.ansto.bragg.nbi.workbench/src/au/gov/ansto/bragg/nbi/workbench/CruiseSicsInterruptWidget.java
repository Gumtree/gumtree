package au.gov.ansto.bragg.nbi.workbench;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.ui.util.resource.SharedImage;
import org.gumtree.ui.util.resource.UIResources;
import org.gumtree.ui.widgets.ExtendedComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.workbench.internal.InternalImage;

@SuppressWarnings("restriction")
public class CruiseSicsInterruptWidget extends ExtendedComposite {

	private static final Logger logger = LoggerFactory
			.getLogger(CruiseSicsInterruptWidget.class);

	private ISicsManager sicsManager;

	@Inject
	public CruiseSicsInterruptWidget(Composite parent, @Optional int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		GridLayoutFactory.swtDefaults().applyTo(this);
		setBackgroundImage(SharedImage.CRUISE_BG.getImage());
		Label button = getWidgetFactory().createLabel(this, "");
		button.setBackgroundImage(SharedImage.CRUISE_BG.getImage());
		button.setImage(InternalImage.STOP_128.getImage());
		button.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		button.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (getSicsManager() != null) {
					try {
						getSicsManager().control().getSicsController()
								.interrupt();
					} catch (Exception ex) {
						logger.error("Failed to interrupt", ex);
					}
				}
			}
		});
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, true).applyTo(button);
	}

	@Override
	protected void disposeWidget() {
		sicsManager = null;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public ISicsManager getSicsManager() {
		return sicsManager;
	}

	@Inject
	public void setSicsManager(ISicsManager sicsManager) {
		this.sicsManager = sicsManager;
	}

}
