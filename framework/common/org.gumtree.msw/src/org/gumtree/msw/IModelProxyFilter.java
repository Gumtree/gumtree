package org.gumtree.msw;

public interface IModelProxyFilter {
	// methods
	public boolean validateProperty(Iterable<String> elementPath, String property, Object newValue);
	public boolean command(ICommand command);
}
