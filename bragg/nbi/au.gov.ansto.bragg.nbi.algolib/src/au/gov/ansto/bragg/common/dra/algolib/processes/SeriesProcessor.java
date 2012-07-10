package au.gov.ansto.bragg.common.dra.algolib.processes;

/**
 * A processor for applying another processor to a series of data sets.
 * @author  hrz
 */
public class SeriesProcessor extends Processor {

	private Processor child;
	/**
	 * Creates a new series processor with a dummy processor that feeds back
	 * what it is given.
	 *
	 */
	public SeriesProcessor()
	{
		child = new Processor(){
			public Signal process(Signal in)
			{
				return in;
			}
		};
	}
	/**
	 * Creates a new series processor.
	 * @param c The processor to perform the calculation on single data sets.
	 */
	public SeriesProcessor(Processor c)
	{
		child = c;
	}
	
	@Override
	public Signal process(Signal in){
		Object[] ins = in.dataAs(Object[].class);
		Object[] outs = new Object[ins.length];
		Signal o = new WrapperSignal(null,"Empty");
		for(int i = 0; i < ins.length; i++)
		{
			outs[i] = (o = child.process(new WrapperSignal(ins[i],in.name()))).rawData();
		}
		
		return new WrapperSignal(outs,o.name());
	}
	
	/**
	 * @param child  the child to set
	 * @uml.property  name="child"
	 */
	public void setChild(Processor child)
	{
		this.child = child;
	}
	
	/**
	 * @return  the child
	 * @uml.property  name="child"
	 */
	public Processor getChild()
	{
		return child;
	}
}
