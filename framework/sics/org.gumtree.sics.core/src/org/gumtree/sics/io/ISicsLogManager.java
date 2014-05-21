/**
 * 
 */
package org.gumtree.sics.io;

/**
 * @author nxi
 *
 */
public interface ISicsLogManager {

	enum LogType{TERTIARY, SECONDARY, STATUS}
	
	public void log(LogType type, String text);
	
	public void close();
	
//	public void open();
}
