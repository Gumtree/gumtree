package au.gov.ansto.bragg.koala.ui.scan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

enum ScanTarget {
	PHI_LOOP,
	PHI_POINTS,
	TEMP_LOOP,
	TEMP_POINTS;
	
	static String[] texts = {"PHI loop", "PHI points", "TEMP loop", "TEMP points"};
	
	public String getText() {
		switch (this) {
		case PHI_LOOP:
			return texts[0];
		case PHI_POINTS:
			return texts[1];
		case TEMP_LOOP:
			return texts[2];
		case TEMP_POINTS:
			return texts[3];
		default:
			return texts[0];
		}
	}
	
	public static ScanTarget valueOfText(String text) {
		if (texts[0].equals(text)) {
			return PHI_LOOP;
		} else if (texts[1].equals(text)) {
			return PHI_POINTS;
		} else if (texts[2].equals(text)) {
			return TEMP_LOOP;
		} else if (texts[3].equals(text)) {
			return TEMP_POINTS;
		} else {
			return PHI_LOOP;
		}
	}
	
	public static String[] getAllText() {
		return texts;
	}
	
	public boolean isPoints() {
		return this == PHI_POINTS || this == TEMP_POINTS;
	}
	
	public boolean isTemperature() {
		return this == TEMP_LOOP || this == TEMP_POINTS;
	}
};

public class SingleScan {

	private ScanTarget target;
	private float start;
	private float inc;
	private int number;
	private float end;
	private int exposure = 60;
	private int erasure = 10;
	private float temp = Float.NaN;
	private float chi = Float.NaN;
	private String status;
	private String comments;
	private String filename;
	private PropertyChangeSupport changeListener = new PropertyChangeSupport(this);
	private InputType inputLast;
	private InputType input2nd;
	private String points;

	enum InputType {START, INC, NUMBER, END};
	
	public SingleScan() {
		target = ScanTarget.PHI_LOOP;
	}
	public SingleScan(ScanTarget target) {
		this.target = target;
	}

	public ScanTarget getTarget() {
		return target;
	}
	public void setTarget(ScanTarget target) {
		this.target = target;
	}
	public float getStart() {
		return start;
	}
	public void setStart(float start) {
		Object old = this.start;
		this.start = start;
		firePropertyChange("start", old, start);
		calculateEntries(InputType.START);
	}
	
