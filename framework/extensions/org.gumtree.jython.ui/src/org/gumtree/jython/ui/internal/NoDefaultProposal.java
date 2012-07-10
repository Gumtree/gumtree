package org.gumtree.jython.ui.internal;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class NoDefaultProposal implements ICompletionProposal {

	public void apply(IDocument document) {
	}

	public String getAdditionalProposalInfo() {
		return null;
	}

	public IContextInformation getContextInformation() {
		return null;
	}

	public String getDisplayString() {
		return "No Default Proposals";
	}

	public Image getImage() {
		return null;
	}

	public Point getSelection(IDocument document) {
		return null;
	}

}
