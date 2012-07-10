package org.gumtree.ui.widgets;

import java.net.URI;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.gumtree.service.dataaccess.DataChangeAdapter;
import org.gumtree.service.dataaccess.IDataChangeListener;
import org.gumtree.service.dataaccess.IDataHandler;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.util.SafeUIRunner;

public class TextDisplayWidget extends DataDisplayWidget implements IEventHandler<TextModifyEvent>, IDataHandler<String> {
	
	private Text text;
	
	private String dataURI;
	
	private IDataChangeListener dataChangeListener; 
	
	public TextDisplayWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		text = getToolkit().createText(this, null, SWT.READ_ONLY | getOriginalStyle());
	}

	protected void widgetDispose() {
		text = null;
		dataURI = null;
		if (dataChangeListener != null && getDataAccessManager() != null) {
			getDataAccessManager().removeDataChangeListener(dataChangeListener);
			dataChangeListener = null;
		}
	}
	
	public void handleEvent(TextModifyEvent event) {
		updateText(event.getText());
	}
	
	public String getDataURI() {
		return dataURI;
	}
	
	public void setDataURI(String dataURI) {
		this.dataURI = dataURI;
	}
	
	public void handleData(URI uri, String data) {
		updateText(data);
	}

	public void handleError(URI uri, Exception exception) {
	}
	
	private void updateText(final String textString) {
		// Set text
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (text != null && !text.isDisposed()) {
					text.setText(textString);
				}
			}
		});
	}

	protected void pullData() {
		if (getDataURI() == null) {
			return;
		}
		getDataAccessManager().get(URI.create(getDataURI()),
				String.class, TextDisplayWidget.this);
	}
	
	protected void setupPush() {
		URI uri = URI.create(getDataURI());
		if (dataChangeListener != null) {
			getDataAccessManager().removeDataChangeListener(dataChangeListener);
		}
		if (uri != null) {
			dataChangeListener = new DataChangeAdapter<String>(uri) {
				public void handleData(String data) {
					updateText(data);
				}
			};
		}
		getDataAccessManager().addDataChangeListener(dataChangeListener);
	}
	
}
