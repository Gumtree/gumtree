package org.gumtree.gumnix.sics.internal.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.terminal.ICommunicationConfigPart;
import org.gumtree.ui.terminal.ITerminalOutputBuffer;
import org.gumtree.ui.terminal.support.telnet.TelnetAdapter;

public class SicsServerAdapter extends TelnetAdapter {

	private SicsConnectionContext connectionContext;

	public ICommunicationConfigPart createConfigPart() {
		return new ICommunicationConfigPart() {
			public void createControlPart(Composite parent) {
				parent.setLayout(new FillLayout());
				Label label = new Label(parent, SWT.NONE);
				label.setText("This communication adapter cannot be configured.");
			}
		};
	}

	public SicsConnectionContext getConnectionContext() {
		if(connectionContext == null) {
			connectionContext = new SicsConnectionContext("", 21);
//			IInstrumentProfile desc = ISicsManager.INSTANCE.service()
//					.getCurrentInstrumentProfile();
//			if (desc != null) {
//				String defaultHost = desc.getProperty(ConfigProperty.DEFAULT_HOST);
//				if(defaultHost != null) {
//					connectionContext.setHost(defaultHost);
//				}
//				String defaultTelnetPort = desc.getProperty(ConfigProperty.DEFAULT_PORT);
//				if(defaultTelnetPort != null) {
//					connectionContext.setPort(Integer.parseInt(defaultTelnetPort));
//				}
//			}
			ISicsConnectionContext currentContext = SicsCore.getDefaultProxy().getConnectionContext();
			if (currentContext != null) {
				connectionContext.setHost(currentContext.getHost());
				connectionContext.setPort(currentContext.getPort());
				connectionContext.setLogin(currentContext.getRole()
						.getLoginId());
				connectionContext.setPassword(currentContext.getPassword());
			}
		}
		return connectionContext;
	}

	public void connect(final ITerminalOutputBuffer outputBuffer) throws CommunicationAdapterException {
		super.connect(outputBuffer);
		send(getConnectionContext().getLogin() + " " + getConnectionContext().getPassword());
	}

	public IAction[] getToolActions() {
		return new IAction[] { new BatchUploadAction(this) };
	}

	@Override
	public void disconnect() {
		try {
			send("logoff");
		} catch (CommunicationAdapterException e) {
			e.printStackTrace();
		}
		super.disconnect();
	}
}
