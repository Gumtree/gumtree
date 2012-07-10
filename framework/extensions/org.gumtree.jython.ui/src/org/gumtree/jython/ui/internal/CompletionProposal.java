package org.gumtree.jython.ui.internal;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class CompletionProposal implements ICompletionProposal {
	
	private String objectName;

	private JythonType type;
	
	private int documentOffset;
	
	private int insertionOffset;
	
	public CompletionProposal(String objectName, JythonType type, int documentOffset, int insertionOffset) {
		this.objectName = objectName;
		this.type = type;
		this.documentOffset = documentOffset;
		this.insertionOffset = insertionOffset;
	}
	
	public void apply(IDocument document) {
		try {
			if (type.equals(JythonType.BUILTIN_FUNCTION) | type.equals(JythonType.FUNCTION)) {
				document.replace(insertionOffset, documentOffset - insertionOffset, objectName + "()");
			} else if (type.equals(JythonType.MODULE)) {
				document.replace(insertionOffset, documentOffset - insertionOffset, objectName + ".");
			} else {
				document.replace(insertionOffset, documentOffset - insertionOffset, objectName);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public String getAdditionalProposalInfo() {
		return null;
	}

	public IContextInformation getContextInformation() {
//		if (type.equals(JepType.BUILTIN_FUNCTION_OR_METHOD) | type.equals(JepType.FUNCTION)) {
//			
//		}
//		IContextInformation information = new ContextInformation("Display", "Info");
//		return information;
		return null;
	}

	public String getDisplayString() {
		if (type.equals(JythonType.BUILTIN_FUNCTION) | type.equals(JythonType.PY_JMETHOD)) {
			return objectName + "(?)" + " : " + type.getType();
		} else if (type.equals(JythonType.MODULE)) {
			return objectName + "." + " : " + type.getType();
		}
		return objectName + " : " + type.getType();
	}

	public Image getImage() {
		if (type.equals(JythonType.BUILTIN_FUNCTION)
				| type.equals(JythonType.FUNCTION)
				| type.equals(JythonType.PY_JMETHOD)) {
			return InternalImage.FUNCTION.getImage();
		} else if (type.equals(JythonType.MODULE)) {
			return InternalImage.MODULE.getImage();
		} else if (type.equals(JythonType.CLASS)) {
			return InternalImage.CLASS.getImage();
		} else if (type.equals(JythonType.STRING)) {
			return InternalImage.STRING.getImage();
		} else if (type.equals(JythonType.TYPE)) {
			return InternalImage.TYPE.getImage();
		} else if (type.equals(JythonType.LIST)) {
			return InternalImage.LIST.getImage();
		} else if (type.equals(JythonType.TUPLE)) {
			return InternalImage.TUPLE.getImage();
		}
		return null;
	}

	public Point getSelection(IDocument document) {
		if (type.equals(JythonType.BUILTIN_FUNCTION)) {
			return new Point(insertionOffset + objectName.length() + 2, 0);
		} else if (type.equals(JythonType.MODULE)) {
			return new Point(insertionOffset + objectName.length() + 1, 0);
		} else {
			return new Point(insertionOffset + objectName.length(), 0);
		}
	}

}
