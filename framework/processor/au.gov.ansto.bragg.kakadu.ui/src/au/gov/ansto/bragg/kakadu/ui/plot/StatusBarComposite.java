/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.plot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import au.gov.ansto.bragg.kakadu.ui.util.Util;

/**
 * The widget is used to display status bar.
 * 
 * @author Danil Klimontov (dak)
 */
public class StatusBarComposite extends Composite {

	private Label zoomValueLabel;
	private Label cursorLocationLabel;
	private Label dataStatusLabel;
	private boolean valueAndZoomEnabled = true;

	/**
	 * @param parent
	 * @param style
	 */
	public StatusBarComposite(Composite parent, int style) {
		super(parent, style);
		initialise();
	}

	protected void initialise() {
		GridLayout gridLayout = new GridLayout ();
		gridLayout.numColumns = 10;
		gridLayout.marginHeight = 1;
		gridLayout.marginWidth = 1;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 0;
		setLayout (gridLayout);

		cursorLocationLabel = new Label(this, SWT.NONE);
		
		final Label zoomLabel = new Label(this, SWT.NONE);
		zoomLabel.setText("| Zoom ");
		
		zoomValueLabel = new Label(this, SWT.NONE);
//
//		final Label separator = new Label(this, SWT.NONE);
//		separator.setText("|");
		
//		final Label separator = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
//		final int y = zoomLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
////		separator.setSize(2, y);
//		final GridData gridData = new GridData();
//		gridData.grabExcessVerticalSpace = false;
//		separator.setLayoutData(gridData);
		

		Label horisontalGlueLabel = new Label(this, SWT.NONE);
		final GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		horisontalGlueLabel.setLayoutData(gridData);
		

//		final Label separator = new Label(this, SWT.NONE);
//		separator.setText("|");

		dataStatusLabel = new Label(this, SWT.NONE);
		
//		new Label(this, SWT.SEPARATOR);
		
		
		setZoomInfo(100);
		setCursorLocation(0, 0);
	}

	public void setZoomInfo(int zoomPresentage) {
		// [Tony] [2008-12-16] Fixed widget is disposed bug
		if (zoomValueLabel != null && !zoomValueLabel.isDisposed()) {
			zoomValueLabel.setText("" + zoomPresentage + "%");
			layout();
		}
	}
	
	public void setZoomInfo(double xZoomPresentage, double yZoomPresentage) {
		// [Tony] [2008-12-16] Fixed widget is disposed bug
		if (zoomValueLabel != null && !zoomValueLabel.isDisposed()) {
			zoomValueLabel.setText("X:" + Util.formatDouble(xZoomPresentage, 2) + "% Y:" + Util.formatDouble(yZoomPresentage,2) + "%");
			layout();
		}
	}
	
	public void setCursorLocation(double x, double y, double value) {
		cursorLocationLabel.setText(
				"X:" + Util.formatDouble(x, 2) + 
				" Y:" + Util.formatDouble(y, 2) +
				(valueAndZoomEnabled ? " Value:" + Util.formatDouble(value, 3) : ""));
		layout();
	}

	public void setCursorLocation(double x, int y) {
		cursorLocationLabel.setText(
				"X:" + Util.formatDouble(x, 2) + 
				" Y:" + Util.formatDouble(y, 2));
		layout();
	}
	
	/**
	 * Sets status information text.
	 * @param dataStatus data status text.
	 */
	public void setDataStatus(String dataStatus) {
		dataStatusLabel.setText(dataStatus != null ? "| Status: " + dataStatus : "");
		layout();
	}
	
	public void setValueAndZoomEnabled(boolean isEnabled){
		valueAndZoomEnabled = isEnabled;
		zoomValueLabel.setVisible(isEnabled);
	}
}
