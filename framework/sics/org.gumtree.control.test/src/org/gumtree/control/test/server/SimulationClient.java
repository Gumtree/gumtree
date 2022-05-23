package org.gumtree.control.test.server;

import org.gumtree.control.test.ConstantSetup;
import org.zeromq.ZMQ;

public class SimulationClient {

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);

        String clientId = String.valueOf(System.currentTimeMillis());
        //  Socket to talk to server
        System.out.println("Connecting to hello world server");

        ZMQ.Socket socket = context.socket(ZMQ.DEALER);
        socket.connect(ConstantSetup.LOCAL_SERVER_ADDRESS);
        socket.setIdentity(clientId.getBytes(ZMQ.CHARSET));

        for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
        	
            String request = "manager ansto";
            System.out.println("Sending " + request);
//            socket.send(request.getBytes(ZMQ.CHARSET), 0);
            socket.send(request);
//            byte[] reply = socket.recv(0);
            String reply = socket.recvStr();
            System.out.println("Received " + reply + " " + requestNbr);

            request = "drive dummy_motor 1";
            System.out.println("Sending " + request);
            socket.send(request.getBytes(ZMQ.CHARSET), 0);
            reply = socket.recvStr();
            System.out.println("Received " + reply + " " + requestNbr);
            reply = socket.recvStr();
            System.out.println("Received " + reply + " " + requestNbr);
            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        socket.close();
        context.term();
    }
}