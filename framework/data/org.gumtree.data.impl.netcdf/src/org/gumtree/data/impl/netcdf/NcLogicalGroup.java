package org.gumtree.data.impl.netcdf;

import java.util.List;

import org.gumtree.data.dictionary.IExtendedDictionary;
import org.gumtree.data.dictionary.ILogicalGroup;
import org.gumtree.data.dictionary.IPathParameter;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.utils.Utilities.ModelType;

public class NcLogicalGroup implements ILogicalGroup {
    
    public NcLogicalGroup(IKey key) {
    }
    
    @Override
    public IDataset getDataset() {
        throw new UnsupportedOperationException();
    }

	@Override
	public ModelType getModelType() {
		return ModelType.LogicalGroup;
	}

	@Override
	public String getLocation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IContainer getRootGroup() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getShortName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IDataItem getDataItem(IKey key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IDataItem getDataItem(String keyPath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IDataItem> getDataItemList(IKey key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IDataItem> getDataItemList(String keyPath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILogicalGroup getGroup(IKey key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILogicalGroup getGroup(String keyPath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILogicalGroup getParentGroup() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getKeyNames(ModelType model) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IKey bindKey(String bind, IKey key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IExtendedDictionary getDictionary() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDictionary(IDictionary dictionary) {
		throw new UnsupportedOperationException();
	}

	public ILogicalGroup clone() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}
	
	// TODO [SOLEIL][clement] below methods that don't seem relevant to me for this object
	// they are inherited from IObject 
    @Override
    public boolean removeAttribute(IAttribute attribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParent(IGroup group) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShortName(String name) {
    	throw new UnsupportedOperationException();
    }

	@Override
	public void addOneAttribute(IAttribute attribute) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addStringAttribute(String name, String value) {
		throw new UnsupportedOperationException();
	}
	
    @Override
    public boolean hasAttribute(String name, String value) {
    	throw new UnsupportedOperationException();
    }

    @Override
	public IAttribute getAttribute(String name) {
    	throw new UnsupportedOperationException();
	}

	@Override
	public List<IAttribute> getAttributeList() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IPathParameter> getParameterValues(IKey key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setParent(ILogicalGroup group) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IExtendedDictionary findAndReadDictionary() {
		throw new UnsupportedOperationException();
	}
	
}
