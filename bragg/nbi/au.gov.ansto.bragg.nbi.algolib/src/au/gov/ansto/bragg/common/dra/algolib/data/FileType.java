package au.gov.ansto.bragg.common.dra.algolib.data;

public enum FileType {

	    	/**
	    	 * Constant representing sample files.
	    	 */
	    	SAMPLE,
	    	/**
	    	 * Constant representing empty cell files.
	    	 */
	    	EMPTY,
	    	/**
	    	 * Constant representing blocked beam files.
	    	 */
	    	BACKGROUND,
	    	/**
	    	 * Constant representing mask files.
	    	 */
	    	MASK,
	    	/**
	    	 * Constant representing detector sensitivity files.
	    	 */
	    	SENSITIVITY,
	    	/**
	    	 * Constant representing protocol files.
	    	 */
	    	PROTOCOL;
	    	public String toString(){
	    		switch(this)
	    		{
	    		case SAMPLE:
	    			return "sample";
	    		case EMPTY:
	    			return "empty";
	    		case BACKGROUND:
	    			return "blocked";
	    		case MASK:
	    			return "mask";
	    		case SENSITIVITY:
	    			return "sensitivity";
	    		case PROTOCOL:
	    			return "protocol";
	    		}
	    		return name();

	    }
}
