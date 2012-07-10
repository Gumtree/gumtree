package org.gumtree.gumnix.sics.internal.io;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import org.gumtree.gumnix.sics.internal.io.ISicsChannel.ChannelState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyOfSicsZipDataSocketListener extends SicsSocketListener {

	private static Logger logger;

	private int tempTransId = 1;

	protected CopyOfSicsZipDataSocketListener(SicsChannel channel) {
		super(channel);
	}

	public void run() {
		try {
			String replyMessage = null;
			while(getChannel().getChannelState() != ChannelState.DISCONNECTED) {
				System.out.println("Start to read");
				int result = getInput().read();
				System.out.println("read char");
				if(result == -1) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				} else if(result == 'O') {
					replyMessage = getReader().readLine();
					replyMessage = "O" + replyMessage;
				} else if(result == 'L') {
					replyMessage = getReader().readLine();
					replyMessage = "L" + replyMessage;
				} else if(result == 'S') {
					replyMessage = getReader().readLine();
					replyMessage = "S" + replyMessage;
				} else {
					continue;
				}

				getLogger().debug("Server replied: " + replyMessage);
				// for broadcasting to console within application
				getChannel().messageRecieved(replyMessage);
				if (getChannel().getChannelState() == ChannelState.NORMAL) {
					handleNormalState(replyMessage);
					continue;
				} else if(getChannel().getChannelState() == ChannelState.CONNECTING) {
					handleConnectingState(replyMessage);
					continue;
				} else if (getChannel().getChannelState() == ChannelState.CONNECTED) {
					handleConnectedState(replyMessage);
					continue;
				} else if (getChannel().getChannelState() == ChannelState.LOGINED) {
					handleLoginedState(replyMessage);
					continue;
				}

			}


//				replyMessage = getReader().readLine();
//
////				if(!isBlocking) {
////					replyMessage = getReader().readLine();
////				} else {
////					try {
////						Thread.sleep(10);
////					} catch (InterruptedException e) {
////						e.printStackTrace();
////					}
////					continue;
////				}
//				// little hack to sics telnet bug
//				while (replyMessage.startsWith("ящ")) {
//					replyMessage = replyMessage.substring(2);
//				}
//				getLogger().debug("Server replied: " + replyMessage);
//				// for broadcasting to console within application
//				getChannel().messageRecieved(replyMessage);
//
//				if (getChannel().getChannelState() == ChannelState.NORMAL) {
//					handleNormalState(replyMessage);
////					if(isBlocking) {
////						try {
////							Thread.sleep(10);
////						} catch (InterruptedException e) {
////							e.printStackTrace();
////						}
////					}
//					continue;
//				} else if(getChannel().getChannelState() == ChannelState.CONNECTING) {
//					handleConnectingState(replyMessage);
//					continue;
//				} else if (getChannel().getChannelState() == ChannelState.CONNECTED) {
//					handleConnectedState(replyMessage);
//					continue;
//				} else if (getChannel().getChannelState() == ChannelState.LOGINED) {
//					handleLoginedState(replyMessage);
//					continue;
//				}
//			}
//			while ((replyMessage = getReader().readLine()) != null && getChannel().getChannelState() != ChannelState.DISCONNECTED) {
//				if(isBlocking) {
//					continue;
//				}
//				// little hack to sics telnet bug
//				while (replyMessage.startsWith("ящ")) {
//					replyMessage = replyMessage.substring(2);
//				}
//				getLogger().debug("Server replied: " + replyMessage);
//				// for broadcasting to console within application
//				getChannel().messageRecieved(replyMessage);
//				if (getChannel().getChannelState() == ChannelState.NORMAL) {
//					handleNormalState(replyMessage);
//					continue;
//				} else if(getChannel().getChannelState() == ChannelState.CONNECTING) {
//					handleConnectingState(replyMessage);
//					continue;
//				} else if (getChannel().getChannelState() == ChannelState.CONNECTED) {
//					handleConnectedState(replyMessage);
//					continue;
//				} else if (getChannel().getChannelState() == ChannelState.LOGINED) {
//					handleLoginedState(replyMessage);
//					continue;
//				}
//			}
		} catch (IOException e) {
			getLogger().debug("Error in reading SICS output.", e);
		}
	}

	protected synchronized void handleNormalState(String replyMessage) {
		System.out.println("processing binary data");
		if (replyMessage.startsWith("SICSBIN")) {
			try {
				StringTokenizer st = new StringTokenizer(replyMessage);
				st.nextToken();
				String type = st.nextToken();
				String name = st.nextToken();
				int length = Integer.parseInt(st.nextToken());

				byte[] zipdata = new byte[length];
				System.out.println("trying to read " + length + "bytes");
//				int counter = 0;
//				while(counter < length) {
					int byteread = getInput().read(zipdata);
//					counter += byteread;
					System.out.println("byteread: " + byteread);
//				}

				Inflater decompresser = new Inflater();
				decompresser.setInput(zipdata, 0, length);
				int iSize = 128;
				int jSize = 512;
				int intSize = 4;
//				int hmsize = size * size;
				byte[] result = new byte[iSize * jSize * intSize];
				int[][] data = new int[iSize][jSize];
				try {
					int resultLength = decompresser.inflate(result);
					System.out.println("resultLength: " + resultLength);
					int b1, b2, b3, b4;
					for (int i = 0; i < iSize; i++) {
						for (int j = 0; j < jSize; j++) {
							b1 = result[(i * iSize + j) * intSize];
							b2 = result[(i * iSize + j) * intSize + 1];
							b3 = result[(i * iSize + j) * intSize + 2];
							b4 = result[(i * iSize + j) * intSize + 3];
							data[i][j] = (int) ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
						}
					}
					getZipDataChannel().handleResponse(data, tempTransId++);
				} catch (DataFormatException e) {
					e.printStackTrace();
				} finally {
					decompresser.end();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
		}
	}
//		BinaryReader reader = new BinaryReader(replyMessage, this);
//		reader.run();
//		while(isBlocking) {
//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		System.out.println("finished");
//		try {
//			wait();
//			System.out.println("about to finish");
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		System.out.println("finished");
//		if (replyMessage.startsWith("SICSBIN")) {
//			try {
//				isBlocking = true;
//				StringTokenizer st = new StringTokenizer(replyMessage);
//				st.nextToken();
//				String type = st.nextToken();
//				String name = st.nextToken();
//				int length = Integer.parseInt(st.nextToken());
//
//				byte[] zipdata = new byte[length];
//				System.out.println("trying to read " + length + "bytes");
//				int byteread = getInput().read(zipdata);
//				System.out.println("byteread: " + byteread);
//				isBlocking = false;
//
//				Inflater decompresser = new Inflater();
//				decompresser.setInput(zipdata, 0, length);
//				int iSize = 128;
//				int jSize = 512;
//				int intSize = 4;
////				int hmsize = size * size;
//				byte[] result = new byte[iSize * jSize * intSize];
//				int[][] data = new int[iSize][jSize];
//				try {
//					int resultLength = decompresser.inflate(result);
//					System.out.println("resultLength: " + resultLength);
//					int b1, b2, b3, b4;
//					for (int i = 0; i < iSize; i++) {
//						for (int j = 0; j < jSize; j++) {
//							b1 = result[(i * iSize + j) * intSize];
//							b2 = result[(i * iSize + j) * intSize + 1];
//							b3 = result[(i * iSize + j) * intSize + 2];
//							b4 = result[(i * iSize + j) * intSize + 3];
//							data[i][j] = (int) ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
//						}
//					}
//					getZipDataChannel().handleResponse(data, tempTransId++);
//				} catch (DataFormatException e) {
//					e.printStackTrace();
//				} finally {
//					decompresser.end();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//				isBlocking = false;
//			}
//		}
//	}

	private SicsZipDataChannel getZipDataChannel() {
		return (SicsZipDataChannel)getChannel();
	}

	private Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(CopyOfSicsZipDataSocketListener.class.getName() + ":" + getChannel().getChannelId());
		}
		return logger;
	}


