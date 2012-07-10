package org.gumtree.gumnix.sics.simulator.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

import org.gumtree.gumnix.sics.simulator.objects.commands.ContextDo;
import org.gumtree.gumnix.sics.simulator.objects.commands.Exe;
import org.gumtree.gumnix.sics.simulator.objects.commands.GetGumTreeXML;
import org.gumtree.gumnix.sics.simulator.objects.commands.HGet;
import org.gumtree.gumnix.sics.simulator.objects.commands.HNotify;
import org.gumtree.gumnix.sics.simulator.objects.commands.HSet;
import org.gumtree.gumnix.sics.simulator.objects.commands.Protocol;
import org.gumtree.gumnix.sics.simulator.objects.commands.Statemon;
import org.gumtree.gumnix.sics.simulator.objects.commands.Status;
import org.gumtree.gumnix.sics.simulator.services.ISicsObjectLibrary;
import org.gumtree.gumnix.sics.simulator.services.SicsConnection;
import org.gumtree.gumnix.sics.simulator.services.SicsObjectLibrary;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsSimulationServer implements ISicsSimulationServer {

	private static final String FILE_CONFIG = "config.txt";

	private static ISicsSimulationServer singleton;

	private ServerSocket serverSocket;

	private Thread serverSocketThread;

	private JSONObject properties;

	private ISicsObjectLibrary objectLibrary;

	private int connectionCount;

	private Logger logger;

	public SicsSimulationServer(String filename) {
		singleton = this;
		connectionCount = 1;
		initialiseProperties(filename);
		initialiseObjectLibrary();
		prepareNetworkConnection();
		// Stress test: start test thread
//		StressTestThread testThread = new StressTestThread();
//		testThread.start();
	}

	public static ISicsSimulationServer getDefault() {
		return singleton;
	}

	private void initialiseProperties(String filename) {
		getLogger().debug("initialiseProperties");
		try {
			BufferedReader reader = null;
			File file = new File(filename);
			if (!file.exists()) {
				reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResource(FILE_CONFIG).openStream()));
			} else { 
				reader = new BufferedReader(new FileReader(file));
			}
			StringBuilder builder = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
			properties = new JSONObject(builder.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initialiseObjectLibrary() {
		getLogger().debug("initialiseObjectLibrary");
		objectLibrary = new SicsObjectLibrary();
		objectLibrary.addSicsObject(new Protocol());
		objectLibrary.addSicsObject(new GetGumTreeXML());
		objectLibrary.addSicsObject(new ContextDo());
		objectLibrary.addSicsObject(new HNotify());
		objectLibrary.addSicsObject(new Statemon());
		objectLibrary.addSicsObject(new HGet());
		objectLibrary.addSicsObject(new HSet());
		objectLibrary.addSicsObject(new Exe());
		objectLibrary.addSicsObject(new Status());
	}

	private void prepareNetworkConnection() {
		getLogger().debug("prepareNetworkConnection");
		connectionCount = 0;
		 boolean tryListen = true;
	        while(tryListen) {
	        	int serverPort = 0;
	            try {
	            	serverPort = getProperties().getJSONObject("config").getInt("serverPort");
	                serverSocket = new ServerSocket(serverPort);
	                serverSocketThread = new Thread() {
	                    public void run() {
	                        startServer();
	                    }
	                };
	                // starts listening to server port
	                serverSocketThread.start();
	                tryListen = false;
	            } catch (IOException e) {
	                System.err.println("Could not listen on port: " + serverPort);
	            } catch (JSONException e) {
					e.printStackTrace();
				}
	        }
	        tryListen = true;
	}

	private void startServer() {
		while (true) {
			try {
				getLogger().debug("Waiting for connection");
				// start new thread to listen on the port
				new SicsConnection(serverSocket.accept(), connectionCount++);
				getLogger().debug("Accept new connection");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public JSONObject getProperties() {
		return properties;
	}

	public ISicsObjectLibrary getObjectLibrary() {
		return objectLibrary;
	}

	public Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger("SicsSimulationServer");
		}
		return logger;
	}

	public static void main(String[] args) {
//		DOMConfigurator.configure("log4j.xml");
		if(args.length > 0) {
			new SicsSimulationServer(args[0]);
		} else {
			new SicsSimulationServer(FILE_CONFIG);
		}
	}

}
