/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package au.gov.ansto.bragg.echidna.ui.preference;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import au.gov.ansto.bragg.echidna.ui.internal.Activator;
import au.gov.ansto.bragg.echidna.ui.internal.EchidnaAnalysisPerspective;
import au.gov.ansto.bragg.echidna.ui.views.EchidnaAnalysisControlView;
import au.gov.ansto.bragg.kakadu.ui.util.Util;

/**
 * @author nxi
 *
 */
public class DRAPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public DRAPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {

		Label newProfileLabel = new Label(getFieldEditorParent(), SWT.NONE);		
		newProfileLabel.setText("Configurations for data reduction");
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(newProfileLabel);
		
		final StringFieldEditor efficiencyFileText = new StringFieldEditor(
				PreferenceConstants.P_EFFICIENCY_FILE, 
				"Detector efficiency file:", 40, getFieldEditorParent());
		Button browseButton = new Button(getFieldEditorParent(), SWT.PUSH);
		browseButton.setText("Browse...");
		addField(efficiencyFileText);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(browseButton);
		browseButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filename = Util.getFilenameFromShell(getShell(), "", "");
				if (filename != null && filename.trim().length() > 0) {
					efficiencyFileText.setStringValue(filename);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final StringFieldEditor angularOffsetFileText = new StringFieldEditor(
				PreferenceConstants.P_ANGULAR_OFFSET_FILE, 
				"Angular offset file:", 40, getFieldEditorParent());
		Button browseButton2 = new Button(getFieldEditorParent(), SWT.PUSH);
		browseButton2.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_HAND));
		browseButton2.setToolTipText("click this to find a scripting file");
		browseButton2.setText("Browse...");
		addField(angularOffsetFileText);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(browseButton2);
		browseButton2.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filename = Util.getFilenameFromShell(getShell(), "", "");
				if (filename != null && filename.trim().length() > 0) {
					angularOffsetFileText.setStringValue(filename);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final ComboFieldEditor normRefCombo = new ComboFieldEditor(
				PreferenceConstants.P_NORM_REF, 
				"Normalisation reference: ", 
				new String[][]{{"bm1_counts", "bm1_counts"}, {"bm2_counts", "bm2_counts"}, 
						{"bm3_counts", "bm3_counts"}}, 
				getFieldEditorParent());
		addField(normRefCombo);
//		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(normRefCombo.get);
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		super.createControl(parent);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(getFieldEditorParent());
	}
	
	@Override
	public boolean performOk() {
		boolean isOK = super.performOk();
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page : pages) {
				IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();
				for (IPerspectiveDescriptor perspective : perspectives) {
					if (perspective.getId().equals(
							EchidnaAnalysisPerspective.ANALYSIS_PERSPECTIVE_ID)) {
						IViewPart view = page.findView(
								EchidnaAnalysisPerspective.ANALYSIS_PARAMETERS_VIEW_ID);
						if (view instanceof EchidnaAnalysisControlView) {
							((EchidnaAnalysisControlView) view).loadPreference();
							return isOK;
						}
					}
				}
			}
		}
		return isOK;
	}
}
