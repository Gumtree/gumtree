package org.gumtree.ui.scripting.tools;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.gumtree.scripting.AttributeChangeEvent;
import org.gumtree.scripting.AttributeChangeEvent.AttributeChangeEventType;
import org.gumtree.scripting.IObservableComponent;
import org.gumtree.scripting.IScriptingListener;
import org.gumtree.scripting.ScriptingChangeEvent;
import org.gumtree.ui.util.SafeUIRunner;

public class AttributeContentProvider implements IStructuredContentProvider {

	private IObservableComponent context;
	
	private IScriptingListener engineListener;
	
	private StructuredViewer viewer;
	
	private Map<String, EngineAttribute> attributeMap;
	
	public Object[] getElements(Object inputElement) {
		return getAttributeMap().values().toArray(new EngineAttribute[getAttributeMap().size()]);
	}

	public void dispose() {
		if (engineListener != null && context != null) {
			context.removeListener(engineListener);
		}
		if (attributeMap != null) {
			attributeMap.clear();
			attributeMap = null;
		}
		engineListener = null;
		context = null;
		viewer = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput != null && oldInput == context) {
			if (engineListener != null) {
				context.removeListener(engineListener);
			}
			context = null;
		}
		if (newInput != null && newInput instanceof IObservableComponent) {
			context = (IObservableComponent) newInput;
			context.addListener(getEngineListener());
		}
		this.viewer = (StructuredViewer) viewer;
	}

	private Map<String, EngineAttribute> getAttributeMap() {
		if (attributeMap == null) {
			attributeMap = new Hashtable<String, EngineAttribute>();
		}
		return attributeMap;
	}
	
	private IScriptingListener getEngineListener() {
		if (engineListener == null) {
			engineListener = new IScriptingListener() {
				public void handleChange(ScriptingChangeEvent event) {
					if (event instanceof AttributeChangeEvent) {
						AttributeChangeEvent acEvent = (AttributeChangeEvent) event;
						if (acEvent.getType().equals(AttributeChangeEventType.SET)) {
							final EngineAttribute newAttribute = new EngineAttribute(acEvent.getName(), acEvent.getValue(), acEvent.getScope());
							getAttributeMap().put(acEvent.getName(), newAttribute);
							SafeUIRunner.asyncExec(new SafeRunnable() {
								public void run() throws Exception {
									viewer.refresh();
									viewer.setSelection(new StructuredSelection(newAttribute));
								}
							});
						} else if (acEvent.getType().equals(AttributeChangeEventType.REMOVED)) {
							getAttributeMap().remove(acEvent.getName());
							SafeUIRunner.asyncExec(new SafeRunnable() {
								public void run() throws Exception {
									viewer.refresh();
								}
							});
						}
					}
				}				
			};
		}
		return engineListener;
	}
	
	protected class EngineAttribute {
	
		private String name;
		
		private Object value;
		
		private int scope;
		
		private EngineAttribute(String name, Object value, int scope) {
			this.name = name;
			this.value = value;
			this.scope = scope;
		}

		public String getName() {
			return name;
		}

		public Object getValue() {
			return value;
		}

		public int getScope() {
			return scope;
		}
		
		// Restrictly we should not have to override this
		// but it is required for doing table viewer selection matching
//		public boolean equals(Object obj) {
//			if (obj instanceof EngineAttribute) {
//				// We only match in the name level
//				EngineAttribute att = (EngineAttribute) obj;
//				return getName().equals(att.getName());
//			}
//			return false;
//		}
//		
//		public int hashCode() {
//			// Make equal hash for equal attribute object
//			return getName().hashCode();
//		}
		
	}
	
}
