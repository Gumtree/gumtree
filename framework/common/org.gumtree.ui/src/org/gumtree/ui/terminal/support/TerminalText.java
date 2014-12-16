package org.gumtree.ui.terminal.support;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.terminal.ITerminalOutputBuffer.OutputStyle;
import org.gumtree.widgets.IWidget;

public class TerminalText extends StyledText implements IWidget {

	private int lineLimit = 1000;
	private Color blue;
	private Color red;
	private Color darkRed;

	private int wrapSize;

	public TerminalText(Composite parent, int style) {
		super(parent, style);
		blue = new Color(getDisplay(), 0, 0, 255);
		red = new Color(getDisplay(), 255, 0, 0);
		darkRed = new Color(getDisplay(), 128, 0, 0);
		wrapSize = 160;
	}

	public void appendInputLine(String text) {
		StyleRange styleRange = new StyleRange();
		styleRange.start = getCharCount();
		styleRange.length = 1;
		styleRange.fontStyle = SWT.BOLD;
		append(">> ");
		setStyleRange(styleRange);
		styleRange = new StyleRange();
		styleRange.start = getCharCount();
		styleRange.length = text.length() + 1;
		styleRange.fontStyle = SWT.BOLD;
		styleRange.foreground = darkRed;
		append(text + "\n");
		setStyleRange(styleRange);
		autoScroll();
	}
	
	public void appendInputText(String text) {
		append("\n");
		StyleRange styleRange = new StyleRange();
		styleRange.start = getCharCount();
		styleRange.length = 1;
		styleRange.fontStyle = SWT.BOLD;
		append(">> ");
		setStyleRange(styleRange);
		styleRange = new StyleRange();
		styleRange.start = getCharCount();
		styleRange.length = text.length() + 1;
		styleRange.fontStyle = SWT.BOLD;
		styleRange.foreground = darkRed;
		append(text + "\n\n");
		setStyleRange(styleRange);
		autoScroll();
	}

	public void appendOutputLine(String text, OutputStyle style) {
		int wrapNewLineCount = 0;
		for(int i = 0; i < text.length(); i += wrapSize) {
			StyleRange styleRange = new StyleRange();
//			if(i != 0) {
//				append("-> ");
//			}
			styleRange.start = getCharCount();
			if((i + wrapSize) >= text.length()) {
				append(text.substring(i, text.length()) + "\n");
				styleRange.length = text.length() - i + 1;
				styleRange.foreground = style == OutputStyle.ERROR ? red : blue;
				setStyleRange(styleRange);
			} else {
				append(text.substring(i, i + wrapSize) + "\n");
				styleRange.length = wrapSize + 1;
				styleRange.foreground = style == OutputStyle.ERROR ? red : blue;
				setStyleRange(styleRange);
			}
			autoScroll();
			wrapNewLineCount++;
		}
	}
	
	public void appendOutputText(String text, OutputStyle style) {
		int wrapNewLineCount = 0;
		for(int i = 0; i < text.length(); i += wrapSize) {
			StyleRange styleRange = new StyleRange();
			if(i != 0) {
				append("-> ");
			}
			styleRange.start = getCharCount();
			if((i + wrapSize) >= text.length()) {
				append(text.substring(i, text.length()) + "\n");
				styleRange.length = text.length() - i + 1;
				styleRange.foreground = blue;
				setStyleRange(styleRange);
			} else {
				append(text.substring(i, i + wrapSize) + "\n");
				styleRange.length = wrapSize + 1;
				styleRange.foreground = blue;
				setStyleRange(styleRange);
			}
			autoScroll();
			wrapNewLineCount++;
		}
	}

	private void autoScroll() {
		StyledTextContent doc = getContent();
		int lineCount = doc.getLineCount();
		if (lineCount > lineLimit){
			int startPoint = doc.getOffsetAtLine(lineCount - lineLimit);
			doc.replaceTextRange(0, startPoint, "");
		}
		int docLength = doc.getCharCount();
		if (docLength > 0) {
			setCaretOffset(docLength);
			showSelection();
		}
	}

	public void dispose() {
		blue.dispose();
		red.dispose();
		darkRed.dispose();
		super.dispose();
	}

	public void setLineLimit(int lineLimit) {
		this.lineLimit = lineLimit;
	}
}
