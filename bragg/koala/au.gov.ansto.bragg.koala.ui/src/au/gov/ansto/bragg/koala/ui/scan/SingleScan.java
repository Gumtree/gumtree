package au.gov.ansto.bragg.koala.ui.scan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

enum ScanTarget {
	PHI_LOOP,
	PHI_POINTS,
	TEMP_LOOP,
	TEMP_POINTS;
	
	static String[] texts = {Activator.PHI + " loop", Activator.PHI + " points", " t loop", " t points"};

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

	private static final String SOURCE_FOLDER = "sics.data.path";
	private static final String SERVER_FOLDER = "gumtree.server.dataPath";
	public static final String DATA_FILENAME = "gumtree.server.dataName";
	private final static int PAUSE_CHECK_INTERVAL = 20;
	private final static float DEVICE_VALUE_TOLERANCE = 0.00001f;
	private final static Logger logger = LoggerFactory.getLogger(SingleScan.class);
	
	protected final int ERASURE_TIME = 10;
	protected final int READING_TIME = 240;
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
	private float phi = Float.NaN;
	private float chi = Float.NaN;
	private String status;
	private String comments;
	private String filename = System.getProperty(DATA_FILENAME, "data.tif");
	private PropertyChangeSupport changeListener = new PropertyChangeSupport(this);
	private InputType inputLast;
	private InputType input2nd;
	private String points;
	private boolean isRunning;
	private long startTimeMilSec;
	private boolean paused;
	private boolean isFinished;
	private int fileIndex = 1;
	private int startIndex = 1;
	private File currentFile;

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
	public float getPhi() {
		return phi;
	}
	public void setPhi(float phi) {
		Object old = this.phi;
		this.phi = phi;
		firePropertyChange("phi", old, phi);
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
		File test = new File(filename.replaceAll("\\*", "a"));
		test.toPath();
		Object old = this.filename;
		this.filename = filename;
		fileIndex = startIndex;
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
	
	public int getStartIndex() {
		return startIndex;
	}
	
	public void setStartIndex(int startIndex) {
		if (startIndex < 0) {
			throw new IllegalArgumentException("must be larger or equal than 0");
		}
		Object old = this.startIndex;
		this.startIndex = startIndex;
		fileIndex = startIndex;
		firePropertyChange("startIndex", old, startIndex);
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
//		phi = scan.getPhi();
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
            while (s <= end + DEVICE_VALUE_TOLERANCE) {
                s += inc;
                number += 1;
            }
            if (number == 0){
                number = 1;
            }
        } else {
            number = 0;
            float s = start;
            while (s >= end - DEVICE_VALUE_TOLERANCE) {
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
			if (getTarget().isTemperature()) {
				if (!Float.isNaN(getPhi())) {
					time += PHI_TIME;
				}
			}
			if (!Float.isNaN(getChi())) {
				time += CHI_TIME;
			}
		}
		return time;
	}
	
	public int getTimeLeft() {
		if (isRunning) {
			if (startTimeMilSec != 0) {
				int totalTime = getTotalTime();
				long now = System.currentTimeMillis();
				return totalTime - Long.valueOf((now - startTimeMilSec) / 1000).intValue();
			} else {
				return getTotalTime();
			}
		} else if ("".equals(getStatus())) {
			return getTotalTime();
		} else {
			return 0;
		}
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
	
	@Override
	public String toString() {
		if (getTarget().isPoints()) {
			return String.format("scan {} of {}", getTarget().getText(), getPoints());
		}
		return String.format("scan {} {} {} {} exposure {}", getTarget().getText(), start, end, number, exposure);
	}
	
	public void run() throws KoalaInterruptionException, KoalaServerException  {
		isRunning = true;
		startTimeMilSec = System.currentTimeMillis();
		logger.warn("start " + toString());
		try {
			ControlHelper.experimentModel.setCurrentScan(this);
			evaluatePauseStatus();
			if (!getTarget().isTemperature()) {
				if (!Float.isNaN(getTemp())) {
					ControlHelper.publishGumtreeStatus("Scan - change temperature");
					ControlHelper.driveTemperature(getTemp());
				}
			}
			evaluatePauseStatus();
			if (getTarget().isTemperature()) {
				if (!Float.isNaN(getPhi())) {
					ControlHelper.publishGumtreeStatus("Scan - drive sample Phi");
					ControlHelper.drivePhi(getPhi());
				}
			}
			if (!Float.isNaN(getChi())) {
				ControlHelper.publishGumtreeStatus("Scan - drive sample Chi");
				ControlHelper.driveChi(getChi());
			}
			ControlHelper.syncExec(String.format("hset %s %s", 
					System.getProperty(ControlHelper.GUMTREE_COMMENTS), getComments()));
			evaluatePauseStatus();
			if (getTarget().isPoints()) {
				List<Float> points = getPointValues();
				int i = 0;
				for (Float value : points) {
					collect(i++, value);
				}
			} else {
				if (getNumber() > 0) {
					for (int i = 0; i < getNumber(); i++) {
						float target = getStart() + getInc() * i;
						collect(i, target);
					}
				}
			}
		} finally {
			startTimeMilSec = 0;
			isFinished = true;
			isRunning = false;
			ControlHelper.publishGumtreeStatus("Scan - finished");
		}
	}
	
	private void collect(final int index, final float target) 
			throws KoalaInterruptionException, KoalaServerException {
		evaluatePauseStatus();
		ControlHelper.syncExec(String.format("hset %s %d", 
				System.getProperty(ControlHelper.STEP_PATH), index + 1));
		if (getTarget().isTemperature()) {
			ControlHelper.publishGumtreeStatus("Scan - driving temperature");			
		} else {
			ControlHelper.publishGumtreeStatus("Scan - driving sample Phi");
		}
		ControlHelper.syncDrive(getTarget().getDeviceName(), target);
		evaluatePauseStatus();
		ControlHelper.publishGumtreeStatus("Scan - image collection cycle");
		ControlHelper.syncCollect(getExposure());
		ControlHelper.publishGumtreeStatus("Scan - image collection finished");
//		copyFile();
		evaluatePauseStatus();
	}
	
	public void copyFile(final String filename) throws KoalaServerException {
//		ISicsController fnController = SicsManager.getSicsModel().findControllerByPath(
//				System.getProperty(ControlHelper.FILENAME_PATH));
		resetCurrentFile();
		try {
//			String source = String.valueOf(((IDynamicController) fnController).getValue());
			String source = filename.replaceAll("/", "\\\\");
			if (source.contains(File.separator)) {
				source = System.getProperty(SOURCE_FOLDER) + source.substring(source.lastIndexOf(File.separator));
			} else {
				source = System.getProperty(SOURCE_FOLDER) + File.separator + source;
			}
//			String tn = this.filename;
//			tn = tn.replaceAll("/", File.separator);
//			if (tn.startsWith(File.separator)) {
//				tn = tn.substring(1);
//			}
			logger.warn(String.format("copy %s to %s", source, currentFile));
			Files.copy((new File(source)).toPath(), currentFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			ControlHelper.experimentModel.setLastFilename(currentFile.getAbsolutePath());
			fileIndex++;
//			System.err.println(source);
//			System.err.println(currentFile.toString());
			File serverFolder = new File(System.getProperty(SERVER_FOLDER));
			if (serverFolder.exists()) {
				File cp = new File(serverFolder + "/" + System.getProperty(DATA_FILENAME, "data.tif"));
				Files.copy((new File(source)).toPath(), cp.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new KoalaServerException("failed to copy file to proposal folder", e);
		} catch (Exception e) {
			throw new KoalaServerException("failed to copy file", e);
		}
		
	}
	
	private void resetCurrentFile() {
		String tn = this.filename;
		tn = tn.replaceAll("/", "\\\\");
		if (tn.startsWith(File.separator)) {
			tn = tn.substring(1);
		}
		String target;
		if (tn.contains("*")) {
			target = ControlHelper.proposalFolder 
					+ tn.replaceAll("\\*", String.format("%03d", this.fileIndex));
			if (!target.toLowerCase().endsWith(".tif")) {
				target += ".tif";
			}
		} else {
			if (tn.toLowerCase().endsWith(".tif")) {
				target = ControlHelper.proposalFolder 
						+ tn.substring(0, tn.length() - 4) 
						+ String.format("%03d", this.fileIndex) 
						+ ".tif";
			} else {
				target = ControlHelper.proposalFolder 
						+ tn + String.format("%03d", this.fileIndex) + ".tif";
			}
		}
		File f = new File(target);
		File parent = f.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		if (f.exists()) {
			fileIndex++;
			resetCurrentFile();
		} else {
			currentFile = f;
		}
	}
	
	public boolean needToRun() {
		if (getTarget().isPoints()) {
			List<Float> values = getPointValues();
			return values.size() > 0;
		} else {
			return getNumber() > 0;
		}
	}
}
