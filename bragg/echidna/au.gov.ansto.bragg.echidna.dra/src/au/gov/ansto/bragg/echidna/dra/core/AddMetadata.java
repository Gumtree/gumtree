package au.gov.ansto.bragg.echidna.dra.core;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class AddMetadata extends ConcreteProcessor {
	NcGroup metadata_in = null;
    NcGroup metadata_out = null;
    private IGroup alignment_out = null;
    private Boolean runAlignment = false;
	@Override
	public Boolean process() throws Exception {
		if (metadata_in == null) {
			return false;
		}
		if (runAlignment) {
			metadata_out = null;
			setAlignment_out(metadata_in);
		} else {
	    metadata_out = metadata_in;
	    setAlignment_out(null);
	    metadata_out.addMetadata("CIF", "_pd_instr_geometry", 
	    		"Cylindrical array of vertical detector tubes centred on " +
	    		"sample illuminated by monochromatic neutrons");
	    metadata_out.addMetadata("CIF", "_pd_instr_location","Echidna High Resolution Powder Diffractometer at " +
	    		" OPAL facility, Bragg Institute, Australia");
	    metadata_out.addMetadata("CIF", "_pd_instr_soller_eq_spec/detc", "0.0833");
	    
	    //In absence of actual crystal face information in NeXuS file, try to guess based on the knowledge
	    //that it will either be the Ge115 or the Ge335.  If it is before April 1st 2009, then it is
	    //definitely the 115 in symmetric setting
	    Double mom = Double.parseDouble(extract_metadata(metadata_in,"$entry/instrument/crystal/omega"));
	    Double tk_angle = Double.parseDouble(extract_metadata(metadata_in,"$entry/instrument/crystal/takeoff_angle"));
	    String monotype = "";        //Try and work this out
	    SimpleDateFormat date_form = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
//	    String file_time = extract_metadata(metadata_in,"/@file_time");
	    String file_time = metadata_in.getRootGroup().findAttribute("file_time").getStringValue();
    	Date file_date = date_form.parse(file_time);
    	Date mono_change = date_form.parse("2009-04-01 01:00:00+1000");
    	if(file_date.before(mono_change)) monotype="115";
    	else if(Math.abs(mom-tk_angle/2.0)>5.0) monotype = "335";
    	else // This is where updates for changed monochromators are inserted
    		monotype = "335";
	    String hklval = pick_hkl(mom-tk_angle/2.0,monotype);
	    double wavelength = 0.0;
	    String coll_string = "";
	    if(!hklval.equals("Unknown")) {        
	    		    metadata_out.addMetadata("CIF", "_pd_instr_monochr_pre_spec",
	    				hklval + " reflection from Ge crystal, "+monotype+" cut");
	    		    wavelength = calc_wavelength(hklval,tk_angle);
	    		    metadata_out.addMetadata("CIF", "_diffrn_radiation_wavelength",String.format("%.3f",wavelength));
	    		    metadata_out.addMetadata("CIF","_[local]_diffrn_radiation_wavelength_determination",
	    		    		"Wavelength is calculated from monochromator hkl and takeoff angle and is therefore approximate");
	    		    // Note that the divergence stated below will be replaced by something else if
	    		    // the primary collimator is found to be inserted in the code below
	    		    metadata_out.addMetadata("CIF", "_pd_instr_divg_eq_src/mono", String.format("%.3f",0.099*2.0*wavelength));
	    		    }
	    
	    double pcr = Double.parseDouble(extract_metadata(metadata_in,"$entry/instrument/collimator/primary_collimator_rotation"));
	    double pcx = Double.parseDouble(extract_metadata(metadata_in,"$entry/instrument/collimator/primary_collimator_translation"));
	    //Perform some logic based on motor positions
	    if(pcx>120) {
	    	if(Math.abs(pcr-360.0) < 5 || Math.abs(pcr) < 5 ) { // 5' collimator
	    		coll_string = "A 5' primary collimator pre-monochromator";
	    		metadata_out.addMetadata("CIF","_pd_instr_divg_eq_src/mono","0.0833");}
	    	else {
	    		metadata_out.addMetadata("CIF","_pd_instr_divg_eq_src/mono","0.1667"); //10' collimator
	    		coll_string = "A 10' primary collimator pre-monochromator";
	    	}
	    } else coll_string = "No primary collimator ";
	    String sc = extract_metadata(metadata_in,"$entry/sample/secondary_collimator");
	    if(sc!="?"&& Double.parseDouble(sc)>0.5) {
	    	coll_string = coll_string + " and a 10' secondary collimator post-monochromator.";
	    	metadata_out.addMetadata("CIF","_pd_instr_divg_eq_mono/spec","0.1667");
	    } else coll_string = coll_string + " and no secondary collimator.";
	    metadata_out.addMetadata("CIF","_diffrn_radiation_collimation",coll_string);
	    // Deal with temperature measurements
	    // We wait for some datafile support on this one...
		}
		return false;
	}
	
	/*
	 * Private function to match omega rotation to hkl index for the 335 crystal used on
	 * Echidna.  Offset should be the difference between omega and half the takeoff angle
	 * 
	 */
	private String pick_hkl(double offset,String monotype) {
		double [] offsets = {40.31,15.08,24.52,28.89,5.05,20.84,36.42,14.42,9.096,0.0};
		String [] hkls = {"004","113","115","117","224","228","331","333","337","335"};
		if(monotype=="115") return monotype;
		double abs_off = Math.abs(offset);
		if(monotype=="335")
			for (int i=0;i<offsets.length;i++) {
			   if (Math.abs(abs_off - offsets[i])<2.5)
				   return hkls[i];
		}
		return "Unknown";
	}
	
	/*
	 *  Given a Ge crystal hkl string, calculate the wavelength
	 *  lambda = 2*d*sin(theta), d = sqrt(h**2+k**2+l**2)*a
	 */
	
	private double calc_wavelength(String hklval,double two_theta) {
		int hkl = Integer.parseInt(hklval);
		int h = hkl/100;
		int k = (hkl - (100*h))/10;
		int l = hkl - 100*h - 10*k;
		double d = 5.657906/Math.sqrt(h*h+k*k+l*l); //Ge crystal spacing
		return 2*d*Math.sin(Math.PI*two_theta/360.0);
	}
	
	private String extract_metadata(IGroup input_data, String first_location) {
		String first_string = "";
		boolean is_number = false;
		try {
			IArray rawdata = null;
			Object item = input_data.getContainer(first_location);
			if(item==null) item = input_data.findContainerByPath(first_location);
			if (item instanceof IDataItem){
				rawdata = ((IDataItem) item).getData();
			}
			else if (item instanceof IAttribute)
				 rawdata = ((IAttribute) item).getValue();
			if (rawdata.getElementType() == Character.TYPE)
				first_string = rawdata.toString();
			else if (rawdata.getElementType() == "".getClass()) first_string = rawdata.toString();
			else  { //Report an average value
				first_string = String.format("%f", rawdata.getArrayMath().sum() / rawdata.getSize());
				is_number = true;
			}
		} catch (Exception e) {}
		if (first_string.equalsIgnoreCase("")) return "?";  //unknown
		return first_string;
	}
	
	public IGroup getMetadata_out() {
		return metadata_out;
	}
	public void setMetadata_in(IGroup metadata_in) {
		this.metadata_in = (NcGroup) metadata_in;
	}

	/**
	 * @param runAlignment the runAlignment to set
	 */
	public void setRunAlignment(Boolean runAlignment) {
		this.runAlignment = runAlignment;
	}

	/**
	 * @return the runAlignment
	 */
	public Boolean getRunAlignment() {
		return runAlignment;
	}

	/**
	 * @param alignment_out the alignment_out to set
	 */
	public void setAlignment_out(IGroup alignment_out) {
		this.alignment_out = alignment_out;
	}

	/**
	 * @return the alignment_out
	 */
	public IGroup getAlignment_out() {
		return alignment_out;
	}

}
