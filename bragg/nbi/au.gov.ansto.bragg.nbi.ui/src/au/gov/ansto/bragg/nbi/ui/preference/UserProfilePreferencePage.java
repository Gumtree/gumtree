package au.gov.ansto.bragg.nbi.ui.preference;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import au.gov.ansto.bragg.nbi.ui.internal.Activator;

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

public class UserProfilePreferencePage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	protected StringFieldEditor userText;
	protected StringFieldEditor existingUsers;
	protected DirectoryFieldEditor userDirectory;
	protected RadioGroupFieldEditor userProfile;
	protected String radioValue = PreferenceConstants.NEW_PROFILE_VALUE;
	private static final int NAME_LENGTH = 20;
	private static final int DIRECTORY_LENGTH = 40;
	private static final int MAX_NUMBER_OF_PROFILES = 13;
	private boolean isLocked = false; 
	
	
	public UserProfilePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
//		setDescription("Select available algorithm set");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(getFieldEditorParent());
		
//		String currentUserName = "";
//		currentUserName = System.getProperty(PreferenceConstants.USER_PROFILE_USERNAME);
//		String currentUserDirectory = "";
//		currentUserDirectory = System.getProperty(PreferenceConstants.USER_PROFILE_DIRECTORY);
		
		Label newProfileLabel = new Label(getFieldEditorParent(), SWT.None);		
		newProfileLabel.setText("Create new user profile");
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(newProfileLabel);
//		new Label(getFieldEditorParent(), SWT.None);
//		Label userLabel = new Label(getFieldEditorParent(), SWT.None);
//		userLabel.setText("User Name:");
//		GridDataFactory.swtDefaults().grab(true, false).applyTo(userLabel);
		userText = new StringFieldEditor(PreferenceConstants.P_USERNAME, 
				"User name:", 30, getFieldEditorParent());
		addField(userText);
		userDirectory = new DirectoryFieldEditor(PreferenceConstants.P_PATH,
				"Project folder:", getFieldEditorParent());
		userDirectory.getTextControl(getFieldEditorParent()).setSize(30, SWT.DEFAULT);
		addField(userDirectory);
		Label separator = new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(separator);

		
//		Label loadProfileLabel = new Label(getFieldEditorParent(), SWT.None);		
//		loadProfileLabel.setText("Or");
//		GridDataFactory.fillDefaults().span(1, 2).grab(true, false).applyTo(loadProfileLabel);

		existingUsers = new StringFieldEditor(PreferenceConstants.P_HIDDEN_FIELD, 
				"OR", 30, getFieldEditorParent());
//		existingUsers.setStringValue(value);
		addField(existingUsers);
		existingUsers.setPreferenceStore(getPreferenceStore());
		existingUsers.load();
		existingUsers.getTextControl(getFieldEditorParent()).setVisible(false);
		String currentUsers = existingUsers.getStringValue();
		if (currentUsers.isEmpty() || !currentUsers.contains("=")){
			currentUsers = "new ...=" + PreferenceConstants.NEW_PROFILE_VALUE;
		}
//		System.out.println(currentUsers);
		
		String[][] existingProfiles = createRadioContents(currentUsers);
		
		userProfile = new RadioGroupFieldEditor(PreferenceConstants.P_EXISTING_PROFILE, 
				"Load an existing profile", 1, existingProfiles, getFieldEditorParent(), false);
		addField(userProfile);
		Font font = new Font(getFieldEditorParent().getDisplay(), 
				new FontData[]{new FontData("SimSun", 9, SWT.NORMAL)});
