package au.gov.ansto.bragg.koala.ui;

import java.io.IOException;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.service.multimonitor.support.MultiMonitorManager;
import org.gumtree.ui.util.SafeUIRunner;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.koala.ui"; //$NON-NLS-1$

	public static final String NAME_PROP_ID = "koala.propId";
	public static final String NAME_USER_NAME = "koala.userName";
	public static final String NAME_LOCAL_SCI = "koala.localSci";
	public static final String NAME_OP_MODE = "koala.operationMode";
	
	
	// The shared instance
	private static Activator plugin;
	
	private static Font fontLarge;
	private static Font fontMiddle;
	private static Cursor handCursor;
	private static Cursor busyCursor;
	private static Cursor defaultCursor;
	private static Color lightColor;
	private static Color highlightColor;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		SafeUIRunner.syncExec(new ISafeRunnable() {
			public void handleException(Throwable exception) {
			}
			@SuppressWarnings("restriction")
			public void run() throws Exception {
				Display currentDisplay = getWorkbench().getDisplay();
				Font systemFont = currentDisplay.getSystemFont();
				FontData[] fD = systemFont.getFontData();
				fD[0].setHeight(32);
				fontLarge = new Font(currentDisplay, fD[0]);
				fD[0].setHeight(16);
				fontMiddle = new Font(currentDisplay, fD[0]);
				handCursor = new Cursor(currentDisplay, SWT.CURSOR_HAND);
				busyCursor = new Cursor(currentDisplay, SWT.CURSOR_WAIT);
				defaultCursor = currentDisplay.getSystemCursor(SWT.CURSOR_ARROW);
				lightColor = Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
				highlightColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
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

	public static Font getLargeFont() {
		return fontLarge;
	}
	
	public static Font getMiddleFont() {
		return fontMiddle;
	}

	public static Cursor getHandCursor() {
		return handCursor;
	}
	public static Cursor getBusyCursor() {
		return busyCursor;
	}
	public static Cursor getDefaultCursor() {
		return defaultCursor;
	}
	public static Color getLightColor() {
		return lightColor;
	}
	public static Color getHighlightColor() {
		return highlightColor;
	}
	
	public static int getMonitorWidth() {
		IMultiMonitorManager mmManager = new MultiMonitorManager();
		return mmManager.getMonitorWidth();
	}
	
	public static void setPreference(String name, String value){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(name, value);
	}

	public static void flushPreferenceStore(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store != null && store.needsSaving()
				&& store instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore) store).save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getPreference(String name) {
		if (name.contains(":")){
			String[] pairs = name.split(":");
			return Platform.getPreferencesService().getString(
					pairs[0], pairs[1], "", null).trim();
		} else {
			return Platform.getPreferencesService().getString(
					Activator.PLUGIN_ID, name, "", null).trim();
		}
	}
	

}
