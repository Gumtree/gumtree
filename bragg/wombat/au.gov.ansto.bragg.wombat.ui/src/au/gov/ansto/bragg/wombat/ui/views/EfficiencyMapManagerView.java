/**
 * 
 */
package au.gov.ansto.bragg.wombat.ui.views;

import java.net.URI;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.ui.util.SafeUIRunner;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.Algorithm.AlgorithmStatus;
import au.gov.ansto.bragg.cicada.core.AlgorithmStatusListener;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOMFactory;
import au.gov.ansto.bragg.kakadu.core.data.DataType;
import au.gov.ansto.bragg.kakadu.ui.plot.Plot;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataItem;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotException;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotType;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kakadu.ui.views.ParameterControlViewer;
import au.gov.ansto.bragg.wombat.ui.internal.WombatAnalysisPerspective;

/**
 * @author nxi
 *
 */
public class EfficiencyMapManagerView extends ViewPart {

	public static final String NAME_ALGORITHM_EFFICIENCY_CREATOR = "Efficiency Map Producer";
	private static final String NAME_TUNER_NEW_MAP_URI = "frame_saveURI";
	protected ParameterControlViewer viewer;
	protected Algorithm efficiencyCreator;
	protected CicadaDOM cicada;
	protected Plot plot;
	//	private boolean isInitilised = false;
	private PlotDataItem plotDataItem;

	/**
	 * 
	 */
	public EfficiencyMapManagerView() {
		cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
		try {
			efficiencyCreator = cicada.loadAlgorithm(NAME_ALGORITHM_EFFICIENCY_CREATOR);
		} catch (Exception e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(final Composite parent) {
		if (efficiencyCreator != null){
			viewer = new ParameterControlViewer(parent, SWT.NONE);
			viewer.setAlgorithm(efficiencyCreator);
			GridLayoutFactory.fillDefaults().applyTo(viewer);
			GridDataFactory.fillDefaults().minSize(700, 0).grab(true, true).align(SWT.LEFT, SWT.TOP).applyTo(viewer);
//			viewer.layout();
		}
		plot = new Plot(parent, SWT.NONE);
		plot.setBackground(viewer.getFormBody().getBackground());
		parent.redraw();
		if (efficiencyCreator != null){
			efficiencyCreator.addStatusListener(new AlgorithmStatusListener() {

				@Override
				public void setStage(int operationIndex, AlgorithmStatus status) {
				}

				@Override
				public void onStatusChanged(AlgorithmStatus status) {
					if (status == AlgorithmStatus.End){
						IGroup result = null;
						try {
							result = (IGroup) efficiencyCreator.getSinkList().get(0).getSignal();
						} catch (Exception e) {
						}
						if (result != null){
							if (plotDataItem == null){
								plotDataItem = new PlotDataItem(result, DataType.Map);
								SafeUIRunner.asyncExec(new SafeRunnable() {

									@Override
									public void run() throws Exception {
										plot.init(PlotType.IntensityPlot);
										plot.layout();
										plot.getMultiPlotDataManager().addPlotDataItem(plotDataItem);
									}
								});
							} else{
								int idx = plot.getMultiPlotDataManager().getPlotDataItems().indexOf(plotDataItem);
								if (idx < 0){
									try {
										plot.getMultiPlotDataManager().addPlotDataItem(plotDataItem);
									} catch (PlotException e) {
										e.printStackTrace();
									}
								}
								else
									plot.getMultiPlotDataManager().updatePlotDataContents(plotDataItem, result);
							}
							informAlgorithm();
						}
					}					
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}

	protected void informAlgorithm(){
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			if (windows[i].getActivePage() == null)
				continue;
			IViewReference[] views = windows[i].getActivePage().getViewReferences();
			for (int j = 0; j < views.length; j++) {
				if (views[j].getId().equals(WombatAnalysisPerspective.ANALYSIS_PARAMETERS_VIEW_ID)){
					IViewPart view = views[j].getView(false);
					if (view instanceof WombatAnalysisControlView){
						try{
						URI newMapUri = (URI) efficiencyCreator.getTuner(NAME_TUNER_NEW_MAP_URI).getSignal();
						if (newMapUri != null)
							((WombatAnalysisControlView) view).updateEfficiencyFolderContents(newMapUri);
						}catch (Exception e) {
							Util.handleException(getSite().getShell(), e);
						}
					}
				}
			}
		}
	}

}