	public float getInc() {
		return inc;
	}
	public void setInc(float inc) {
		Object old = this.inc;
		this.inc = inc;
		firePropertyChange("inc", old, inc);
		calculateEntries(InputType.INC);
	}
	
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		Object old = this.number;
		this.number = number;
		firePropertyChange("number", old, number);
		calculateEntries(InputType.NUMBER);
	}
	
	public float getEnd() {
		return end;
	}
	public void setEnd(float end) {
		Object old = this.end;
		this.end = end;
		firePropertyChange("end", old, end);
		calculateEntries(InputType.END);
	}
	
	public int getErasure() {
		return erasure;
	}
	public void setErasure(int erasure) {
		Object old = this.erasure;
		this.erasure = erasure;
		firePropertyChange("erasure", old, erasure);
	}

	public int getExposure() {
		return exposure;
	}
	public void setExposure(int exposure) {
		Object old = exposure;
		this.exposure = exposure;
		firePropertyChange("exposure", old, exposure);
	}
	
	public float getTemp() {
		return temp;
	}
	public void setTemp(float temp) {
		Object old = temp;
		this.temp = temp;
		firePropertyChange("temp", old, temp);
	}
	public float getChi() {
		return chi;
	}
	public void setChi(float chi) {
		Object old = chi;
		this.chi = chi;
		firePropertyChange("chi", old, chi);
	}
	public String getStatus() {
		if (status != null) {
			return status;
		}
		return "";
	}
	public void setStatus(String status) {
		Object old = this.status;
		this.status = status;
		firePropertyChange("status", old, status);
	}
	
	public String getComments() {
		if (comments != null) {
			return comments;
		} else {
			return "";
		}
	}
	public void setComments(String comments) {
		Object old = this.comments;
		this.comments = comments;
		firePropertyChange("comments", old, comments);
	}
	public String getFilename() {
		if (filename != null) {
			return filename;
		} else {
			return "";
		}
	}
	public void setFilename(String filename) {
		Object old = this.filename;
		this.filename = filename;
		firePropertyChange("filename", old, filename);
	}
	public String getPoints() {
		if (points == null) {
			return "";
		} else {
			return points;
		}
	}
	public void setPoints(String points) {
		String[] items = points.split(",");
		for (int i = 0; i < items.length; i++) {
			Float.valueOf(items[i].trim());
		}
		Object old = this.points;
		this.points = points;
		firePropertyChange("points", old, points);
	}
	public SingleScan getCopy() {
		SingleScan scan = new SingleScan(target);
		scan.copyFrom(this);
		return scan;
	}
	
	public void copyFrom(SingleScan scan) {
		target = scan.getTarget();
		start = scan.getStart();
		inc = scan.getInc();
		number = scan.getNumber();
		end = scan.getEnd();
		exposure = scan.getExposure();
		erasure = scan.getErasure();
//		temp = scan.getTemp();
//		chi = scan.getChi();
		comments = scan.getComments();
		filename = scan.getFilename();
		points = scan.getPoints();
	}
	
	private void calculateEntries(InputType input) {
		if (input != InputType.START && input != inputLast) {
			input2nd = inputLast;
			inputLast = input;
		}
		if (inputLast == null || input2nd == null) {
			calculateScanFinal();
		} else if (inputLast == InputType.INC) {
			if (input2nd == InputType.NUMBER) {
				calculateScanFinal();
			} else if (input2nd == InputType.END) {
				calculateScanNumber();
			}
		} else if (inputLast == InputType.NUMBER) {
			if (input2nd == InputType.INC) {
				calculateScanFinal();
			} else if (input2nd == InputType.END) {
				calculateScanInc();
			}
		} else if (inputLast == InputType.END) {
			if (input2nd == InputType.INC) {
				calculateScanNumber();
			} else if (input2nd == InputType.NUMBER) {
				calculateScanInc();
			}
		}
	}
	
    private void calculateScanInc() {
    	float old = inc;
        if (number == 0) {
            return;
        } else if (number == 1) {
            inc = 0;
        } else {
            inc = (float) (Math.round((end - start) / (number - 1) * 1e5) / 1e5);
        }
        firePropertyChange("inc", old, inc);
    }
    
    private void calculateScanNumber() {
    	int old = number;
    	if (inc == 0) {
            number = 1;
        } else if (start == end) {
            number = 1;
        } else if (inc > 0) {
            number = 0;
            float s = start;
            while (s <= end) {
                s += inc;
                number += 1;
            }
            if (number == 0){
                number = 1;
            }
        } else {
            number = 0;
            float s = start;
            while (s >= end) {
                s += inc;
                number += 1;
            }
            if (number == 0) {
            	number = 1;
            }
        }
    	firePropertyChange("number", old, number);
    }

    private void calculateScanFinal() {
    	float old = end;
        if (number <= 0) {
            return;
        }
        if (inc == 0) {
            end = start;
        } else if (number == 1) {
            end = start;
        } else {
            end = Float.valueOf(start + (number - 1) * inc);
        }
        firePropertyChange("end", old, end);
    }
        
//    private void createNumbers() {
//        r = [start]
//        if inc == 0:
//            r.append(final)
//        elif inc > 0:
//            while start <= final:
//                start += inc
//                r.append(start)
//        else :
//            while start >= final:
//                start += inc
//                r.append(start)
//        s = ', '.join([float_to_str(i) for i in r])
//        self.psnumbers.value = s

	protected void firePropertyChange(String name, Object oldValue, Object newValue) {
		changeListener.firePropertyChange(name, oldValue, newValue);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeListener.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeListener.removePropertyChangeListener(listener);
	}

	public void dispose() {
		PropertyChangeListener[] listeners = changeListener.getPropertyChangeListeners();
		for (PropertyChangeListener listener : listeners) {
			changeListener.removePropertyChangeListener(listener);
		}
	}
}
