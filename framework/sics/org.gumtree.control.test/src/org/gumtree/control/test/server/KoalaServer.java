package org.gumtree.control.test.server;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.gumtree.control.batch.BatchStatus;
import org.gumtree.control.core.ICommandController;
import org.gumtree.control.core.IDriveableController;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.control.imp.SicsChannel;
import org.gumtree.control.model.PropertyConstants;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.model.PropertyConstants.FlagType;
import org.gumtree.control.model.PropertyConstants.MessageType;
import org.gumtree.control.model.SicsModel;
import org.gumtree.control.test.ConstantSetup;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import org.zeromq.ZMQ;


public class KoalaServer {

	public static final String CMD_MANAGER = "manager ";
	public static final String CMD_USER = "user ";
	public static final String CMD_SPY = "spy ";
	public static final String CMD_DRIVE = "drive ";
	public static final String CMD_RUN = "run ";
	public static final String CMD_EXE = "exe";
	public static final String CMD_MODEL = "getgumtreexml";
	public static final String CMD_HSET = "hset";
	public static final String CMD_HGET = "hget";
	public static final String CMD_STATUS = "status";
	public static final String CMD_HISTMEM = "histmem";
	public static final String CMD_PAUSE = "pause";
	
	private static int MESSAGE_SEQ = 0;
//	private static int COMMAND_TRANS = 0;

	private static int COUNT_RATE = 1000;
	private static final String MODEL_FILENAME = "C:\\Gumtree\\docs\\GumtreeXML\\koala.xml";
	private ServerStatus status;
	private BatchStatus batchStatus = BatchStatus.IDLE;
    private HistmemServer histmemServer;
    private String batchFile = null;
	
	private SicsModel model;
	private ZMQ.Socket serverSocket;
	private ZMQ.Socket publisherSocket;
    private ZMQ.Context context;
	
    private boolean isRunning;
    
    enum HistmemStatus{Counting, Stopped};
    enum HistmemMode{time, count, unlimited};
    
    class HistmemServer {
    	
    	HistmemMode mode = HistmemMode.time;
    	int preset = 10;
    	HistmemStatus status = HistmemStatus.Stopped;
    	boolean stopFlag = false; 
    	Thread runner;
    	
