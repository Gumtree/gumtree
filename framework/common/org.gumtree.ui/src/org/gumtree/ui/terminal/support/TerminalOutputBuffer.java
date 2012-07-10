package org.gumtree.ui.terminal.support;

import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.terminal.ITerminalOutputBuffer;

public class TerminalOutputBuffer implements ITerminalOutputBuffer {

	private TerminalText textDisplay;

	public TerminalOutputBuffer(TerminalText textDisplay) {
		this.textDisplay = textDisplay;
	}

	public void appendOutput(final String text, final OutputStyle style) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if(textDisplay != null && !textDisplay.isDisposed()) {
					textDisplay.appendOutputText(text, style);
				}
			}
		});
	}

	public void appendInput(final String text) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if(textDisplay != null && !textDisplay.isDisposed()) {
					textDisplay.appendInputText(text);
				}
			}
		});
	}

}
