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
import org.gumtree.control.imp.SicsChannel;
import org.gumtree.control.model.PropertyConstants;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.model.PropertyConstants.MessageType;
import org.gumtree.control.model.SicsModel;
import org.gumtree.control.test.ConstantSetup;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import org.zeromq.ZMQ;


public class SimulationServer {

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

	private static final String MODEL_FILENAME = "C:\\Gumtree\\docs\\GumtreeXML\\hipadaba.xml";
	private ServerStatus status;
	private BatchStatus batchStatus = BatchStatus.IDLE;
    private HistmemServer histmemServer;
    private String batchFile = null;
	
	private SicsModel model;
	private ZMQ.Socket serverSocket;
	private ZMQ.Socket publisherSocket;
    private ZMQ.Context context;
	
    private boolean isRunning;
    
    class HistmemServer {
    	String mode = "time";
    	float preset = 10;
    	String status = "Stopped";
    }
    
	public SimulationServer(String serverAddress, String pubAddress) throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException {

		model = new SicsModel(null);
		model.loadFromFile((MODEL_FILENAME));
		
		setStatus(ServerStatus.EAGER_TO_EXECUTE);
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
	
	private void send(String client, String message) {
//		socket.send(message.getBytes(ZMQ.CHARSET), 0);
		System.out.println("send acknowledge: " + message);
		serverSocket.sendMore(client);
		serverSocket.send(message.getBytes(ZMQ.CHARSET));
	}

	private void publish(String message) {
		System.out.println("publish update: " + message);
		publisherSocket.send(message.getBytes(ZMQ.CHARSET));
	}


	public void processCommand(String client, String cid, String command) {
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
			sendInternalError(client, cid, "command not recognised");
		}
	}
	
	private void processDrive(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String dev = parts[1];
			String target = parts[2];
			ISicsController controller = model.findController(dev);
			if (controller == null) {
				sendInternalError(client, cid, "device " + String.valueOf(dev) + " not found");
				return;
			}
			if (!(controller instanceof IDriveableController)) {
				sendInternalError(client, cid, "device " + String.valueOf(dev) + " is not driveable");
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
					json.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.VALUE);
					json.put(PropertyConstants.PROP_UPDATE_NAME, driveable.getPath());
					json.put(PropertyConstants.PROP_UPDATE_VALUE, target);
					json.put(SicsChannel.JSON_KEY_FINISHED, "true");
					json.put(SicsChannel.JSON_KEY_CID, cid);
					send(client, json.toString());
					return;
				}
			}
			
			setStatus(ServerStatus.DRIVING);
			json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.DRIVING);
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());

//			Thread.sleep(1000);
			json = new JSONObject();
			json.put(PropertyConstants.PROP_COMMAND_REPLY, dev + " = " + String.valueOf(target));
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());

//			Thread.sleep(1000);

			json = new JSONObject();
			json.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.STATE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, driveable.getPath());
			json.put(PropertyConstants.PROP_UPDATE_VALUE, ControllerState.BUSY);
			publish(json.toString());
			
			if (command.endsWith("interrupt")) {
				Thread.sleep(1000);
				generateInterrupt(client);
			} else if (command.endsWith("wait")) {
				Thread.sleep(1000);
			}
			
			json = new JSONObject();
			json.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.STATE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, driveable.getPath());
			json.put(PropertyConstants.PROP_UPDATE_VALUE, ControllerState.IDLE);
			publish(json.toString());
			
			driveable.updateModelValue(target);
			json = new JSONObject();
			json.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.VALUE);
			json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, driveable.getPath());
			json.put(PropertyConstants.PROP_UPDATE_VALUE, target);
			publish(json.toString());
			
			json = new JSONObject();
			json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
