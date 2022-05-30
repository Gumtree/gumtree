/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.ui.viewer.ControlViewer;

import au.gov.ansto.bragg.nbi.ui.widgets.SicsRealtimeDataViewer;

/**
 * @author nxi
 *
 */
public class AdminPanel extends AbstractPanel {

	private static final int WIDTH_HINT = 1560;
	private static final int HEIGHT_HINT = 720;
	private MainPart mainPart;
	private ControlViewer controlViewer;

	/**
	 * @param parent
	 * @param style
	 */
	public AdminPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		GridLayoutFactory.fillDefaults().numColumns(2).margins(2, 2).applyTo(this);
		GridDataFactory.swtDefaults().minSize(480, 480).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
		final Composite left = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(left);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 2).applyTo(left);
		
		SicsRealtimeDataViewer plotViewer = new SicsRealtimeDataViewer(this, SWT.NONE);

		
		
		controlViewer = new ControlViewer();
		if (mainPart.getControl().isConnected()) {
			controlViewer.createPartControl(left, null);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(
					controlViewer.getTreeViewer().getControl());
		}
		mainPart.getControl().addProxyListener(new SicsProxyListenerAdapter() {
			@Override
			public void connect() {
				// TODO Auto-generated method stub
				super.connect();
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						controlViewer.createPartControl(left, null);
						GridDataFactory.fillDefaults().grab(true, true).applyTo(
								controlViewer.getTreeViewer().getControl());
						left.update();
						left.layout();
					}
				});
			}
		});
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#next()
	 */
	@Override
	public void next() {
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#back()
	 */
	@Override
	public void back() {
		mainPart.showCurrentMainPanel();
	}

	@Override
	public void show() {
		mainPart.showPanel(this, WIDTH_HINT, HEIGHT_HINT);
		mainPart.enableBackButton();
		mainPart.disableNextButton();
		mainPart.setTitle("Administration Page");
	}


}
