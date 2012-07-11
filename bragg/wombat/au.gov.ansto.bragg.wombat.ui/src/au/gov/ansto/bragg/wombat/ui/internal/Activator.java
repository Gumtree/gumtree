package au.gov.ansto.bragg.wombat.ui.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.nbi.ui.preference.PreferenceUtils;
import au.gov.ansto.bragg.wombat.ui.views.WombatDataSourceView;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.wombat.ui";
	private int saveCount;
	private List<ResourceChangeListener> resourceChangeListeners = new ArrayList<ResourceChangeListener>();

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		addListeners();
	}

	private void addListeners(){
		final ISicsController sics = SicsCore.getSicsController();
		if (sics != null){
			final IDynamicController filenameNode = (IDynamicController) sics.findComponentController(
					WombatDataSourceView.FILENAME_NODE_PATH);
//			final IDynamicController currentPointNode = (IDynamicController) sics.findComponentController(
//					WombatDataSourceView.CURRPOINT_NODE_PATH);
			//		final IDynamicController currentPointNode = (IDynamicController) sics.findComponentController(
			//				CURRENT_POINT_PATH);
			IDynamicController saveCountNode = (IDynamicController) sics.findComponentController(
					WombatDataSourceView.SAVE_COUNT_PATH);
			//		for (IDynamicControllerListener listener : statusListeners)
			//			saveCountNode.removeComponentListener(listener);
			//		statusListeners.clear();
			try{
				saveCount = saveCountNode.getValue().getIntData();
			}catch (Exception e) {
			}
			IDynamicControllerListener statusListener = new DynamicControllerListenerAdapter() {
				public void valueChanged(IDynamicController controller, final IComponentData newValue) {
					int newCount = Integer.valueOf(newValue.getStringData());
					if(newCount != saveCount) {
						saveCount = newCount;
						try{
							File checkFile = new File(filenameNode.getValue().getStringData());
							String dataPath = System.getProperty("sics.data.path");
							checkFile = new File(dataPath + "/" + checkFile.getName());
							final String filePath = checkFile.getAbsolutePath();
							if (!checkFile.exists()){
								String errorMessage = "The target file :" + checkFile.getAbsolutePath() + 
								" can not be found";
//								throw new FileNotFoundException(errorMessage);
								LoggerFactory.getLogger(this.getClass()).error(errorMessage);
								return;
							}
							Display.getDefault().asyncExec(new Runnable() {

								public void run() {
//									boolean isFileSelected = DataSourceManager.getSelectedFile() 
//										== DataSourceManager.getInstance().getDataSourceFile(filePath);
//									AlgorithmTask task = ProjectManager.getCurrentAlgorithmTask();
//									final Operation operation = task.getOperationManager(0).getOperation(NAVIGATION_PROCESSOR_NAME);
//									final Tuner currentStepIndexTuner = ((ProcessorAgent) operation.getAgent()).getTuner(CURRENT_INDEX_TUNER_NAME);
//									try {
//										currentStepIndexTuner.setSignal(currentPointNode.getValue().getIntData() + 1);
//									} catch (Exception e) {
//										e.printStackTrace();
//									} 
									String newFilePath = PreferenceUtils.getUserDirectoryPreference();
									File oldFile = new File(filePath);
									newFilePath += "\\" + oldFile.getName();
									if (NexusUtils.copyfile(filePath, newFilePath)){
										System.out.println(newFilePath);
										for (ResourceChangeListener listener: resourceChangeListeners){
											listener.fileAdded(newFilePath);
										}
//										dataSourceComposite.removeFile(newFilePath);
//										dataSourceComposite.addFile(filePath, true, 0);
//										dataSourceComposite.refresh();
									}
								}
							});
						}catch (Exception e) {
							LoggerFactory.getLogger(this.getClass()).error("can not read new file", e);
						}
					}
				}
			};
			saveCountNode.addComponentListener(statusListener);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (InternalImage.isInstalled()) {
			InternalImage.dispose();
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public interface ResourceChangeListener{
		public void fileAdded(String filename);
	}
	
	public void addResourceChangeListener(ResourceChangeListener listener){
		resourceChangeListeners.add(listener);
	}
	
	public void removeResourceChangeListener(ResourceChangeListener listener){
		resourceChangeListeners.remove(listener);
	}
}
