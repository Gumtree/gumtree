package org.gumtree.control.test.server.exp;

import org.zeromq.ZMQ;

public class Client {

	private ZMQ.Socket subscriberSocket;
	private ZMQ.Context context;
	private String id;

	public Client() {

		id = String.valueOf(System.currentTimeMillis());
		context = ZMQ.context(1);
		subscriberSocket = context.socket(ZMQ.DEALER);
		subscriberSocket.connect("tcp://localhost:5556");
		subscriberSocket.setIdentity(id.getBytes(ZMQ.CHARSET));
//		subscriberSocket.subscribe("m".getBytes(ZMQ.CHARSET));
//		subscriberSocket.subscribe("");
	}
	
	public void run() throws InterruptedException {

		System.out.println("Simulation client " + id + " started");
		while (!Thread.currentThread().isInterrupted()) {

			subscriberSocket.send("I am " + id);
			String reply = subscriberSocket.recvStr(0);
			System.out.println(reply);
			Thread.sleep(1000);
		}
		subscriberSocket.close();
		context.term();
	}

	public static void main(String[] args) throws Exception
    {
		Client client = new Client();
    	client.run();

    }
}