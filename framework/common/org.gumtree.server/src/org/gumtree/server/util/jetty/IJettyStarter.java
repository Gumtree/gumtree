package org.gumtree.server.util.jetty;

public interface IJettyStarter {

	public void init();
	
	public void cleanup();
	
	public int getPort();
	
	public void setPort(int port);
	
	public boolean isEnable();
	
	public void setEnable(boolean enable);
	
}
