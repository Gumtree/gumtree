package au.gov.ansto.bragg.kakadu.ui.widget;
/**
 * Transfer type to transfer SWT ImageData objects.
 */

import java.io.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class ImageTransfer extends ByteArrayTransfer {
	
	private static final String TYPENAME = "CF_BITMAP";
	private static final int TYPEID = 2;//registerType(TYPENAME);
	private static ImageTransfer _instance = new ImageTransfer();

public static ImageTransfer getInstance () {
	return _instance;
}

public void javaToNative (Object object, TransferData transferData) {
	if (!checkImage(object) || !isSupportedType(transferData)) {
		DND.error(DND.ERROR_INVALID_DATA);
	}
	ImageData imdata = (ImageData)object;
	try {
		// write data to a byte array and then ask super to convert to pMedium
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream writeOut = new DataOutputStream(out);
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData [] {imdata};
		loader.save(writeOut, SWT.IMAGE_BMP);
		writeOut.close();
		byte[] buffer = out.toByteArray();
		super.javaToNative(buffer, transferData);
		out.close();
	} 
	catch (IOException e) {
		e.printStackTrace();
	}
}
public Object nativeToJava(TransferData transferData){	
	if (!isSupportedType(transferData)) return null;
		
	byte[] buffer = (byte[])super.nativeToJava(transferData);
	if (buffer == null) return null;
	
	ImageData imdata;
	try {
		ByteArrayInputStream in = new ByteArrayInputStream(buffer);
		DataInputStream readIn = new DataInputStream(in);
		imdata = new ImageData(readIn);
		readIn.close();
	} catch (IOException ex) {
		ex.printStackTrace();
		return null;
	}
	return imdata;
}
protected String[] getTypeNames(){
	return new String[]{TYPENAME};
}
protected int[] getTypeIds(){
	return new int[] {TYPEID};
}
boolean checkImage(Object object) {
	return (object != null  && object instanceof ImageData);
}
protected boolean validate(Object object) {
	return checkImage(object);
}
}

//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.dnd.ByteArrayTransfer;
//import org.eclipse.swt.dnd.DND;
//import org.eclipse.swt.dnd.TransferData;
//import org.eclipse.swt.graphics.ImageData;
//import org.eclipse.swt.graphics.ImageLoader;
//
///**
// * Transfer type to transfer SWT ImageData objects.
// */
//public class ImageTransfer extends ByteArrayTransfer {
//
//  private static final String TYPENAME = "imagedata";
//
//  private static final int TYPEID = registerType(TYPENAME);
//
//  private static ImageTransfer _instance = new ImageTransfer();
//
//  public static ImageTransfer getInstance() {
//    return _instance;
//  }
//
//  public void javaToNative(Object object, TransferData transferData) {
//    if (!checkImage(object) || !isSupportedType(transferData)) {
//      DND.error(DND.ERROR_INVALID_DATA);
//    }
//    ImageData imdata = (ImageData) object;
//    try {
//      // write data to a byte array and then ask super to convert to
//      // pMedium
//      ByteArrayOutputStream out = new ByteArrayOutputStream();
//      DataOutputStream writeOut = new DataOutputStream(out);
//      ImageLoader loader = new ImageLoader();
//      loader.data = new ImageData[] { imdata };
//      loader.save(writeOut, SWT.IMAGE_BMP);
//      writeOut.close();
//      byte[] buffer = out.toByteArray();
//      super.javaToNative(buffer, transferData);
//      out.close();
//    } catch (IOException e) {
//    }
//  }
//
//  public Object nativeToJava(TransferData transferData) {
//    if (!isSupportedType(transferData))
//      return null;
//
//    byte[] buffer = (byte[]) super.nativeToJava(transferData);
//    if (buffer == null)
//      return null;
//
//    ImageData imdata;
//    try {
//      ByteArrayInputStream in = new ByteArrayInputStream(buffer);
//      DataInputStream readIn = new DataInputStream(in);
//      imdata = new ImageData(readIn);
//      readIn.close();
//    } catch (IOException ex) {
//      return null;
//    }
//    return imdata;
//  }
//
//  protected String[] getTypeNames() {
//    return new String[] { TYPENAME };
//  }
//
//  protected int[] getTypeIds() {
//    return new int[] { TYPEID };
//  }
//
//  boolean checkImage(Object object) {
//    return (object != null && object instanceof ImageData);
//  }
//
//  protected boolean validate(Object object) {
//    return checkImage(object);
//  }
//}
