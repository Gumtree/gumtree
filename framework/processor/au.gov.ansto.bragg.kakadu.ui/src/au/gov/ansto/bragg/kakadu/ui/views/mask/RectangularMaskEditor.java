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
package au.gov.ansto.bragg.kakadu.ui.views.mask;

import java.awt.geom.Rectangle2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.vis.mask.Abstract2DMask;
import org.gumtree.vis.mask.AbstractMask;

/**
 * The editor provides UI components for editing rectangular mask points.
 * 
 * @author Danil Klimontov (dak)
 */
public class RectangularMaskEditor extends AbstractMaskEditor {

	
	private Text xMaxText;
	private Text xMinText;
	private Text yMinText;
	private Text yMaxText;
	private Group poitEditorGroup;
	private Label xMinLabel;
	private Label xMaxLabel;
	private Label yMinLabel;
	private Label yMaxLabel;

	public RectangularMaskEditor(Composite parent, int style) {
		super(parent, style);
	}

	protected Composite createPointEditor() {
		poitEditorGroup = new Group(this, SWT.NONE);
		poitEditorGroup.setText("Points");
		
		GridLayout gridLayout = new GridLayout ();
		gridLayout.numColumns = 4;
		gridLayout.marginHeight = 3;
		gridLayout.marginWidth = 3;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 3;
		poitEditorGroup.setLayout (gridLayout);

		xMinLabel = new Label (poitEditorGroup, SWT.NONE);
		xMinLabel.setText ("X min");
		
		xMinText = new Text (poitEditorGroup, SWT.BORDER);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		xMinText.setLayoutData (data);

		xMaxLabel = new Label (poitEditorGroup, SWT.NONE);
		xMaxLabel.setText ("X Max");
		data = new GridData ();
		data.horizontalIndent = 6;
		xMaxLabel.setLayoutData (data);

		xMaxText = new Text (poitEditorGroup, SWT.BORDER);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		xMaxText.setLayoutData (data);

		yMinLabel = new Label (poitEditorGroup, SWT.NONE);
		yMinLabel.setText ("Y min");
		
		yMinText = new Text (poitEditorGroup, SWT.BORDER);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		yMinText.setLayoutData (data);

		yMaxLabel = new Label (poitEditorGroup, SWT.NONE);
		yMaxLabel.setText ("Y Max");
		data = new GridData ();
		data.horizontalIndent = 6;
		yMaxLabel.setLayoutData (data);

		yMaxText = new Text (poitEditorGroup, SWT.BORDER);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		yMaxText.setLayoutData (data);

		
		return poitEditorGroup;
	}

	protected void clearPoints() {
		xMinText.setText("");
		yMinText.setText("");
		xMaxText.setText("");
		yMaxText.setText("");
	}

	protected void setPoints(AbstractMask mask) {
		if (mask instanceof Abstract2DMask) {
			Rectangle2D region = ((Abstract2DMask) mask).getRectangleFrame();
			xMinText.setText(String.format("%.1f", region.getMinX()));
			yMinText.setText(String.format("%.1f", region.getMinY()));
			xMaxText.setText(String.format("%.1f", region.getMaxX()));
			yMaxText.setText(String.format("%.1f", region.getMaxY()));
		}
	}

	protected void initListeners() {
		super.initListeners();
		xMinText.addModifyListener(textModifyListener);
		yMinText.addModifyListener(textModifyListener);
		xMaxText.addModifyListener(textModifyListener);
		yMaxText.addModifyListener(textModifyListener);
	}

	protected void setEditorEnabled(boolean enabled) {
		super.setEditorEnabled(enabled);
		
		xMinLabel.setEnabled(enabled);
		yMinLabel.setEnabled(enabled);
		xMaxLabel.setEnabled(enabled);
		yMaxLabel.setEnabled(enabled);
		poitEditorGroup.setEnabled(enabled);
		xMinText.setEnabled(enabled);
		yMinText.setEnabled(enabled);
		xMaxText.setEnabled(enabled);
		yMaxText.setEnabled(enabled);

	}

	public double getYMax() throws NumberFormatException {
		return Double.parseDouble(yMaxText.getText());
	}

	public double getXMax() throws NumberFormatException {
		return Double.parseDouble(xMaxText.getText());
	}

	public double getYMin() throws NumberFormatException {
		return Double.parseDouble(yMinText.getText());
	}

	public double getXMin() throws NumberFormatException {
		return Double.parseDouble(xMinText.getText());
	}

	protected boolean isValuesValid() {
		boolean valuesValid = super.isValuesValid();
		boolean xNumbersValid = true;
		boolean yNumbersValid = true;
		try {
			getXMin();
			xMinText.setForeground(defaultTextColor);
		} catch (NumberFormatException e) {
			xMinText.setForeground(errorTextColor);
			valuesValid &= false;
			xNumbersValid &= false;
		}
		try {
			getYMin();
			yMinText.setForeground(defaultTextColor);
		} catch (NumberFormatException e) {
			yMinText.setForeground(errorTextColor);
			valuesValid &= false;
			yNumbersValid &= false;
		}
		try {
			getXMax();
			xMaxText.setForeground(defaultTextColor);
		} catch (NumberFormatException e) {
			xMaxText.setForeground(errorTextColor);
			valuesValid &= false;
			xNumbersValid &= false;
		}
		try {
			getYMax();
			yMaxText.setForeground(defaultTextColor);
		} catch (NumberFormatException e) {
			yMaxText.setForeground(errorTextColor);
			valuesValid &= false;
			yNumbersValid &= false;
		}
		
		if (xNumbersValid && getXMin() > getXMax()) {
			xMinText.setForeground(errorTextColor);
			xMaxText.setForeground(errorTextColor);
			valuesValid &= false;
		}
		
		if (yNumbersValid && getYMin() > getYMax()) {
			yMinText.setForeground(errorTextColor);
			yMaxText.setForeground(errorTextColor);
			valuesValid &= false;
		}
		
		return valuesValid;
	}
	
	protected void applyChanges() {
		try {
//			regionParameter.g
//			regionParameter.updateRegion(
//					getId(), 
//					getMaskName(), 
//					isInclusive(),
//					getXMin(),
//					getYMin(),
//					getXMax(),
//					getYMax());
			AbstractMask mask = getMask();
			if (mask instanceof Abstract2DMask) {
				((Abstract2DMask) mask).setRectangleFrame(new Rectangle2D.Double(
						Math.min(getXMin(), getXMax()), 
						Math.min(getYMin(), getYMax()),
						Math.abs(getXMax() - getXMin()),
						Math.abs(getYMax() - getYMin())));
				mask.setInclusive(isInclusive());
				regionParameter.fireMaskUpdatedEvent(mask);
			}
		} catch (NumberFormatException e) {
			showErrorMessage("Validation error.");
		}
		isChanged = false;
	}


}
