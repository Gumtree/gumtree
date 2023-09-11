/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.mjpeg.MjpegViewer;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

/**
 * @author nxi
 *
 */
public class CrystalPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 2200;
	private static final int HEIGHT_HINT = 1080;
	private static final int WIDTH_HINT_SMALL = 1560;
	private static final int HEIGHT_HINT_SMALL = 720;

	private static Logger logger = LoggerFactory.getLogger(CrystalPanel.class);
	private MainPart mainPart;
	private int panelWidth;
	private int panelHeight;
	private MjpegViewer mjpegViewer;
	private ControlHelper controlHelper;
	
	/**
	 * @param parent
	 * @param style
	 */
	public CrystalPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		
		if (Activator.getMonitorWidth() < 2500) {
			panelWidth = WIDTH_HINT_SMALL;
			panelHeight = HEIGHT_HINT_SMALL;
		} else {
			panelWidth = WIDTH_HINT;
			panelHeight = HEIGHT_HINT;			
		}

		controlHelper = ControlHelper.getInstance();
		GridLayoutFactory.fillDefaults().margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(panelWidth, panelHeight).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
		mjpegViewer = new MjpegViewer(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mjpegViewer);
		
	}

	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#next()
	 */
	@Override
	public void next() {
		mainPart.showInitScanPanel();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#back()
	 */
	@Override
	public void back() {
		mainPart.showProposalPanel();
	}

	public void pauseVideo() {
		mjpegViewer.setRunnerPaused(true);
	}
	
	@Override
	public void show() {
		mainPart.showPanel(this, panelWidth, panelHeight);
		mainPart.enableBackButton();
		mainPart.enableNextButton();
		mainPart.setTitle("Crystal Mounting");
		mjpegViewer.setRunnerPaused(false);
		mainPart.setCurrentPanelName(PanelName.CRYSTAL);
	}

}