//			json.put(SicsChannel.JSON_KEY_VALUE, "OK");
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
			json.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.VALUE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, driveable.getPath());
			json.put(PropertyConstants.PROP_COMMAND_REPLY, target);
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		} catch (SicsModelException e) {
			// TODO Auto-generated catch block
			sendInternalError(client, cid, "model exception");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			sendInternalError(client, cid, e.getMessage());
		} finally {
			setStatus(ServerStatus.EAGER_TO_EXECUTE);
		}
	}

	private void generateInterrupt(String client) throws JSONException {
		JSONObject json = new JSONObject();
		json.put(SicsChannel.JSON_KEY_INTERRUPT, "true");
		send(client, json.toString());
	}

	private void processRun(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String dev = parts[1];
			String target = parts[2];
			ISicsController controller = model.findController(dev);
			if (controller == null) {
				sendInternalError(client, cid, "device " + String.valueOf(dev) + " not found");
				return;
			}
			if (!(controller instanceof IDriveableController)) {
				sendInternalError(client, cid, "device " + String.valueOf(dev) + " is not moveable");
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
//				if (Math.abs(targetValue - currentValue) <= precision) {
//					json.put(SicsChannel.JSON_KEY_VALUE, currentValue);
//					json.put(SicsChannel.JSON_KEY_FINISHED, "true");
//					send(client, json.toString());
//					return;
//				}
			}
			
			setStatus(ServerStatus.DRIVING);
			json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.DRIVING);
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());

//			Thread.sleep(1000);
//			json = new JSONObject();
//			json.put(SicsChannel.JSON_KEY_VALUE, dev + " = " + String.valueOf(target));
//			send(client, json.toString());

