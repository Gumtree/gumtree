package org.gumtree.jython.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformationValidator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.ui.scripting.ICommandLineContentAssistProcessor;
import org.python.core.PyBoolean;
import org.python.core.PyBuiltinCallable;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyMethod;
import org.python.core.PyNone;
import org.python.core.PyType;

public class JythonContentAssistProcessor implements ICommandLineContentAssistProcessor {

	private static final ICompletionProposal[] EMPTY_PROPOSALS = new ICompletionProposal[0];
	
	private IScriptExecutor executor;
	
	// Cache for builtin functions details
	private volatile PyList builtinsOutput;
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		String text = viewer.getDocument().get();
		int startOffset = 0;
		// Find start offset
		for (int i = 0; i < text.length(); i++) {
			if (isSeparator(text, i)) {
				startOffset = i + 1;
			}
		}
		// If start method, offset increase by 1
		if (text.length() > startOffset && text.charAt(startOffset) == '(') {
			startOffset = startOffset - 1;	
		}
//		
//		
//		
//		LinkedList<String> tokens = new LinkedList<String>();
//		StringBuilder builder = new StringBuilder();
//		for (int i = 0; i < text.length(); i++) {
//			Character c = text.charAt(i);
//			if (Character.isWhitespace(text.charAt(i))) {
//				if (isSeparator(text, i)) {
//					tokens.add(builder.toString());
//					builder.replace(0, builder.length(), "");
//					continue;
//				}
//			}
//			builder.append(c);
//		}
//		// Storing missing element
//		if (builder.length() > 0) {
//			tokens.add(builder.toString());
//		}
//		if (tokens.size() > 0) {
//			if (!isSeparator(text, offset - 1)) {
//				text = tokens.getLast().trim();
//			} else {
//				text = "";
//			}
//		}
		
//		if (text.startsWith("from ")) {
//			return computeImportCompletionProposals(viewer, startOffset, offset);
//		} else {
			return computeNormalCompletionProposals(viewer, startOffset, offset);
//		}
	}
		
	public ICompletionProposal[] computeImportCompletionProposals(ITextViewer viewer, int startOffset, int endOffset) {
		return EMPTY_PROPOSALS;
	}
	
	public ICompletionProposal[] computeNormalCompletionProposals(ITextViewer viewer, int startOffset, int offset) {
		Context context = analysis(viewer, startOffset, offset);
		PyList result = queryCodeCompletion(context.completionContext);
		List<ICompletionProposal> proposalHolder = parse(context, result, offset);
		if (proposalHolder != null) {
//			if (context.completionContext == null || context.completionContext.length() == 0) {
//				// Add builtins
//				proposalHolder.addAll(parse(context, getBuiltinDetails(), viewer, offset));
//			}
			// Add default if no proposal found
			if (proposalHolder.size() == 0) {
				proposalHolder.add(CompletionProposalFactory.NO_PROSOPAL);
			}
			// Sort in alphabetical order
//			Collections.sort(proposalHolder, new Comparator<ICompletionProposal>() {
//				public int compare(ICompletionProposal prop1, ICompletionProposal prop2) {
//					if (prop1.getAdditionalProposalInfo() != null && prop2.getAdditionalProposalInfo() != null) {
//						return prop1.getAdditionalProposalInfo().compareTo(prop2.getAdditionalProposalInfo());
//					}
//					return 0;
//				}
//			});
			return proposalHolder.toArray(new ICompletionProposal[proposalHolder.size()]);
		} else {
			return EMPTY_PROPOSALS;
		}
//		try {
//			getScriptExecutor().getEngine().eval("__result__ = __info__('')");
//			Object result = getScriptExecutor().getEngine().get("__result__");
//			
//			getScriptExecutor().getEngine().eval("__result__ = x");
//			result = getScriptExecutor().getEngine().get("__result__");
//			System.out.println(result);
//		} catch (ScriptException e) {
//			e.printStackTrace();
//		}
	}

	protected Context analysis(ITextViewer viewer, int startOffset, int endOffset) {
		IDocument document = viewer.getDocument();
		String text = document.get().substring(startOffset, endOffset);
		// Nothing to analysis
		if (text.length() == 0) {
			return new Context("", "", endOffset);
		}
		
		// Find partially completed word and offset for insertion
		StringBuffer buf = new StringBuffer();
		while (endOffset >= startOffset) {
			try {
				char c = document.getChar(--endOffset);
				if (Character.isWhitespace(c) || c == '.' || c == '(') {
					endOffset++;
					break;
				}
				buf.append(c);
			} catch (Exception e) {
				endOffset++;
				break;
			}
		}
		String partiallyCompletedWord = buf.reverse().toString();
		int insertionOffset = endOffset;
		// Find completion context
//		buf = new StringBuffer();
//		boolean found = false;
//		while (true) {
//			try {
//				char c = document.getChar(--documentOffset);
//				if (c == '.') {
//					if (!found) {
//						found = true;
//						continue;	
//					}
//				} else if (Character.isWhitespace(c) || c == '(') {
//					break;
//				}
//				buf.append(c);
//			} catch (BadLocationException e) {
//				break;
//			}
//		}
		int completionContextIndex = insertionOffset - 1;
		if (completionContextIndex < startOffset) {
			completionContextIndex = startOffset;
		}
		String completionContext = document.get().substring(startOffset, completionContextIndex);
		return new Context(partiallyCompletedWord, completionContext, insertionOffset);
	}
	
	private PyList queryCodeCompletion(final String completionContext) {
		// Try not to bother the engine when it is busy
//		if (executor.isBusy()) {
//			return null;
//		}
		String query = "__result__ = __info__('" + completionContext + "')";
		
		try {
			executor.getEngine().getContext().getBindings(ScriptContext.ENGINE_SCOPE).put(IScriptExecutor.VAR_SILENCE_MODE, true);
			getScriptExecutor().getEngine().eval(query);
			return (PyList) getScriptExecutor().getEngine().get("__result__");
		} catch (ScriptException e) {
		}
		return new PyList();
//		executor.runTask(new Runnable() {
//			public void run() {
//				try {
//					String query = "__result__ = __info__('" + completionContext + "')";
//					executor.getEngine().getContext().getBindings(
//							ScriptContext.ENGINE_SCOPE).put(
//							IScriptExecutor.VAR_SILENCE_MODE, true);
//					executor.getEngine().eval(query);
//					byte[] data = ((ExtendedJepScriptEngine) executor
//							.getEngine()).getJep().getValue_bytearray(
//							"__result__");
//					engineOutput = new String(data);
//				} catch (Throwable e) {
//					errorFlag = true;
//				}
//			}
//		});
//		// Wait
//		LoopRunner.run(new ILoopExitCondition() {
//			public boolean getExitCondition() {
//				if (errorFlag) {
//					return true;
//				}
//				return engineOutput != null;
//			}
//		}, 1000);
//		if (!errorFlag && engineOutput != null) {
//			return engineOutput;
//		}
//		return null;
	}
	
	private List<ICompletionProposal> parse(Context context, PyList output, final int offset) { 
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (Object entry : output) {
			PyList entryList = (PyList) entry;
			String objectName = entryList.get(0).toString();
			// Only propose on currently typed string
			if (objectName.startsWith(context.partiallyCompletedWord)) {
				JythonType type = JythonType.UNKNOWN;
				if (entryList.get(1) instanceof Class) {
					Class typeObject = (Class) entryList.get(1);
					if (PyInteger.class.isAssignableFrom(typeObject)) {
						type = JythonType.INT;
					} else if (PyLong.class.isAssignableFrom(typeObject)) {
						type = JythonType.LONG;
					} else if (PyBoolean.class.isAssignableFrom(typeObject)) {
						type = JythonType.BOOLEAN;
					} else if (PyNone.class.isAssignableFrom(typeObject)) {
						type = JythonType.NONE;
					} else if (PyBuiltinCallable.class
							.isAssignableFrom(typeObject)) {
						type = JythonType.BUILTIN_FUNCTION;
					} else if (PyMethod.class.isAssignableFrom(typeObject)) {
						type = JythonType.FUNCTION;
					} else if (PyType.class.isAssignableFrom(typeObject)) {
						type = JythonType.CLASS;
					} else if (Class.class.isAssignableFrom(typeObject)) {
						type = JythonType.CLASS;
					}
				} else if (entryList.get(1) instanceof PyType) {

				}
				// if (typeObject instanceof Class) {
				// type = JythonType.CLASS;
				// } else if (typeObject.equals(PyBuiltinCallable.class)) {
				// type = JythonType.BUILTIN_FUNCTION;
				// } else if ()
				// Normal proposal
				proposals.add(CompletionProposalFactory.createDefaultProposal(
						objectName, type, offset, context.insertionOffset));
			}
		}
		return proposals;
	}
	
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		// Turn on code completion when '.' is typed
		return new char[] { '.' };
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		// Enable context information
		return new ContextInformationValidator(this);
	}

	@Override
	public IScriptExecutor getScriptExecutor() {
		return executor;
	}

	@Override
	public void setScriptExecutor(IScriptExecutor executor) {
		this.executor = executor;
	}

	private PyList getBuiltinDetails() {
		if (builtinsOutput == null) {
			synchronized (this) {
				if (builtinsOutput == null) {
					builtinsOutput = queryCodeCompletion("__builtin__");
				}
			}
		}
		return builtinsOutput;
	}
	
	class Context {
		
		String partiallyCompletedWord;
		String completionContext;
		int insertionOffset;

		private Context(String partiallyCompletedWord,
				String completionContext, int insertionOffset) {
			this.partiallyCompletedWord = partiallyCompletedWord;
			this.completionContext = completionContext;
			this.insertionOffset = insertionOffset;
		}
	}
	
	
	private static boolean isSeparator(String text, int position) {
		if (text == null || position >= text.length()) {
			return false;
		}
		if (text.charAt(position) == '(' && position == text.length() - 1) {
			return true;
		} else if (!Character.isWhitespace(text.charAt(position))) {
			return false;
		}
		
		LinkedList<Character> boundCharaters = new LinkedList<Character>();
		for (int i = 0; i < text.length(); i++) {
			if (i == position) {
				// Not bounded not '' or "" means it is a separator
				return boundCharaters.size() == 0;
			}
			Character c = text.charAt(i);
			if (c == '\'' || c == '\"') {
				if (boundCharaters.size() > 0) {
					if (boundCharaters.getLast().equals(c)) {
						// Level down
						boundCharaters.removeLast();
					} else {
						// Level up
						boundCharaters.add(c);	
					}
				} else {
					// Level up
					boundCharaters.add(c);
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		System.out.println(isSeparator("this is a \'sentence of sentence\'", 4));  	// True
		System.out.println(isSeparator("this is a \'sentence of sentence\'", 7));  	// True
		System.out.println(isSeparator("this is a \'sentence of sentence\'", 9));  	// True
		System.out.println(isSeparator("this is a \'sentence of sentence\'", 19));  // False
		System.out.println(isSeparator("this is a \'sentence of sentence\'", 22));  // False
		System.out.println(isSeparator("\' \" \" \' ", 1));							// False
		System.out.println(isSeparator("\' \" \" \' ", 3));							// False
		System.out.println(isSeparator("\' \" \" \' ", 5));							// False
		System.out.println(isSeparator("\' \" \" \' ", 7));							// True
	}
	
}