    	void start() {
    		runner = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						setHistmemStatus(HistmemStatus.Counting);
						if (mode.equals(HistmemMode.time)) {
							try {
								Thread.sleep(preset * 1000);
							} catch (InterruptedException e) {
								setHistmemStatus(HistmemStatus.Stopped);
							}
						} else if (mode == HistmemMode.count) {
							try {
								int count = 0;
								while (count < preset) {
									Thread.sleep(1000);
									count += COUNT_RATE;
								}
							} catch (Exception e) {
								setHistmemStatus(HistmemStatus.Stopped);
							}
						} else {
							stopFlag = false;
							while (!stopFlag) {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									setHistmemStatus(HistmemStatus.Stopped);
								}
							}
						}
						setHistmemStatus(HistmemStatus.Stopped);
					} catch (Exception e) {
					}
				}
			});
    		runner.start();
    	}
    	
    	void stop() {
    		runner.interrupt();
    	}
    	
    	void setHistmemStatus(HistmemStatus status) throws JSONException{
    		this.status = status; 
    		if (status == HistmemStatus.Counting) {
    			setStatus(ServerStatus.COUNTING);
    		} else if (status == HistmemStatus.Stopped) {
    			setStatus(ServerStatus.EAGER_TO_EXECUTE);
    		}
    	}
    }
    
    
	public KoalaServer(String serverAddress, String pubAddress) throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException {
		histmemServer = new HistmemServer();
		model = new SicsModel(null);
		model.loadFromFile((MODEL_FILENAME));
		
//		setStatus(ServerStatus.EAGER_TO_EXECUTE);
		status = ServerStatus.EAGER_TO_EXECUTE;
		context = ZMQ.context(2);
        //  Socket to talk to clients
        serverSocket = context.socket(ZMQ.ROUTER);
//        serverSocket.bind(ConstantSetup.LOCAL_SERVER_ADDRESS);
        serverSocket.bind(serverAddress);
        
        publisherSocket = context.socket(ZMQ.PUB);
//        publisherSocket.bind(ConstantSetup.LOCAL_PUBLISHER_ADDRESS);
        publisherSocket.bind(pubAddress);
        
        histmemServer = new HistmemServer();
	}
	
	private void respond(String client, String message) {
//		socket.send(message.getBytes(ZMQ.CHARSET), 0);
		if (message.length() > 200) {
			System.err.println("respond to command: " + message.substring(0, 200));
		} else {
			System.err.println("respond to command: " + message);
		}
		serverSocket.sendMore(client);
		serverSocket.send(message.getBytes(ZMQ.CHARSET));
	}

	private void respondFinal(String client, String cid, String cmd, String reply) throws JSONException {
		JSONObject json = new JSONObject();
		json.put(PropertyConstants.PROP_COMMAND_FLAG, FlagType.OK);
		json.put(PropertyConstants.PROP_COMMAND_FINAL, true);
		json.put(PropertyConstants.PROP_COMMAND_CMD, cmd);
		json.put(PropertyConstants.PROP_COMMAND_REPLY, reply);
		json.put(PropertyConstants.PROP_COMMAND_TEXT, cmd);
		json.put(PropertyConstants.PROP_COMMAND_TRANS, cid);
		respond(client, json.toString());
	}

	private void respondReply(String client, String cid, String cmd, String reply) throws JSONException {
		JSONObject json = new JSONObject();
		json.put(PropertyConstants.PROP_COMMAND_FLAG, FlagType.OK);
		json.put(PropertyConstants.PROP_COMMAND_FINAL, false);
		json.put(PropertyConstants.PROP_COMMAND_CMD, cmd);
		json.put(PropertyConstants.PROP_COMMAND_REPLY, reply);
		json.put(PropertyConstants.PROP_COMMAND_TEXT, cmd);
		json.put(PropertyConstants.PROP_COMMAND_TRANS, cid);
		respond(client, json.toString());
	}
	
	private void publishValueUpdate(String path, Object value) throws JSONException {
		JSONObject json = new JSONObject();
		json.put(PropertyConstants.PROP_UPDATE_NAME, path);
		json.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.VALUE);
		json.put(PropertyConstants.PROP_UPDATE_VALUE, value);
		publish(json);
	}

	private void publishState(String device, ControllerState state) throws JSONException {
		JSONObject json = new JSONObject();
		json.put(PropertyConstants.PROP_UPDATE_NAME, state);
		json.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.STATE);
		json.put(PropertyConstants.PROP_UPDATE_VALUE, device);
		publish(json);
	}

	private void publish(JSONObject json) throws JSONException {
		json.put(PropertyConstants.PROP_UPDATE_SEQ, MESSAGE_SEQ ++);
		json.put(PropertyConstants.PROP_UPDATE_TS, System.currentTimeMillis());
		String message = json.toString();
		System.out.println("publish update: " + message);
		publisherSocket.send(message.getBytes(ZMQ.CHARSET));
	}

	private void respondState(String client, String cid, String cmd, ControllerState state) throws JSONException {
		JSONObject json = new JSONObject();
		json.put(PropertyConstants.PROP_COMMAND_FLAG, FlagType.OK);
		json.put(PropertyConstants.PROP_COMMAND_FINAL, false);
		json.put(PropertyConstants.PROP_COMMAND_CMD, cmd);
		json.put(PropertyConstants.PROP_COMMAND_REPLY, state);
		json.put(PropertyConstants.PROP_COMMAND_TEXT, cmd);
		json.put(PropertyConstants.PROP_COMMAND_TRANS, cid);
		respond(client, json.toString());
	}

	private void drive(IDriveableController driveable, String target) throws JSONException, SicsModelException {
		setStatus(ServerStatus.DRIVING);

		publishValueUpdate(driveable.getPath() + "/target", target);
		publishState(driveable.getPath(), ControllerState.BUSY);
		
//		if (command.endsWith("interrupt")) {
//			Thread.sleep(1000);
//			generateInterrupt(client, cid);
//		} else if (command.endsWith("wait")) {
//			Thread.sleep(1000);
//		}
		int steps = 3;
		float org = Float.valueOf(driveable.getValue().toString());
		float tgt = Float.valueOf(target);
		float gap = (tgt - org) / steps;
		for (int i = 0; i < steps; i++) {
			
			float cur = org + gap * i;
			driveable.updateModelValue(String.valueOf(cur));
			publishValueUpdate(driveable.getPath(), String.valueOf(cur));
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
//				generateInterrupt(client, cid);
			}
		}

		driveable.updateModelValue(target);
		publishValueUpdate(driveable.getPath(), target);
		publishState(driveable.getPath(), ControllerState.IDLE);
		
		setStatus(ServerStatus.EAGER_TO_EXECUTE);
	}
	
	public void processCommand(String client, String cid, String command) throws InterruptedException {
		if (command.startsWith(CMD_MANAGER)) {
			processLogin(client, cid, command);
		} else if (command.startsWith(CMD_DRIVE)) {
			processDrive(client, cid, command);
		} else if (command.startsWith(CMD_MODEL)) {
			processModel(client, cid, command);
		} else if (command.startsWith(CMD_RUN)) {
			processRun(client, cid, command);
		} else if (command.startsWith(CMD_HISTMEM)) {
			processHistmem(client, cid, command);
		} else if (command.startsWith(CMD_HGET)) {
			processHget(client, cid, command);
		} else if (command.startsWith(CMD_EXE)) {
			processExe(client, cid, command);
		} else if (command.startsWith(CMD_PAUSE)) {
			processPause(client, cid, command);
		} else if (command.startsWith(CMD_HSET)) {
			if (command.endsWith(" start")) {
				processGroupCommand(client, cid, command);
			} else {
				processHset(client, cid, command);
			}
		} else if (command.equals(CMD_STATUS)) {
			processStatus(client, cid, command);
		} else {
			if (!command.contains(" ")) {
				processDevice(client, cid, command);
			} else {
				sendInternalError(client, cid, command, "command not recognised");
			}
		}
	}
	
