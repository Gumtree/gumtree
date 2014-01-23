package au.gov.ansto.bragg.taipan.workbench.interal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CounterService {

	public CounterService() {
		synchronized (CounterService.class) {
			if (monitorThread == null || !monitorThread.isAlive()){
				createMonitorThread();
			}
		}
	}

	private final static String DEFAULT_SOCKET_URL = "ics1-taipan-test.nbi.ansto.gov.au";
	private final static String SOCKET_URL_PROPERTY_ID = "gumtree.taipan.monitorURL";
	private final static int MONITOR_PORT = 33000;
	private final static int DETECTOR_PORT = 33001;
	private final static int DEFAULT_POST_THREAD_HARTBEAT = 50;
	private final static int DEFAULT_REPORT_HEARTBEAT = 10;
	private static Thread monitorThread;
	private static Thread detectorThread;
	private static List<IMonitorEventListener> monitorListeners;
	private static List<IMonitorEventListener> detectorListeners;
	private static final String COUNTER_HEARTBEAT_PROPERTY_ID = "gumtree.counter.heartBeat";
	private static int heartbeat = DEFAULT_POST_THREAD_HARTBEAT;
	private static int reportLength = DEFAULT_REPORT_HEARTBEAT;
	private static String hostName = DEFAULT_SOCKET_URL;

	static{
		try {
			heartbeat = Integer.valueOf(System.getProperty(COUNTER_HEARTBEAT_PROPERTY_ID));
		} catch (Exception e) {
		}
		try {
			hostName = System.getProperty(SOCKET_URL_PROPERTY_ID);
			if (hostName == null || hostName.trim().length() == 0) {
				hostName = DEFAULT_SOCKET_URL;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		monitorListeners = new ArrayList<IMonitorEventListener>();
		detectorListeners = new ArrayList<IMonitorEventListener>();
		if (monitorThread == null || !monitorThread.isAlive()) {
			createMonitorThread();
		}
		if (detectorThread == null || !detectorThread.isAlive()) {
			createDetectorThread();
		}
	}

	private static void triggerMonitorEvent(double value) {
		for (IMonitorEventListener listener : monitorListeners) {
			listener.update(value);
		}
	}

	private static void triggerDetectorEvent(double value) {
		for (IMonitorEventListener listener : detectorListeners) {
			listener.update(value);
		}
	}

	private static void createMonitorThread() {
		try{
			final Socket socket = new Socket(hostName, MONITOR_PORT);
			final BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			final PrintStream outputStream = new PrintStream(socket.getOutputStream());
			monitorThread = new Thread(new Runnable() {
				public void run() {
					try {
						String replyMessage;
						while (true) {
							outputStream.println("read");
							while (true) {
								replyMessage = inputStream.readLine();
								if (replyMessage != null && replyMessage.trim().length() > 0) {
									String[] items = replyMessage.split(" ");
									String valueText = items[items.length - 1];
									double value = Double.valueOf(valueText);
									if (value > 0) {
										value = Math.log10(value);
									} else {
										value = 0;
									}

//									value = value - 995;
//									if (value > 10){
//										value = 10;
//									} else if (value < 0) {
//										value = 0;
//									}
									triggerMonitorEvent(value);
									break;
								}
							}
							try {
								Thread.sleep(heartbeat);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (IOException e) {
						try {
							socket.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			});
			monitorThread.start();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createDetectorThread() {
		try{
			final Socket socket = new Socket(hostName, DETECTOR_PORT);
			final BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			final PrintStream outputStream = new PrintStream(socket.getOutputStream());
			detectorThread = new Thread(new Runnable() {
				public void run() {
					try {
						String replyMessage;
						boolean checkCounter = true;
						int counter = 0;
						float countsSum = 0;
						Queue<Float> queue = new LinkedList<Float>();
						while (true) {
							outputStream.println("read");
							while (true) {
								replyMessage = inputStream.readLine();
								if (replyMessage != null && replyMessage.trim().length() > 0) {
									String[] items = replyMessage.split(" ");
									String valueText = items[items.length - 1];
									float value = Float.valueOf(valueText);
									
//									if (value > 0) {
//										value = Math.log10(value);
//									} else {
//										value = 0;
//									}
//
//									triggerDetectorEvent(value);
									
									if (checkCounter) {
										counter ++;
										queue.add(value);
										countsSum += value;
										if (counter >= reportLength) {
											checkCounter = false;
											if (countsSum > 1) {
												triggerDetectorEvent(Math.log10(countsSum / reportLength));
											} else {
												triggerDetectorEvent(0);
											}
										}
									} else {
										countsSum += value;
										countsSum -= queue.poll();
										queue.add(value);
										if (countsSum > 1) {
											triggerDetectorEvent(Math.log10(countsSum / reportLength));
										} else {
											triggerDetectorEvent(0);
										}
									}
									
									
//									value = value - 995;
//									if (value > 10){
//										value = 10;
//									} else if (value < 0) {
//										value = 0;
//									}
									
									break;
								}
							}
							try {
								Thread.sleep(heartbeat);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (IOException e) {
						try {
							socket.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			});
			detectorThread.start();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public interface IMonitorEventListener {
		public void update(double value);
	}
	
	public static void addMonitorListener(IMonitorEventListener listener) {
		monitorListeners.add(listener);
	}
	
	public static void removeMonitorListener(IMonitorEventListener listener) {
		monitorListeners.remove(listener);
	}
	
	public static void addDetectorListener(IMonitorEventListener listener) {
		detectorListeners.add(listener);
	}
	
	public static void removeDetectorListener(IMonitorEventListener listener) {
		detectorListeners.remove(listener);
	}
}
