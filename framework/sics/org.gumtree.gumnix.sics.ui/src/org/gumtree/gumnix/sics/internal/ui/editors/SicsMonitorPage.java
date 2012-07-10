package org.gumtree.gumnix.sics.internal.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.gumtree.gumnix.sics.internal.ui.SicsServerAdapter;
import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.terminal.ITerminalOutputBuffer;
import org.gumtree.ui.terminal.ITerminalOutputBuffer.OutputStyle;
import org.gumtree.ui.terminal.support.TerminalOutputBuffer;
import org.gumtree.ui.terminal.support.TerminalText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsMonitorPage extends FormPage {

	public static final String ID = "monitor";

	private static final String TITLE = "SICS Monitor";

	private static Logger logger;

	private TerminalText text;

	private SicsServerAdapter sicsAdapter;

	public SicsMonitorPage(FormEditor editor) {
		super(editor, ID, TITLE);
	}

	protected void createFormContent(IManagedForm managedForm) {
		managedForm.getForm().setAlwaysShowScrollBars(false);
		Composite parent = managedForm.getForm().getBody();
		parent.setLayout(new FillLayout());
		text = new TerminalText(parent,  SWT.MULTI | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.READ_ONLY);
		sicsAdapter = new SicsServerAdapter();
		ITerminalOutputBuffer buffer = new TerminalOutputBuffer(text);
		try {
			sicsAdapter.connect(buffer);
			sicsAdapter.send("config listen 1");
		} catch (CommunicationAdapterException e) {
			buffer.appendOutput("Cannot connect to SICS", OutputStyle.ERROR);
			getLogger().error("Cannot connect to SICS", e);
		}
	}

	public void dispose() {
		if(sicsAdapter != null) {
			if(sicsAdapter.isConnected()) {
				sicsAdapter.disconnect();
			}
			sicsAdapter = null;
		}
		if(text != null) {
			text.dispose();
			text = null;
		}
		super.dispose();
	}

	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(SicsMonitorPage.class);
		}
		return logger;
	}
}
