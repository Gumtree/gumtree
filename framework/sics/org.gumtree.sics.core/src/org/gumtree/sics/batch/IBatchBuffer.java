package org.gumtree.sics.batch;

public interface IBatchBuffer {

	public String getName();
	
	public void setName(String name);

	public String getContent();
	
//	public void setContent(String content);
	
	public Object getSource();
	
	public void setSource(Object source);
	
}
