package org.gumtree.control.ui.internal;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.CommandNotMappedException;
import org.gumtree.control.core.ISicsConnectionContext;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.imp.SicsReplyData;
import org.gumtree.control.imp.client.ClientChannel;
import org.gumtree.control.imp.client.IClientListener;
import org.gumtree.control.model.PropertyConstants;
import org.gumtree.control.model.PropertyConstants.FlagType;
import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.terminal.ICommunicationAdapter;
import org.gumtree.ui.terminal.ICommunicationConfigPart;
import org.gumtree.ui.terminal.ITerminalOutputBuffer;
import org.gumtree.ui.terminal.ITerminalOutputBuffer.OutputStyle;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZMQAdapter implements ICommunicationAdapter {

	public static String EVENT_TOPIC_TELNET = "org/gumtree/control/ui/zeroMQ";
	
	public static String EVENT_TOPIC_TELNET_SENT = EVENT_TOPIC_TELNET + "/sent";
	
	public static String EVENT_TOPIC_TELNET_RECEIVED = EVENT_TOPIC_TELNET + "/received";
	
	public static String EVENT_PROP_MESSAGE = "sentMessage";
	
	public static String EVENT_PROP_ADAPTER = "adapter";
			
	private static Logger logger = LoggerFactory.getLogger(ZMQAdapter.class);
	
//	private BufferedReader inputStream;

//	private PrintStream outputStream;

//	private Thread listenerThread;

	private boolean isConnected;
	
	private ClientChannel channel;
	
	private ITerminalOutputBuffer outputBuffer;

	public ZMQAdapter() {
		isConnected = false;
		outputBuffer = null;
	}

//	public IConnectionContext getConnectionContext() {
//		if(part != null) {
//			return part.getConnectionContext();
//		}
//		return null;
//	}
//
//	public ICommunicationConfigPart createConfigPart() {
//		part = new TelnetConfigPart();
//		return part;
//	}

	public void connect(ITerminalOutputBuffer outputBuffer) throws CommunicationAdapterException {
		ISicsConnectionContext context = SicsManager.getSicsProxy().getConnectionContext();
		if(context == null) {
			throw new CommunicationAdapterException("Missing configuration.");
		}
		if(isConnected()) {
			throw new CommunicationAdapterException("Attempt to reconnect on an existing connection");
		}
		this.outputBuffer = outputBuffer;
		String serverAddress = "";
		try {
			serverAddress = context.getServerAddress();
//			socket = new Socket(host, port);
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			channel = new ClientChannel();
			try {
				channel.connect(context.getServerAddress(), context.getPublisherAddress());
			} catch (Exception e) {
				throw new CommandNotMappedException("failed to connect to server", e);
			}
//			inputStream = new BufferedReader(new InputStreamReader(channel
//					.getInputStream()));
//			outputStream = new PrintStream(channel.getOutputStream());
			isConnected = true;
			channel.addClientListener(new IClientListener() {
				
				@Override
				public void processMessage(JSONObject json) {
					if (json.has(PropertyConstants.PROP_COMMAND_REPLY)) {
						try {
							OutputStyle style = OutputStyle.NORMAL;
							String text = json.getString(PropertyConstants.PROP_COMMAND_REPLY);
							if (!text.equals(SicsReplyData.COMMAND_REPLY_DEFERRED) && !text.equals(SicsReplyData.COMMAND_REPLY_RUNNING)) {
								if (json.has(PropertyConstants.PROP_COMMAND_FLAG)) {
									if (FlagType.parseString(json.getString(PropertyConstants.PROP_COMMAND_FLAG)) == FlagType.ERROR) {
										style = OutputStyle.ERROR;
										text = "ERROR: " + text;
									}
								}
								getOutputBuffer().appendOutput(text, style);
							}
						} catch (JSONException e) {
						}
					}
				}
			});
			getOutputBuffer().appendInput("connected");
//			listenerThread = new Thread(new Runnable() {
//				public void run() {
//					try {
//						String replyMessage;
////						while ((replyMessage = inputStream.readLine()) != null && isConnected) {
//						while (isConnected) {
//							// little hack to sics telnet bug
//							while (replyMessage.startsWith("ящ")) {
//								replyMessage = replyMessage.substring(2);
//							}
//							logger.info("Server replied: " + replyMessage);
//							// Post replied message to event bus
//							new EventBuilder(EVENT_TOPIC_TELNET_RECEIVED)
//									.append(EVENT_PROP_MESSAGE, replyMessage)
//									.append(EVENT_PROP_ADAPTER,
//											ZMQAdapter.this).post();
//							getOutputBuffer().appendOutput(replyMessage, OutputStyle.NORMAL);
//						}
//					} catch (IOException e) {
//					}
//				}
//			});
//			listenerThread.start();
		} catch (Exception e) {
			throw new CommunicationAdapterException("Cannot connection to " + context.getServerAddress(), e);
		}
	}

	public void disconnect() {
		if (channel != null && channel.isConnected()) {
			channel.disconnect();
		}
		if(!isConnected) {
			return;
		}
		getOutputBuffer().appendInput("disconnected");
//		listenerThread = null;
		isConnected = false;
	}

	public void send(String text) throws CommunicationAdapterException {
		if(getOutputBuffer() != null) {
			getOutputBuffer().appendInput(text);
		}
//		if(outputStream != null) {
//			logger.info("Client sent: " + text);
//			// Post sent message to event bus
//			new EventBuilder(EVENT_TOPIC_TELNET_SENT)
//					.append(EVENT_PROP_MESSAGE, text)
//					.append(EVENT_PROP_ADAPTER, this).post();
//			outputStream.println(text);
//			outputStream.flush();
//		}
		if (channel != null) {
			try {
				channel.asyncSend(text, null);
			} catch (Exception e) {
				throw new CommunicationAdapterException("failed to send command", e);
			}
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

	@Override
	public ICommunicationConfigPart createConfigPart() {
		// TODO Auto-generated method stub
		return null;
	}

}