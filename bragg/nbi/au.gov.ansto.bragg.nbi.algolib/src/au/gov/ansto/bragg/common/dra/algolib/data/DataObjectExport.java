package au.gov.ansto.bragg.common.dra.algolib.data;

import java.util.*;
import java.util.prefs.*;
import java.io.*;
public class DataObjectExport {
//	  ArrayList<NXElements[]> elems = new ArrayList<NXElements[]>();
      int numElem = 0;
      String[]  elems  = new String[numElem];
      String[]  eleval = new String[numElem];
  
	
	public void  setElements (Preferences p){
		int jeles = elems.length;
		for (int j=0; j<jeles; j++) p.put(elems[j], eleval[j]);
		
	}
	public enum NXElements{
		
		
	}
}
