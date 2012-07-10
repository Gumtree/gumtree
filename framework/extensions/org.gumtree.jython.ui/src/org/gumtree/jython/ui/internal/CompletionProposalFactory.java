package org.gumtree.jython.ui.internal;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;

public final class CompletionProposalFactory {

	public static final ICompletionProposal NO_PROSOPAL = new NoDefaultProposal();
	
	private static final String CONTEXT_ID = "function";
	
	private static final TemplateContextType contextType = new TemplateContextType(
			CONTEXT_ID, "Function Templates");
	
	public static final ICompletionProposal createDefaultProposal(
			String objectName, JythonType type, int documentOffset,
			int insertionOffset) {
		return new CompletionProposal(objectName, type, documentOffset,
				insertionOffset);
	}
	
	public static final ICompletionProposal createFunctionProposal(
			String objectName, String args, int insertionOffset,
			IDocument document) {
		StringBuilder builder = new StringBuilder();
		builder.append(objectName);
		builder.append("(");
		if (args.contains("[")) {
			args = args.substring(args.indexOf("[") + 1, args.lastIndexOf("]"));
			String[] argArray = args.split(";");
			for (int i = 0; i < argArray.length; i++) {
				String arg = argArray[i];
				if (arg.length() == 0) {
					continue;
				}
				builder.append("${");
				builder.append(arg.substring(arg.indexOf("'") + 1, arg
						.lastIndexOf("'")));
				builder.append("}");
				if (i != argArray.length - 1) {
					builder.append(", ");
				}
			}
		}
		builder.append(")");
		
		Template template = new Template(objectName, "", CONTEXT_ID, builder
				.toString(), false);
		Region region = new Region(insertionOffset, 0);
		TemplateContext templateContext = new DocumentTemplateContext(
				contextType, document, insertionOffset, 0);
		return new FunctionTemplateProposal(template, templateContext, region, InternalImage.FUNCTION.getImage());
	}

	private CompletionProposalFactory() {
		super();
	}
	
}
