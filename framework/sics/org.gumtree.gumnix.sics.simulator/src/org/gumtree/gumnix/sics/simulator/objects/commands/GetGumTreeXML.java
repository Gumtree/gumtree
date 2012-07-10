package org.gumtree.gumnix.sics.simulator.objects.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.gumtree.gumnix.sics.simulator.objects.SicsCommand;
import org.gumtree.gumnix.sics.simulator.server.SicsSimulationServer;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;
import org.gumtree.gumnix.sics.simulator.services.SicsOutput;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection.Flag;
import org.json.JSONException;

public class GetGumTreeXML extends SicsCommand {

	private static final String ID = "getgumtreexml";

	public GetGumTreeXML() {
		super(ID);
	}

	public void processCommand(List<String> args, ISicsConnection conn, int contextId) {
		if(args.size() == 1) {
			if(args.get(0).equals("/")) {
				conn.write(new SicsOutput(contextId, ID, Flag.status, getXML()));
			}
		}
	}

	private String getXML() {
		StringBuilder builder = new StringBuilder();
		try {
			String filename = SicsSimulationServer.getDefault().getProperties().getJSONObject("config").getString("hipadabaModel");
			BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
			String line = null;
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

}
