/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IWorkbenchPage;
import org.gumtree.data.ui.part.PlotView;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;

import au.gov.ansto.bragg.nbi.scripting.ScriptModel;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ScriptControlViewer;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ScriptDataSourceViewer;


/**
 * @author nxi
 *
 */
public class ScriptPageRegister {

	private static Map<Integer, ScriptPageRegister> registerTray = 
			new HashMap<Integer, ScriptPageRegister>();
	
	private IWorkbenchPage workbenchPage;
	private ScriptControlViewer controlViewer;
	private ScriptDataSourceViewer dataSourceViewer;
	private ICommandLineViewer consoleViewer;
	private PlotView plot1;
	private PlotView plot2;
	private PlotView plot3;
	private List<PlotView> plotList;
	private ScriptModel scriptModel;
	
	public ScriptPageRegister() {
		plotList = new ArrayList<PlotView>();
	}

	public ScriptControlViewer getControlViewer() {
		return controlViewer;
	}

	public void setControlViewer(ScriptControlViewer controlViewer) {
		this.controlViewer = controlViewer;
	}

	public ScriptDataSourceViewer getDataSourceViewer() {
		return dataSourceViewer;
	}

	public void setDataSourceViewer(ScriptDataSourceViewer dataSourceViewer) {
		this.dataSourceViewer = dataSourceViewer;
	}

	public ICommandLineViewer getConsoleViewer() {
		return consoleViewer;
	}

	public void setConsoleViewer(ICommandLineViewer consoleViewer) {
		this.consoleViewer = consoleViewer;
	}

	public List<PlotView> getPlotList() {
		return plotList;
	}

	public void setPlotList(List<PlotView> plotList) {
		this.plotList = plotList;
	}

	public PlotView getPlot1() {
		return plot1;
	}

	public void setPlot1(PlotView plot) {
		this.plot1 = plot;
	}

	public PlotView getPlot2() {
		return plot2;
	}

	public void setPlot2(PlotView plot) {
		this.plot2 = plot;
	}

	public PlotView getPlot3() {
		return plot3;
	}

	public void setPlot3(PlotView plot) {
		this.plot3 = plot;
	}

	public static void registPage(int registerID, ScriptPageRegister register) {
		registerTray.put(registerID, register);
	}
	
	public static ScriptPageRegister getRegister(int registerID) {
		return registerTray.get(registerID);
	}

	/**
	 * @return the scriptModel
	 */
	public ScriptModel getScriptModel() {
		return scriptModel;
	}

	/**
	 * @param scriptModel the scriptModel to set
	 */
	public void setScriptModel(ScriptModel scriptModel) {
		this.scriptModel = scriptModel;
	}

	public IWorkbenchPage getWorkbenchPage() {
		return workbenchPage;
	}

	public void setWorkbenchPage(IWorkbenchPage workbenchPage) {
		this.workbenchPage = workbenchPage;
	}
	
	
}
