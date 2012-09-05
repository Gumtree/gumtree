package org.gumtree.widgets.swt.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.widgets.swt.IWidgetFactory;

public class FormWidgetFactory implements IWidgetFactory {

	private FormToolkit toolkit;

	@Override
	public Composite createComposite(Composite parent) {
		return getToolkit().createComposite(parent);
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return getToolkit().createComposite(parent, style);
	}

	@Override
	public Group createGroup(Composite parent, String text) {
		return createGroup(parent, text, SWT.NONE);
	}

	@Override
	public Group createGroup(Composite parent, String text, int style) {
		Group widget = new Group(parent, style);
		widget.setText(text);
		getToolkit().adapt(widget);
		return widget;
	}

	@Override
	public Text createText(Composite parent, String text) {
		return getToolkit().createText(parent, text);
	}

	@Override
	public Text createText(Composite parent, String text, int style) {
		return getToolkit().createText(parent, text, style);
	}

	@Override
	public Label createLabel(Composite parent, String text) {
		return getToolkit().createLabel(parent, text);
	}

	@Override
	public Label createLabel(Composite parent, String text, int style) {
		return getToolkit().createLabel(parent, text, style);
	}

	@Override
	public Button createButton(Composite parent, String text, int style) {
		return getToolkit().createButton(parent, text, style);
	}

	@Override
	public SashForm createSashForm(Composite parent) {
		return createSashForm(parent, SWT.NONE);
	}

	@Override
	public SashForm createSashForm(Composite parent, int style) {
		SashForm widget = new SashForm(parent, style);
		getToolkit().adapt(widget);
		return widget;
	}

	@Override
	public Combo createCombo(Composite parent) {
		return createCombo(parent, SWT.NONE);
	}

	@Override
	public Combo createCombo(Composite parent, int style) {
		Combo widget = new Combo(parent, style);
		getToolkit().adapt(widget);
		return widget;
	}

	@Override
	public void dispose() {
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
	}
	
	public FormToolkit getToolkit() {
		if (toolkit == null) {
			toolkit = new FormToolkit(Display.getDefault());
		}
		return toolkit;
	}

}
