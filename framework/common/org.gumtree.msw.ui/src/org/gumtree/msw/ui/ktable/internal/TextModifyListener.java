package org.gumtree.msw.ui.ktable.internal;

import java.util.Objects;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListener;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.IModelCellDefinition;

// used to bind text control to (read-only) model
public class TextModifyListener implements ModifyListener {
	// fields
	private IElementAdapter element;
	private IModelCellDefinition definition;
	// helpers
	private ModelListener modelListener = new ModelListener();
	private TextControlAdapter textControl = new TextControlAdapter();
	private CComboControlAdapter ccomboControl = new CComboControlAdapter();
	
	// methods
	public void update(IModelCellDefinition definition, IElementAdapter element) {
		this.element = element;
		this.definition = definition;
	}
	public void open(Control control) {
		ITextControlAdapter text = getAdapter(control);
		if (text != null) {
			text.addModifyListener(this);
			
    		modelListener.update(definition, text);
    		element.addPropertyListener(modelListener);
		}
	}
	public void close(Control control) {
		ITextControlAdapter text = getAdapter(control);
		if (text != null) {
			text.removeModifyListener(this);

    		modelListener.update(definition, null);
    		element.removePropertyListener(modelListener);
		}
	}
	@Override
	public void modifyText(ModifyEvent event) {
		ITextControlAdapter text = getAdapter(event.widget);
		if (text != null) {
			String newValue = text.getText();
			try {
        		Object oldValue = definition.convertFromModel(element.get(definition.getProperty()));
        		
				if (Objects.equals(newValue, oldValue))
					text.setBackground(Resources.COLOR_DEFAULT);
				else if (element.validate(definition.getProperty(), definition.convertToModelValue(newValue)))
					text.setBackground(Resources.COLOR_EDITING);
				else
					text.setBackground(Resources.COLOR_ERROR);
			}
			catch (Exception e) {
				text.setBackground(Resources.COLOR_ERROR);
			}
		}
	}
	// helper
	private ITextControlAdapter getAdapter(Widget widget) {
		if (widget instanceof Text) {
			textControl.setControl((Text)widget);
			return textControl;
		}
		else if (widget instanceof CCombo) {
			ccomboControl.setControl((CCombo)widget);
			return ccomboControl;
		}

		return null;
	}

	// forward any changes from model to control
	private static class ModelListener implements IElementListener {
		// fields
		private IModelCellDefinition definition;
		private ITextControlAdapter target;
					
		// methods
		public void update(IModelCellDefinition definition, ITextControlAdapter target) {
			this.definition = definition;
			this.target = target;
		}
		@Override
		public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
			if ((target != null) && Objects.equals(property, definition.getProperty())) {
				Object converted = definition.convertFromModel(newValue);
				if (converted instanceof String)
					target.setText((String)converted);
			}
		}
		@Override
		public void onDisposed() {
			target = null;
		}
	}
}
