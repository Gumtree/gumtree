package au.gov.ansto.bragg.common.dra.algolib.processes;

import au.gov.ansto.bragg.common.dra.algolib.data.DataProvider1D;
import au.gov.ansto.bragg.common.dra.algolib.data.DataProvider2D;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.collections.ArrayConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;

/**
 * A composite of several processors into a single processor.
 * @author  hrz
 */
public class ProcessorComposite extends Processor {

	private ArrayList<BoundProcessor> processors = new ArrayList<BoundProcessor>();
	private Signal commonIn;
	private Object[] parameters;
	
	/**
	 * @param processors  the processors to set
	 * @uml.property  name="processors"
	 */
	public void setProcessors(ArrayList<BoundProcessor> processors)
	{
		this.processors = processors;
	}
	
	/**
	 * @return  the processors
	 * @uml.property  name="processors"
	 */
	public ArrayList<BoundProcessor> getProcessors()
	{
		return processors;
	}
	
	public String toXML()
	{
		XStream xs = new XStream();
		xs.registerConverter(new JavaBeanConverter(xs.getMapper(),"class"));
		xs.registerConverter(new ArrayConverter(xs.getMapper()));
		xs.registerConverter(new CollectionConverter(xs.getMapper()));
		xs.registerConverter(new StringConverter());
		return xs.toXML(this);
	}
	
	/**
	 * @author  jgw
	 */
	private class BoundProcessor{
		Processor processor;
		BoundProcessor input;
		Signal lastOut;
		ArrayList<InputSource> argSources;
		
		/**
		 * @return  the processor
		 * @uml.property  name="processor"
		 */
		public Processor getProcessor()
		{
			return processor;
		}
		
		/**
		 * @param processor  the processor to set
		 * @uml.property  name="processor"
		 */
		public void setProcessor(Processor processor)
		{
			this.processor = processor;
		}
		
		/**
		 * @return  the argSources
		 * @uml.property  name="argSources"
		 */
		public ArrayList<InputSource> getArgSources()
		{
			return argSources;
		}
		
		/**
		 * @param argSources  the argSources to set
		 * @uml.property  name="argSources"
		 */
		public void setArgSources(ArrayList<InputSource> argSources)
		{
			this.argSources = argSources;
		}
		
		/**
		 * @return  the input
		 * @uml.property  name="input"
		 */
		public BoundProcessor getInput()
		{
			return input;
		}
		
		/**
		 * @param input  the input to set
		 * @uml.property  name="input"
		 */
		public void setInput(BoundProcessor input)
		{
			this.input = input;
		}
		
