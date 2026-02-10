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
package au.gov.ansto.bragg.spatz.ui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanCommand;
import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanCommandView;
import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanParameter;
import au.gov.ansto.bragg.nbi.ui.core.commands.DndTransferData;
import au.gov.ansto.bragg.nbi.ui.core.commands.ParameterValidator;


/**
 * @author nxi
 * Created on 05/08/2009
 */
public class AngleParameter extends AbstractScanParameter {

	private AngleCommand command;
	private int angle = 0;
	private float omega;
	private float ss1vg;
	private float ss2vg;
	private float ss3vg;
	private int pos;
	
	/**
	 * @return the angle
	 */
	public int getAngle() {
		return angle;
	}

	/**
	 * @param angle the angle to set
	 */
	public void setAngle(int angle) {
		int oldValue = this.angle;
		this.angle = angle;
		firePropertyChange("angle", oldValue, angle);
	}

	/**
	 * @return the omega
	 */
	public float getOmega() {
		return omega;
	}

	/**
	 * @param omega the omega to set
	 */
	public void setOmega(float omega) {
		float oldValue = this.omega;
		this.omega = omega;
		firePropertyChange("omega", oldValue, omega);
	}

	/**
	 * @return the ss1vg
	 */
	public float getSs1vg() {
		return ss1vg;
	}

	/**
	 * @param ss1vg the ss1vg to set
	 */
	public void setSs1vg(float ss1vg) {
		float oldValue = this.ss1vg;
		this.ss1vg = ss1vg;
		firePropertyChange("ss1vg", oldValue, ss1vg);
	}

	/**
	 * @return the ss2vg
	 */
	public float getSs2vg() {
		return ss2vg;
	}

	/**
	 * @param ss2vg the ss2vg to set
	 */
	public void setSs2vg(float ss2vg) {
		float oldValue = this.ss2vg;
		this.ss2vg = ss2vg;
		firePropertyChange("ss2vg", oldValue, ss2vg);
	}

	/**
	 * @return the ss3vg
	 */
	public float getSs3vg() {
		return ss3vg;
	}

	/**
	 * @param ss3vg the ss3vg to set
	 */
	public void setSs3vg(float ss3vg) {
		float oldValue = this.ss3vg;
		this.ss3vg = ss3vg;
		firePropertyChange("ss3vg", oldValue, ss3vg);
	}

