/**
 * 
 */
package au.gov.ansto.bragg.nbi.scripting;

/**
 * @author nxi
 *
 */
public class ScriptObjectTab extends ScriptObjectGroup {

	public ScriptObjectTab(String name) {
		super(name);
	}


//	@Override
//	public String getHtml() {
//		String html = getBeginHtml();
//		int index = 0;
//		html += "<tr>";
//		for (IPyObject control : objectList) {
//			html += control.getHtml();
//			index += control.getColSpan();
//			if (index % getNumColumns() == 0) {
//				html += "</tr>";
//				if (index <= objectList.size()){
//					html += "<tr>";
//				}
//			}
//		}
//		if ((index - 1) % getNumColumns() != 0) {
//			html += "</tr>";
//		}
//		return html + getEndHtml();
//	}
//	
//	public String getBeginHtml(){
//		return "<td colspan=\"" + getColSpan() * 2 + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_group\""  
//					+ "><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"scrollTable\">"
//                    + "<tbody class=\"table_jython_content\" id=\"table_jason_group\">"
//                    + "<tr><td colspan=\"" + getNumColumns() * 2 + "\"><div class=\"table_jython_group_header_td\">" 
//                    + getTitle() + "</div></td></tr>";
//	}
//	
//	public String getEndHtml(){
//		return "</tbody></table></div></td>";
//	}

	@Override
	public PyObjectType getObjectType() {
		return PyObjectType.TAB;
	}
}