//			Thread.sleep(1000);

			json = new JSONObject();
			json.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.STATE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, driveable.getPath());
			json.put(PropertyConstants.PROP_COMMAND_REPLY, ControllerState.BUSY);
			json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.DRIVING);
			publish(json.toString());
			
			Thread runThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					JSONObject jobj = new JSONObject();
					try {
						Thread.sleep(1000);
						driveable.updateModelValue(target);
						jobj.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.STATE);
						jobj.put(PropertyConstants.PROP_UPDATE_VALUE, driveable.getPath());
						jobj.put(PropertyConstants.PROP_UPDATE_NAME, ControllerState.IDLE);
						publish(jobj.toString());
						
						jobj = new JSONObject();
						jobj.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
						jobj.put(PropertyConstants.PROP_UPDATE_TYPE, MessageType.STATUS);
						jobj.put(PropertyConstants.PROP_UPDATE_NAME, driveable.getPath());
						jobj.put(PropertyConstants.PROP_COMMAND_REPLY, target);
						publish(jobj.toString());
					} catch (Exception e) {
					}
					
				}
			});
			runThread.start();
			
			json = new JSONObject();
			json.put(PropertyConstants.PROP_COMMAND_REPLY, "OK");
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
//			json.put(PropertyConstants.PROP_MESSAGE_TYPE, MessageType.UPDATE);
//			json.put(PropertyConstants.PROP_UPDATE_PATH, driveable.getPath());
//			json.put(PropertyConstants.PROP_UPDATE_VALUE, Float.valueOf(target));
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		} catch (SicsModelException e) {
			// TODO Auto-generated catch block
			sendInternalError(client, cid, "model exception");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			sendInternalError(client, cid, e.getMessage());
		} finally {
			setStatus(ServerStatus.EAGER_TO_EXECUTE);
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
			json.put(PropertyConstants.PROP_COMMAND_REPLY, xml);
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());
		} catch (JSONException | IOException e) {
			sendInternalError(client, cid, "json exception");
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
			send(client, json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		}
	}
	
	private void processStatus(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			json.put(SicsChannel.JSON_KEY_STATUS, status);
			publish(json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		}
		try {
			json = new JSONObject();
			json.put(PropertyConstants.PROP_COMMAND_REPLY, status);
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		}
	}
	
	private void processHistmem(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String prop = parts[1];
			String target = null;
			if (parts.length > 2) {
				target = parts[2];
			}

			if ("preset".equals(prop)) {
				if (target == null) {
					json = new JSONObject();
					json.put(PropertyConstants.PROP_COMMAND_REPLY, histmemServer.preset);
					json.put(SicsChannel.JSON_KEY_FINISHED, "true");
					json.put(SicsChannel.JSON_KEY_CID, cid);
					send(client, json.toString());
					return;
				}
			} else if ("start".equals(prop)) {
				json = new JSONObject();
				json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.COUNTING);
				publish(json.toString());
				json.put(SicsChannel.JSON_KEY_FINISHED, "true");
				json.put(SicsChannel.JSON_KEY_CID, cid);
				send(client, json.toString());
				return;
			} else if ("stop".equals(prop)) {
				json = new JSONObject();
				json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
				publish(json.toString());
				json.put(SicsChannel.JSON_KEY_FINISHED, "true");
				json.put(SicsChannel.JSON_KEY_CID, cid);
				send(client, json.toString());
				return;
			} else if ("pause".equals(prop)) {
				json = new JSONObject();
				json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.PAUSED);
				publish(json.toString());
				json.put(SicsChannel.JSON_KEY_FINISHED, "true");
				json.put(SicsChannel.JSON_KEY_CID, cid);
				send(client, json.toString());
				return;
			}
			sendInternalError(client, cid, "command not recognised");
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		}
	}
	
	private void processHset(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String path = parts[1];
			String target = parts[2];
			ISicsController controller = model.findController(path);
			if (controller == null) {
				sendInternalError(client, cid, "device " + String.valueOf(path) + " not found");
				return;
			}
			if (!(controller instanceof IDynamicController)) {
				sendInternalError(client, cid, "device " + String.valueOf(path) + " can not change");
				return;
			}
			IDynamicController dynamic = (IDynamicController) controller;
			
			dynamic.updateModelValue(target);
			json = new JSONObject();
			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.VALUE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
			json.put(PropertyConstants.PROP_COMMAND_REPLY, target);
			publish(json.toString());
			
			json = new JSONObject();
			json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.VALUE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
			json.put(PropertyConstants.PROP_COMMAND_REPLY, target);
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		} catch (Exception e) {
			sendInternalError(client, cid, e.getMessage());
		} finally {
			setStatus(ServerStatus.EAGER_TO_EXECUTE);
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
				send(client, json.toString());
				runBatch();
				json = new JSONObject();
			}
			json.put(SicsChannel.JSON_KEY_CID, cid);
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
			send(client, json.toString());
			
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		} catch (Exception e) {
			sendInternalError(client, cid, e.getMessage());
		} finally {
			setStatus(ServerStatus.EAGER_TO_EXECUTE);
		}
	}

	void runBatch() {
//		Thread thread = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
				batchStatus = BatchStatus.EXECUTING;
				JSONObject json = new JSONObject();
				try {
					json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.DRIVING);
					json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.STATE);
					json.put(PropertyConstants.PROP_COMMAND_REPLY, PropertyConstants.ControllerState.BUSY);
					json.put(PropertyConstants.PROP_UPDATE_NAME, "dm");
					publish(json.toString());
					Thread.sleep(1000);
					json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.WAIT);
					publish(json.toString());
					Thread.sleep(1000);
					json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.COUNTING);
					publish(json.toString());
					Thread.sleep(1000);
					json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
					publish(json.toString());
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
		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String path = parts[1];
			String target;
			ISicsController controller = model.findController(path);
			if (controller == null) {
				sendInternalError(client, cid, "device " + String.valueOf(path) + " not found");
				return;
			}
			if (!(controller instanceof IDynamicController)) {
				sendInternalError(client, cid, "device " + String.valueOf(path) + " doesn't have value");
				return;
			}
			IDynamicController dynamic = (IDynamicController) controller;
			
			target = String.valueOf(dynamic.getValue());
			
			json = new JSONObject();
			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.VALUE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
			json.put(PropertyConstants.PROP_COMMAND_REPLY, target);
			publish(json.toString());
			
			json = new JSONObject();
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.VALUE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
			json.put(PropertyConstants.PROP_COMMAND_REPLY, target);
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		} catch (Exception e) {
			sendInternalError(client, cid, e.getMessage());
		} finally {
		}
	}

	private void processPause(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String prop = parts[1];
			String target = null;
			if (parts.length > 2) {
				target = parts[2];
			}

			if ("on".equals(prop)) {
				status = ServerStatus.PAUSED;
				json = new JSONObject();
				json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.PAUSED);
				publish(json.toString());
				json.put(SicsChannel.JSON_KEY_FINISHED, "true");
				json.put(SicsChannel.JSON_KEY_CID, cid);
				send(client, json.toString());
				return;
			} else if ("off".equals(prop)) {
				status = ServerStatus.COUNTING;
				json = new JSONObject();
				json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.COUNTING);
				publish(json.toString());
				json.put(SicsChannel.JSON_KEY_FINISHED, "true");
				json.put(SicsChannel.JSON_KEY_CID, cid);
				send(client, json.toString());
				return;
			} 
			sendInternalError(client, cid, "command not recognised");
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		}
	}
	
	private void processGroupCommand(String client, String cid, String command) {
		JSONObject json = new JSONObject();
		try {
			String[] parts = command.split(" ");
			String path = parts[1];
			String target = parts[2];
			ISicsController controller = model.findController(path);
			if (controller == null) {
				sendInternalError(client, cid, "device " + String.valueOf(path) + " not found");
				return;
			}
			if (!(controller instanceof ICommandController)) {
				sendInternalError(client, cid, "device " + String.valueOf(path) + " is not runnable");
				return;
			}
			ICommandController dynamic = (ICommandController) controller;
			
			json = new JSONObject();
			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.STATE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
			json.put(PropertyConstants.PROP_COMMAND_REPLY, PropertyConstants.ControllerState.BUSY);
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());
			publish(json.toString());
			
			Thread.sleep(1000);
			
			json = new JSONObject();
			json.put(SicsChannel.JSON_KEY_STATUS, ServerStatus.EAGER_TO_EXECUTE);
			json.put(SicsChannel.JSON_KEY_FINISHED, "true");
			json.put(PropertyConstants.PROP_UPDATE_TYPE, PropertyConstants.MessageType.STATE);
			json.put(PropertyConstants.PROP_UPDATE_NAME, dynamic.getPath());
			json.put(PropertyConstants.PROP_COMMAND_REPLY, PropertyConstants.ControllerState.IDLE);
			publish(json.toString());
			json.put(SicsChannel.JSON_KEY_CID, cid);
			send(client, json.toString());
		} catch (JSONException e) {
			sendInternalError(client, cid, "json exception");
		} catch (Exception e) {
			sendInternalError(client, cid, e.getMessage());
		} finally {
			setStatus(ServerStatus.EAGER_TO_EXECUTE);
		}
	}
	
	private void sendInternalError(String client, String cid, String message) {
		send(client, "{ \"" + SicsChannel.JSON_KEY_ERROR + "\":\"internal error, " + message + "\","
				+ " \"" + SicsChannel.JSON_KEY_FINISHED + "\":\"true\","
				+ " \"" + SicsChannel.JSON_KEY_CID + "\":\"" + cid + "\" }");
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
		            
//		            System.out.println("command: " + command);
		            
		            JSONObject json = null;
		            try {
						json = new JSONObject(commandText);
		            	String command = json.getString(SicsChannel.JSON_KEY_COMMAND);
		            	String cid = json.getString(SicsChannel.JSON_KEY_CID);
		            	System.out.println(command);
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
						// TODO Auto-generated catch block
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
    	SimulationServer server = new SimulationServer(ConstantSetup.LOCAL_SERVER_ADDRESS, ConstantSetup.LOCAL_PUBLISHER_ADDRESS);
    	server.run();
    	
    	SimulationServer validator = new SimulationServer(ConstantSetup.VALIDATOR_SERVER_ADDRESS, ConstantSetup.VALIDATOR_PUBLISHER_ADDRESS);
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
	 */
	private void setStatus(ServerStatus status) {
		this.status = status;
	}
}