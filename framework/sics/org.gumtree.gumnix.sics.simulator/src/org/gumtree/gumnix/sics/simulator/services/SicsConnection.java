package org.gumtree.gumnix.sics.simulator.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gumtree.gumnix.sics.simulator.objects.ISicsObject;
import org.gumtree.gumnix.sics.simulator.objects.commands.Connection;
import org.gumtree.gumnix.sics.simulator.server.SicsSimulationServer;

public class SicsConnection implements ISicsConnection {

	private int connectionId;

	private Connection con;

	private ISicsPrototocol protocol;

	private Thread exec;

	private Socket socket;

	private ConnectionState state;

	private int internalContextId;

	private PrintWriter out;

	public SicsConnection(Socket socket, int connectionId) {
		this.connectionId = connectionId;
		this.socket = socket;
		con = new Connection("con" + connectionId, this);
		protocol = new NormalProtocol();
		internalContextId = 1;
		setConnectionState(ConnectionState.LOGIN);
		SicsSimulationServer.getDefault().getObjectLibrary().addSicsObject(con);
		listenInput();
	}

	public int getConnectionId() {
		return connectionId;
	}

//	 Listen to TCP/IP
	private void listenInput() {
		exec = new Thread() {
			public void run() {
				try {
					out = new PrintWriter(socket.getOutputStream(), true);
					out.println("OK");
					BufferedReader in = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						processCommand(inputLine);
					}
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
		};
		exec.start();
	}

	public ISicsPrototocol getProtocol() {
		return protocol;
	}

	public void setProtocol(ISicsPrototocol protocol) {
		this.protocol = protocol;
	}

	public ConnectionState getConnectionState() {
		return state;
	}

	private void setConnectionState(ConnectionState state) {
		this.state = state;
	}

	public void write(ISicsOutput output) {
		String outputString = getProtocol().formatOutput(getConnectionId(), output);
		out.println(outputString);
	}

	public void processCommand(String line) {
		line = line.trim();
		SicsSimulationServer.getDefault().getLogger().debug("Accepted input: " + line);
		String[] tokens = line.split("\\s+");
		if(tokens.length > 0) {
			if(getConnectionState().equals(ConnectionState.LOGIN)) {
				if(tokens.length == 2) {
					setConnectionState(ConnectionState.NORMAL);
					out.println("Login OK");
					return;
				}
			} else if(getConnectionState().equals(ConnectionState.NORMAL)) {
				String objectId = tokens[0];
				ISicsObject sicsObject = SicsSimulationServer.getDefault().getObjectLibrary().getSicsObject(objectId);
				if(sicsObject != null) {
					List<String> args = new ArrayList<String>(Arrays.asList(tokens));
					args.remove(0);
					SicsSimulationServer.getDefault().getLogger().debug("Passed input to object " + sicsObject.getId());
					sicsObject.processCommand(args, this, internalContextId++);
				}
			}
		}
	}

}