//				new FontData[]{new FontData("Courier New", 8, SWT.NORMAL)});
		Control[] controls = userProfile.getRadioBoxControl(getFieldEditorParent()).getChildren();
		for (Control control : controls)
			control.setFont(font);
		
		
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

	private String[][] createRadioContents(String profileString){
		String[][] profilePairs = PreferenceUtils.readProfiles(profileString);
		if (profilePairs != null && profilePairs.length > 0){
			for (int i = 0; i < profilePairs.length; i++) {
				if (!profilePairs[i][1].equals(PreferenceConstants.NEW_PROFILE_VALUE)){
					String userName = profilePairs[i][0];
					if (userName.length() > NAME_LENGTH)
						userName = userName.substring(0, NAME_LENGTH - 3) + "...";
					else 
						userName = String.format("%-" + NAME_LENGTH + "s", userName);
					String path = profilePairs[i][1];
					if (path.length() > DIRECTORY_LENGTH){
						path = path.substring(0, 16) + "..." + path.substring(
								path.length() - (DIRECTORY_LENGTH - 19), path.length());
					}
					profilePairs[i][0] = userName + " " + path;
				}
			}
		}
		return profilePairs;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	@Override
	protected void initialize() {
		super.initialize();
		String directory = System.getProperty(PreferenceConstants.USER_PROFILE_DIRECTORY);
		setRadioFieldSelection(directory);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		checkState();
		String userName = null;
		String directory = null;
//		String radioSelection = userProfile.getPreferenceStore().getString(PreferenceConstants.P_EXISTING_PROFILE);
		if (radioValue.equals(PreferenceConstants.NEW_PROFILE_VALUE)){
			userName = userText.getStringValue();
			directory = userDirectory.getStringValue();
			if (!userName.isEmpty() && !directory.isEmpty()){
				String currentUsers = existingUsers.getStringValue();
				if (currentUsers.isEmpty())
					currentUsers = "new ...=NULL";
				currentUsers = addProfiles(currentUsers, userName, directory);
				existingUsers.setStringValue(currentUsers);
			}
		}else{
			directory = radioValue;
			userName = PreferenceUtils.findUserName(PreferenceUtils.readProfiles(
					existingUsers.getStringValue()), directory);
		}
		if (!userName.isEmpty() && !directory.isEmpty()){
			System.setProperty(PreferenceConstants.USER_PROFILE_USERNAME, userName);
			System.setProperty(PreferenceConstants.USER_PROFILE_DIRECTORY, directory);
			setNewDirectory(directory);
//			setRadioFieldSelection(directory);
			System.out.println("set name=" + userName + "; directory=" + directory);
		}
		isLocked = true;
		userText.setStringValue("");
		userDirectory.setStringValue("");
		isLocked = false;
		return super.performOk();
	}

	private String addProfiles(String currentUsers, String userName,
			String directory) {
		String profileString;
		String[] entries = currentUsers.split("&");
		if (entries.length >= MAX_NUMBER_OF_PROFILES){
			profileString = entries[0];
			for (int i = entries.length - MAX_NUMBER_OF_PROFILES + 2; i < entries.length; i++) {
				profileString += "&" + entries[i];
			}
		}else
			profileString = currentUsers;
		profileString += "&" + userName + "=" + directory;
		return profileString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		checkState();
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)){
			String newValue = (String) event.getNewValue();
			String oldValue = (String) event.getOldValue();
			if (newValue.equals(oldValue))
				return;
			Object source = event.getSource();
			if (source instanceof RadioGroupFieldEditor){
				radioValue = newValue;
				if (!isLocked){
					isLocked = true;
					if (!newValue.equals(PreferenceConstants.NEW_PROFILE_VALUE)){
						userText.setStringValue("");
						userDirectory.setStringValue("");
					}
//					else{
//						String[][] profiles = readProfiles(existingUsers.getStringValue());
//						String userName = findUserName(profiles, newValue);
//						if (userName != null){
//							userText.setStringValue(userName);
//							userDirectory.setStringValue(newValue);
//						}
//					}
					isLocked = false;
				}
			}else if (source instanceof StringFieldEditor){
				StringFieldEditor stringField = (StringFieldEditor) source;
				String name = stringField.getPreferenceName();
				String value = stringField.getStringValue();
				if ((name.equals(PreferenceConstants.P_USERNAME) || name.equals(PreferenceConstants.P_PATH)) 
						&& !value.isEmpty()){
					if (!isLocked){
						isLocked = true;
						radioValue = PreferenceConstants.NEW_PROFILE_VALUE;
//						userProfile.loadDefault();
						setRadioFieldSelection(PreferenceConstants.NEW_PROFILE_VALUE);
						isLocked = false;
					}
				}
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		String currentProfiles = existingUsers.getStringValue();
		super.performDefaults();
		existingUsers.setStringValue(currentProfiles);
	}

	protected void setRadioFieldSelection(String value){
		if (value == null)
			return;
		Composite composite = userProfile.getRadioBoxControl(getFieldEditorParent());
		Control[] controls = composite.getChildren();
		boolean isFound = false;
		for (int i = 0; i < controls.length; i ++){
			if (controls[i] instanceof Button){
				Button radio = (Button) controls[i];
				if (value.equals(radio.getData())){
					IPreferenceStore store = Activator.getDefault().getPreferenceStore();
					store.setValue(PreferenceConstants.P_EXISTING_PROFILE, value);
					userProfile.load();
					radio.setSelection(true);
					isFound = true;
				}else
					radio.setSelection(false);
			}
		}
		if (!isFound)
			if (controls[0] instanceof Button){
				((Button) controls[0]).setSelection(true);
			}
	}

	protected void setNewDirectory(String value){
		if (value == null)
			return;
		Composite composite = userProfile.getRadioBoxControl(getFieldEditorParent());
		Control[] controls = composite.getChildren();
		for (int i = 0; i < controls.length; i ++){
			if (controls[i] instanceof Button){
				Button radio = (Button) controls[i];
				if (PreferenceConstants.NEW_PROFILE_VALUE.equals(radio.getData())){
					radio.setData(value);
					IPreferenceStore store = Activator.getDefault().getPreferenceStore();
					store.setValue(PreferenceConstants.P_EXISTING_PROFILE, value);
					userProfile.load();
					break;
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#checkState()
	 */
	@Override
	protected void checkState() {
		String directory = userDirectory.getStringValue();
		if (!existingUsers.getStringValue().isEmpty()){
			String[][] profiles = PreferenceUtils.readProfiles(existingUsers.getStringValue());
			if (profiles != null && profiles.length > 0){
				for (int i = 0; i < profiles.length; i++) {
					if (profiles[i][1].equals(directory)){
						setErrorMessage("The folder is already used by exising user.");
						setValid(false);
						return;
					}
				}
			}
		}
		setErrorMessage(null);
		setValid(true);
		if (userDirectory.getStringValue().contains("&") || userDirectory.getStringValue().contains("=")){
			setErrorMessage("'&' or '=' are not allowed in the folder path");
			setValid(false);
			return;
		}else{
			setErrorMessage(null);
			setValid(true);
		}
		if (userText.getStringValue().contains("&") || userText.getStringValue().contains("=")){
			setErrorMessage("'&' or '=' are not allowed in the user name");
			setValid(false);
			return;
		}else{
			setErrorMessage(null);
			setValid(true);
		}
		if (!userDirectory.getStringValue().isEmpty() && userText.getStringValue().isEmpty()){
			setErrorMessage("User name can not be empty.");
			setValid(false);
			return;
		}else{
			setErrorMessage(null);
			setValid(true);
		}
		super.checkState();
	}
	
	
}