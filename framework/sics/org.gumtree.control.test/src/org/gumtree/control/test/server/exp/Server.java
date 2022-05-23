package org.gumtree.control.test.server.exp;

import org.zeromq.ZMQ;

public class Server {

	private ZMQ.Socket publishSocket;
	private ZMQ.Context context;

	public Server() {

		context = ZMQ.context(1);
		publishSocket = context.socket(ZMQ.ROUTER);
		publishSocket.bind("tcp://*:5556");

	}
	
	public void run() throws InterruptedException {

		System.out.println("Simulation server started");
		while (!Thread.currentThread().isInterrupted()) {

			String cid = publishSocket.recvStr();
			System.out.println("receive " + cid);
			publishSocket.sendMore(cid);
			publishSocket.send("message" + " for " + cid, 0);
//			Thread.sleep(1000); 
		}
		publishSocket.close();
		context.term();
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server();
    	server.run();

    }
}