	/**
	 * @return the pos
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(int pos) {
		int oldValue = this.pos;
		this.pos = pos;
		firePropertyChange("pos", oldValue, pos);
	}

	public AngleParameter() {
		super();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.command.AbstractScanParameter#createParameterUI(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createParameterUI(final Composite parameterComposite, final AbstractScanCommandView commandView, 
			final FormToolkit toolkit) {
//			final Label dragLabel = toolkit.createLabel(parent, "\u2022");
//		parameterComposite = toolkit.createComposite(parent);
//		parameterComposite = new Composite(parent, SWT.NONE);
		final Label dragLabel = toolkit.createLabel(parameterComposite, "\u21c5");
		dragLabel.setCursor(parameterComposite.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		final Label angleLabel = toolkit.createLabel(parameterComposite, String.valueOf(angle));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(angleLabel);
		getInstance().addPropertyChangeListener("angle", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				angleLabel.setText(String.valueOf(evt.getNewValue()));
			}
		});;

		final Text omegaText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(omegaText);
		addValidator(omegaText, ParameterValidator.floatValidator);

		final Text ss1vgText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(ss1vgText);
		addValidator(ss1vgText, ParameterValidator.floatValidator);

		final Text ss2vgText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(ss2vgText);
		addValidator(ss2vgText, ParameterValidator.floatValidator);

		final Text ss3vgText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(ss3vgText);
		addValidator(ss3vgText, ParameterValidator.floatValidator);

		final Text posText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(posText);
		addValidator(posText, ParameterValidator.integerValidator);

		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
//				bindingContext.bindValue(SWTObservables.observe(angleLabel, SWT.Modify),
//						BeansObservables.observeValue(getInstance(), "angle"),
//						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(omegaText),
						BeanProperties.value("omega").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(ss1vgText),
						BeanProperties.value("ss1vg").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(ss2vgText),
						BeanProperties.value("ss2vg").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(ss3vgText),
						BeanProperties.value("ss3vg").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(posText),
						BeanProperties.value("pos").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
		
		Button addButton = toolkit.createButton(parameterComposite, "", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 0).applyTo(addButton);
		try {
			addButton.setImage(SicsBatchUIUtils.getBatchEditorImage("ADD"));
		} catch (FileNotFoundException e2) {
			LoggerFactory.getLogger(this.getClass()).error("can not find ADD image", e2);
		}
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addNewParameter(command);
				commandView.refreshParameterComposite();
			}
		});
		
		Button removeButton = toolkit.createButton(parameterComposite, "", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).indent(0, 0).applyTo(removeButton);
		try {
			removeButton.setImage(SicsBatchUIUtils.getBatchEditorImage("REMOVE"));
		} catch (FileNotFoundException e1) {
			LoggerFactory.getLogger(this.getClass()).error("can not find REMOVE image", e1);
		}
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeParameter(command);
				commandView.refreshParameterComposite();
			}
		});
		
		DragSource dragSource = new DragSource(dragLabel, DND.DROP_MOVE);

		LocalSelectionTransfer transferObject = LocalSelectionTransfer.getTransfer();

		Transfer[] types = new Transfer[] {transferObject};
		dragSource.setTransfer(types);
		final AngleParameter child = this;
		dragSource.addDragListener(new DragSourceAdapter() {
			
			@Override
			public void dragFinished(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(null);
			}
			@Override
			public void dragSetData(DragSourceEvent event) {
				if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
					DndTransferData transferData = new DndTransferData();
					transferData.setParent(command);
					transferData.setChild(child);
					LocalSelectionTransfer.getTransfer().setSelection(
							new StructuredSelection(transferData));
				}
			}
		});
		
	}

	@Override
	public String toString() {
		String text = "";
		text += angle + " ";
		text += omega + " ";
		text += ss1vg + " ";
		text += ss2vg + " ";
		text += ss3vg + " ";
		text += pos;
		return text;
	}

	@Override
	public String getDriveScript(String indexName, String indent) {
		String script = "";
		script += indent + "if {$idx == " + angle + "} {\n";
		script += indent + "\tclientput \"selecting angle " + angle + "\"\n";
		script += indent + "\tdrive omega " + omega;
		script += " ss1vg "+ ss1vg;
		script += " ss2vg "+ ss2vg;
		script += " ss3vg "+ ss3vg;
		script += " \n";
		script += indent + "\tclientput \"angle " + angle + " selected\"\n";
		script += indent + "}\n";
		return script;
	}
	
	@Override
	public String getBroadcastScript(String indexName, String indent) {
		return "";
	}
	
	@Override
	public String getPritable(boolean isFirstLine) {
		String text = "";
		text += angle + ", \t";
		text += omega + ", \t";
		text += ss1vg + ", \t";
		text += ss2vg + ", \t";
		text += ss3vg + ", \t";
		text += pos + "\n";
		return text;
	}

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
	public AngleCommand getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(AngleCommand command) {
		this.command = command;
	}

	@Override
	protected void addNewParameter(AbstractScanCommand command){
		AngleParameter newParameter = new AngleParameter();
		newParameter.setCommand((AngleCommand) command);
		newParameter.setOmega(omega);
		newParameter.setSs1vg(ss1vg);
		newParameter.setSs2vg(ss2vg);
		newParameter.setSs3vg(ss3vg);
		newParameter.setPos(pos);
		command.insertParameter(command.indexOfParameter(this) + 1, newParameter);
	}
	
	protected void removeParameter(AbstractScanCommand command){
		if (command.getParameterList().size() <= 1)
			return;
		command.removeParameter(this);
		firePropertyChange("parameter_remove", true, false);
	}

}
