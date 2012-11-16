/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.kowari.exp.command;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import au.gov.ansto.bragg.kowari.exp.commandView.AbstractScanCommandView;

/**
 * @author nxi
 * Created on 05/08/2009
 */
public class TableScanParameter extends AbstractScanParameter {

	private SimpleTableScanCommand command;
	private boolean isSelected;
	private float sx;
	private float sy;
	private float sz;
	private float som;
	private float time;
	private float ga;
	private float gb;
	private float gc;
	
	public TableScanParameter() {
		super();
	}

	public float getSx() {
		return sx;
	}

	public void setSx(float sx) {
		float oldValue = this.sx;
		this.sx = sx;
		firePropertyChange("sx", oldValue, sx);
	}

	public float getSy() {
		return sy;
	}

	public void setSy(float sy) {
		float oldValue = this.sy;
		this.sy = sy;
		firePropertyChange("sy", oldValue, sy);
	}

	public float getSz() {
		return sz;
	}

	public void setSz(float sz) {
		float oldValue = this.sz;
		this.sz = sz;
		firePropertyChange("sz", oldValue, sz);
	}

	public float getSom() {
		return som;
	}

	public void setSom(float som) {
		float oldValue = this.som;
		this.som = som;
		firePropertyChange("som", oldValue, som);
	}

	public float getGa() {
		return ga;
	}

	public void setGa(float ga) {
		float oldValue = this.ga;
		this.ga = ga;
		firePropertyChange("ga", oldValue, ga);
	}

	public float getGb() {
		return gb;
	}

	public void setGb(float gb) {
		float oldValue = this.gb;
		this.gb = gb;
		firePropertyChange("gb", oldValue, gb);
	}

	public float getGc() {
		return gc;
	}

	public void setGc(float gc) {
		float oldValue = this.gc;
		this.gc = gc;
		firePropertyChange("gc", oldValue, gc);
	}

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		float oldValue = this.time;
		this.time = time;
		firePropertyChange("time", oldValue, time);
	}

	
	public boolean getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(boolean isSelected) {
		boolean oldValue = this.isSelected;
		this.isSelected = isSelected;
		firePropertyChange("isSelected", oldValue, isSelected);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#createParameterUI(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createParameterUI(Composite parent, final AbstractScanCommandView commandView, 
			final FormToolkit toolkit) {
		if (command.getNumberOfMotor() == 4) {
			final Label dragLabel = toolkit.createLabel(parent, "\u2022");
			dragLabel.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

			final Button selectBox = toolkit.createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 2).applyTo(selectBox);

			final Text sxText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(sxText);
			addValidator(sxText, ParameterValidator.floatValidator);

			final Text syText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(syText);
			addValidator(syText, ParameterValidator.floatValidator);

			final Text szText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(szText);
			addValidator(szText, ParameterValidator.floatValidator);

			final Text somText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(somText);
			addValidator(somText, ParameterValidator.floatValidator);

			final Text timeText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(timeText);
			addValidator(timeText, ParameterValidator.floatValidator);

			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(SWTObservables.observeSelection(selectBox),
							BeansObservables.observeValue(getInstance(), "isSelected"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(sxText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "sx"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(syText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "sy"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(szText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "sz"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(somText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "som"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(timeText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "time"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		} else if (command.getNumberOfMotor() == 7) {
			final Label dragLabel = toolkit.createLabel(parent, "\u2022");
			dragLabel.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

			final Button selectBox = toolkit.createButton(parent, "", SWT.CHECK);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 2).applyTo(selectBox);
			
			final Text sxText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(sxText);
			addValidator(sxText, ParameterValidator.floatValidator);

			final Text syText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(syText);
			addValidator(syText, ParameterValidator.floatValidator);

			final Text szText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(szText);
			addValidator(szText, ParameterValidator.floatValidator);

			final Text somText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(somText);
			addValidator(somText, ParameterValidator.floatValidator);

			final Text eomText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(eomText);
			addValidator(eomText, ParameterValidator.floatValidator);

			final Text echiText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(echiText);
			addValidator(echiText, ParameterValidator.floatValidator);

			final Text ephiText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(ephiText);
			addValidator(ephiText, ParameterValidator.floatValidator);

			final Text timeText = toolkit.createText(parent, "");
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(timeText);
			addValidator(timeText, ParameterValidator.floatValidator);

			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(SWTObservables.observeSelection(selectBox),
							BeansObservables.observeValue(getInstance(), "isSelected"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(sxText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "sx"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(syText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "sy"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(szText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "sz"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(somText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "som"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(eomText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "ga"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(echiText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "gb"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(ephiText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "gc"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(SWTObservables.observeText(timeText, SWT.Modify),
							BeansObservables.observeValue(getInstance(), "time"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		}
		
		
	}

	@Override
	public String toString() {
		String text = sx + " " + sy + " " + sz + " " + som;
		if (command.getNumberOfMotor() == 7) {
			text += " " + ga + " " + gb + " " + gc;
		}
		return text + " " + time;
	}

	@Override
	public String getDriveScript(String indexName, String indent) {
		String script = "";
		if (command.getColumn1()) {
			script += indent + "drive sx " + ((float) sx) + "\n";
		}
		if (command.getColumn2()) {
			script += indent + "drive sy " + ((float) sy) + "\n";
		}
		if (command.getColumn3()) {
			script += indent + "drive sz " + ((float) sz) + "\n";
		}
		if (command.getColumn4()) {
			script += indent + "drive som " + ((float) som) + "\n";
		}
		if (command.getNumberOfMotor() == 7) {
			if (command.getColumn5()) {
				script += indent + "drive ga " + ((float) ga) + "\n";
			}
			if (command.getColumn6()) {
				script += indent + "drive gb " + ((float) gb) + "\n";
			}
			if (command.getColumn7()) {
				script += indent + "drive gc " + ((float) gc) + "\n";
			}
		}
		script += indent + "histmem preset " + ((int) time) + "\n";
		script += indent + "histmem start block\n";
		script += indent + "save " + indexName + "\n";
		return script;
	}
	
	@Override
	public String getBroadcastScript(String indexName, String indent) {
		String text = indent + "broadcast " + sx + " " + sy + " " + sz + " " + som;
		if (command.getNumberOfMotor() == 7) {
			text += " " + ga + " " + gb + " " + gc;
		}
		return text + " " + time + "\n";
	}
	
	@Override
	public String getPritable(boolean isFirstLine) {
		String text = "";
		if (command.getColumn1()) {
			text += sx + ", \t";
		}
		if (command.getColumn2()) {
			text += sy + ", \t";
		}
		if (command.getColumn3()) {
			text += sz + ", \t";
		}
		if (command.getColumn4()) {
			text += som + ", \t";
		}
		if (command.getNumberOfMotor() == 7) {
			if (command.getColumn5()) {
				text += ga + ", \t";
			}
			if (command.getColumn6()) {
				text += gb + ", \t";
			}
			if (command.getColumn7()) {
				text += gc + ", \t";
			}
		}
		return text + time + "\n";	}

	@Override
	public void startIteration() {
		// leave empty
	}

	@Override
	public boolean iterationHasNext() {
		// leave empty
		return false;
	}

	@Override
	public String iterationGetNext() {
		// leave empty
		return null;
	}

	@Override
	public int getNumberOfPoints() {
		// leave empty
		return 0;
	}

	/**
	 * @return the command
	 */
	public SimpleTableScanCommand getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(SimpleTableScanCommand command) {
		this.command = command;
	}
}
