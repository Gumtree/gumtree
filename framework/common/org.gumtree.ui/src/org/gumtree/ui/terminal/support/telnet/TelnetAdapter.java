package org.gumtree.ui.terminal.support.telnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import org.eclipse.jface.action.IAction;
import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.terminal.ICommunicationAdapter;
import org.gumtree.ui.terminal.ICommunicationConfigPart;
import org.gumtree.ui.terminal.ITerminalOutputBuffer;
import org.gumtree.ui.terminal.ITerminalOutputBuffer.OutputStyle;
import org.gumtree.util.messaging.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelnetAdapter implements ICommunicationAdapter {

	public static String EVENT_TOPIC_TELNET = "org/gumtree/ui/terminal/telnet";
	
	public static String EVENT_TOPIC_TELNET_SENT = EVENT_TOPIC_TELNET + "/sent";
	
	public static String EVENT_TOPIC_TELNET_RECEIVED = EVENT_TOPIC_TELNET + "/received";
	
	public static String EVENT_PROP_MESSAGE = "sentMessage";
	
	public static String EVENT_PROP_ADAPTER = "adapter";
			
	private static Logger logger = LoggerFactory.getLogger(TelnetAdapter.class);
	
	private Socket socket;

	private BufferedReader inputStream;

	private PrintStream outputStream;

	private Thread listenerThread;

	private TelnetConfigPart part;

	private boolean isConnected;

	private ITerminalOutputBuffer outputBuffer;

	public TelnetAdapter() {
		isConnected = false;
		outputBuffer = null;
	}

	public IConnectionContext getConnectionContext() {
		if(part != null) {
			return part.getConnectionContext();
		}
		return null;
	}

	public ICommunicationConfigPart createConfigPart() {
		part = new TelnetConfigPart();
		return part;
	}

	public void connect(ITerminalOutputBuffer outputBuffer) throws CommunicationAdapterException {
		if(getConnectionContext() == null) {
			throw new CommunicationAdapterException("Missing configuration.");
		}
		if(isConnected()) {
			throw new CommunicationAdapterException("Attempt to reconnect on an existing connection");
		}
		this.outputBuffer = outputBuffer;
		int port = 0;
		String host = "";
		try {
			port = getConnectionContext().getPort();
			host = getConnectionContext().getHost();
			socket = new Socket(host, port);
			inputStream = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			outputStream = new PrintStream(socket.getOutputStream());
			isConnected = true;
			listenerThread = new Thread(new Runnable() {
				public void run() {
					try {
						String replyMessage;
						while ((replyMessage = inputStream.readLine()) != null && isConnected) {
							// little hack to sics telnet bug
							while (replyMessage.startsWith("ящ")) {
								replyMessage = replyMessage.substring(2);
							}
							logger.info("Server replied: " + replyMessage);
							// Post replied message to event bus
							new EventBuilder(EVENT_TOPIC_TELNET_RECEIVED)
									.append(EVENT_PROP_MESSAGE, replyMessage)
									.append(EVENT_PROP_ADAPTER,
											TelnetAdapter.this).post();
							getOutputBuffer().appendOutput(replyMessage, OutputStyle.NORMAL);
						}
					} catch (IOException e) {
					}
				}
			});
			listenerThread.start();
		} catch (Exception e) {
			throw new CommunicationAdapterException("Cannot connection to " + host + ":" + port, e);
		}
	}

	public void disconnect() {
		if(!isConnected) {
			return;
		}
		socket = null;
		inputStream = null;
		outputStream = null;
		part = null;
		listenerThread = null;
		isConnected = false;
	}

	public void send(String text) throws CommunicationAdapterException {
		if(getOutputBuffer() != null) {
			getOutputBuffer().appendInput(text);
		}
		if(outputStream != null) {
			logger.info("Client sent: " + text);
			// Post sent message to event bus
			new EventBuilder(EVENT_TOPIC_TELNET_SENT)
					.append(EVENT_PROP_MESSAGE, text)
					.append(EVENT_PROP_ADAPTER, this).post();
			outputStream.println(text);
			outputStream.flush();
		}
	}

	private ITerminalOutputBuffer getOutputBuffer() {
		return outputBuffer;
	}

	public IAction[] getToolActions() {
		return null;
	}

	public boolean isConnected() {
		return isConnected;
	}

}