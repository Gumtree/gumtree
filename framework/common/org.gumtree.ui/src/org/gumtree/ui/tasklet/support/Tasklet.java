package org.gumtree.ui.tasklet.support;

import org.eclipse.e4.xwt.internal.utils.ObjectUtil;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.util.string.StringUtils;

import com.google.common.base.Objects;

public class Tasklet implements ITasklet {

	private String label;

	private String tags;

	private String contributionURI;
	
	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public String getTags() {
		return tags;
	}
	
	public String getContributionURI() {
		return contributionURI;
	}

	public String serialise() {
		return "";
	}

	public String toString() {
		return Objects.toStringHelper(this)
				.add("label", getLabel())
				.add("tags", getTags()).toString();
	}

}
