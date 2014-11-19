package de.kupzog.ktable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class DefaultTooltip implements IKTableTooltip {

	private String text;
	private Shell tip;
	
	public DefaultTooltip(String text) {
		this.text = text;
	}
	
	public boolean isEmpty() {
		return text == null || text.isEmpty();
	}
	
	public void dispose(KTable ktable) {
		if (tip != null && !tip.isDisposed()) {
			tip.dispose();
		}
	}
	
	public boolean isDisposed() {
		return tip.isDisposed();
	}
	
	public boolean isLocked() {
		return false;
	}
	
	public void show(final KTable ktable, TooltipAssistant calc) {
		tip = new Shell (ktable.getShell(), SWT.ON_TOP);
        GridLayout gl = new GridLayout();
        gl.marginWidth=2;
        gl.marginHeight=2;
        tip.setLayout (gl);
        tip.setBackground(ktable.getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
        Label label = new Label (tip, SWT.NONE);                    
        label.setLayoutData(new GridData(GridData.FILL_BOTH));
        label.setForeground (ktable.getDisplay().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
        label.setBackground (ktable.getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
        label.setText(text);
        
        Listener labelListener = new Listener () {
    		public void handleEvent (Event event) {
    			Label label = (Label)event.widget;
    			Shell shell = label.getShell ();
    			// forward mouse events directly to the underlying KTable
    			switch (event.type) {
    				case SWT.MouseDown:
    					Event e = new Event ();
    					e.item = ktable;
    					e.button = event.button;
    					e.stateMask = event.stateMask;
    					ktable.notifyListeners(SWT.MouseDown, e);
    					// fall through
    				default:
    					shell.dispose ();
    					break;
    			}
    		}
    	};
        label.addListener (SWT.MouseExit, labelListener);
        label.addListener (SWT.MouseDown, labelListener);
        label.addListener(SWT.MouseMove, labelListener);
		tip.setBounds(calc.calcBounds(tip.computeSize(SWT.DEFAULT, SWT.DEFAULT)));
		tip.setVisible(true);
	}
	
	//[+] accessors
	public String getTooltip() {
		return text;
	}
	public void setTooltip(String tooltip) {
		this.text = tooltip;
	}
	//[.]
	
	public String toString() {
		return text;
	}
}
