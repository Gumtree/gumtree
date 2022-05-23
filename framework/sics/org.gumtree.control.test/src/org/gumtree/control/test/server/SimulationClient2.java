package org.gumtree.control.test.server;

import org.gumtree.control.test.ConstantSetup;
import org.zeromq.ZMQ;

public class SimulationClient2 {

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);

        //  Socket to talk to server
        System.out.println("Connecting to hello world server");

        ZMQ.Socket subscriber = context.socket(ZMQ.SUB);
        subscriber.connect(ConstantSetup.LOCAL_PUBLISHER_ADDRESS);

        for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
        	
//            byte[] reply = subscriber.recv(0);
//            System.out.println("Received " + new String(reply, ZMQ.CHARSET) + " " + requestNbr);
        	String reply = subscriber.recvStr(0);
        	System.out.println(reply);
        }

        subscriber.close();
        context.term();
    }
}