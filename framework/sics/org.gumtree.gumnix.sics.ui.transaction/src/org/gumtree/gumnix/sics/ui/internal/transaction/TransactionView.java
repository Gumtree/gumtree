package org.gumtree.gumnix.sics.ui.internal.transaction;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;

public class TransactionView extends ViewPart {

	private final static int lineLimit = 1000;
	private StyledText styledText;
	
	private Thread updateThread;
	
	private ISicsProxyListener proxyListener;
	
	private Map<String, StyledText> controlMap;
	
	private boolean controlCreated;
	
	private Composite parent;
	
	public TransactionView() {
		super();
		controlCreated = false;
		
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		SicsCore.getDefaultProxy().addProxyListener(getProxyListener());
		// Create UI when SICS has already connected (ie don't reply on the proxy notification)
		if(SicsCore.getDefaultProxy().isConnected()) {
			controlCreated = true;
			createControl();
		}
	}

	public void dispose() {
		SicsCore.getDefaultProxy().removeProxyListener(getProxyListener());
	}
	
	@Override
	public void setFocus() {
	}
	
	private void createControl() {
		parent.setLayout(new FillLayout());
		
		TabFolder folder = new TabFolder(parent, SWT.NONE);
		
		for(String channelId : SicsCore.getDefaultProxy().getConnectedChannelIds()) {
			TabItem item = new TabItem(folder, SWT.NONE);
			item.setText(channelId);
			Composite composite = new Composite(folder, SWT.NONE);
			createTabControl(composite, channelId);
			item.setControl(composite);
		}

		updateThread = new Thread(new Runnable() {
			public void run() {
				while(!styledText.isDisposed()) {
					final ISicsTransaction[] trans = ITransactionListener.INSTANCE.getNewTransactions();
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							for(ISicsTransaction tran : trans) {
								String message = tran.getMessage();
								if(tran.getMessage().length() > 200) {
									message = message.substring(0, 200);
									message = message + " ...";
								}
								StyledText styledText = getControlMap().get(tran.getChannelId());
								styledText.append(message + "\n");
//								autoScroll(styledText);
								pushOut(styledText);
							}
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
				}
			}
		});
		updateThread.start();
		
		parent.layout();
		parent.update();
	}
	
	private void createTabControl(Composite parent, String channelId) {
		parent.setLayout(new FillLayout());
		styledText = new StyledText(parent, SWT.MULTI | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.READ_ONLY);
		getControlMap().put(channelId, styledText);
	}
	
	private void autoScroll(StyledText styledText) {
		StyledTextContent doc = styledText.getContent();
		int docLength = doc.getCharCount();
		if (docLength > 0) {
			styledText.setCaretOffset(docLength);
			styledText.showSelection();
		}
	}

	private void pushOut(StyledText styledText){
		int lineCount = styledText.getLineCount();
		if (lineCount > lineLimit){
			int startPoint = styledText.getOffsetAtLine(lineCount - lineLimit);
			StyledTextContent content = styledText.getContent();
			content.replaceTextRange(0, startPoint, "");
			autoScroll(styledText);
		}
	}

	public ISicsProxyListener getProxyListener() {
		if(proxyListener == null) {
			proxyListener = new SicsProxyListenerAdapter() {
				// Creates UI when SICS is activated
				public void proxyConnected() {
					if(!controlCreated) {
						controlCreated = true;
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								createControl();
							}
						});
					}
				}
			};
		}
		return proxyListener;
	}
	
	private Map<String, StyledText> getControlMap() {
		if(controlMap == null) {
			controlMap = new HashMap<String, StyledText>();	
		}
		return controlMap;
	}
	
}