//	private void publishValueUpdate(String path, String value) 
//			throws JSONException {
//		JSONObject json = new JSONObject();
//		json.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.VALUE);
//		json.put(PropertyConstants.PROP_UPDATE_NAME, path);
//		json.put(PropertyConstants.PROP_UPDATE_VALUE, value);
//		publish(json);
//	}
	
	private void processDrive(String client, String cid, String command) {
		try {
			String[] parts = command.split(" ");
			String dev = parts[1];
			String target = parts[2];
			ISicsController controller = model.findController(dev);
			if (controller == null) {
				sendInternalError(client, cid, command, "device " + String.valueOf(dev) + " not found");
				return;
			}
			if (!(controller instanceof IDriveableController)) {
				sendInternalError(client, cid, command, "device " + String.valueOf(dev) + " is not driveable");
				return;
			}
			IDriveableController driveable = (IDriveableController) controller;
			double precision = Double.NaN;
			for (ISicsController child : driveable.getChildren()) {
				if ("precision".equals(child.getId())) {
					try {
						precision = (float) ((IDynamicController) child).getValue();
					} catch (Exception e) {
					}
				}
			}
			if (! Double.isNaN(precision)) {
				double targetValue = Double.valueOf(target);
				double currentValue = (float) driveable.getValue();
				if (Math.abs(targetValue - currentValue) <= precision) {
					respondFinal(client, cid, command, dev + " is already at " + currentValue);
					return;
				}
			}
			
			respondReply(client, cid, command, "start driving " + dev);
			drive(driveable, target);
			respondFinal(client, cid, command, dev + " = " + target);
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		} catch (SicsModelException e) {
			sendInternalError(client, cid, command, "model exception");
		} catch (Exception e) {
			sendInternalError(client, cid, command, e.getMessage());
		} finally {
			status = ServerStatus.EAGER_TO_EXECUTE;
		}
	}

	private void generateInterrupt(String client, String cid) throws JSONException {
		JSONObject json = new JSONObject();
		json.put(SicsChannel.JSON_KEY_INTERRUPT, true);
		respond(client, json.toString());
	}

	private void processRun(String client, String cid, String command) {
//		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String dev = parts[1];
			String target = parts[2];
			ISicsController controller = model.findController(dev);
			if (controller == null) {
				sendInternalError(client, cid, command, "device " + String.valueOf(dev) + " not found");
				return;
			}
			if (!(controller instanceof IDriveableController)) {
				sendInternalError(client, cid, command, "device " + String.valueOf(dev) + " is not moveable");
				return;
			}
			final IDriveableController driveable = (IDriveableController) controller;
			double precision = Double.NaN;
			for (ISicsController child : driveable.getChildren()) {
				if ("precision".equals(child.getId())) {
					try {
						precision = (float) ((IDynamicController) child).getValue();
					} catch (Exception e) {
					}
				}
			}
			if (! Double.isNaN(precision)) {
				double targetValue = Double.valueOf(target);
				double currentValue = (float) driveable.getValue();
				if (Math.abs(targetValue - currentValue) <= precision) {
					respondFinal(client, cid, command, dev + " is already at " + currentValue);
					return;
				}
			}
			
//			setStatus(ServerStatus.DRIVING);
//			publishState(driveable.getPath(), ControllerState.BUSY);
			

			Thread runThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
//						Thread.sleep(2000);
//						driveable.updateModelValue(target);
//						publishValueUpdate(driveable.getPath(), target);
////						jobj.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.STATE);
////						jobj.put(PropertyConstants.PROP_UPDATE_VALUE, driveable.getPath());
////						jobj.put(PropertyConstants.PROP_UPDATE_NAME, ControllerState.IDLE);
////						publish(jobj);
//						publishState(driveable.getPath(), ControllerState.IDLE);
//						
////						jobj = new JSONObject();
////						jobj.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
////						jobj.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.STATUS);
////						jobj.put(PropertyConstants.PROP_UPDATE_NAME, driveable.getPath());
////						jobj.put(PropertyConstants.PROP_COMMAND_REPLY, target);
//						setStatus(ServerStatus.EAGER_TO_EXECUTE);
						drive(driveable, target);
					} catch (Exception e) {
					}
					
				}
			});
			runThread.start();
			
			respondFinal(client, cid, command, "running " + dev + " to " + target);
			
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		} catch (SicsModelException e) {
			sendInternalError(client, cid, command, "model exception");
		} catch (Exception e) {
			sendInternalError(client, cid, command, e.getMessage());
		} finally {
			status = ServerStatus.EAGER_TO_EXECUTE;
		}
	}

	private void processModel(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			File file = new File(MODEL_FILENAME);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();
			String xml = new String(data, "UTF-8");
			
//			String xml = model.getGumtreeXML();
			json.put(PropertyConstants.PROP_COMMAND_FLAG, FlagType.OK);
			json.put(PropertyConstants.PROP_COMMAND_REPLY, xml);
			json.put(SicsChannel.JSON_KEY_FINISHED, true);
			json.put(SicsChannel.JSON_KEY_CID, cid);
			respond(client, json.toString());
		} catch (JSONException | IOException e) {
			sendInternalError(client, cid, command, "json exception");
		} 
	}
	
	private void processLogin(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			if (command.equals("manager ansto")) {
				json.put(PropertyConstants.PROP_COMMAND_REPLY, "OK");
			} else {
				json.put(SicsChannel.JSON_KEY_ERROR, "wrong login");
			}
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
			json.put(SicsChannel.JSON_KEY_CID, cid);
			respond(client, json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		}
	}
	
	private void processStatus(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			json.put(SicsChannel.JSON_KEY_STATUS, status);
			publish(json);
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		}
		try {
			json = new JSONObject();
			json.put(PropertyConstants.PROP_COMMAND_FLAG, FlagType.OK);
			json.put(PropertyConstants.PROP_COMMAND_REPLY, status);
			json.put(SicsChannel.JSON_KEY_FINISHED, true);
			json.put(SicsChannel.JSON_KEY_CID, cid);
			respond(client, json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		}
	}
	
	private void processHistmem(String client, String cid, String command) throws InterruptedException {
		try {
			String[] parts = command.split(" ");
			String prop = parts[1];
			String target = null;
			if (parts.length > 2) {
				target = parts[2];
			}

			if ("mode".equals(prop)) {
				if (target == null) {
					respondFinal(client, cid, command, histmemServer.mode.name());
				} else {
					histmemServer.mode = HistmemMode.valueOf(target);
					respondFinal(client, cid, command, "OK");
				}
			} else if ("preset".equals(prop)) {
				if (target == null) {
					respondFinal(client, cid, command, String.valueOf(histmemServer.preset));
				} else {
					histmemServer.preset = Integer.valueOf(target);
					respondFinal(client, cid, command, "OK");
				}
			} else if ("start".equals(prop)) {
				if ("blocked".equals(target)) {
					respondReply(client, cid, command, "start = histogram");
					histmemServer.start();
					while (histmemServer.status == HistmemStatus.Counting) {
						Thread.sleep(100);
					}
					respondFinal(client, cid, command, "stop = histogram");
				} else {
					histmemServer.start();
					while (histmemServer.status == HistmemStatus.Counting) {
						Thread.sleep(100);
					}
					respondFinal(client, cid, command, "OK");
				}
			} else if ("stop".equals(prop)) {
				histmemServer.stop();
				while (histmemServer.status == HistmemStatus.Counting) {
					Thread.sleep(100);
				}
				respondFinal(client, cid, command, "OK");
			} else if ("status".equals(prop)) {
				respondFinal(client, cid, command, histmemServer.status.name());
			} else if ("pause".equals(prop)) {
				respondFinal(client, cid, command, "OK");
			} else {
				sendInternalError(client, cid, command, "command not recognised");
			}
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		}
	}
	
	private void processHset(String client, String cid, String command) {
		try {
			String[] parts = command.split(" ");
			String path = parts[1];
			String target = parts[2];
			ISicsController controller = model.findController(path);
			if (controller == null) {
				sendInternalError(client, cid, command, "device " + String.valueOf(path) + " not found");
				return;
			}
			if (!(controller instanceof IDynamicController)) {
				sendInternalError(client, cid, command, "device " + String.valueOf(path) + " can not change");
				return;
			}
			IDynamicController dynamic = (IDynamicController) controller;
			
			dynamic.updateModelValue(target);
//			json = new JSONObject();
//			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.VALUE);
//			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
//			json.put(PropertyConstants.PROP_COMMAND_REPLY, target);
//			publish(json);
			publishValueUpdate(dynamic.getPath(), target);
			
//			json = new JSONObject();
//			json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
//			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
//			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.VALUE);
//			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
//			json.put(PropertyConstants.PROP_COMMAND_REPLY, target);
//			json.put(SicsChannel.JSON_KEY_CID, cid);
//			respond(client, json.toString());
			respondFinal(client, cid, command, dynamic.getPath() + " = " + target);
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		} catch (Exception e) {
			sendInternalError(client, cid, command, e.getMessage());
		} finally {
			status = ServerStatus.EAGER_TO_EXECUTE;
		}
	}

	private void processExe(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String cmd = parts[1];
			String target = null;
			if (parts.length > 2) {
				target = parts[2];
			}

			json = new JSONObject();
			if (cmd.equals("info")) {
				json.put(PropertyConstants.PROP_COMMAND_REPLY, batchStatus);
			} else if (cmd.equals("interest")) {
				if (batchFile == null) {
					json.put(PropertyConstants.PROP_COMMAND_REPLY, "");
				} else {
					json.put(PropertyConstants.PROP_COMMAND_REPLY, batchFile);
				}
			} else if (cmd.equals("enqueue")) {
				if (target != null) {
					batchFile = target;
					json.put(PropertyConstants.PROP_COMMAND_REPLY, "OK");
				}
			} else if (cmd.equals("run")) {
				json.put(SicsChannel.JSON_KEY_CID, cid);
				json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.RUNNING_A_SCAN);
				json.put(PropertyConstants.PROP_COMMAND_REPLY, "OK");
				respond(client, json.toString());
				runBatch();
				json = new JSONObject();
			}
			json.put(SicsChannel.JSON_KEY_CID, cid);
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
			respond(client, json.toString());
			
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		} catch (Exception e) {
			sendInternalError(client, cid, command, e.getMessage());
		} finally {
			status = ServerStatus.EAGER_TO_EXECUTE;
		}
	}

	void runBatch() {
//		Thread thread = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
				batchStatus = BatchStatus.RUNNING;
				JSONObject json = new JSONObject();
				try {
					json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.DRIVING);
					json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.STATE);
					json.put(PropertyConstants.PROP_COMMAND_REPLY, PropertyConstants.ControllerState.BUSY);
					json.put(PropertyConstants.PROP_UPDATE_NAME, "dm");
					publish(json);
					Thread.sleep(1000);
					json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.WAIT);
					publish(json);
					Thread.sleep(1000);
					json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.COUNTING);
					publish(json);
					Thread.sleep(1000);
					json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
					publish(json);
				} catch (JSONException e) {
				} catch (InterruptedException e) {
				} finally {
					batchStatus = BatchStatus.IDLE;
				}
