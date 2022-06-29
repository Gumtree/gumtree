package au.gov.ansto.bragg.koala.ui.scan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

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
	
	public String getDeviceName() {
		return this == TEMP_LOOP || this == ScanTarget.TEMP_POINTS ? 
				ControlHelper.TEMP_DEVICE_NAME : ControlHelper.PHI_DEVICE_NAME;
	}
};

public class SingleScan {

	private final static int PAUSE_CHECK_INTERVAL = 20;
	
	protected final int ERASURE_TIME = 10;
	protected final int READING_TIME = 20;
	protected final int TEMP_TIME = 300;
	protected final int CHI_TIME = 10;
	protected final int PHI_TIME = 10;
	
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
	private boolean isRunning;
	private boolean paused;
	private boolean isFinished;

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
		Object old = this.exposure;
		this.exposure = exposure;
		firePropertyChange("exposure", old, exposure);
	}
	
	public float getTemp() {
		return temp;
	}
	public void setTemp(float temp) {
		Object old = this.temp;
		this.temp = temp;
		firePropertyChange("temp", old, temp);
	}
	public float getChi() {
		return chi;
	}
	public void setChi(float chi) {
		Object old = this.chi;
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
	
	private List<Float> getPointValues() {
		String[] items = getPoints().split(",");
		List<Float> values = new ArrayList<Float>();
		for (int i = 0; i < items.length; i++) {
			float v = Float.valueOf(items[i].trim());
			if (!Float.isNaN(v)) {
				values.add(v);
			}
		}
		return values;
	}
	
	public int getTotalTime() {
		int time = 0;
		if (getTarget().isPoints()) {
			List<Float> values = getPointValues();
			if (values.size() > 0) {
				if (getExposure() > 0) {
					time += values.size() * (
							PHI_TIME + 
							getExposure() + 
							ERASURE_TIME + 
							READING_TIME);
				}
				if (!Float.isNaN(getTemp())) {
					time += TEMP_TIME;
				}
				if (!Float.isNaN(getChi())) {
					time += CHI_TIME;
				}
			}
		} else {
			if (getExposure() > 0) {
				time += getNumber() * (
						PHI_TIME + 
						getExposure() + 
						ERASURE_TIME + 
						READING_TIME);
			}
			if (getTarget() != ScanTarget.TEMP_LOOP 
					&& getTarget() != ScanTarget.TEMP_POINTS) {
				if (!Float.isNaN(getTemp()) && getTemp() != 0) {
					time += TEMP_TIME;
				}
			}
			if (!Float.isNaN(getChi())) {
				time += CHI_TIME;
			}
		}
		return time;
	}
	
	public int getTimeLeft() {
		
		return 0;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	
	private void evaluatePauseStatus() throws KoalaInterruptionException {
		if (isPaused()) {
			while (isPaused()) {
				try {
					Thread.sleep(PAUSE_CHECK_INTERVAL);
				} catch (InterruptedException e) {
					throw new KoalaInterruptionException("user interrupted");
				}
			}
		}
	}
	public void run() throws KoalaInterruptionException, KoalaServerException  {
		isRunning = true;
		try {
			evaluatePauseStatus();
			if (!getTarget().isTemperature()) {
				if (!Float.isNaN(getTemp())) {
					ControlHelper.driveTemperature(getTemp());
				}
			}
			evaluatePauseStatus();
			if (!Float.isNaN(getChi())) {
				ControlHelper.driveChi(getChi());
			}
			evaluatePauseStatus();
			if (getTarget().isPoints()) {

			} else {
				if (getNumber() > 0) {
					for (int i = 0; i < getNumber(); i++) {
						evaluatePauseStatus();
						ControlHelper.syncExec(String.format("hset %s %d", 
								System.getProperty(ControlHelper.STEP_PATH), i));
						float target = getStart() + getInc() * i;
						ControlHelper.syncDrive(getTarget().getDeviceName(), target);
						evaluatePauseStatus();
						ControlHelper.syncCollect(getExposure(), getErasure());
						evaluatePauseStatus();
					}
				}
			}
		} finally {
			isFinished = true;
			isRunning = false;
		}
	}
}
