package org.gumtree.jython.ui.internal;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;

public class FunctionTemplateProposal extends TemplateProposal {
	
	public FunctionTemplateProposal(Template template, TemplateContext context,
			IRegion region, Image image) {
		super(template, context, region, image);
	}

	public String getDisplayString() {
		return super.getAdditionalProposalInfo();
	}
	
	public String getAdditionalProposalInfo() {
		return null;
	}
	
}
