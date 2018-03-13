package org.gumtree.control.batch;

public interface IBatchScript {

	public String getName();
	
	public void setName(String name);

	public String getContent();
	
//	public void setContent(String content);
	
	public Object getSource();
	
	public void setSource(Object source);
	
	public void setTimeEstimation(int seconds);
	
	public int getTimeEstimation();
}
