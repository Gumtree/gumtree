package org.gumtree.msw.ui;

public abstract class ModelValueConverterAdapter<TModel, TTarget> implements IModelValueConverter<TModel, TTarget> {
	// fields
	private final Class<TModel> modelValueType;
	private final Class<TTarget> targetValueType;
	
	// construction
	protected ModelValueConverterAdapter(Class<TModel> modelValueType, Class<TTarget> targetValueType) {
		this.modelValueType = modelValueType;
		this.targetValueType = targetValueType;
	}
	
	// properties
	@Override
	public Class<TModel> getModelValueType() {
		return modelValueType;
	}
	@Override
	public Class<TTarget> getTargetValueType() {
		return targetValueType;
	}
}
