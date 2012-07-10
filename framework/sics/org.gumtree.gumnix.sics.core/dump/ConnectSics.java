package org.gumtree.gumnix.sics.internal.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.sdo.EDataGraph;
import org.eclipse.emf.ecore.sdo.util.SDOUtil;
import org.gumtree.gumnix.sics.core.io.ISicsData;
import org.gumtree.gumnix.sics.core.io.ISicsProxy;
import org.gumtree.gumnix.sics.core.io.ISicsProxyContext;
import org.gumtree.gumnix.sics.core.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.core.io.ISycamoreResponse;
import org.gumtree.gumnix.sics.core.io.SicsIOException;
import org.gumtree.gumnix.sics.internal.core.io.SicsProxy;
import org.gumtree.gumnix.sics.internal.core.io.SicsProxyContext;
import org.gumtree.gumnix.sics.internal.core.io.SicsProxyListenerAdapter;

import ch.psi.sics.hipadaba.Instrument;
import ch.psi.sics.hipadaba.impl.HipadabaPackageImpl;

public class ConnectSics {
	
	private static Logger logger = Logger.getLogger(ConnectSics.class);
	
	private static int TIME_OUT = 5000;	
	
	private static int WAIT_TIME = 50;
	
	private static Instrument deserialise(byte[] data) throws IOException {
		// Similar to SDOUtil.loadDataGraph(InputStream, Map), but it needs
		// to register ProtocolPackage for XML deserialisation
		// see: http://www.devx.com/Java/Article/29093/1954?pf=true
		ResourceSet resourceSet = SDOUtil.createResourceSet();
		resourceSet.getPackageRegistry().put(HipadabaPackageImpl.eNS_URI,
				HipadabaPackageImpl.eINSTANCE);
		Resource resource = resourceSet.createResource(URI
				.createURI("all.datagraph"));
		resource.load(new ByteArrayInputStream(data), null);
		EDataGraph dataGraph = (EDataGraph) resource.getContents().get(0);
		EObject root = dataGraph.getERootObject();
		if (root instanceof Instrument) {
			return (Instrument) root;
		} else {
			return null;
		}
	}
	
	private static void sendCommand(ISicsProxy proxy, String command, ISicsProxyListener listener) {
		try {
			proxy.send(command, listener);
			int counter = 0;
			while(!listener.isListenerCompleted()) {
				try {
					Thread.sleep(WAIT_TIME);
					counter += WAIT_TIME;
					if(WAIT_TIME > TIME_OUT) {
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (SicsIOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		DOMConfigurator.configure("log4j.xml");
		Properties properties = new Properties();
		try {
			FileInputStream input = new FileInputStream(new File(
					"localhostConfig.xml"));
			properties.loadFromXML(input);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException", e);
		} catch (InvalidPropertiesFormatException e) {
			logger.error("InvalidPropertiesFormatException", e);
		} catch (IOException e) {
			logger.error("IOException", e);
		}
		ISicsProxyContext context = new SicsProxyContext(properties);
		ISicsProxy proxy = new SicsProxy(context);
		try {
			logger.info("Initialising...");
			proxy.initialise();
			logger.info("Initialised");
		} catch (SicsIOException e) {
			e.printStackTrace();
		}
		ISicsProxyListener listener = new SicsProxyListenerAdapter() {
			public void receiveReply(ISycamoreResponse response) {
//				ISicsData[] data = response.getMessageValues();
//				System.out.println(data[0].getValue());
//				System.out.println(response.getMessage());
				String content = response.getMessage().substring(9, response.getMessage().length() - 1);
				System.out.println(content);
				byte[] data = content.getBytes();
				try {
					Instrument instrument = deserialise(data);
					logger.info("instrument.getLabel() = " + instrument.getLabel());
					System.out.println(instrument.getLabel());
				} catch (IOException e) {
					e.printStackTrace();
				}
				setListenerCompleted(true);
			}
		};
		sendCommand(proxy, "xhlist -xml", listener);
		listener = new SicsProxyListenerAdapter() {
			public void receiveReply(ISycamoreResponse response) {
				ISicsData[] data = response.getMessageValues();
				logger.info("hget /slits/2/ss2u/position = " + data[0].getValue());
			}
		};
		sendCommand(proxy, "hget /slits/2/ss2u/position", listener);
		logger.info("Test completed");
	}
}
