package org.gumtree.msw.ui;

public interface IModelValueConverter<TModel, TTarget> {
	// properties
	public Class<TModel> getModelValueType();
	public Class<TTarget> getTargetValueType();
	
	// methods
	public TTarget fromModelValue(TModel value);
	public TModel toModelValue(TTarget value);
}