		private void process()
		{
			Signal in = input==null?commonIn:input.lastOut;
			if(argSources != null)
			for(InputSource arg : argSources)
			{
				if(arg == null)
					continue;
				Object value = arg.retrieveInput();
				try{
					arg.descriptor().getWriteMethod().invoke(processor, new Object[] {value});
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			lastOut = processor.process(in);
			if(processor instanceof DataProvider1D || processor instanceof DataProvider2D)
				return; //Don't complain about data providers.
			else if(lastOut == null)
				System.out.println("processor "+processor+" produced null!");
			else if(lastOut.rawData() == null)
				System.out.println("processor "+processor+" produced empty Signal!");
		}
	}
	/**
	 * An interface for sources of arguments.
	 * @author hrz
	 *
	 */
	public interface InputSource{
		/**
		 * @return The data from this input source.
		 */
		public Object retrieveInput();
		public PropertyDescriptor descriptor();
	}
	/**
	 * @author hrz
	 * An InputSource from the arguments of the parent.
	 */
	public class ArgumentInputSource implements InputSource{
		int index;
		PropertyDescriptor descriptor;
		/**
		 * Creates an InputSource from the specified argument of the parent.
		 * @param index The index of the argument to use.
		 */
		public ArgumentInputSource(int index, PropertyDescriptor descriptor)
		{
			this.index = index;
			this.descriptor = descriptor;
		}
		
		public Object retrieveInput() {
			return parameters[index];
		}

		public PropertyDescriptor descriptor() {
			return descriptor;
		}
		
	}
	/**
	 * @author  hrz  An input source using the output of another process
	 */
	public class ProcessInputSource implements InputSource{

		Class target;
		BoundProcessor in;
		String property;
		transient PropertyDescriptor descriptor;
		/**
		 * Creates a new argument input source based on a processor.
		 * The processor must be a member of this composite.
		 * @param input The processor to use the output of
		 */
		public ProcessInputSource(Processor input, Class sourceClass, String name)
		{
			target = sourceClass;
			in = findBoundInstance(input);
			setProperty(name);
		}
		
		public Object retrieveInput() {
			if(in == null)
				return commonIn.rawData();
			return in.lastOut.rawData();
		}

		public PropertyDescriptor descriptor() {
			return descriptor;
		}
		
		public Processor getInput()
		{
			return in.processor;
		}
		
		public void setInput(Processor in)
		{
			this.in = findBoundInstance(in);
		}
		
		/**
		 * @return  the property
		 * @uml.property  name="property"
		 */
		public String getProperty()
		{
			return descriptor.getName();
		}
		
		/**
		 * @param property  the property to set
		 * @uml.property  name="property"
		 */
		public void setProperty(String name)
		{
			this.property = name;
			try{
			PropertyDescriptor[] pds = Introspector.getBeanInfo(target).getPropertyDescriptors();
			for(PropertyDescriptor pd : pds)
			{
				if(pd.getName().compareTo(name) == 0)
				{
					descriptor = pd;
					return;
				}
			}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			throw new NoSuchMethodError("Method "+name+" not in class "+target);
		}
		
	}
	/**
	 * An input source based on a mask manipulator.
	 * @author hrz
	 *
	 */
//	public class MaskInputSource implements InputSource{
//
//		private MaskManipulator mm;
//		private MaskManipulator.addMode mode;
//		PropertyDescriptor descriptor;
//		/**
//		 * Creates an inputsource from a mask manipulator for a specific
//		 * add mode.
//		 * @param mm The mask manipulator to observe.
//		 * @param mode The mask mode to treat as open.
//		 */
//		public MaskInputSource(MaskManipulator mm, MaskManipulator.addMode mode, PropertyDescriptor descriptor)
//		{
//			this.mm = mm;
//			this.mode = mode;
//			this.descriptor = descriptor;
//		}
//		
//		public Object retrieveInput() {
//			return mm.getMask(mode);
//		}
//
//		public PropertyDescriptor descriptor() {
//			return descriptor;
//		}
//		
//	}
	/**
	 * Exchanges a processor with another processor. Each processor
	 * takes the connections the other had (if any).
	 * @param proc1 The first processor
	 * @param proc2 The other processor
	 */
	public void hotSwap(Processor proc1, Processor proc2)
	{
		for(BoundProcessor p : processors)
		{
			if(p.processor == proc1)
				p.processor = proc2;
			else if(p.processor == proc2)
				p.processor = proc1;
		}
	}
	
	private BoundProcessor findBoundInstance(Processor in)
	{
		for(BoundProcessor b : processors)
		{
			if(in == b.processor)
				return b;
		}
		return null;
	}
	
	public void bindArgumentToOutput(Processor target, Processor source, String property)
	{
		try{
		BoundProcessor boundTarget = findBoundInstance(target);
				if(boundTarget.argSources == null)
					boundTarget.argSources = new ArrayList<InputSource>();
				boundTarget.argSources.add(new ProcessInputSource(source,target.getClass(),property));
				return;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param effcorrect
	 * @param input
	 * @param composite 
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void addProcessor( Processor input, ProcessorComposite composite) throws Exception
	{
		BoundProcessor in = null;
		for(BoundProcessor pr : processors)
		{
			if(pr.processor == input)
			{
				in = pr;
				break;
			}
		}
		if(in == null && input != this)
			throw new Exception("Add input processor first");
		BoundProcessor newProc = new BoundProcessor();
		newProc.input = in;
		//if(input != this && proc.getInputType().isAssignableFrom(input.getReturnType()))
		//	throw new WrongDataTypeException("Input data type "+proc.getInputType()+" not assignable from "+input.getReturnType());
		newProc.lastOut = null;
//		newProc.processor = effcorrect;
		processors.add(newProc);
	}
	
	@Override
	public Signal process(Signal in){
		commonIn = in;
		for(BoundProcessor bp : processors)
			bp.process();
		return processors.get(processors.size()-1).lastOut;
	}

	/**
	 * @param parameters  the parameters to set
	 * @uml.property  name="parameters"
	 */
	public void setParameters(Object[] params) {
		this.parameters = params;
	}

}
