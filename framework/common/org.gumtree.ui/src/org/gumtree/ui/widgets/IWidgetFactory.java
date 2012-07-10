package org.gumtree.ui.widgets;

import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.core.object.IDisposable;

public interface IWidgetFactory extends IDisposable {

	public Composite createComposite(Composite parent);
	
	public Composite createComposite(Composite parent, int style);
	
	public Group createGroup(Composite parent, String text);
	
	public Group createGroup(Composite parent, String text, int style);
	
	public Text createText(Composite parent, String text);
	
	public Text createText(Composite parent, String text, int style);
	
	public Label createLabel(Composite parent, String text);
	
	public Label createLabel(Composite parent, String text, int style);
	
	public Button createButton(Composite parent, String text, int style);
	
	public SashForm createSashForm(Composite parent);
	
	public SashForm createSashForm(Composite parent, int style);
	
	public Combo createCombo(Composite parent);
	
	public Combo createCombo(Composite parent, int style);
	
}
