/**
 * 
 */
package au.gov.ansto.bragg.nbi.scripting;

/**
 * @author nxi
 *
 */
public interface IPyObject {

	public enum PyObjectType {
		PAR, ACT, GROUP, TAB;
	}
	
	public PyObjectType getObjectType();
	
	public String getName();
	
	public void setName(String name);
	
	public String getHtml();
	
	public String getInitJs();
	
	public String getEventJs(String property);
	
	public int getColSpan();
	
	public int getRowSpan();
	
	public int getId();
}
