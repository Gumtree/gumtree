package org.gumtree.widgets.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SwtWidgetFactory implements IWidgetFactory {

	@Override
	public Composite createComposite(Composite parent) {
		return createComposite(parent, SWT.NONE);
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite widget = new Composite(parent, style);
		widget.setBackgroundMode(SWT.INHERIT_FORCE);
		widget.setForeground(parent.getForeground());
		return widget;
	}

	@Override
	public Group createGroup(Composite parent, String text) {
		return createGroup(parent, text, SWT.NONE);
	}
	
	@Override
	public Group createGroup(Composite parent, String text, int style) {
		Group widget = new Group(parent, style);
		widget.setText(text);
		return widget;
	}

	@Override
	public Text createText(Composite parent, String text) {
		return createText(parent, text, SWT.NONE);
	}

	@Override
	public Text createText(Composite parent, String text, int style) {
		Text widget = new Text(parent, style);
		widget.setText(text);
		return widget;
	}

	@Override
	public Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, SWT.NONE);
	}
	
	@Override
	public Label createLabel(Composite parent, String text, int style) {
		Label widget = new Label(parent, style);
		widget.setForeground(parent.getForeground());
		widget.setText(text);
		return widget;
	}

	@Override
	public Label createLabel(Composite parent, Image image) {
		Label widget = new Label(parent, SWT.NONE);
		widget.setImage(image);
		return widget;
	}
	
	@Override
	public Button createButton(Composite parent, String text, int style) {
		Button widget = new Button(parent, style);
		widget.setText(text);
		return widget;
	}

	@Override
	public SashForm createSashForm(Composite parent) {
		return createSashForm(parent, SWT.NONE);
	}
	
	@Override
	public SashForm createSashForm(Composite parent, int style) {
		return new SashForm(parent, style);
	}


	@Override
	public Combo createCombo(Composite parent) {
		return createCombo(parent, SWT.NONE);
	}

	@Override
	public Combo createCombo(Composite parent, int style) {
		return new Combo(parent, style);
	}
	
	@Override
	public void dispose() {
	}

}
