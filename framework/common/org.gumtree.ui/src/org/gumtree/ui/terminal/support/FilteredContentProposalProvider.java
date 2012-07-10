package org.gumtree.ui.terminal.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

public class FilteredContentProposalProvider implements IContentProposalProvider {

	private Map<String, IContentProposal> proposalMap;

	public FilteredContentProposalProvider() {
		proposalMap = new TreeMap<String, IContentProposal>();
	}

	public IContentProposal[] getProposals(String content, int position) {
		if(content == null || content.length() == 0) {
			return proposalMap.values().toArray(new IContentProposal[proposalMap.values().size()]);
		}
		List<IContentProposal> matchedProposals = new ArrayList<IContentProposal>();
		for(Entry<String, IContentProposal> proposalEntry : proposalMap.entrySet()) {
			if(proposalEntry.getKey().startsWith(content)) {
				matchedProposals.add(proposalEntry.getValue());
			}
		}
		return matchedProposals.toArray(new IContentProposal[matchedProposals.size()]);
	}

	public void addNewProposal(final String content) {
		if(!proposalMap.containsKey(content)) {
			proposalMap.put(content, new IContentProposal() {
				public String getContent() {
					return content;
				}
				public int getCursorPosition() {
					return getContent().length();
				}
				public String getDescription() {
					return null;
				}
				public String getLabel() {
					return null;
				}
			});
		}
	}

}
