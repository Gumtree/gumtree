package org.gumtree.ui.widgets;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.gumtree.service.eventbus.IEventBus;
import org.gumtree.ui.util.forms.FormControlWidget;

public class TextEditorWidget extends FormControlWidget {

	private IEventBus eventBus;
	
	private Text text;
	
	public TextEditorWidget(Composite parent, int style) {
		super(parent, style);
		init();
	}

	public void setEventBus(IEventBus eventBus) {
		this.eventBus = eventBus;
	}

	protected void init() {
		setLayout(new FillLayout());
		text = getToolkit().createText(this, "", getOriginalStyle());
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// Send event on edit
				if (eventBus != null) {
					eventBus.postEvent(new TextModifyEvent(TextEditorWidget.this, text.getText()));
				}
			}
		});
	}

	protected void widgetDispose() {
		eventBus = null;
		text = null;
	}

	public void afterParametersSet() {
	}
	
}
