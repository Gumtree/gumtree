package org.gumtree.data.nexus.utils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;

import org.gumtree.data.IFactory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.utils.FactoryManager;

public class BinLoadHelper {

	private final static int FILE_FRAME_LENGTH = 20000; // 50hz -> microseconds (detector)
	
	public final static int HISTO_BINS_X = 192; // pixel
	public final static int HISTO_BINS_Y = 192; // pixel
	private             int HISTO_BINS_T =   0; // seconds

	private final static int[] DetectorDimensions = new int[]{HISTO_BINS_Y, HISTO_BINS_X};

	FactoryManager manager = new FactoryManager();
	IFactory factory = manager.getFactory();

	private String _binPath = null;
	private List<IArray> _timeBins = null;
	private IArray _histogram = null;
	private IArray _timeHistogram = null;
	private double _beamTime = 0;

	private int _minPixelCount;
	private int[] _numPixelsVsRadius = null;
	private double _centerX;
	private double _centerY;

	// properties
	public int getTimeBinLength() {
		return HISTO_BINS_T;
	}
	public double getCenterX() {
		return _centerX;
	}
	public double getCenterY() {
		return _centerY;
	}
	public String getBinPath() {
		return _binPath;
	}
	public IArray getHistogram() {
		return _histogram;
	}
	public IArray getTimeHistogram() {
		return _timeHistogram;
	}
	public double getBeamTime() {
		return _beamTime;
	}

	// constructors
	public BinLoadHelper() throws IOException {
		this(null, -1);
	}
	public BinLoadHelper(String path) throws IOException {
		this(path, -1);
	}
	public BinLoadHelper(String path, int histo_bins_t) throws IOException {
		initCenterDependencies(HISTO_BINS_X / 2.0, HISTO_BINS_Y / 2.0, 5);
		if (path != null)
			loadFile(path, histo_bins_t);
	}