//			}
//		});
//		thread.start();
	}
	
	private void processHget(String client, String cid, String command) {
		try {
			String[] parts = command.split(" ");
			String path = parts[1];
			String target;
			ISicsController controller = model.findController(path);
			if (controller == null) {
				sendInternalError(client, cid, command, "device " + String.valueOf(path) + " not found");
				return;
			}
			if (!(controller instanceof IDynamicController)) {
				sendInternalError(client, cid, command, "device " + String.valueOf(path) + " doesn't have value");
				return;
			}
			IDynamicController dynamic = (IDynamicController) controller;
			
			target = String.valueOf(dynamic.getValue());
			
//			json = new JSONObject();
//			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.VALUE);
//			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
//			json.put(PropertyConstants.PROP_COMMAND_REPLY, target);
//			publish(json);
			publishValueUpdate(dynamic.getPath(), target);
			
//			json = new JSONObject();
//			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
//			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.VALUE);
//			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
//			json.put(PropertyConstants.PROP_COMMAND_REPLY, target);
//			json.put(SicsChannel.JSON_KEY_CID, cid);
//			respond(client, json.toString());
			respondFinal(client, cid, command, dynamic.getPath() + " = " + target);
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		} catch (Exception e) {
			sendInternalError(client, cid, command, e.getMessage());
		} finally {
		}
	}

	private void processPause(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String prop = parts[1];
//			String target = null;
//			if (parts.length > 2) {
//				target = parts[2];
//			}

			if ("on".equals(prop)) {
				status = ServerStatus.PAUSED;
				json = new JSONObject();
				json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.PAUSED);
				publish(json);
				json.put(SicsChannel.JSON_KEY_FINISHED, "true");
				json.put(SicsChannel.JSON_KEY_CID, cid);
				respond(client, json.toString());
				return;
			} else if ("off".equals(prop)) {
				status = ServerStatus.COUNTING;
				json = new JSONObject();
				json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.COUNTING);
				publish(json);
				json.put(SicsChannel.JSON_KEY_FINISHED, "true");
				json.put(SicsChannel.JSON_KEY_CID, cid);
				respond(client, json.toString());
				return;
			} 
			sendInternalError(client, cid, command, "command not recognised");
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		}
	}
	
	private void processGroupCommand(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String path = parts[1];
//			String target = parts[2];
			ISicsController controller = model.findController(path);
			if (controller == null) {
				sendInternalError(client, cid, command, "device " + String.valueOf(path) + " not found");
				return;
			}
			if (!(controller instanceof ICommandController)) {
				sendInternalError(client, cid, command, "device " + String.valueOf(path) + " is not runnable");
				return;
			}
			ICommandController dynamic = (ICommandController) controller;
			
			json = new JSONObject();
			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.STATE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
			json.put(PropertyConstants.PROP_COMMAND_REPLY, PropertyConstants.ControllerState.BUSY);
			json.put(SicsChannel.JSON_KEY_CID, cid);
			respond(client, json.toString());
			publish(json);
			
			Thread.sleep(1000);
			
			json = new JSONObject();
			json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.STATE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
			json.put(PropertyConstants.PROP_COMMAND_REPLY, PropertyConstants.ControllerState.IDLE);
			publish(json);
			json.put(SicsChannel.JSON_KEY_CID, cid);
			respond(client, json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		} catch (Exception e) {
			sendInternalError(client, cid, command, e.getMessage());
		} finally {
			status = ServerStatus.EAGER_TO_EXECUTE;
		}
	}
	
	private void processDevice(String client, String cid, String command) {
		try {
			String[] parts = command.split(" ");
			String dev = parts[0];
			ISicsController controller = model.findController(dev);
			if (controller == null) {
				sendInternalError(client, cid, command, "device " + String.valueOf(dev) + " not found");
				return;
			}
			if (controller instanceof DynamicController) {
				respondFinal(client, cid, command, ((DynamicController) controller).getValue().toString());
			} 
		} catch (JSONException e) {
			sendInternalError(client, cid, command, "json exception");
		} catch (SicsModelException e) {
			sendInternalError(client, cid, command, "model exception");
		} catch (Exception e) {
			sendInternalError(client, cid, command, e.getMessage());
		} finally {
		}
	}

	private void sendInternalError(String client, String cid, String command, String message) {
//		respond(client, "{ \"" + SicsChannel.JSON_KEY_ERROR + "\":\"internal error, " + message + "\","
//				+ " \"" + SicsChannel.JSON_KEY_FINISHED + "\":\"true\","
//				+ " \"" + SicsChannel.JSON_KEY_CID + "\":\"" + cid + "\" }");
		try {
			JSONObject json = new JSONObject();
			json.put(PropertyConstants.PROP_COMMAND_FLAG, FlagType.ERROR);
			json.put(PropertyConstants.PROP_COMMAND_FINAL, true);
			json.put(PropertyConstants.PROP_COMMAND_CMD, command);
			json.put(PropertyConstants.PROP_COMMAND_REPLY, message);
			json.put(PropertyConstants.PROP_COMMAND_TEXT, command);
			json.put(PropertyConstants.PROP_COMMAND_TRANS, cid);
			respond(client, json.toString());
		} catch (Exception e) {
		}
	}
	
	public void run() throws InterruptedException {
		
		System.out.println("Simulation server started");
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				isRunning = true;
		        while (!Thread.currentThread().isInterrupted()) {
		        	
		            String client = serverSocket.recvStr();
		            
//		            System.out.println("client id: " + client);
		            
//		            byte[] received = serverSocket.recv(0);
//		            
//		            System.out.println("Received " + ": [" + new String(received, ZMQ.CHARSET) + "]");
		//
//		            processCommand(client, new String(received, ZMQ.CHARSET));
		            
		            String commandText = serverSocket.recvStr();
		            
		            System.out.println("command: " + commandText);
		            
		            JSONObject json = null;
		            try {
						json = new JSONObject(commandText);
		            	String command = json.getString(SicsChannel.JSON_KEY_COMMAND);
		            	String cid = json.getString(SicsChannel.JSON_KEY_CID);
//		            	ExecutorService executor = Executors.newSingleThreadExecutor();
//		            	executor.submit(new Runnable() {
//							
//							@Override
//							public void run() {
//				                processCommand(client, cid, command);
//							}
//							
//						});
		            	processCommand(client, cid, command);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		            
//		            Thread.sleep(50); 
		        }
		        close();
		        isRunning = false;
			}
		});
		thread.start();
	}
	
	public void close() {
        serverSocket.close();
        context.term();
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
    public static void main(String[] args) throws Exception
    {
    	KoalaServer server = new KoalaServer(ConstantSetup.LOCAL_SERVER_ADDRESS, ConstantSetup.LOCAL_PUBLISHER_ADDRESS);
    	server.run();
    	
    	KoalaServer validator = new KoalaServer(ConstantSetup.VALIDATOR_SERVER_ADDRESS, ConstantSetup.VALIDATOR_PUBLISHER_ADDRESS);
    	validator.run();
    	
    	while (server.isRunning) {
    		Thread.sleep(1000);
    	}
    }

	/**
	 * @return the status
	 */
	public ServerStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 * @throws JSONException 
	 */
	private void setStatus(ServerStatus status) throws JSONException {
		this.status = status;
		JSONObject json = new JSONObject();
		json.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.STATUS);
		json.put(PropertyConstants.PROP_UPDATE_NAME, MessageType.STATUS);
		json.put(PropertyConstants.PROP_UPDATE_VALUE, status);
//		json.put(SicsChannel.JSON_KEY_CID, cid);
		publish(json);
	}
}