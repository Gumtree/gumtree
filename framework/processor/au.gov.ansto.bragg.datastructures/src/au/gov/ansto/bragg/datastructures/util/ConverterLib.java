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
package au.gov.ansto.bragg.datastructures.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.filesystem.IFileStore;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.util.eclipse.EclipseUtils;

public class ConverterLib {

	static String dictionaryPath;

	/**
	 * Convert the GDM array to 3D java primary double array. 
	 * @param array GDM Array
	 * @return primary java double array
	 * Created on 10/11/2008
	 */
	public static double[][][] get3DDouble(IArray array){
		int[] shape = array.getShape();
		double[][][] data = new double[shape[0]][shape[1]][shape[2]];
		IIndex index = array.getIndex();
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

//	public static double[][] get2DDouble(Array array){
//	int[] shape = array.getShape();
//	double[][] data = null;
//	Index index = null;
//	if (shape.length == 2){
//	data = new double[shape[0]][shape[1]];
//	index = Factory.createIndex(shape);
//	}else if (shape.length == 3 && shape[0] == 1){
//	data = new double[shape[1]][shape[2]];
//	index = Factory.createIndex(new int[]{shape[1], shape[2]});
//	}
//	for (int i = 0; i < data.length; i ++){
//	index.set0(i);
//	for (int j = 0; j < data[0].length; j++){
//	index.set1(j);
//	data[i][j] = array.getDouble(index);
//	}
//	}
//	return data;
//	}

	/**
	 * Convert the GDM array to 2D java primary array
	 * @param array GDM Array
	 * @return 2D java primary array
	 */
	public static double[][] get2DDouble(IArray array){
		boolean flattening = false; // ignoring a third dimension
		int[] shape = array.getShape();
		double[][] data = null;
		IIndex index = array.getIndex();
		if (shape.length == 2){
			data = new double[shape[0]][shape[1]];
		}else if (shape.length == 3 && shape[0] == 1){
			data = new double[shape[1]][shape[2]];
			index.set0(0);
			flattening = true;
		}
		for (int i = 0; i < data.length; i ++){
			if (flattening)
				index.set1(i);
			else index.set0(i);
			for (int j = 0; j < data[0].length; j++){
				if ( flattening)
					index.set2(j);
				else index.set1(j);
				data[i][j] = array.getDouble(index);
//				if (data[i][j] > 0)
//					System.out.println("i=" + i + "; j=" + j);
			}
		}
		return data;
	}


	/**
	 * Convert the GDM Array to one dimension primary java double array
	 * @param array GDM Array
	 * @return 1D java double array
	 * Created on 10/11/2008
	 */
	public static double[] get1DDouble(IArray array){
		int[] shape = array.getShape();
		double[] data = null;
		IIndex index = array.getIndex();
		if (shape.length == 1){
			data = new double[shape[0]];
		}
		else if (shape.length == 2 && shape[0] == 1){
			data = new double[shape[1]];
		}
		for (int i = 0; i < data.length; i ++){
			index.set(i);
			data[i] = array.getDouble(index);
		}
		return data;
	}	

	/**
	 * Convert the GDM array to a 1D primary java long array. 
	 * @param array GDM Array
	 * @return 1D java primary long array
	 * Created on 10/11/2008
	 */
	public static long[] get1DLong(IArray array){
		int[] shape = array.getShape();
		long[] data = new long[shape[0]];
		IIndex index = array.getIndex();
		for (int i = 0; i < shape[0]; i ++){
			index.set(i);
			data[i] = array.getLong(index);
		}
		return data;
	}	

	/**
	 * Get the first item of GDM array
	 * @param array GDM array
	 * @return double value
	 * Created on 10/11/2008
	 */
	public static Double getDouble(IArray array){
		return array.getDouble(array.getIndex().set0(0));
	}

	/**
	 * Convert the 2D primary array into a 1D array storage. 
	 * @param data primary 2D java primary array
	 * @return 1D java primary array
	 * Created on 10/11/2008
	 */
	public static double[] get1DDoubleStorage(double[][] data){
		double[] storage = new double[data.length * data[0].length];
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[0].length; j++)
				storage[i * data[0].length + j] = data[i][j];
		return storage;
	}

	/**
	 * Convert the 3D primary array into a 1D array storage. 
	 * @param data primary 3D java primary array
	 * @return 1D java primary array
	 * Created on 10/11/2008
	 */
	public static double[] get1DDoubleStorage(double[][][] data){
		double[] storage = new double[data.length * data[0].length * data[1].length];
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[0].length; j++)
				for (int k = 0; k < data[1].length; k++)
					storage[(i * data[0].length + j) * data[1].length + k] = data[i][j][k];
		return storage;
	}

	/**
	 * Find the dictionary path in the given plugin. The dictionary file is usually a text
	 * file named 'path_table.txt' in the xml folder of the plugin. 
	 * @param pluginID in String type
	 * @return String type 
	 * @throws IOException
	 * @throws URISyntaxException
	 * Created on 10/11/2008
	 */
	public static String getDictionaryPath(String pluginID) throws IOException, URISyntaxException {
		if (dictionaryPath == null){
//			Bundle bundle = null;
//			bundle = Platform.getBundle(pluginID);
//			URL pluginXMLURL = FileLocator.toFileURL(FileLocator.find(bundle, new Path("xml/path_table.txt"), null));
//			dictionaryPath = pluginXMLURL.toURI().getPath();
			IFileStore fileStore = null;
			try{
				fileStore = EclipseUtils.find(pluginID, "xml/path_table.txt");
			}catch (Exception e1){
				e1.printStackTrace();
			}
			dictionaryPath = fileStore.toURI().getPath();		
		}
		return dictionaryPath;
	}
	
	/**
	 * Convert the file path from String type to URI type. 
	 * @param filePath in String type
	 * @return a URI object
	 * @throws FileAccessException
	 * Created on 10/11/2008
	 */
	public static URI path2URI(String filePath) throws FileAccessException{
		if (filePath == null ||filePath.trim().length() == 0 || filePath.equals("null"))
			return null;
		URL url = null;
		try {
			url = new File(filePath).toURL();
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			throw new FileAccessException("can not find the file");
		}
		URI uri = null;
		try {
			uri = new File(filePath).toURI();
		} catch (Exception e) {
			// TODO: handle exception
			try {
				// this is the step that can fail, and so
				// it should be this step that should be fixed
				uri = url.toURI();
			} catch (URISyntaxException e1) {
				// OK if we are here, then obviously the URL did
				// not comply with RFC 2396. This can only
				// happen if we have illegal unescaped characters.
				// If we have one unescaped character, then
				// the only automated fix we can apply, is to assume
				// all characters are unescaped.
				// If we want to construct a URI from unescaped
				// characters, then we have to use the component
				// constructors:
				try {
					uri = new URI(url.getProtocol(), url.getUserInfo(), url
							.getHost(), url.getPort(), url.getPath(), url
							.getQuery(), url.getRef());
				} catch (URISyntaxException e2) {
					// The URL is broken beyond automatic repair
					throw new FileAccessException("broken URL: " + url);
				}
			}
		}
		return uri;
	}

	/**
	 * Find a file in the plugin with a given plugin id and file name. 
	 * @param pluginID in String type
	 * @param filename in String type
	 * @return a File object
	 * @throws FileAccessException
	 * Created on 10/11/2008
	 */
	public static File findFile(String pluginID, String filename) throws FileAccessException {
		IFileStore gumtreefile = null;
		try {
			gumtreefile = EclipseUtils.find(pluginID, filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FileAccessException("can not find the file");
		}
		return new File(gumtreefile.toURI());
	}
	
}
