package org.gumtree.ui.terminal;

public interface ITerminalOutputBuffer {

	public enum OutputStyle {
		NORMAL, WARNING, ERROR
	}

	public void appendOutput(String text, OutputStyle style);

	public void appendInput(String text);

}
