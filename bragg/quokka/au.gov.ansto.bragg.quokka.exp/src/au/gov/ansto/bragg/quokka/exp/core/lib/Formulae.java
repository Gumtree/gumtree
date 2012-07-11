/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.quokka.exp.core.lib;

import java.math.BigDecimal;

public class Formulae {
	private static double pi = Math.PI;
	
	/**
	 * s = a0*sin(x*2pi/T+a1)+c
	 * parameters: a0, x, a2, T, c, d(common difference)
	 */
	
	public static double[] generateSineData(int a0, double t, int x, double a1, int c, int size){
		return generateSineData(a0, 1, t, x, a1, c, size);
	}
    public static double[] generateSineData(int a0, int increment, double t, 
    		int x, double a1, int c, int size){
    	double[] d = new double[size];
    	for(int i=0; i<size; i++){
    		d[i] = (int)Math.round(c+a0*Math.sin(2*pi*x/t+a1));
    		x+=increment;
    	}
    	return d;
    }
    /**
     * Generate 2D or 3D sine curve data
     * @param a0
     * @param increment
     * @param t
     * @param x
     * @param a1
     * @param c
     * @param size
     * @param expandSize
     * @return
     * @throws Exception
     */
	public static Object generateSineData(int a0, int[] increment, double t, int[] x,
			double a1, int c, int[] size) throws Exception{
		int dim = size.length;
		Object o = new Object();
		switch(dim){
        case 2: o = generate2DSineData(a0, increment, t, x, a1, c, size);
                break;
        case 3: o = generate3DSineData(a0, increment, t, x, a1, c, size);	   	        
                break;	
        default: throw new Exception("Unhandled dimension size: "+dim);
		}
		return o;
	}
	
	private static double[][] generate2DSineData(int a0, int[] increment, double t, 
    		int[] x, double a1, int c, int[] size){
    	
    	int width = size[0];
    	int height = size[1];
    	
    	double[] hVector = generateSineData(width, increment[0], t, x[0], a1, c, width);
		double[] wVector = generateSineData(height, increment[1], t, x[1], a1, c, height);
		double[][] d = new double[height][width];
    	for(int i=0; i<height; i++)
    	{
    		for(int j=0; j<width; j++)
    		{
    			d[i][j] = (int)(hVector[i]*wVector[j]);
    		}
    	}
    	return d;
    }
    
    private static double[][][] generate3DSineData(int a0, int[] increment, double t, 
    		int[] x, double a1, int c, int[] size){
    	
    	int width = size[0];
    	int height = size[1];
    	int length = size[2];    	
    	double[] hVector = generateSineData(width, increment[0], t, x[0], a1, c, width);
		double[] wVector = generateSineData(height, increment[1], t, x[1], a1, c, height);
		double[] lVector = generateSineData(length, increment[2], t, x[2], a1, c, length);
		double[][][] d = new double[height][width][length];
    	for(int i=0; i<height; i++)
    	{
    		for(int j=0; j<width; j++)
    		{
    			for(int k=0; k<length; k++)
    			{
    			  d[i][j][k] = (int)(hVector[i]*wVector[j]*lVector[k]);
    			}
    		}
    	}
    	return d;
    }
	/**
	 * 1/var*sqrt(2*pi) * exp(-(x-mean)^2/2*var^2)
	 */
    public static double[] generateGaussian(int size, double mean, double var){
    	return generateGaussian(size, mean, var, 1, 1);
    }
    public static double[] generateGaussian(int size, double mean, double var, int coefficient){
    	return generateGaussian(size, mean, var, 1, coefficient);
    }
    public static double[] generateGaussian(int size, double mean, double var, double increment){
    	return generateGaussian(size, mean, var, increment, 1);
    }
	public static double[] generateGaussian(int size, double mean, double var, double increment, int coefficient){
		double[] d = new double[size];
		int x = 0;
		for(int i=0; i<size; i++)
		{
			d[i] = coefficient*((1/(var*Math.sqrt(2*pi)))*Math.exp(((-(x-mean)*(x-mean)))/2*var*var));	
			x+=increment;
		}
		return d;
	}
	public static Object generateGaussian(int[] size, double mean, double var)
	throws Exception{
		double[] d = {1, 1};
		return generateGaussian(size, mean, var, d);
	}
	
