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
package au.gov.ansto.bragg.kakadu.ui.editors;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import au.gov.ansto.bragg.datastructures.core.plot.StepDirection;
import au.gov.ansto.bragg.datastructures.core.plot.StepDirection.StepDirectionType;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.Activator;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.process.port.TunerPortListener;

/**
 * @author nxi
 * Created on 13/10/2008
 */
public class StepDirectionOperationParameterEditor extends
		OperationParameterEditor implements SelectionListener{
	
	private static Color defaultTextColor, errorTextColor;
	private Button stepForwardButton;
	private Button stepBackwardButton;

	/**
	 * @param operationParameter
	 * @param parentComposite
	 */
	public StepDirectionOperationParameterEditor(
			OperationParameter operationParameter, Composite parentComposite) {
		super(operationParameter, parentComposite);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor#addApplyParameterListener(org.eclipse.swt.events.SelectionListener)
	 */
	@Override
	public void addApplyParameterListener(SelectionListener selectionListener) {
		
		stepBackwardButton.addSelectionListener(selectionListener);
		stepForwardButton.addSelectionListener(selectionListener);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor#createEditor()
	 */
	@Override
	protected Control createEditor() {
		
		Composite editorComposite =	new Composite(parentComposite, SWT.NONE);
		
		if (defaultTextColor == null) {
			defaultTextColor = editorComposite.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_FOREGROUND);
			errorTextColor = editorComposite.getDisplay().getSystemColor(
					SWT.COLOR_RED);
		}

		
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		editorComposite.setLayout(gridLayout);

		stepBackwardButton = new Button(editorComposite, SWT.PUSH);
		stepBackwardButton.setData(new StepDirection(StepDirectionType.holding));
		stepBackwardButton.setToolTipText("Click to navigate backwards");
		stepBackwardButton.setImage(Activator.getImageDescriptor("icons/nav_backward.gif").createImage());
		stepBackwardButton.addSelectionListener(new SelectionListener(){
			long timer = 0;
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				long sysTime = System.currentTimeMillis();
				if (sysTime - timer > 800){
					setChangeListenerEnable(false);
					stepBackwardButton.setData(new StepDirection(StepDirectionType.backward));
					operationParameter.setChanged(false);
					setChangeListenerEnable(true);
				}
				timer = sysTime;
			}
		});
		stepBackwardButton.addSelectionListener(this);

		stepForwardButton = new Button(editorComposite, SWT.PUSH);
		stepForwardButton.setToolTipText("Click to navigate forwards");
		stepForwardButton.setImage(Activator.getImageDescriptor("icons/nav_forward.gif").createImage());
		stepForwardButton.addSelectionListener(new SelectionListener(){
			long timer = 0;
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				long sysTime = System.currentTimeMillis();
				if (sysTime - timer > 800){
					setChangeListenerEnable(false);
					stepBackwardButton.setData(new StepDirection(StepDirectionType.forward));
					operationParameter.setChanged(false);
					setChangeListenerEnable(true);
				}
				timer = sysTime;
			}
			
		});
		stepForwardButton.addSelectionListener(this);

 		return editorComposite;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor#getEditorData()
	 */
	@Override
	protected Object getEditorData() {
		
		return stepBackwardButton.getData();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor#loadData()
	 */
	@Override
	public void loadData() {
		

	}

	public void widgetDefaultSelected(SelectionEvent arg0) {
		
		
	}

	public void widgetSelected(SelectionEvent arg0) {
		
		dataUpdated();
	}

	@Override
	protected void initializeTunerListener() {
		listener = new TunerPortListener(operationParameter.getTuner()){

			@Override
			public void updateUIMax(final Object max) {
			}

			@Override
			public void updateUIMin(final Object min) {
				
			}

			@Override
			public void updateUIOptions(final List<?> options) {
				
			}

			@Override
			public void updateUIValue(final Object value) {
				if (value instanceof StepDirection)
					DisplayManager.getDefault().asyncExec(new Runnable(){

						public void run() {
							stepBackwardButton.setData((StepDirection) value);	
						}

					});
			}
		};
	}
}
