package org.gumtree.gumnix.sics.internal.ui;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleContent {

	private MessageConsole console;

	private MessageConsoleStream stream;

	private int bufferCount;

	public ConsoleContent(MessageConsole console) {
		this.console = console;
	}

	public MessageConsole getConsole() {
		return console;
	}

	public MessageConsoleStream getStream() {
		if(stream == null) {
			stream = getConsole().newMessageStream();
		}
		return stream;
	}

	public void increaseBufferCount() {
		bufferCount++;
	}

	public void clearBufferCount() {
		bufferCount	= 0;
	}

	public int getBufferCount() {
		return bufferCount;
	}

}