	public static Object generateGaussian(int[] size, double mean, double var, double coef)
	throws Exception{
		double[] d = {1, 1};
		return generateGaussian(size, mean, var, d, coef);
	}
	
	public static Object generateGaussian(int[] size, double mean, double var, double[] increment)
	throws Exception{
		return generateGaussian(size, mean, var, increment, 1);
	}
	public static Object generateGaussian(int[] size, double mean, double var, double[] increment, double coef)
	throws Exception{
		int dim = size.length;
		Object o = new Object();
		switch(dim){
		case 2: o = generate2DGaussian(size, mean, var, increment, coef);
                break;
        case 3: o = generate3DGaussian(size, mean, var, increment, coef);	   	        
                break;
        default: throw new Exception("Unhandled dimension size: "+dim);
		}
		return o;
	}
	
	private static double[][] generate2DGaussian(int[] dimension, double mean, double var, double[] increment, double coefficient){
		int height = dimension[0];
		int width = dimension[1];
		double hInc = increment[0];
		double wInc = increment[1];
		double[] hVector = generateGaussian(width, mean, var, wInc);		
		double[] wVector = generateGaussian(height, mean, var, hInc);
		double[][] d = new double[height][width];
		for(int i=0; i<height; i++)
		{
			for(int j=0; j<width; j++)
			{					
				d[i][j] = coefficient*hVector[i]*wVector[j];
			}
		}
		return d;
	}    
	private static double[][][] generate3DGaussian(int[] dimension, double mean, double var, double[] increment, double coefficient){
		int height = dimension[0];
		int width = dimension[1];
		int length = dimension[2];
		
		double[] hVector = generateGaussian(width, mean, var, increment[0]);		
		double[] wVector = generateGaussian(height, mean, var, increment[1]);
		double[] lVector = generateGaussian(length, mean, var, increment[2]);
		double[][][] d = new double[height][width][length];
		for(int i=0; i<height; i++)
		{
			for(int j=0; j<width; j++)
			{			
				for(int k=0; k<length; k++)
				{
				    d[i][j][k] = coefficient*hVector[i]*wVector[j]*lVector[k];
				}
			}
		}
		return d;
	}    
	public static double[] generatePlatypus(int size, double start, double peak)
	throws Exception{
		return generatePlatypus(size, start, peak, 1);
	}
	public static double[] generatePlatypus(int size, double start, double peak,
			double increment) throws Exception{
		double[] in = new double[size];
		int distance = countDistance(start, peak, increment);
		int i = 0;
		for(; i<distance; i++){
			in[i] = start;
			start+=increment;		
		}
		int temp = size-distance;
		for(; i<temp; i++)
			in[i] = peak;
		peak -= increment;
		for(; i<size; i++)
		{
			in[i] = peak;
			peak -= increment;
		}
		return in;
	}
	public static Object generatePlatypusData(int[] size, double[] originalPT, 
			double[] peakPT, double[] increment) throws Exception{
		int dim = size.length;
		Object o = new Object();
		switch(dim){
		case 2: o = generate2DPlatypus(size, originalPT, peakPT, increment);
		        break;
		case 3: o = generate3DPlatypus(size, originalPT, peakPT, increment);
		        break;
		default: throw new Exception("Unhandled dimension size: "+dim);
		}
		return o;
	}
	private static double[][] generate2DPlatypus(int[] size, double[] start, 
			double[] peak, double[] inc) throws Exception{
		int height = size[0];
		int width = size[1];
		double[][] d = new double[height][width];
		double[] hVector = generatePlatypus(height, start[0], peak[0], inc[0]);
		double[] wVector = generatePlatypus(width, start[1], peak[1], inc[1]);
		for(int i=0; i<height; i++)
		{
			for(int j=0; j<width; j++)
			{				
				BigDecimal bd = new BigDecimal(hVector[i]*wVector[j]).setScale(3,BigDecimal.ROUND_HALF_UP);	
				d[i][j] = new Double(bd.doubleValue());
				if(d[i][j]<0)
					{
					System.out.println(hVector[i]+" "+wVector[j]+" "+d[i][j]);
					}
			}
		}
		return d;
	}
	private static double[][][] generate3DPlatypus(int[] size, double[] start, 
			double[] peak, double[] inc) throws Exception{
		int height = size[0];
		int width = size[1];
		int length = size[2];
		double[][][] d = new double[height][width][length];
		double[] hVector = generatePlatypus(height, start[0], peak[0], inc[0]);
		double[] wVector = generatePlatypus(width, start[1], peak[1], inc[1]);
		double[] lVector = generatePlatypus(length, start[2], peak[2], inc[2]);
		for(int i=0; i<height; i++)
		{
			for(int j=0; j<width; j++)
			{	
				for(int k=0; k<length; k++)
				{
				    BigDecimal bd = new BigDecimal(hVector[i]*wVector[j]*lVector[k]).setScale(3,BigDecimal.ROUND_HALF_UP);	
				    d[i][j][k] = new Double(bd.doubleValue());
				}
			}
		}
		return d;
	}
	public static Object duplicate(int time, Object data)throws Exception{
		int dimension = getDimension(data);
		Object o = new Object();
		switch (dimension){
        case 1: o = dup1Ddata(time, data);
                break;
        case 2: o = dup2Ddata(time, data);	   	        
                break;		    
//        default: throw new HdfException("Unhandled Dataset/Variable data");
		}
		return o;
	}
//	public static Attribute makeAttribute(String aName, int aSize, FileFormat f, String[] content)
//    throws Exception
//    {
//		long[] dim = {1};
//		Datatype dtype = f.createDatatype(Datatype.CLASS_STRING, aSize+1, Datatype.NATIVE, Datatype.NATIVE);
//        Attribute attr = new Attribute(aName, dtype, dim);
//        attr.setValue(content); 
//        return attr;
//    }
	private static double[][] dup1Ddata(int time, Object data)throws Exception{
		double[][] in = new double[time][];
		double[] temp = (double[]) data;
		for(int i=0; i<time; i++)
		{
			in[i]=temp;
		}
		return in;
	}
	private static double[][][] dup2Ddata(int time, Object data)throws Exception{
		double[][][] in = new double[time][][];
		double[][] temp = (double[][]) data;
		for(int i=0; i<time; i++)
		{
			in[i]=temp;
		}
		return in;
	}
	private static int getDimension(Object d) throws Exception{
		String s = (String) d.getClass().toString();
		int count = 0;
		String[] temp = s.split("");
		for(int i=0; i<temp.length; i++)
		{
			if(temp[i].equals("["))
				count++;
		}
		return count;
	}
	
	private static int countDistance(double start, double end, double increment){
		int i = 0;
		while(start<end)
		{
			start+=increment;
			i++;
		}
		return i;
	}
	
    
    /*
     * attribute 1: NX_class = NXentry
     * attribute 2: NX_class = NXdata
     * attribute 3: signal = data
     * attribute 4: signal = 1
     */
//    public static Attribute[] designAttribute(FileFormat f) throws Exception{
//        Attribute[] a = new Attribute[4];
//    	String rootGroup = "NX_class";
//		
//		String[] content1 = {"NXentry"};
//		String[] content2 = {"NXdata"};
//		
//		String dataGroup = "signal";
//		
//		String[] content3 = {"data"};		
//		String[] content4 = {"1"};
//	
//		a[0] = makeAttribute(rootGroup, content1[0].length(), f, content1);
//		a[1] = makeAttribute(rootGroup, content2[0].length(), f, content2);
//		a[2] = makeAttribute(dataGroup, content3[0].length(), f, content3);
//		a[3] = makeAttribute(dataGroup, content4[0].length(), f, content4);
//    
//        return a;
//    }
}
