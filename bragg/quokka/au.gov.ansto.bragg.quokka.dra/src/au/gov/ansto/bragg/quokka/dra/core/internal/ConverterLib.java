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
package au.gov.ansto.bragg.quokka.dra.core.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;
import org.osgi.framework.Bundle;

import au.gov.ansto.bragg.quokka.dra.internal.Activator;


/**
 * @author nxi
 * Created on 16/05/2008
 * @deprecated use GDM ConverterLib instead
 * @see org.gumtree.data.gdm.core.lib.ConverterLib
 */
public class ConverterLib {
	
	static String dictionaryPath;
	
	public static double[][][] get3DDouble(IArray array){
		int[] shape = array.getShape();
		double[][][] data = new double[shape[0]][shape[1]][shape[2]];
		IIndex index = Factory.createIndex(shape);
		for (int i = 0; i < shape[0]; i ++){
			index.set0(i);
			for (int j = 0; j < shape[1]; j++){
				index.set1(j);
				for (int k = 0; k < shape[2]; k++){
					index.set2(k);
					data[i][j][k] = array.getDouble(index);
//					System.out.println(index.toString() + " " + i + " " + j + " " + k + " " + data[i][j][k]);
				}
			}
		}
		return data;
	}

	public static double[][] get2DDouble(IArray array){
		int[] shape = array.getShape();
		double[][] data = new double[shape[0]][shape[1]];
		IIndex index = Factory.createIndex(shape);
		for (int i = 0; i < shape[0]; i ++){
			index.set0(i);
			for (int j = 0; j < shape[1]; j++){
				index.set1(j);
				data[i][j] = array.getDouble(index);
			}
		}
		return data;
	}

	public static double[] get1DDouble(IArray array){
		int[] shape = array.getShape();
		double[] data = new double[shape[0]];
		IIndex index = Factory.createIndex(shape);
		for (int i = 0; i < shape[0]; i ++){
			index.set(i);
			data[i] = array.getDouble(index);
		}
		return data;
	}	

	public static long[] get1DLong(IArray array){
		int[] shape = array.getShape();
		long[] data = new long[shape[0]];
		IIndex index = Factory.createIndex(shape);
		for (int i = 0; i < shape[0]; i ++){
			index.set(i);
			data[i] = array.getLong(index);
		}
		return data;
	}	

	public static Double getDouble(IArray array){
		return array.getDouble(Factory.createIndex(new int[]{0}));
	}

	public static double[] get1DDoubleStorage(double[][] data){
		double[] storage = new double[data.length * data[0].length];
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[0].length; j++)
				storage[i * data[0].length + j] = data[i][j];
		return storage;
	}
	
	public static double[] get1DDoubleStorage(double[][][] data){
		double[] storage = new double[data.length * data[0].length * data[1].length];
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[0].length; j++)
				for (int k = 0; k < data[1].length; k++)
					storage[(i * data[0].length + j) * data[1].length + k] = data[i][j][k];
		return storage;
	}

	public static String getDictionaryPath() throws IOException, URISyntaxException {
		if (dictionaryPath == null){
			Bundle bundle = null;
			bundle = Platform.getBundle(Activator.PLUGIN_ID);
			URL pluginXMLURL = FileLocator.toFileURL(FileLocator.find(bundle, new Path("xml/path_table.txt"), null));
			dictionaryPath = pluginXMLURL.toURI().getPath();
		}
		return dictionaryPath;
	}

	public static URI getURIFromPath(String path) throws URISyntaxException{
		path = path.replace('\\', '/');
		if (!path.startsWith("/")) 
			path = "/" + path;
		return new URI("file:" + path);
	}
}
