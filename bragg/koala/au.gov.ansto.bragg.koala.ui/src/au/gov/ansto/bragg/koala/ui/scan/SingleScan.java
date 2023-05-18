package au.gov.ansto.bragg.koala.ui.scan;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.sics.CollectionHelper;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

enum ScanTarget {
	PHI_LOOP,
	PHI_POINTS,
	TEMP_LOOP,
	TEMP_POINTS;
	
	static String[] texts = {Activator.PHI + " loop", Activator.PHI + " points", " t loop", " t points"};
	static String[] plainTexts = {"Phi loop", "Phi points", "Temp loop", "Temp points"};
	static String[] htmls = {Activator.PHI_HTML + " loop", Activator.PHI_HTML + " points", " t loop", " t points"};
	
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
	
	public String getHtml() {
		switch (this) {
		case PHI_LOOP:
			return htmls[0];
		case PHI_POINTS:
			return htmls[1];
		case TEMP_LOOP:
			return htmls[2];
		case TEMP_POINTS:
			return htmls[3];
		default:
			return htmls[0];
		}
	}
	
	public String toString() {
		switch (this) {
		case PHI_LOOP:
			return plainTexts[0];
		case PHI_POINTS:
			return plainTexts[1];
		case TEMP_LOOP:
			return plainTexts[2];
		case TEMP_POINTS:
			return plainTexts[3];
		default:
			return plainTexts[0];
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

	private static final int SERVER_IMAGE_WIDTH = 1440;
	private static final String SOURCE_FOLDER = "sics.data.path";
	private static final String SERVER_FOLDER = "gumtree.server.dataPath";
	public static final String DATA_FILENAME = "gumtree.server.dataName";
	public static final String LIVEPNG_FILENAME = "gumtree.server.livePNGName";
	private final static int WAIT_BETWEEN_COLLECTION = 5000;
	private final static int WAIT_AFTER_SCAN = 1000;
	private final static int PAUSE_CHECK_INTERVAL = 20;
	private final static float DEVICE_VALUE_TOLERANCE = 0.00001f;
	private final static Logger logger = LoggerFactory.getLogger(SingleScan.class);
	private final static String FOLDER_HIGHGAIN = "higain";
	private final static String FOLDER_LOWGAIN = "logain";
	private final static String FOLDER_LOWGAIN_COPY = "lowgain";
	private final static String NAME_LOWGAIN_EXTENSION = "_lowgain";
	private final static String NAME_PREFIX_HIGHGAIN = "HIKOL";
	private final static String NAME_PREFIX_LOWGAIN = "LOKOL";
	
	public static final String NAME_SCAN = "scan";
	
//	protected final int ERASURE_TIME = 10;
//	protected final int READING_TIME = 240;
	protected final int TEMP_TIME = 300;
	protected final int CHI_TIME = 10;
	protected final int PHI_TIME = 10;
	
	private ScanTarget target;
	private float start;
	private float inc;
	private int number = 1;
	private float end;
	private int exposure = 60;
	private int erasure = CollectionHelper.ERASE_TIME;
	private float temp = Float.NaN;
	private float phi = Float.NaN;
	private float chi = Float.NaN;
	private String status;
	private String comments;
	private String filename = System.getProperty(DATA_FILENAME, "data.tif");
	private int startIndex = 1;
	private PropertyChangeSupport changeListener = new PropertyChangeSupport(this);
	private InputType inputLast;
	private InputType input2nd;
	private String points;
	private boolean isRunning;
	private long startTimeMilSec;
	private boolean paused;
	private boolean isFinished;
	private int fileIndex = 1;
	private File currentFile;
	private File currentLowFile;
	private List<String> tiffFiles;

	enum InputType {START, INC, NUMBER, END};
	
	public SingleScan() {
		this(ScanTarget.PHI_LOOP);
	}
	public SingleScan(ScanTarget target) {
		this.target = target;
		tiffFiles = new ArrayList<String>();
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
							CollectionHelper.ERASE_TIME + 
							CollectionHelper.READ_TIME);
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
						CollectionHelper.ERASE_TIME + 
						CollectionHelper.READ_TIME);
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
			return String.format("scan %s of $s", getTarget().toString(), getPoints());
		}
		return String.format("scan %s %f %f %d exposure %d", getTarget().toString(), start, end, number, exposure);
	}
	
	public void run() throws KoalaInterruptionException, KoalaServerException  {
		isRunning = true;
		tiffFiles.clear();
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
			ControlHelper.asyncExec(String.format("hset %s %s", 
					System.getProperty(ControlHelper.GUMTREE_COMMENTS), getComments()));
			evaluatePauseStatus();
			if (getTarget().isPoints()) {
				List<Float> points = getPointValues();
				int i = 0;
				for (Float value : points) {
					if (i > 0) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					collect(i++, value);
				}
			} else {
				if (getNumber() > 0) {
					for (int i = 0; i < getNumber(); i++) {
						float target = getStart() + getInc() * i;
						if (i > 0) {
							try {
								Thread.sleep(WAIT_BETWEEN_COLLECTION);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						collect(i, target);
					}
				}
			}
			try {
				Thread.sleep(WAIT_AFTER_SCAN);
			} catch (InterruptedException e) {
			}
		} finally {
			startTimeMilSec = 0;
			isFinished = true;
			isRunning = false;
			ControlHelper.publishGumtreeStatus("Scan - finished");
			publishScanResult();
		}
	}
	
	
	private void publishScanResult() {
		if (tiffFiles.size() > 0) {
			try {
				ControlHelper.getLogbookService().appendTableEntry(
						ControlHelper.experimentModel.getSampleName(), 
						getHtml());
			} catch (Exception e) {
//				ControlHelper.experimentModel.publishErrorMessage(
//					"failed to publish scan result: server is donw?");
				logger.error("failed to publish scan result: server is donw?", e);
			}
		}
	}
	
	private void collect(final int index, final float target) 
			throws KoalaInterruptionException, KoalaServerException {
		evaluatePauseStatus();
		ControlHelper.asyncExec(String.format("hset %s %d", 
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
//		resetCurrentFile();
		try {
//			String source = String.valueOf(((IDynamicController) fnController).getValue());
			String source = filename.replaceAll("/", "\\\\");
			String lowSource;
			String hiSource;
			if (source.contains(File.separator)) {
				hiSource = System.getProperty(SOURCE_FOLDER) + File.separator + FOLDER_HIGHGAIN + source.substring(source.lastIndexOf(File.separator));
				lowSource = System.getProperty(SOURCE_FOLDER) + File.separator + FOLDER_LOWGAIN + source.substring(source.lastIndexOf(File.separator));
			} else {
				hiSource = System.getProperty(SOURCE_FOLDER) + File.separator + FOLDER_HIGHGAIN + File.separator + source;
				lowSource = System.getProperty(SOURCE_FOLDER) + File.separator + FOLDER_LOWGAIN + File.separator + source;
			}
			lowSource = lowSource.replaceAll(NAME_PREFIX_HIGHGAIN, NAME_PREFIX_LOWGAIN);
//			String tn = this.filename;
//			tn = tn.replaceAll("/", File.separator);
//			if (tn.startsWith(File.separator)) {
//				tn = tn.substring(1);
//			}
			logger.warn(String.format("copy %s to %s", hiSource, currentFile));
			Files.copy((new File(hiSource)).toPath(), currentFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			logger.warn(String.format("copy %s to %s", lowSource, currentLowFile));
			Files.copy((new File(lowSource)).toPath(), currentLowFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			ControlHelper.experimentModel.setLastFilename(currentFile.getAbsolutePath());
			fileIndex++;
//			System.err.println(source);
//			System.err.println(currentFile.toString());
			convertTiffToPng(hiSource);
		} catch (IOException e) {
			throw new KoalaServerException("failed to copy file to proposal folder", e);
		} catch (Exception e) {
			throw new KoalaServerException("failed to copy file", e);
		}
		
	}
	
	private void convertTiffToPng(final String source) {
		Thread convertThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					File f = new File(source);
					File serverFolder = new File(System.getProperty(SERVER_FOLDER));
					if (f.exists() && serverFolder.exists()) {
						BufferedImage image = ImageIO.read(f);
						int width = SERVER_IMAGE_WIDTH;
						int height = image.getHeight() * width / image.getWidth();
						BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
						Graphics2D g2 = newImage.createGraphics();
						g2.drawImage(image, 0, 0, width, height, null);
						File cp = new File(serverFolder + "/" + System.getProperty(LIVEPNG_FILENAME, "live.png"));
						logger.warn("create live PNG: " + cp.getPath());
						FileOutputStream out = new FileOutputStream(cp);
						ImageIO.write(newImage, "png", out);
					}
				} catch (Exception e) {
					logger.error("failed to convert tiff file to png version", e);
				}
			}
		});
		convertThread.start();
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
			File lowFolder = new File(parent.getPath() + File.separator + FOLDER_LOWGAIN_COPY);
			if (!lowFolder.exists()) {
				lowFolder.mkdir();
			}
			String fname = f.getName();
			String lowFName = fname.substring(0, fname.length() - 4) + NAME_LOWGAIN_EXTENSION + ".tif";
			currentLowFile = new File(lowFolder.getPath() + File.separator + lowFName);
		}
	}
	
	private void addTiffFile(String filename) {
		synchronized (tiffFiles) {
			logger.warn("record file: " + filename);
			tiffFiles.add(filename);
		}
	}
	
	public void labelTiffFile() {
		resetCurrentFile();
		addTiffFile(currentFile.getPath());
	}
	
	public boolean needToRun() {
		if (getTarget().isPoints()) {
			List<Float> values = getPointValues();
			return values.size() > 0;
		} else {
			return getNumber() > 0;
		}
	}
	
	public Element serialize(final Document document) {
		Element scan = document.createElement(NAME_SCAN);
		scan.appendChild(createChild(document, "target", target.name()));
		scan.appendChild(createChild(document, "start", start));
		scan.appendChild(createChild(document, "inc", inc));
		scan.appendChild(createChild(document, "number", number));
		scan.appendChild(createChild(document, "end", end));
		scan.appendChild(createChild(document, "exposure", exposure));
		scan.appendChild(createChild(document, "erasure", erasure));
		scan.appendChild(createChild(document, "temp", temp));
		scan.appendChild(createChild(document, "phi", phi));
		scan.appendChild(createChild(document, "chi", chi));
		scan.appendChild(createChild(document, "comments", comments));
		scan.appendChild(createChild(document, "filename", filename));
		scan.appendChild(createChild(document, "startIndex", startIndex));
		return scan;
	}
	
	private Element createChild(final Document document, final String name, final Object value) {
		Element child = document.createElement(name);
		child.setTextContent(value.toString());
		return child;
	}
	
	public void fromNode(final Node node) {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i ++) {
			Node item = nodeList.item(i);
			String name = item.getNodeName();
			if ("target".equals(name)) {
				target = ScanTarget.valueOf(item.getTextContent());
			} else if ("start".equals(name)) {
				start = Float.valueOf(item.getTextContent());
			} else if ("inc".equals(name)) {
				inc = Float.valueOf(item.getTextContent());
			} else if ("number".equals(name)) {
				number = Integer.valueOf(item.getTextContent());
			} else if ("end".equals(name)) {
				end = Float.valueOf(item.getTextContent());
			} else if ("exposure".equals(name)) {
				exposure = Integer.valueOf(item.getTextContent());
			} else if ("erasure".equals(name)) {
				erasure = Integer.valueOf(item.getTextContent());
			} else if ("temp".equals(name)) {
				temp = Float.valueOf(item.getTextContent());
			} else if ("phi".equals(name)) {
				phi = Float.valueOf(item.getTextContent());
			} else if ("chi".equals(name)) {
				chi = Float.valueOf(item.getTextContent());
			} else if ("comments".equals(name)) {
				comments = item.getTextContent();
			} else if ("filename".equals(name)) {
				filename = item.getTextContent();
			} else if ("startIndex".equals(name)) {
				startIndex = Integer.valueOf(item.getTextContent());
			} 
		}
	}
	
	
	public String getHtml() {
		DocumentBuilder documentBuilder = ControlHelper.getDocumentBuilder();
		Document document = documentBuilder.newDocument();
		document.setXmlStandalone(true);

		Element root = document.createElement("table");
		root.setAttribute("border", "1");
		root.setAttribute("cellpadding", "2");
		root.setAttribute("cellspacing", "0");
		root.setAttribute("style", "width:100%; text-align: center");
		
		Element cell;
		Element row;
		row = document.createElement("tr");
		cell = document.createElement("th");
		Calendar now = Calendar.getInstance();
		SimpleDateFormat timeFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");
		cell.setTextContent(timeFormat.format(now.getTime()));
		cell.setAttribute("colspan", "7");
		row.appendChild(cell);
		root.appendChild(row);
		
		Element header = document.createElement("tr");
		
		if (!getTarget().isPoints()) {
			cell = document.createElement("th");
			cell.setTextContent("Scan setup");
			cell.setAttribute("rowspan", "3");
			header.appendChild(cell);

			cell = document.createElement("th");
			cell.setAttribute("colspan", "4");
			cell.setTextContent(getTarget().getHtml());

			Node disableEscaping = document.createProcessingInstruction(StreamResult.PI_DISABLE_OUTPUT_ESCAPING, "&");
			header.appendChild(disableEscaping);
			header.appendChild(cell);
			cell = document.createElement("th");
			cell.setAttribute("rowspan", "2");
			cell.setTextContent(getTarget().isTemperature() ? Activator.PHI_HTML : "Temperature");
			header.appendChild(cell);

			cell = document.createElement("th");
			cell.setAttribute("rowspan", "2");
			cell.setTextContent(Activator.CHI_HTML);
			header.appendChild(cell);

			root.appendChild(header);

			header = document.createElement("tr");
			cell = document.createElement("th");
			cell.setTextContent("start");
			header.appendChild(cell);
			cell = document.createElement("th");
			cell.setTextContent("increment");
			header.appendChild(cell);
			cell = document.createElement("th");
			cell.setTextContent("steps");
			header.appendChild(cell);
			cell = document.createElement("th");
			cell.setTextContent("final");
			header.appendChild(cell);
			root.appendChild(header);

			row = document.createElement("tr");
			cell = document.createElement("td");
			cell.setTextContent(String.valueOf(start));
			row.appendChild(cell);

			cell = document.createElement("td");
			cell.setTextContent(String.valueOf(inc));
			row.appendChild(cell);

			cell = document.createElement("td");
			cell.setTextContent(String.valueOf(number));
			row.appendChild(cell);

			cell = document.createElement("td");
			cell.setTextContent(String.valueOf(end));
			row.appendChild(cell);

			cell = document.createElement("td");
			if (getTarget().isTemperature()) {
				cell.setTextContent(String.valueOf(Float.isNaN(phi) ? "" : phi));
			} else {
				cell.setTextContent(String.valueOf(Float.isNaN(temp) ? "" : temp));
			}
			row.appendChild(cell);

			cell = document.createElement("td");
			cell.setTextContent(String.valueOf(Float.isNaN(chi) ? "" : chi));
			row.appendChild(cell);
			root.appendChild(row);
		} else {
			cell = document.createElement("th");
			cell.setTextContent("Scan setup");
			cell.setAttribute("rowspan", "2");
			header.appendChild(cell);

			cell = document.createElement("th");
			cell.setAttribute("colspan", "4");
			cell.setTextContent(getTarget().getHtml());

			Node disableEscaping = document.createProcessingInstruction(StreamResult.PI_DISABLE_OUTPUT_ESCAPING, "&");
			header.appendChild(disableEscaping);
			header.appendChild(cell);
			cell = document.createElement("th");
			cell.setTextContent(getTarget().isTemperature() ? Activator.PHI_HTML : "Temperature");
			header.appendChild(cell);

			cell = document.createElement("th");
			cell.setTextContent(Activator.CHI_HTML);
			header.appendChild(cell);

			root.appendChild(header);

			row = document.createElement("tr");
			cell = document.createElement("td");
			cell.setAttribute("colspan", "4");
			cell.setTextContent(getPoints());
			row.appendChild(cell);

			cell = document.createElement("td");
			if (getTarget().isTemperature()) {
				cell.setTextContent(String.valueOf(Float.isNaN(phi) ? "" : phi));
			} else {
				cell.setTextContent(String.valueOf(Float.isNaN(temp) ? "" : temp));
			}
			row.appendChild(cell);

			cell = document.createElement("td");
			cell.setTextContent(String.valueOf(Float.isNaN(chi) ? "" : chi));
			row.appendChild(cell);
			root.appendChild(row);
			
		}

		row = document.createElement("tr");
		cell = document.createElement("th");
		cell.setTextContent("Comments");
		row.appendChild(cell);
		
		cell = document.createElement("td");
		cell.setAttribute("style", "text-align: left");
		cell.setAttribute("colspan", "6");
		cell.setTextContent(comments);
		row.appendChild(cell);
		root.appendChild(row);
		
		row = document.createElement("tr");
		cell = document.createElement("th");
		cell.setTextContent("TIFF files");
		if (tiffFiles.size() == 1) {
			row.appendChild(cell);
			cell = document.createElement("td");
			cell.setAttribute("colspan", "6");
			cell.setTextContent(tiffFiles.get(0));
			row.appendChild(cell);
			root.appendChild(row);
		} else if (tiffFiles.size() > 1) {
			cell.setAttribute("rowspan", String.valueOf(tiffFiles.size()));
			row.appendChild(cell);
			cell = document.createElement("td");
			cell.setAttribute("colspan", "6");
			cell.setTextContent(tiffFiles.get(0));
			row.appendChild(cell);
			root.appendChild(row);
			synchronized (tiffFiles) {
				for (int i = 1; i < tiffFiles.size(); i++) {
					row = document.createElement("tr");
					cell = document.createElement("td");
					cell.setAttribute("colspan", "6");
					cell.setTextContent(tiffFiles.get(i));
					row.appendChild(cell);
					root.appendChild(row);
				}
			}
		} else {
			row.appendChild(cell);
			cell = document.createElement("td");
			cell.setAttribute("colspan", "6");
			cell.setTextContent("No file was recorded, check with the computing team");
			row.appendChild(cell);
			root.appendChild(row);
		}
		
		
		document.appendChild(root);
		try {
			// write to buffer
			Writer stringWriter = new StringWriter();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();			
			Transformer transformer = transformerFactory.newTransformer();
			
		    transformer.setOutputProperty(OutputKeys.METHOD, "html");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		    
			transformer.transform(
					new DOMSource(document),
					new StreamResult(stringWriter));

			return stringWriter.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}
	}
}
