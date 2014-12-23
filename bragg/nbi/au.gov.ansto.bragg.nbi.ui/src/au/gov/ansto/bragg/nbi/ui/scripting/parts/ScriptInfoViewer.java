package au.gov.ansto.bragg.nbi.ui.scripting.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResourceManager;

import au.gov.ansto.bragg.nbi.ui.internal.Activator;

public class ScriptInfoViewer extends Composite {

	public enum StyleType {NORMAL, ERROR, WARNING, BOLD};
	private static final int DEFAULT_LINE_LIMIT = 1000;
	
	private StyledText infoText;
	private Font fontBold;
	private Font fontItalic;
	private Font fontNormal;
	
	public ScriptInfoViewer(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().applyTo(this);
		infoText = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(infoText);
		infoText.setEditable(false);
		UIResourceManager resourceManager = new UIResourceManager(Activator.PLUGIN_ID);
		fontBold = resourceManager.createFont("Courier New", 12, SWT.BOLD); 
		fontItalic = resourceManager.createFont("Courier New", 12, SWT.ITALIC);
		fontNormal = resourceManager.createFont("Courier New", 12, SWT.NORMAL);
	}

	public void appendText(String text) {
		appendText(text, StyleType.NORMAL);
	}
	
	public void appendText(String text, String type){
		StyleType styleType = StyleType.NORMAL;
		try {
			styleType = StyleType.valueOf(type.toUpperCase());
		} catch (Exception e) {
		}
		appendText(text, styleType);
	}
	
	public void appendText(final String text, final StyleType type) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (infoText != null && !infoText.isDisposed()) {
					StyleRange styleRange = new StyleRange();
					styleRange.start = infoText.getCharCount();
					styleRange.length = text.length();
					styleRange.font = getFont(type);
					switch (type) {
					case NORMAL:
						styleRange.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
						break;
					case ERROR:
						styleRange.foreground = Display.getDefault().getSystemColor(SWT.COLOR_RED);
						break;
					case WARNING:
						styleRange.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
						break;
					case BOLD:
						styleRange.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
						break;
					default:
						styleRange.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
						break;
					}
					// Append text
					infoText.append(text);
					infoText.setStyleRange(styleRange);
					autoScroll();
				}

			}
		});
		
	}
	
	private Font getFont(StyleType type) {
		switch (type) {
		case NORMAL:
			return fontNormal;
		case ERROR:
			return fontBold;
		case WARNING:
			return fontItalic;
		case BOLD:
			return fontBold;
		default:
			return fontNormal;
		}
	}
	
	private void autoScroll() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (infoText != null && !infoText.isDisposed()) {
					StyledTextContent doc = infoText.getContent();
					int lineCount = doc.getLineCount();
					int lineLimit = DEFAULT_LINE_LIMIT;
					if (lineCount > lineLimit){
						int startPoint = doc.getOffsetAtLine(lineCount - lineLimit);
						doc.replaceTextRange(0, startPoint, "");
					}
					int docLength = doc.getCharCount();
					infoText.setCaretOffset(docLength);
					infoText.showSelection();
					infoText.redraw();
				}
			}
		}, 200);
	}

	public void clear() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (infoText != null && !infoText.isDisposed()) {
					infoText.setText("");
				}
			}
		});
	}
	
}
