package org.gumtree.ui.tasklet;

import java.util.Map;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.core.object.IDisposable;

@SuppressWarnings("restriction")
public interface IActivatedTasklet extends IDisposable {
	
	public String getId();

	public String getLabel();
	
	public ITasklet getTasklet();
	
	public IEclipseContext getEclipseContext();
	
	public Composite getParentComposite();
	
	public void setParentComposite(Composite parent);

	public MPerspective getMPerspective();
	
	public void setMPerspective(MPerspective mPerspective);
	
	public Map<Object, Object> getContext();
	
}
