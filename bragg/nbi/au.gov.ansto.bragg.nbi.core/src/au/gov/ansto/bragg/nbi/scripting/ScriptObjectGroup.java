/**
 * 
 */
package au.gov.ansto.bragg.nbi.scripting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nxi
 *
 */
public class ScriptObjectGroup extends PyObjectImp {

	public static final String PROP_NUM_COLUMNS_NAME = "numColumns";
	private List<IPyObject> objectList = new ArrayList<IPyObject>();
	
	public ScriptObjectGroup(String name) {
		setName(name);
	}

	/**
	 * @return the objectList
	 */
	public List<IPyObject> getObjectList() {
		return objectList;
	}

	public void addObject(IPyObject object) {
		objectList.add(object);
	}

	public void removeObject(IPyObject object) {
		objectList.remove(object);
	}
	
	public IPyObject getObject(String objName) {
		for (IPyObject obj : objectList) {
			if (obj.getName().equals(objName)) {
				return obj;
			}
		}
		return null;
	}

	@Override
	public String getHtml() {
		String html = getBeginHtml();
		int index = 1;
		html += "<tr>";
		for (IPyObject control : objectList) {
			html += control.getHtml();
			if (index % getNumColumns() == 0) {
				html += "</tr>";
				if (index <= objectList.size()){
					html += "<tr>";
				}
			}
			index += 1;
		}
		if ((index - 1) % getNumColumns() != 0) {
			html += "</tr>";
		}
		return html + getEndHtml();
	}
	
	public String getBeginHtml(){
		return "<td colspan=\"" + getColSpan() * 2 + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_group\""  
					+ "><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"scrollTable\">"
                    + "<tbody class=\"table_jython_content\" id=\"table_jason_group\">"
                    + "<tr><td colspan=\"" + getNumColumns() * 2 + "\"><div class=\"table_jython_group_header_td\">" + getTitle() + "</div></td></tr>";
	}
	
	public String getEndHtml(){
		return "</tbody></table></div></td>";
	}

	@Override
	public String getInitJs() {
		return null;
	}

	@Override
	public String getEventJs(String property) {
		return null;
	}
	
	public int getNumColumns(){
		if (getProperty(PROP_NUM_COLUMNS_NAME) != null){
			return Integer.valueOf(getProperty(PROP_NUM_COLUMNS_NAME));
		}
		return 1;
	}
}
