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
package au.gov.ansto.bragg.process.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import au.gov.ansto.bragg.process.common.Common;
import au.gov.ansto.bragg.process.exception.DimensionOutOfRangeException;
import au.gov.ansto.bragg.process.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.process.port.Port;
import au.gov.ansto.bragg.process.processor.Processor;

/**
 * @author nxi
 * Created on 06/02/2007, 3:24:20 PM
 * Last modified 06/02/2007, 3:24:20 PM
 * Not included in the package.
 * @param <T>
 */
public 	class SortedArrayList<T>
		extends ArrayList<Common> 
		implements List<Common> 
{

	public static final long serialVersionUID = 1L;
	
	public Common get(int id){
		
		Common common = null;
		for (Iterator<Common> iter = this.iterator(); iter.hasNext();){
			Common item = iter.next();
			if (item.getID() == id) common = item;
		}
		return common;
	}
	
	public static Common get(final List<?> list, final int id) throws IndexOutOfBoundException {
		Common common = null;
		for (Iterator<?> iter = list.iterator(); iter.hasNext();){
			Common item = (Common) iter.next();
//			System.out.println(item.getID());
			if (item.getID() == id) common = item;
		}
		if (common == null) throw new IndexOutOfBoundException("can't find such item");
		return common;
	}

	public static Port getPortFromName(final List<?> list, final String name) throws IndexOutOfBoundException{
		Port port = null;
		for (Iterator<?> iter = list.iterator(); iter.hasNext();){
			Port item = (Port) iter.next();
//			System.out.println(item.getID());
			if (item.getName().equals(name)) port = item;
		}
		if (port == null) throw new IndexOutOfBoundException("can't find port " + name);
		return port;
	}

	public static Common getObjectFromName(final List<?> list, final String name) throws Exception{
		Common object = null;
		for (Iterator<?> iter = list.iterator(); iter.hasNext();){
			Common item = (Common) iter.next();
//			System.out.println(item.getID());
			if (item.getName().equals(name)) object = item;
		}
		return object;
	}

	public static Port getPortFromReceipeName(final List<Port> list, final String name) throws IndexOutOfBoundException{
		Port port = null;
		for (Iterator<Port> iter = list.iterator(); iter.hasNext();){
			Port item = (Port) iter.next();
//			System.out.println(item.getName());
			if (item.getName().equals(name)) {
				port = item;
				break;
			}
		}
		if (port == null) throw new IndexOutOfBoundException("can't find such port " + name);
		return port;
	}
	
	public static Processor getProcessorFromName(final List<?> list, 
			final String name) throws IndexOutOfBoundException{
		Processor processor = null;
//		System.out.println(name);
		for (Iterator<?> iter = list.iterator(); iter.hasNext();){
			Processor item = (Processor) iter.next();
//			System.out.println(item.getName());
			if (item.getName().equals(name)) processor = item;
		}
		if (processor == null) throw new IndexOutOfBoundException("can't find such processor");
		return processor;
	}

	public static String arrayToString(Object array) {
		if (array == null) {
			return "[null]";
		} else {
			Object obj = null;
			if (array instanceof Hashtable) {
				array = ((Hashtable<?,?>)array).entrySet().toArray();
			} else if (array instanceof HashSet) {
				array = ((HashSet<?>)array).toArray();
			} else if (array instanceof Collection) {
				array = ((Collection<?>)array).toArray();
			}
			int length = Array.getLength(array);
			int lastItem = length - 1;
			StringBuffer sb = new StringBuffer("[");
			for (int i = 0; i < length; i++) {
				obj = Array.get(array, i);
				if (obj != null) {
					sb.append(obj);
				} else {
					sb.append("[null]");
				}
				if (i < lastItem) {
					sb.append(", ");
				}
			}
			sb.append("]");
			return sb.toString();
		}
	}
	
	public static int[] getDimensionArgument(final int dimension) throws DimensionOutOfRangeException {
		int[] argument = null;
		if (dimension > 3) throw new DimensionOutOfRangeException();
		else{
			switch(dimension){
			case 0: int[] temp0 = {0}; argument = temp0; break;
			case 1: int[] temp1 = {1}; argument = temp1; break;
			case 2: int[] temp2 = {1, 1}; argument = temp2; break;
			case 3: int[] temp3 = {1, 1, 1}; argument = temp3; break;
			}
		}
		return argument;
	}
	
	public static Double add(Double x, Double y){
		return x+y;
	}
	
	public static Double preAdd(Object x, Object y){
		Double[] a = (Double[]) x;
		Double[] b = (Double[]) y;
		Double result = 0D;
		if (a.length == b.length){
			for (int i = 0; i< a.length; i++) result += a[i]*b[i]; 
		}
		return result;
	}
/*	public static Port get(final List<Port> list, final int id){
		Port port = null;
		for (Iterator<Port> iter = list.iterator(); iter.hasNext();){
			Port item = iter.next();
			if (item.getID() == id) port = item;
		}
		return port;
	}

	public static Processor get(final List<Processor> list, final int id){
		Processor processor= null;
		for (Iterator<Processor> iter = list.iterator(); iter.hasNext();){
			Processor item = iter.next();
			System.out.println(item.getID());
			if (item.getID() == id) processor = item;
		}
		return processor;
	}
	*/
	
}
