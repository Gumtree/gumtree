/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import au.gov.ansto.bragg.koala.ui.parts.KoalaConstants.KoalaMode;
import au.gov.ansto.bragg.koala.ui.scan.ChemistryModel;
import au.gov.ansto.bragg.koala.ui.scan.AbstractScanModel;
import au.gov.ansto.bragg.koala.ui.scan.PhysicsModel;

/**
 * @author nxi
 *
 */
public class MainPart extends Composite {

	/**
	 * @param parent
	 * @param style
	 */
	private ScrolledComposite holder;
	private EnvironmentPanel environmentPanel;
	private JoeyPanel joeyPanel;
	private ProposalPanel proposalPanel;
	private CrystalPanel crystalPanel;
	private InitScanPanel initScanPanel;
	private ChemistryPanel chemExpPanel;
	private PhysicsPanel physicsPanel;
	
	private AbstractControlPanel currentMainPanel;
	private AbstractPanel currentPanel;
//	private AbstractControlPanel nextPanel;
	private ChemistryModel chemModel;
	private PhysicsModel physModel;
	private KoalaMode instrumentMode = KoalaMode.CHEMISTRY;

	
	public MainPart(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		chemModel = new ChemistryModel();
		physModel = new PhysicsModel();
		
		createPanels();
		
	}

	private void createPanels() {
		holder = new ScrolledComposite(this, SWT.NONE);
//		holder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayoutFactory.fillDefaults().applyTo(holder);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(holder);
//		cmpMain.setBackground(getBackground());
		holder.setExpandHorizontal(true);
		holder.setExpandVertical(true);
		
		environmentPanel = new EnvironmentPanel(holder, SWT.BORDER, this);
		joeyPanel = new JoeyPanel(holder, SWT.BORDER, this);
		proposalPanel = new ProposalPanel(holder, SWT.BORDER, this);
		crystalPanel = new CrystalPanel(holder, SWT.BORDER, this);
		initScanPanel = new InitScanPanel(holder, SWT.BORDER, this);
		chemExpPanel = new ChemistryPanel(holder, SWT.BORDER, this);
		physicsPanel = new PhysicsPanel(holder, SWT.BORDER, this);
	}
	

	public void showEnvironmentPanel() {
		environmentPanel.show();
	}

	public void showJoeyPanel() {
		joeyPanel.show();
	}

	public void showProposalPanel() {
		proposalPanel.show();
	}
	
	public void showCrystalPanel() {
		crystalPanel.show();
	}
	
	public void showInitScanPanel() {
		initScanPanel.show();
	}
	
	public void showChemistryPanel() {
		chemExpPanel.show();
	}

	public void showPhysicsPanel() {
		physicsPanel.show();
	}

	public void showPanel(AbstractPanel panel, int xHint, int yHint) {
		holder.setContent(panel);
		panel.layout();
		holder.setMinSize(panel.computeSize(xHint, yHint));
		holder.getParent().layout();
		currentPanel = panel;

		if (panel instanceof AbstractControlPanel) {
			currentMainPanel = (AbstractControlPanel) panel;
		}
	}
	
	public void showCurrentMainPanel() {
		if (currentMainPanel != null) {
			currentMainPanel.show();
		}
	}
	
	public void showBackPanel() {
		if (currentPanel != null) {
			currentPanel.back();
		}
	}

	public void showNextPanel() {
		if (currentPanel != null) {
			currentPanel.next();;
		}
	}

	public KoalaMainViewer getParentViewer() {
		return (KoalaMainViewer) getParent();
	}
	
	public void enableBackButton() {
		getParentViewer().getFooterPart().setBackButtonEnabled(true);
	}

	public void disableBackButton() {
		getParentViewer().getFooterPart().setBackButtonEnabled(false);
	}

	public void enableNextButton() {
		getParentViewer().getFooterPart().setNextButtonEnabled(true);
	}

	public void disableNextButton() {
		getParentViewer().getFooterPart().setNextButtonEnabled(false);
	}

	public EnvironmentPanel getEnvironmentPanel() {
		return environmentPanel;
	}
	
	public JoeyPanel getJoeyPanel() {
		return joeyPanel;
	}
	
	public ProposalPanel getProposalPanel() {
		return proposalPanel;
	}
	
	public CrystalPanel getCrystalPanel() {
		return crystalPanel;
	}
	
	public PhysicsPanel getPhysicsPanel() {
		return physicsPanel;
	}
	
	public void setMode(KoalaMode mode) {
		instrumentMode = mode;
		switch (mode) {
		case CHEMISTRY:
			getParentViewer().getFooterPart().enableChemistryButton();
			getParentViewer().getHeaderPart().disablePhysicsButton();
			break;
		case PHYSICS:
			getParentViewer().getHeaderPart().enablePhysicsButton();
			getParentViewer().getFooterPart().disableChemistryButton();
			break;
		default:
			getParentViewer().getFooterPart().disableChemistryButton();
			getParentViewer().getHeaderPart().disablePhysicsButton();
			break;
		}
	}
	
	public ChemistryModel getChemistryModel() {
		return chemModel;
	}

	public PhysicsModel getPhysicsModel() {
		return physModel;
	}

	public KoalaMode getInstrumentMode() {
		return instrumentMode;
	}
	
	public void setTitle(String title) {
		getParentViewer().getHeaderPart().setTitle(title);
	}
}
