package org.gumtree.ui.widgets;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.util.SafeUIRunner;

public class AutoScrollStyledText extends StyledText implements IWidget {

	private static final String KEY_PREV_DOC_LEN = "previousDocLength";
	
	private boolean isScrollLocked;

	private int delay = 200;
	
	public AutoScrollStyledText(Composite parent, int style) {
		super(parent, style);
		setData(KEY_PREV_DOC_LEN, 0);
	}

	public boolean isScrollLocked() {
		return isScrollLocked;
	}

	public void setScrollLocked(boolean isScrollLocked) {
		this.isScrollLocked = isScrollLocked;
	}
	
	/**
	 * Amount of delay in millisecond for updating auto scroll.
	 * 
	 * @return
	 */
	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public void resetScroll() {
		setData(KEY_PREV_DOC_LEN, 0);
	}
	
	public void autoScroll() {
		// Don't scroll text if scroll is locked
		if (isScrollLocked()) {
			return;
		}
		// Set delay to avoid scrolling too often
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				StyledTextContent doc = getContent();
				int docLength = doc.getCharCount();
				if (docLength > (Integer) getData(KEY_PREV_DOC_LEN)) {
					setCaretOffset(docLength);
					showSelection();
					setData(KEY_PREV_DOC_LEN, docLength);
				}
				redraw();
			}
		}, getDelay());
	}
	
}
