package org.gumtree.gumnix.sics.simulator.objects.commands;

import java.util.List;

import org.gumtree.gumnix.sics.simulator.objects.IStatemon;
import org.gumtree.gumnix.sics.simulator.objects.SicsCommand;
import org.gumtree.gumnix.sics.simulator.server.SicsSimulationServer;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;
import org.gumtree.gumnix.sics.simulator.services.SicsOutput;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection.Flag;

public class Exe extends SicsCommand {

	private static final String ID = "exe";

	private int numberOfLine;

	public Exe() {
		super(ID);
		numberOfLine = 0;
	}

	public void processCommand(List<String> args, ISicsConnection conn, int contextId) {
		if(args.size() == 1) {
			if(args.get(0).equals("clear")) {
				conn.write(new SicsOutput(contextId, ID, Flag.status, "OK"));
			} else if(args.get(0).equals("upload")) {
				conn.write(new SicsOutput(contextId, ID, Flag.status, "OK"));
			} else if(args.get(0).equals("run")) {
				IStatemon statemon = (IStatemon)SicsSimulationServer.getDefault().getObjectLibrary().getSicsObject(IStatemon.ID);
				statemon.interestCallback("STARTED = exe xxx.tcl");
				for(int i = 0; i < numberOfLine; i++) {
					conn.write(new SicsOutput(contextId, "gumput", Flag.error, (i + 1) + ""));
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				statemon.interestCallback("FINISH = exe xxx.tcl");
			}
		} else if(args.size() >= 2) {
			if(args.get(0).equals("append")) {
				if(args.size() == 3) {
					if(args.get(1).equals("gumput")) {
						try {
							numberOfLine = Integer.parseInt(args.get(2));
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
				conn.write(new SicsOutput(contextId, ID, Flag.status, "OK"));
			} else if(args.get(0).equals("forcesave")) {
				conn.write(new SicsOutput(contextId, ID, Flag.status, "OK"));
			} else if(args.get(0).equals("enqueue")) {
				conn.write(new SicsOutput(contextId, ID, Flag.status, "OK"));
			}
		}
	}

}
