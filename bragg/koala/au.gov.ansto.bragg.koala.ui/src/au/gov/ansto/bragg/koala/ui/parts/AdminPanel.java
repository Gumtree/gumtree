/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.ui.ControlTerminalView;
import org.gumtree.control.ui.viewer.ControlViewer;
import org.gumtree.control.ui.viewer.model.INodeSet;
import org.gumtree.control.ui.viewer.model.NodeSet;
import org.gumtree.util.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.nbi.ui.realtime.control.ControlRealtimeDataViewer;

/**
 * @author nxi
 *
 */
public class AdminPanel extends AbstractPanel {

	private static final int WIDTH_HINT = 2200;
	private static final int HEIGHT_HINT = 1080;
	private static final int WIDTH_HINT_SMALL = 1560;
	private static final int HEIGHT_HINT_SMALL = 720;
	private static final Logger logger = LoggerFactory.getLogger(AdminPanel.class);

	private MainPart mainPart;
	private int panelWidth;
	private int panelHeight;
	private ControlViewer filterViewer;
	private ControlViewer controlViewer;

	/**
	 * @param parent
	 * @param style
	 */
	public AdminPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		if (Activator.getMonitorWidth() < 2500) {
			panelWidth = WIDTH_HINT_SMALL;
			panelHeight = HEIGHT_HINT_SMALL;
		} else {
			panelWidth = WIDTH_HINT;
			panelHeight = HEIGHT_HINT;			
		}
		GridLayoutFactory.fillDefaults().margins(1, 1).applyTo(this);
		GridDataFactory.swtDefaults().minSize(panelWidth, panelHeight).align(SWT.FILL, SWT.FILL).applyTo(this);
		
		Composite controlComposite = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(1, 1).applyTo(controlComposite);
		
		Button reloadButton = new Button(controlComposite, SWT.PUSH);
		reloadButton.setText("Reload SICS Model");
		reloadButton.setFont(Activator.getMiddleFont());
		reloadButton.setImage(KoalaImage.RELOAD32.getImage());
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).hint(320, SWT.DEFAULT).applyTo(reloadButton);

		reloadButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.warn("Reload SICS Model button pressed");
				Thread reloadThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							SicsManager.getSicsProxy().updateGumtreeXML();
						} catch (SicsException e) {
							mainPart.popupError("failed to update SICS model, " + e.getMessage());
						}
					}
				});
				reloadThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Button reconnectButton = new Button(controlComposite, SWT.PUSH);
		reconnectButton.setText("Reconnect to SICS");
		reconnectButton.setFont(Activator.getMiddleFont());
		reconnectButton.setImage(KoalaImage.CONNECT32.getImage());
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).hint(320, SWT.DEFAULT).applyTo(reconnectButton);

		reconnectButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.warn("Reconnect to SICS button pressed");
				Thread reconnectThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						SicsManager.getSicsProxy().disconnect();
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
						}
						try {
							SicsManager.getSicsProxy().reconnect();
						} catch (SicsException e) {
							mainPart.popupError("failed to reconnect to SICS, " + e.getMessage());
						}
					}
				});
				reconnectThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		SashForm level1Form = new SashForm(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(level1Form);
		
		SashForm level2Left = new SashForm(level1Form, SWT.VERTICAL);
		final Composite leftUp = new Composite(level2Left, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(leftUp);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(leftUp);

		final Composite leftDown = new Composite(level2Left, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(leftDown);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(leftDown);
		level2Left.setWeights(new int[] {3, 7});

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
		filterViewer = new ControlViewer();
		controlViewer = new ControlViewer();
		if (mainPart.getControl().isConnected()) {
			String filterPath = SicsCoreProperties.FILTER_PATH.getValue();
			if (!StringUtils.isEmpty(filterPath)) {
				try {
					IFileStore filterFolder = EFS.getStore(new URI(filterPath));
					IFileStore child = filterFolder.getChild(Activator.MAINTENANCE_FILTER);
					INodeSet nodeSet = NodeSet.read(child.openInputStream(EFS.NONE, new NullProgressMonitor()));
					filterViewer.createPartControl(leftUp, nodeSet);
					GridDataFactory.fillDefaults().grab(true, true).applyTo(
							filterViewer.getTreeViewer().getControl());
				}catch (Exception e) {
					e.printStackTrace();
				}
			}			
			controlViewer.createPartControl(leftDown, null);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(
					controlViewer.getTreeViewer().getControl());
		}
		
		mainPart.getControl().addProxyListener(new SicsProxyListenerAdapter() {
			
			@Override
			public void modelUpdated() {
				super.connect();
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if (!controlViewer.isDisposed()) {
							controlViewer.dispose();
						}
						controlViewer = new ControlViewer();
						if (!controlViewer.isInitialised()) {
							controlViewer.createPartControl(leftDown, null);
							GridDataFactory.fillDefaults().grab(true, true).applyTo(
									controlViewer.getTreeViewer().getControl());
							leftDown.update();
							leftDown.layout();
						}
						if (!filterViewer.isDisposed()) {
							filterViewer.dispose();
						}
						filterViewer = new ControlViewer();
						if (!filterViewer.isInitialised()) {
							String filterPath = SicsCoreProperties.FILTER_PATH.getValue();
							if (!StringUtils.isEmpty(filterPath)) {
								try {
									IFileStore filterFolder = EFS.getStore(new URI(filterPath));
									IFileStore child = filterFolder.getChild(Activator.MAINTENANCE_FILTER);
									INodeSet nodeSet = NodeSet.read(child.openInputStream(EFS.NONE, new NullProgressMonitor()));
									filterViewer.createPartControl(leftUp, nodeSet);
									GridDataFactory.fillDefaults().grab(true, true).applyTo(
											filterViewer.getTreeViewer().getControl());
								}catch (Exception e) {
									e.printStackTrace();
								}
							}			
							leftUp.update();
							leftUp.layout();
						}
					}
				});
			}
			
			@Override
			public void disconnect() {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if (controlViewer != null && !controlViewer.isDisposed()) {
							controlViewer.dispose();
						}
						if (filterViewer != null && !filterViewer.isDisposed()) {
							filterViewer.dispose();
						}
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
		mainPart.showPanel(this, panelWidth, panelHeight);
		mainPart.enableBackButton();
		mainPart.disableNextButton();
		mainPart.setTitle("Administration Page");
		mainPart.setCurrentPanelName(PanelName.ADMIN);
	}


}
