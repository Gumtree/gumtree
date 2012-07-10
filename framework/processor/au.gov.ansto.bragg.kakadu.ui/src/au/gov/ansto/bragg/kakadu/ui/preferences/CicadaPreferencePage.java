package au.gov.ansto.bragg.kakadu.ui.preferences;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import au.gov.ansto.bragg.cicada.core.extension.AlgorithmRegistration;
import au.gov.ansto.bragg.cicada.core.extension.AlgorithmSet;
import au.gov.ansto.bragg.kakadu.ui.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class CicadaPreferencePage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	AlgorithmRegistration algorithmRegistration = null;

	public CicadaPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
//		setDescription("Select available algorithm set");
		algorithmRegistration = AlgorithmRegistration.getInstance();
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		List<AlgorithmSet> algorithmSetNames = algorithmRegistration.getAlgorithmSetList();
		for (Iterator<AlgorithmSet> iterator = algorithmSetNames.iterator(); iterator
		.hasNext();) {
			AlgorithmSet algorithmSet = iterator.next();
			if (algorithmSet.isDefault()){
				Label label = new Label(getFieldEditorParent(), SWT.None);
				label.setText("The default algorithm set is " + algorithmSet.getName() 
						+ ", please select additional algorithm sets");
			}
		}
		for (Iterator<AlgorithmSet> iterator = algorithmSetNames.iterator(); iterator
		.hasNext();) {
			AlgorithmSet algorithmSet = iterator.next();
			BooleanFieldEditor algorithmSetPreference = null;
			if (!algorithmSet.isDefault()){
				algorithmSetPreference = new BooleanFieldEditor(
						algorithmSet.getName(),
						algorithmSet.getName(),
						getFieldEditorParent()); 
				addField(algorithmSetPreference);
			}
		}

//		addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
//				"&Directory preference:", getFieldEditorParent()));
//		addField(
//				new BooleanFieldEditor(
//						PreferenceConstants.P_BOOLEAN,
//						"&An example of a boolean preference",
//						getFieldEditorParent()));
//
//		addField(new RadioGroupFieldEditor(
//				PreferenceConstants.P_CHOICE,
//				"An example of a multiple-choice preference",
//				1,
//				new String[][] { { "&Choice 1", "choice1" }, {
//					"C&hoice 2", "choice2" }
//				}, getFieldEditorParent()));
//		addField(
//				new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}