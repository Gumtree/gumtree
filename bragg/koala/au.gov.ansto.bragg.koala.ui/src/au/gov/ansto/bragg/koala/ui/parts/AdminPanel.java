/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.ui.ControlTerminalView;
import org.gumtree.control.ui.viewer.ControlViewer;

import au.gov.ansto.bragg.nbi.ui.realtime.control.ControlRealtimeDataViewer;
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
		GridLayoutFactory.fillDefaults().margins(1, 1).applyTo(this);
		GridDataFactory.swtDefaults().minSize(1200, 800).align(SWT.FILL, SWT.FILL).applyTo(this);
		
		SashForm level1Form = new SashForm(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(level1Form);
		
		final Composite left = new Composite(level1Form, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(left);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(left);
		
//		final Composite right1 = new Composite(this, SWT.BORDER);
//		GridLayoutFactory.fillDefaults().applyTo(right1);
//		GridDataFactory.fillDefaults().grab(false, true).minSize(600, 400).applyTo(right1);
		SashForm level2Right = new SashForm(level1Form, SWT.VERTICAL);
		level1Form.setWeights(new int[]{7, 5});
		
		ControlRealtimeDataViewer plotViewer = new ControlRealtimeDataViewer(level2Right, SWT.BORDER);
		GridLayoutFactory.fillDefaults().applyTo(plotViewer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plotViewer);
		
//		final Composite right2 = new Composite(this, SWT.BORDER);
//		right2.setLayout(new FillLayout());
//		GridDataFactory.fillDefaults().grab(false, true).minSize(600, 400).applyTo(right2);

		ControlTerminalView terminal = new ControlTerminalView();
		terminal.createPartControl(level2Right);
		level2Right.setWeights(new int[] {5, 4});
		
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