	// functions
	private void initCenterDependencies(double centerX, double centerY, int minPixelCount) {
		if (centerX < 0 || centerX >= HISTO_BINS_X)
			throw new IllegalArgumentException("centerX");
		if (centerY < 0 || centerY >= HISTO_BINS_Y)
			throw new IllegalArgumentException("centerY");
		if (minPixelCount < 0)
			throw new IllegalArgumentException("minPixelCount");

		_centerX = centerX;
		_centerY = centerY;
		_minPixelCount = minPixelCount;

		// find max possible radius
		double xMin = (0               ) - _centerX;
		double yMin = (0               ) - _centerY;
		double xMax = (HISTO_BINS_X - 1) - _centerX;
		double yMax = (HISTO_BINS_Y - 1) - _centerY;

		int maxRIndex = 0;
		maxRIndex = Math.max(maxRIndex, (int)Math.ceil(Math.hypot(xMin, yMin)));
		maxRIndex = Math.max(maxRIndex, (int)Math.ceil(Math.hypot(xMax, yMin)));
		maxRIndex = Math.max(maxRIndex, (int)Math.ceil(Math.hypot(xMin, yMax)));
		maxRIndex = Math.max(maxRIndex, (int)Math.ceil(Math.hypot(xMax, yMax)));

		_numPixelsVsRadius = new int[maxRIndex + 1]; // [0, ..., maxRIndex]
		for (int y = 0; y < HISTO_BINS_Y; y++)
			for (int x = 0; x < HISTO_BINS_X; x++)
				_numPixelsVsRadius[(int)Math.round(Math.hypot(x - _centerX, y - _centerY))]++;

		// reduce length
		while (maxRIndex > 0 && _numPixelsVsRadius[maxRIndex] < minPixelCount)
			maxRIndex--;

		// resize _numPixelsVsRadius
		if (maxRIndex + 1 < _numPixelsVsRadius.length) {
			int[] tmp = new int[maxRIndex + 1];
			System.arraycopy(_numPixelsVsRadius, 0, tmp, 0, tmp.length);
			_numPixelsVsRadius = tmp;
		}

		// to avoid division by zero and to minimise noise when pixel count is to low
		for (int i = 0; i < _numPixelsVsRadius.length; i++)
			if (_numPixelsVsRadius[i] < minPixelCount)
				_numPixelsVsRadius[i] = 0x7FFFFFFF;
	}
	// public
	public void setCenter(double centerX, double centerY) {
		setCenter(centerX, centerY, _minPixelCount);
	}
	public void setCenter(double centerX, double centerY, int minPixelCount) {
		if (centerX != _centerX || centerY != _centerY) {
			initCenterDependencies(centerX, centerY, minPixelCount);
			_timeHistogram = null; // not valid anymore
		}
	}
	public void loadHistogram() {
		loadHistogram(0, -1.0);
	}
	public void loadHistogram(double startTime, double endTime) {
		if (_timeBins == null)
			throw new IllegalStateException("no bin file has been loaded yet.");

		double maxTime = _timeBins.size() * HISTO_BINS_T;
		// check end time
		if (endTime == -1.0)
			endTime = maxTime;
		else if (endTime > maxTime)
			throw new IllegalArgumentException("endTime");
		// check start time
		if (startTime < 0 || startTime > endTime)
			throw new IllegalArgumentException("startTime");

		// find aligned start/end time for resulting histogram image
		double startMod = startTime % HISTO_BINS_T;
		double endMod   = endTime   % HISTO_BINS_T;

		if (startMod > 1e-03) { // floating point threshold
			startTime -= startMod;
			// print 'Warning: start time was not aligned and is lowered to', startTime
		} else {
			startTime -= startMod;
		}

		if (endMod > 1e-03) { // floating point threshold
			endTime += Math.min(maxTime, HISTO_BINS_T - endMod);
			//print 'Warning: end time was not aligned and is raised to', endTime
		} else {
			endTime -= endMod;
		}

		// accumulation
		_histogram = factory.createArray(int.class, DetectorDimensions);
		IIndex histogramIndex = _histogram.getIndex();
		// for progress
		int i0 = (int)Math.round(startTime / HISTO_BINS_T);
		int i1 = (int)Math.round(endTime   / HISTO_BINS_T);
		for (int i = i0; i < i1; i++) {
			IArray timeBin = _timeBins.get(i);
			IIndex timeBinIndex = timeBin.getIndex();
			for (int y = 0; y < HISTO_BINS_Y; y++) {
				timeBinIndex.set0(y);
				histogramIndex.set0(y);
				for (int x = 0; x < HISTO_BINS_X; x++) {
					timeBinIndex.set1(x);
					histogramIndex.set1(x);
					_histogram.setInt(
							histogramIndex,
							_histogram.getInt(histogramIndex) + timeBin.getInt(timeBinIndex));
				}
			}
		}
	}
	public void loadTimeHistogram() {
		if (_timeBins == null)
			throw new IllegalStateException("no bin file has been loaded yet.");

		int timeHistogramLength = _timeBins.size();
		_timeHistogram = factory.createArray(double.class, new int[]{_numPixelsVsRadius.length, timeHistogramLength});
		IIndex timeHistogramIndex = _timeHistogram.getIndex();
		IIndex histogramIndex = _timeBins.get(0).getIndex();
		for (int y = 0; y < HISTO_BINS_Y; y++) {
			histogramIndex.set0(y);
			for (int x = 0; x < HISTO_BINS_X; x++) {
				histogramIndex.set1(x);
				int rIndex = (int)Math.round(Math.hypot(x - _centerX, y - _centerY));
				if (rIndex < _numPixelsVsRadius.length) {
					timeHistogramIndex.set0(rIndex);
					for (int i = 0; i < timeHistogramLength; i++) {
						timeHistogramIndex.set1(i);
						_timeHistogram.setDouble(
								timeHistogramIndex,
								_timeHistogram.getDouble(timeHistogramIndex) + _timeBins.get(i).getInt(histogramIndex));
					}
				}
			}
		}

		// scale by pixel count versus radius
		for (int r = 0; r < _numPixelsVsRadius.length; r++) {
			timeHistogramIndex.set0(r);
			for (int i = 0; i < timeHistogramLength; i++) {
				timeHistogramIndex.set1(i);
				_timeHistogram.setDouble(
						timeHistogramIndex,
						_timeHistogram.getDouble(timeHistogramIndex) / _numPixelsVsRadius[r]);
			}
		}
	}
	public void loadTimeHistogram(double centerX, double centerY) {
		setCenter(centerX, centerY);
		loadTimeHistogram();
	}
	// access bin file
	public void loadFile(String path) throws IOException {
		loadFile(path, -1);
	}
	public void loadFile(String path, int histo_bins_t) throws IOException {
		File file = new File(path);
		long fileLen = file.length();
		if (histo_bins_t == -1)
			HISTO_BINS_T = Math.max(1, (int)Math.floor(fileLen / (10.0 * (1000 * 1000)))); // estimation
		else if (histo_bins_t > 0)
			HISTO_BINS_T = histo_bins_t;
		else
			throw new IllegalArgumentException("histo_bins_t");

		_timeBins = new ArrayList<IArray>();
		DataInputStream f = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
		try {
			// remember path
			_binPath = path;

			// skip header
			f.skip(128);
			int state = 0;

			// valid time horizon
			int t0 = 0 * FILE_FRAME_LENGTH;
			int t2 = 2 * FILE_FRAME_LENGTH;

			int t1 = 0; // current time
			int dt = 0; // current time step

			// beam time
			double frameLengthSec = FILE_FRAME_LENGTH / (double)(1000 * 1000);
			_beamTime = frameLengthSec;

			// event
			int x  = 0;
			int y  = 0;
			//int v  = 0;

			long timeBinLen = (long)HISTO_BINS_T * (1000L * 1000L); // to microseconds
			long timeBinEnd = timeBinLen;
			IArray timeBin = factory.createArray(int.class, DetectorDimensions);
			IIndex timeBinIndex = timeBin.getIndex();

			try {
				while (true) {
					int c = f.readByte();
					// to unsigned char (8-bit)
					if (c < 0)
						c += 256;

					switch (state) {
					case 0:
						x = c;
						state++;
						break;

					case 1:
						x |= (c & 0x03) << 8;
						y  = c >> 2;
						state++;
						break;

					case 2:
						y |= (c & 0x07) << 6;
						//v  = (c & 0x78) >> 3;
						dt = (c & 0x80) >> 7;
						state++;
						break;

					default:
						boolean event_ended = ((c & 0xC0) != 0xC0) || (state >= 8);
						if (!event_ended)
							c &= 0x3F;

						dt |= c << (1 + 6 * (state - 3));
						if (!event_ended)
							state++;
						else {
							state = 0;
							if (x == 0 && y == 0 && dt == -1) {
								t1          = t0;
								_beamTime  += frameLengthSec;
								timeBinEnd -= (long)FILE_FRAME_LENGTH;
							} else {
								int t1_new = t1 + dt;
								if (t1_new < t0 || t1_new > t2) {
									// jump error
								} else {
									t1 = t1_new;
								}

								while (t1 > timeBinEnd) {
									_timeBins.add(timeBin);
									timeBin      = factory.createArray(int.class, DetectorDimensions);
									timeBinIndex = timeBin.getIndex();
									timeBinEnd  += timeBinLen;
								}

								timeBinIndex.set(y, x);
								timeBin.setInt(timeBinIndex, 1 + timeBin.getInt(timeBinIndex));
							}
						}
						break;
					}
				}
			} catch (EOFException eof) {
			}

			// append last timeBin to timeBins
			_timeBins.add(timeBin);
		} finally {
			f.close();
		}
	}
	public List<IArray> loadTimeBins(double[] timestamps) throws IOException {
		if (_timeBins == null)
			throw new IllegalStateException("no bin file has been loaded yet.");

		List<IArray> resultingTimeBins = new ArrayList<IArray>();
		DataInputStream f = new DataInputStream(new BufferedInputStream(new FileInputStream(_binPath)));
		try {
			// skip header
			f.skip(128);
			int state = 0;

			// valid time horizon
			int t0 = 0 * FILE_FRAME_LENGTH;
			int t2 = 2 * FILE_FRAME_LENGTH;

			int t1 = 0; // current time
			int dt = 0; // current time step

			// beam time
			double frameLengthSec = FILE_FRAME_LENGTH / (double)(1000 * 1000);
			_beamTime = frameLengthSec;
			
			// event
			int x  = 0;
			int y  = 0;
			//int v  = 0;

			long timeBinEnd    = 0L;
			int timestampIndex = 0;
			
			while (timeBinEnd <= 0L) {
				if ((timestamps != null) && (timestamps.length > timestampIndex))
					timeBinEnd = (long)Math.round((1000 * 1000) * timestamps[timestampIndex++]); // to microseconds
				else
					timeBinEnd = 0x7FFFFFFFFFFFFFFFL;
			}
			
			IArray timeBin = factory.createArray(int.class, DetectorDimensions);
			IIndex timeBinIndex = timeBin.getIndex();

			try {
				while (true) {
					int c = f.readByte();
					// to unsigned char (8-bit)
					if (c < 0)
						c += 256;

					switch (state) {
					case 0:
						x = c;
						state++;
						break;

					case 1:
						x |= (c & 0x03) << 8;
						y  = c >> 2;
						state++;
						break;

					case 2:
						y |= (c & 0x07) << 6;
						// v  = (c & 0x78) >> 3;
						dt = (c & 0x80) >> 7;
						state++;
						break;

					default:
						boolean event_ended = ((c & 0xC0) != 0xC0) || (state >= 8);
						if (!event_ended)
							c &= 0x3F;

						dt |= c << (1 + 6 * (state - 3));
						if (!event_ended)
							state++;
						else {
							state = 0;
							if (x == 0 && y == 0 && dt == -1) {
								t1 = t0;
								_beamTime += frameLengthSec;
								if (timeBinEnd != 0x7FFFFFFFFFFFFFFFL)
									timeBinEnd -= FILE_FRAME_LENGTH;
							} else {
								int t1_new = t1 + dt;
								if (t1_new < t0 || t1_new > t2) {
									// jump error
								} else {
									t1 = t1_new;
								}

								while (t1 > timeBinEnd) {
									resultingTimeBins.add(timeBin);
									timeBin      = factory.createArray(int.class, DetectorDimensions);
									timeBinIndex = timeBin.getIndex();
									if ((timestamps != null) && (timestamps.length > timestampIndex)) {
										timeBinEnd += (long)Math.round((1000 * 1000) * (
												timestamps[timestampIndex] - timestamps[timestampIndex - 1]));
										timestampIndex++;
									} else {
										timeBinEnd = 0x7FFFFFFFFFFFFFFFL;
									}
								}

								timeBinIndex.set(y, x);
								timeBin.setInt(timeBinIndex, 1 + timeBin.getInt(timeBinIndex));
							}
						}
						break;
					}
				}
			} catch (EOFException eof) {
			}

			// append last timeBin to result
			resultingTimeBins.add(timeBin);
		} finally {
			f.close();
		}
		
		return resultingTimeBins;
	}

	// testing
	public static void main(String[] args) throws IOException {

		long time = System.currentTimeMillis();

		BinLoadHelper loader = new BinLoadHelper("C:/Documents and Settings/davidm/Desktop/Bin for David/Bin for David/DAQ_2011-08-11T15-17-08/DATASET_0/EOS.bin");
		//BinLoadHelper loader = new BinLoadHelper("C:/Documents and Settings/davidm/Desktop/DATASET_1/EOS.bin");
		
		loader.loadHistogram();
		loader.loadTimeHistogram();
		List<IArray> result = loader.loadTimeBins(new double[]{10, 100, 120});

		System.out.println(System.currentTimeMillis() - time);
		System.out.println(result.size());
	}
}
