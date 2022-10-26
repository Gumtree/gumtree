package org.gumtree.control.ui.login;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.util.Geometry;
import org.eclipse.nebula.animation.AnimationRunner;
import org.eclipse.nebula.animation.effects.ShakeEffect;
import org.eclipse.nebula.animation.movement.SinusVariation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.control.core.ISicsConnectionContext;
import org.gumtree.control.model.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseLoginDialog extends TitleAreaDialog {

//	protected static final String OPTION_SICS_HOST = "sicsHost";

//	protected static final String OPTION_SICS_PORT = "sicsPort";

//	protected static final String OPTION_SICS_ROLE = "sicsRole";

//	protected static final String OPTION_SICS_PASSWORD = "sicsPassword";

//	protected static final String OPTION_SICS_INSTRUMENT = "sicsInstr";

	private static Logger logger;

	private String errorMessage;

	private Button noLoginButton;

//	private IInstrumentProfile profile;

	private ILoginHandler handler;

	private ISicsConnectionContext connectionContext;

	public BaseLoginDialog(Shell parentShell, ILoginHandler handler) {
		super(parentShell);
		this.handler = handler;
//		setShellStyle(SWT.SHELL_TRIM);
	}

//	protected DataBindingContext getBindingContext() {
//		if(bindingContext == null) {
//			bindingContext = BasicBindingFactory.createContext(getShell());
//		}
//		return bindingContext;
//	}

	protected Point getInitialLocation(Point initialSize) {
		Shell topLevelShell = getTopLevelShell();
		Point centerPoint = Geometry.centerPoint(topLevelShell.getClientArea());
		return new Point(centerPoint.x - (initialSize.x / 2), centerPoint.y - (initialSize.y / 2));
	}
	
	private Shell getTopLevelShell() {
		Shell shell = getShell();
		while(shell.getParent() != null && shell.getParent() instanceof Shell) {
			shell = (Shell) shell.getParent();
		}
		return shell;
	}

	protected ISicsConnectionContext getConnectionContext() {
		if (connectionContext == null) {
			
			connectionContext = ModelUtils.createConnectionContext();
			
//			connectionContext = new SicsConnectionContext();
//			if (getInstrumentProfile() != null) {
//				String defaultHost = getInstrumentProfile().getProperty(ConfigProperty.DEFAULT_HOST);
//				String defaultPort = getInstrumentProfile().getProperty(ConfigProperty.DEFAULT_PORT);
//				if(defaultHost != null) {
//					connectionContext.setHost(defaultHost);
//				}
//				if(defaultPort != null) {
//					connectionContext.setPort(Integer.parseInt(defaultPort));
//				}
//			}
//			
//			// TODO: Refactor this
//			
//			// Order of precedence for settings
//			// Command line option (high)
//			// Runtime properties
//			// Instrument profile extension (low) <- to be removed
//			
//			String runtimeDefinedHost = RuntimeEnv.resolveSystemProperty("sics.host");
//			if (runtimeDefinedHost != null) {
//				connectionContext.setHost(runtimeDefinedHost);
//			}
//			String runtimeDefinedPort = RuntimeEnv.resolveSystemProperty("sics.port");
//			if (runtimeDefinedPort != null) {
//				try {
//					connectionContext.setPort(Integer.parseInt(runtimeDefinedPort));
//				} catch (Exception e) {
//				}
//			}
//			
		}
		return connectionContext;
	}

//	protected void handleCommandLineOptions() {
//		String host = GTPlatform.getCommandLineOptions().getOptionValue(OPTION_SICS_HOST);
//		if(host != null) {
//			getConnectionContext().setHost(host);
//		}
//		String port = GTPlatform.getCommandLineOptions().getOptionValue(OPTION_SICS_PORT);
//		if(port != null) {
//			try {
//				getConnectionContext().setPort(Integer.parseInt(port));
//			} catch (NumberFormatException e) {
//				e.printStackTrace();
//			}
//		}
//		String role = GTPlatform.getCommandLineOptions().getOptionValue(OPTION_SICS_ROLE);
//		if(role != null) {
//			SicsRole sicsRole = SicsRole.getRole(role);
//			if(sicsRole != null) {
//				getConnectionContext().setRole(sicsRole);
//				getLogger().debug("Set role: " + sicsRole);
//			}
//		}
//		String password = GTPlatform.getCommandLineOptions().getOptionValue(OPTION_SICS_PASSWORD);
//		if(password != null) {
//			getConnectionContext().setPassword(password);
//		}
//	}

//	protected IInstrumentProfile getInstrumentProfile() {
//		return profile;
//	}

//	protected void setInstrumentProfile(IInstrumentProfile profile) {
//		this.profile = profile;
//	}

	protected String getInitialErrorMessage() {
		return errorMessage;
	}

	protected void setInitialErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		((GridLayout)parent.getLayout()).numColumns = 1;
		noLoginButton = new Button(parent, SWT.CHECK);
		noLoginButton.setText("Do not ask to connect again");
		noLoginButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handler.setNoMoreLogin(noLoginButton.getSelection());
			}
		});
		createButton(parent, IDialogConstants.OK_ID, "Connect", true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	protected void okPressed() {
		try {
			new LoginProgressMonitorDialog(getParentShell()).runDialog(getConnectionContext());
		} catch (Exception e) {
			handleError(e);
			return;
		}
		setReturnCode(OK);
		close();
	}
	
	@SuppressWarnings("deprecation")
	private void handleError(Exception e) {
		setErrorMessage(e.getMessage());
		ShakeEffect.shake(new AnimationRunner(), getShell(), 500, new SinusVariation(10, 1), null, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	@SuppressWarnings("deprecation")
	public int open() {
		if (getShell() == null || getShell().isDisposed()) {
			create();
		}

//		// limit the shell size to the display size
//		constrainShellSize();
//
//		// open the window
////		getShell().open();

//		new SATRunner().grow(getShell(), 1000, new SinusDecreaseVariation(1, 30), null, null);
		ShakeEffect.shake(new AnimationRunner(), getShell(), 350, new SinusVariation(5, 2), null, null);
		return super.open();
	}
	
	protected static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(BaseLoginDialog.class);
		}
		return logger;
	}

}
