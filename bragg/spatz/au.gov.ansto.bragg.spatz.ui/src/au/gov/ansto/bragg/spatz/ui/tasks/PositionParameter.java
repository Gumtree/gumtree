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
public class PositionParameter extends AbstractScanParameter {

	private PositionCommand command;
	private int position = 0;
	private float sx;
	private float sz;
	private float sth;
	private float sphi;
	private String samplename;
	
	/**
	 * @return the angle
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param angle the angle to set
	 */
	public void setPosition(int position) {
		int oldValue = this.position;
		this.position = position;
		firePropertyChange("position", oldValue, position);
	}

	/**
	 * @return the omega
	 */
	public float getSx() {
		return sx;
	}

	/**
	 * @param omega the omega to set
	 */
	public void setSx(float sx) {
		float oldValue = this.sx;
		this.sx = sx;
		firePropertyChange("sx", oldValue, sx);
	}

	/**
	 * @return the ss1vg
	 */
	public float getSz() {
		return sz;
	}

	/**
	 * @param ss1vg the ss1vg to set
	 */
	public void setSz(float sz) {
		float oldValue = this.sz;
		this.sz = sz;
		firePropertyChange("sz", oldValue, sz);
	}

	/**
	 * @return the ss2vg
	 */
	public float getSth() {
		return sth;
	}

	/**
	 * @param ss2vg the ss2vg to set
	 */
	public void setSth(float sth) {
		float oldValue = this.sth;
		this.sth = sth;
		firePropertyChange("sth", oldValue, sth);
	}

	/**
	 * @return the ss3vg
	 */
	public float getSphi() {
		return sphi;
	}

	/**
	 * @param ss3vg the ss3vg to set
	 */
	public void setSphi(float sphi) {
		float oldValue = this.sphi;
		this.sphi = sphi;
		firePropertyChange("sphi", oldValue, sphi);
	}

	/**
	 * @return the pos
	 */
	public String getSamplename() {
		return samplename;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setSamplename(String samplename) {
		String oldValue = this.samplename;
		this.samplename = samplename;
		firePropertyChange("samplename", oldValue, samplename);
	}

	public PositionParameter() {
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

		final Label positionLabel = toolkit.createLabel(parameterComposite, String.valueOf(position));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(positionLabel);
		getInstance().addPropertyChangeListener("angle", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				positionLabel.setText(String.valueOf(evt.getNewValue()));
			}
		});;

		final Text sxText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(sxText);
		addValidator(sxText, ParameterValidator.floatValidator);

		final Text szText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(szText);
		addValidator(szText, ParameterValidator.floatValidator);

		final Text sthText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(sthText);
		addValidator(sthText, ParameterValidator.floatValidator);

		final Text sphiText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(sphiText);
		addValidator(sphiText, ParameterValidator.floatValidator);

		final Text samplenameText = toolkit.createText(parameterComposite, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(WIDTH_PARAMETER_LONG, SWT.DEFAULT).applyTo(samplenameText);
		addValidator(samplenameText, ParameterValidator.integerValidator);

		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
//				bindingContext.bindValue(SWTObservables.observe(angleLabel, SWT.Modify),
//						BeansObservables.observeValue(getInstance(), "angle"),
//						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(sxText),
						BeanProperties.value("sx").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(szText),
						BeanProperties.value("sz").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(sthText),
						BeanProperties.value("sth").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(sphiText),
						BeanProperties.value("sphi").observe(getInstance()),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(samplenameText),
						BeanProperties.value("samplename").observe(getInstance()),
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
		final PositionParameter child = this;
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
		text += position + " ";
		text += sx + " ";
		text += sz + " ";
		text += sth + " ";
		text += sphi + " ";
		text += samplename;
		return text;
	}

	@Override
	public String getDriveScript(String indexName, String indent) {
		String script = "";
		script += indent + "if {$idx == " + position + "} {\n";
		script += indent + "\tclientput \"selecting position " + position + "\"\n";
		script += indent + "\tdrive sx " + sx;
		script += " sz "+ sz;
		script += " sth "+ sth;
		script += " sphi "+ sphi;
		script += " \n";
		script += indent + "\tclientput \"position " + position + " selected\"\n";
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
		text += position + ", \t";
		text += sx + ", \t";
		text += sz + ", \t";
		text += sth + ", \t";
		text += sphi + ", \t";
		text += samplename + "\n";
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
	public PositionCommand getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(PositionCommand command) {
		this.command = command;
	}

	@Override
	protected void addNewParameter(AbstractScanCommand command){
		PositionParameter newParameter = new PositionParameter();
		newParameter.setCommand((PositionCommand) command);
		newParameter.setSx(sx);
		newParameter.setSz(sz);
		newParameter.setSth(sth);
		newParameter.setSphi(sphi);
		newParameter.setSamplename(samplename);
		command.insertParameter(command.indexOfParameter(this) + 1, newParameter);
	}
	
	protected void removeParameter(AbstractScanCommand command){
		if (command.getParameterList().size() <= 1)
			return;
		command.removeParameter(this);
		firePropertyChange("parameter_remove", true, false);
	}

}
