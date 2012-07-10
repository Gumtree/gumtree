package org.gumtree.ui.service.sidebar.support;

import static org.gumtree.ui.service.sidebar.support.GadgetRegistryConstants.GLOBAL_PERSPECTIVE_ID;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.ui.service.sidebar.IGadget;
import org.gumtree.ui.service.sidebar.IGadgetRegistry;
import org.gumtree.util.string.StringUtils;
import org.hamcrest.Matcher;

import ch.lambdaj.Lambda;
import ch.lambdaj.collection.LambdaCollections;
import ch.lambdaj.function.matcher.Predicate;

import com.google.common.base.Strings;

public class GadgetRegistry implements IGadgetRegistry {

	private volatile GadgetRegistryReader reader;
	
	// gadget list
	List<IGadget> gadgets;
	
	public GadgetRegistry() {
		gadgets = new ArrayList<IGadget>();
	}
	
	@Override
	public void addGadget(IGadget gadget) {
		gadgets.add(gadget);
	}

	@Override
	public void removeGadget(IGadget gadget) {
		gadgets.remove(gadget);
	}

	@Override
	public IGadget[] getAllGadgets() {
		checkReader();
		synchronized (gadgets) {
			return LambdaCollections.with(gadgets).toArray(IGadget.class);
		}
	}
	
	@Override
	public IGadget[] getGadgetsByPerspective(final String perspectiveId) {
		checkReader();
		synchronized (gadgets) {
			Matcher<IGadget> matcher = new Predicate<IGadget>() {
				@Override
				public boolean apply(IGadget gadget) {
					if (Strings.isNullOrEmpty(gadget.getPerspectives())) {
						return false;
					}
					if (GLOBAL_PERSPECTIVE_ID.equals(gadget.getPerspectives())) {
						return true;
					}
					if (StringUtils.split(gadget.getPerspectives(), ",").contains(perspectiveId)) {
						return true;
					}
					return false;
				}
			};
			return LambdaCollections.with(Lambda.filter(matcher, gadgets))
					.toArray(IGadget.class);
		}
	}

	protected void checkReader() {
		// Use lazy instantiation to avoid spending time on reading
		// the registry in the constructor
		// (help to improve Spring bean creation time)
		if (reader == null) {
			synchronized (this) {
				if (reader == null) {
					reader = new GadgetRegistryReader(this);
					reader.readGadgets();
				}
			}
		}
	}

}
