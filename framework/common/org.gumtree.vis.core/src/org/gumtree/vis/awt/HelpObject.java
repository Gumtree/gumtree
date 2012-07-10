/**
 * 
 */
package org.gumtree.vis.awt;

/**
 * @author nxi
 *
 */
public class HelpObject {

	private String name;
	
	private String key;
	
	private String discription;

	public HelpObject(String name) {
		this.name = name;
	}
	
	public HelpObject(String name, String key, String discription) {
		this.name = name;
		this.key = key;
		this.discription = discription;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDiscription() {
		return discription;
	}

	public void setDiscription(String discription) {
		this.discription = discription;
	}
	
	
}
