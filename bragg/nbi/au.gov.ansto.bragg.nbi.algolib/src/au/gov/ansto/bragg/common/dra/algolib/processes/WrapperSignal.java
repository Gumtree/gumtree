package au.gov.ansto.bragg.common.dra.algolib.processes;


/**
 * A simple implementation of the Signal interface that acts as a wrapper
 * for an object. It will attempt to unwrap Object[] arrays into their correct
 * type when hasData and dataAs are called.
 * @author hrz
 *
 */
public class WrapperSignal implements Signal {

	private Object data;
	private String name;
	/**
	 * Creates a new signal as a simple wrapper for an object.
	 * @param d The object to wrap.
	 * @param name The name of the data.
	 */
	public WrapperSignal(Object d, String name)
	{
		data = d;
		this.name = name;
	}

	public Object rawData() {
		return data;
	}
	
	private <T> boolean hasArrayData(Class<T[]> type, Class<T> ele)
	{
		Object[] o = (Object[])data;
		try{
		ele.cast(o[0]);
		return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}


	@SuppressWarnings("unchecked")
	public <T> boolean hasData(Class<T> type) 
	{
		try{
			type.cast(data);
		}
		catch(ClassCastException e)
		{
			if(type.isArray() && data instanceof Object[])
			{
				return hasArrayData((Class)type,(Class)type.getComponentType());
			}
			return false;
		}
		return true;
	}

	private <T extends Object> T[] dataAsArray(Class<T[]> arrClass, Class<T> c)
	{
		if(data instanceof Object[])
		{
			Object[] d = (Object[])data;
			T[] t = arrClass.cast(java.lang.reflect.Array.
				newInstance(c, d.length));
			for(int i = 0; i < d.length; i++)
				t[i] = (T)c.cast(d[i]);
			return t;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T dataAs(Class<T> c) {
		try{
		T t = c.cast(data);
		return t;
		}
		catch(Exception e)
		{
			if(c.isArray())
				return c.cast(dataAsArray((Class)c, (Class)c.getComponentType()));
		}
		return null;
		}

	public String name() {
		return name;
	}

}