//	private class BinaryReader extends Thread {
//		private String infoMessage;
//
//		private SicsZipDataSocketListener listener;
//
//		private BinaryReader(String infoMessage, SicsZipDataSocketListener listener) {
//			this.infoMessage = infoMessage;
//			this.listener = listener;
//		}
//
//		public synchronized void run() {
//			if (infoMessage.startsWith("SICSBIN")) {
//				try {
//					StringTokenizer st = new StringTokenizer(infoMessage);
//					st.nextToken();
//					String type = st.nextToken();
//					String name = st.nextToken();
//					int length = Integer.parseInt(st.nextToken());
//
//					byte[] zipdata = new byte[length];
//					System.out.println("trying to read " + length + "bytes");
//					int byteread = getInput().read(zipdata);
//					System.out.println("byteread: " + byteread);
//
//					Inflater decompresser = new Inflater();
//					decompresser.setInput(zipdata, 0, length);
//					int iSize = 128;
//					int jSize = 512;
//					int intSize = 4;
////					int hmsize = size * size;
//					byte[] result = new byte[iSize * jSize * intSize];
//					int[][] data = new int[iSize][jSize];
//					try {
//						int resultLength = decompresser.inflate(result);
//						System.out.println("resultLength: " + resultLength);
//						int b1, b2, b3, b4;
//						for (int i = 0; i < iSize; i++) {
//							for (int j = 0; j < jSize; j++) {
//								b1 = result[(i * iSize + j) * intSize];
//								b2 = result[(i * iSize + j) * intSize + 1];
//								b3 = result[(i * iSize + j) * intSize + 2];
//								b4 = result[(i * iSize + j) * intSize + 3];
//								data[i][j] = (int) ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
//							}
//						}
//						getZipDataChannel().handleResponse(data, tempTransId++);
//					} catch (DataFormatException e) {
//						e.printStackTrace();
//					} finally {
//						decompresser.end();
//						listener.notifyAll();
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				} finally {
//				}
//			}
//		}
//	}

	public static void main(String[] args) throws Exception {
		final Socket socket = new Socket("bluegum", 60002);
		final PrintStream proxyOutput = new PrintStream(socket.getOutputStream());
		final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					String replyMessage = null;
					while ((replyMessage = reader.readLine()) != null) {
						System.out.println(replyMessage);
						if(replyMessage.equals("OK")) {
							proxyOutput.println("manager ansto");
							continue;
						} else if(replyMessage.equals("Login OK")) {
							proxyOutput.println("hmm zipget 1");
							continue;
						} else if (replyMessage.startsWith("SICSBIN")) {
							StringTokenizer st = new StringTokenizer(replyMessage);
							st.nextToken();
							String type = st.nextToken();
							String name = st.nextToken();
							int length = Integer.parseInt(st.nextToken());

							byte[] zipdata = new byte[length];
							socket.getInputStream().read(zipdata);

							Inflater decompresser = new Inflater();
							decompresser.setInput(zipdata, 0, length);
							int hmsize = 65536;
							byte[] result = new byte[hmsize * 4];
							double[] data = new double[hmsize];
							try {
								int resultLength = decompresser.inflate(result);
								System.out.println(resultLength);
								for(int i = 0; i < hmsize; i++) {
									int b1, b2, b3, b4;
									b1 = result[i * 4];
									b2 = result[i * 4 + 1];
									b3 = result[i * 4 + 2];
									b4 = result[i * 4 + 3];
									data[i] = (double) ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
								}
							} catch (DataFormatException e) {
								e.printStackTrace();
							}
							decompresser.end();

							System.out.println(data);
//							ZipInputStream zis = new ZipInputStream(socket.getInputStream());
//							int counter = 0;
//							int data = 0;
//							while((data = zis.read()) != -1) {
//								counter++;
//							}
//							System.out.println("counter: " + counter);

//							InflaterInputStream inf = new InflaterInputStream(socket.getInputStream(),
//									new Inflater(false));
//							DataInputStream din = new DataInputStream(inf);
//							int b1, b2, b3, b4;
//							int iLength, i;
//							long count = 0;
//							double[] data = new double[128 * 512];
//							try {
//								b1 = inf.read();
//								b2 = inf.read();
//								b3 = inf.read();
//								b4 = inf.read();
//								iLength = ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
//								System.out.println(iLength);
//								for (i = 0; i < iLength; i++) {
//									// inlining the readInt code
//									b1 = inf.read();
//									b2 = inf.read();
//									b3 = inf.read();
//									b4 = inf.read();
//									data[i] = (double) ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
//									System.out.println(data[i]);
//									count += data[i];
//								}
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
				}
			}
		});
		thread.start();
		thread.setPriority(Thread.MAX_PRIORITY);
	}

}
