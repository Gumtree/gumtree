/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.quokka.ui.workflow;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.gumtree.ui.util.SafeUIRunner;

import au.gov.ansto.bragg.quokka.experiment.model.Acquisition;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionEntry;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionSetting;
import au.gov.ansto.bragg.quokka.experiment.model.ControlledAcquisition;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.model.ScanMode;

import com.ibm.icu.text.DecimalFormat;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableDefaultModel;
import de.kupzog.ktable.editors.KTableCellEditorText;
import de.kupzog.ktable.renderers.CheckableCellRenderer;
import de.kupzog.ktable.renderers.DefaultCellRenderer;
import de.kupzog.ktable.renderers.FixedCellRenderer;
import de.kupzog.ktable.renderers.TextCellRenderer;

public class ScanTableModel extends KTableDefaultModel {

	// TODO: How to dispose?
	private static final Color COLOUR_LIGHT_BLUE = new Color(Display.getDefault(), 0x99, 0xCC, 0xFF);
	
	private static final Color COLOUR_LIGHT_RED = new Color(Display.getDefault(), 0xFF, 0x99, 0xCC);
	
	// There is 5 columns for fix / global section
	private static final int COLUMN_FIXED_SIZE = 5;
	
	// Each config contributes 5 columns
	private static final int COLUMN_SECTION_SIZE = 5;
	
	private enum GlobalColumn {
		RUN(0, ""),
		SEQUENCE(1, "Sequence"),
		POSITION(2, "Position"),
		SAMPLE_NAME(3, "Sample Name"),
		THICKNESS(4, "Thickness");
		private GlobalColumn(int index, String label) {
			this.index = index;
			this.label = label;
		}
		public int getIndex() {
			return index;
		}
		public String getLabel() {
			return label;
		}
		public static String getColumnLabel(int index) {
			for (GlobalColumn column : GlobalColumn.values()) {
				if (column.getIndex() == index) {
					return column.getLabel();
				}
			}
			return "";
		}
		private int index;
		private String label;
	}
	
	private enum ConfigColumn {
		TRANSMISSION_RUN(0, ""),
		TRANSMISSION(1, "Transmission"),
		SCATTERING_RUN(2, ""),
		SCATTERING(3, "Scattering"),
		PRESET(4, "Preset");
		private ConfigColumn(int offset, String label) {
			this.offset = offset;
			this.label = label;
		}
		public int getOffset() {
			return offset;
		}
		public String getLabel() {
			return label;
		}
		public static String getColumnLabel(int index) {
			for (ConfigColumn column : ConfigColumn.values()) {
				if (column.getOffset() == (index % COLUMN_SECTION_SIZE)) {
					return column.getLabel();
				}
			}
			return "";
		}
		private int offset;
		private String label;
	}
	
	// First row: header group
	private static final int ROW_HEADER_GROUP = 0;
	
	// Second row: general header
	private static final int ROW_HEADER = 1;
	
	private static final DefaultCellRenderer fFixedRenderer = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
	
	private static final DefaultCellRenderer fFixedRenderer1 = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
	
	private static final DefaultCellRenderer fFixedRenderer1Bold = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT | SWT.BOLD);
	
	private static final DefaultCellRenderer fFixedRenderer2 = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
	
	private static final DefaultCellRenderer fFixedRenderer2Bold = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT | SWT.BOLD);
	
	private static final KTableCellRenderer fCheckableRenderer = new CheckableCellRenderer( CheckableCellRenderer.INDICATION_CLICKED | CheckableCellRenderer.INDICATION_FOCUS);
	
	private static final TextCellRenderer ftextRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS_ROW);
	
	private static final ProgressCellRenderer fBarDiagramCellRenderer = new ProgressCellRenderer(ProgressCellRenderer.INDICATION_FOCUS_ROW);
	
	private KTable table;
	
	private Experiment experiment;
	
	private Acquisition acquisition;

	private DecimalFormat numberFormat;
	
	public ScanTableModel(KTable table, Experiment experiment, Acquisition acquisition) {
		this.table = table;
		this.experiment	= experiment;
		this.acquisition = acquisition;
		// Set number format
		numberFormat = new DecimalFormat();
		numberFormat.setScientificNotation(true);
		// Set colours
		fFixedRenderer1.setBackground(COLOUR_LIGHT_BLUE);
		fFixedRenderer1Bold.setBackground(COLOUR_LIGHT_BLUE);
		fFixedRenderer2.setBackground(COLOUR_LIGHT_RED);
		fFixedRenderer2Bold.setBackground(COLOUR_LIGHT_RED);
		fBarDiagramCellRenderer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		fBarDiagramCellRenderer.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
	}
	
	public Acquisition getAcquisition() {
		return acquisition;
	}
	
	public Experiment getExperiment() {
		return experiment;
	}
	
	/*************************************************************************
	 * 
	 * Table rendering and editing support
	 * 
	 *************************************************************************/
	
	@Override
	public KTableCellEditor doGetCellEditor(int col, int row) {
		if (row == ROW_HEADER_GROUP || row == ROW_HEADER) {
			// No editing for header
			return null;
		}
		if (col == 0) {
			// Global sample run
			return new FixedKTableCellEditorCheckbox();
		} else if (col >= COLUMN_SECTION_SIZE && (col % COLUMN_SECTION_SIZE == 0 || col % COLUMN_SECTION_SIZE == 2)) {
			// Transmission / scattering run
			return new FixedKTableCellEditorCheckbox();
		} else if (col >= COLUMN_SECTION_SIZE && (col % COLUMN_SECTION_SIZE == 4)) {
			// Preset
			return new KTableCellEditorText();
		}
		return null;
	}

	@Override
	public KTableCellRenderer doGetCellRenderer(int col, int row) {
		if (row == ROW_HEADER_GROUP) {
			// First row
			if (col < COLUMN_FIXED_SIZE) {
				// Spanned fixed cell
				return fFixedRenderer;
			} else if (((col - COLUMN_FIXED_SIZE) / COLUMN_SECTION_SIZE) % 2 == 0) {
				// Odd configs
				return fFixedRenderer1Bold;
			} else {
				// Even configs
				return fFixedRenderer2Bold;
			}
		} else if (row == ROW_HEADER) {
			// Second row
			if (col < COLUMN_FIXED_SIZE) {
				return fFixedRenderer;
			} else if (((col - COLUMN_FIXED_SIZE) / COLUMN_SECTION_SIZE) % 2 == 0) {
				// Odd configs
				return fFixedRenderer1;
			} else {
				// Even configs
				return fFixedRenderer2;
			}
		}
		if (col == 0) {
			// Global run
			return fCheckableRenderer;
		}
		if (col >= COLUMN_SECTION_SIZE) {
			int offset = col % COLUMN_SECTION_SIZE;
			if (offset == ConfigColumn.TRANSMISSION_RUN.getOffset() || offset == ConfigColumn.SCATTERING_RUN.getOffset()) {
				// Transmission or scattering run
				return fCheckableRenderer;
			} else if (offset == ConfigColumn.TRANSMISSION.getOffset() || offset == ConfigColumn.SCATTERING.getOffset()) {
//				if ac
//				fBarDiagramCellRenderer	
//				return ftextRenderer;
				return fBarDiagramCellRenderer;
			}
		}
		return ftextRenderer;
	}

	/*************************************************************************
	 * 
	 * Table content
	 * 
	 *************************************************************************/
	
	@Override
	public Object doGetContentAt(int col, int row) {
		/*********************************************************************
		 * Top 2 rows
		 *********************************************************************/
		if (row < 2) {
			return getHeaderContent(row, col);
		}
		/*********************************************************************
		 * Sample columns
		 *********************************************************************/
		if (col < COLUMN_FIXED_SIZE) {
			return getSampleContent(row - 2, col);
		}
		/*********************************************************************
		 * Config columns
		 *********************************************************************/
		return getConfigContent(row - 2, (col - COLUMN_FIXED_SIZE) / COLUMN_SECTION_SIZE, (col - COLUMN_FIXED_SIZE) % COLUMN_SECTION_SIZE);
	}

	// Returns header text
	private Object getHeaderContent(int row, int col) {
		if (row == ROW_HEADER_GROUP) {
			// Row 0
			if (col >= COLUMN_SECTION_SIZE && col % COLUMN_SECTION_SIZE == 1) {
				return experiment.getInstrumentConfigs().get(col / COLUMN_SECTION_SIZE - 1).getName();
			}
		} else if (row == ROW_HEADER) {
			// Row 1
			if (col >= 0 && col < COLUMN_SECTION_SIZE) {
				// Global
				return GlobalColumn.getColumnLabel(col);
			} else {
				// Add units to preset
				if (col >= COLUMN_SECTION_SIZE && col % COLUMN_SECTION_SIZE == 4) {
					ScanMode mode = experiment.getInstrumentConfigs().get(col / COLUMN_SECTION_SIZE - 1).getMode();
					
					if (mode == ScanMode.TIME) {
						return ConfigColumn.getColumnLabel(col) + " (sec)";
					} else if (mode == ScanMode.COUNTS || mode == ScanMode.BM1) {
						return ConfigColumn.getColumnLabel(col) + " (count)";
					}
				}
				// Config specific
				return ConfigColumn.getColumnLabel(col);
			}
		}
		return "";
	}

	// Returns content for fixed columns
	private Object getSampleContent(int index, int col) {
		if (acquisition.getEntries().size() <= index) {
			// Out of range
			return "";
		}
		AcquisitionEntry entry = acquisition.getEntries().get(index);
		if (col == GlobalColumn.RUN.getIndex()) {
			return entry.isRunnable();
		} else if (col == GlobalColumn.SEQUENCE.getIndex()) {
			return index + 1;
		} else if (col == GlobalColumn.POSITION.getIndex()) {
			return entry.getSample().getPosition();
		} else if (col == GlobalColumn.SAMPLE_NAME.getIndex()) {
			return entry.getSample().getName();
		} else if (col == GlobalColumn.THICKNESS.getIndex()) {
			return entry.getSample().getThickness();
		}
		return "";
	}

	// Returns content for configs
	private Object getConfigContent(int entryIndex, int configIndex, int offset) {
		if (acquisition.getEntries().size() <= entryIndex) {
			// Out of range
			return "";
		}
		AcquisitionEntry entry = acquisition.getEntries().get(entryIndex);
		if (experiment.getInstrumentConfigs().size() <= configIndex) {
			return "";
		}
		InstrumentConfig config = experiment.getInstrumentConfigs().get(configIndex);
		AcquisitionSetting setting = entry.getConfigSettings().get(config);
		if (setting != null) {
			if (offset == ConfigColumn.TRANSMISSION_RUN.getOffset()) {
				return setting.isRunTransmission();
			} else if (offset == ConfigColumn.TRANSMISSION.getOffset()) {
				if (setting.getTransmissionDataFile() != null) {
					return new ProgressData(0.0f, setting.getTransmissionDataFile());
				} else if (!setting.isRunningTransmission()) {
					return new ProgressData(0.0f, "");
				} else {
					return new ProgressData(1.0f, "RUNNING...");
				}
			} else if (offset == ConfigColumn.SCATTERING_RUN.getOffset()) {
				return setting.isRunScattering();
			} else if (offset == ConfigColumn.SCATTERING.getOffset()) {
				if (setting.getScatteringDataFile() != null) {
					return new ProgressData(0.0f, setting.getScatteringDataFile());
				} else if (!setting.isRunningScattering()) {
					return new ProgressData(0.0f, "");
				} else {
					return new ProgressData(1.0f, "RUNNING...");
				}
			} else if (offset == ConfigColumn.PRESET.getOffset()) {
				if (setting.getPreset() >= 100000) {
					return numberFormat.format(setting.getPreset());
				} else {
					return setting.getPreset();
				}
			}
		}
		return "";
	}
	
	@Override
	public void doSetContentAt(int col, int row, Object value) {
		if (row < 2) {
			// Header
			return;
		}
		int entryIndex = row - 2;
		if (acquisition.getEntries().size() <= entryIndex) {
			// Out of range
			return;
		}
		// Get acquisition entry
		AcquisitionEntry entry = acquisition.getEntries().get(entryIndex);
		updateAcquisionEntry(entry, col, value);
		if (!experiment.isControlledEnvironment() && (acquisition instanceof ControlledAcquisition)) {
			entry = experiment.getNormalAcquisition().getEntries().get(entryIndex);
			updateAcquisionEntry(entry, col, value);
		} 
		
	}
	
	private void updateAcquisionEntry(AcquisitionEntry entry, int col, Object value) {
		if (col == 0) {
			entry.setRunnable((Boolean) value);
			if (table != null) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						for (int row = 2; row < getRowCount(); row++) {
							for (int col = 5; col < getColumnCount(); col += 5) {
								table.updateCell(col, row);
								table.updateCell(col + 2, row);
							}
						}
					}
				});
			}
		}
		if (col >= COLUMN_FIXED_SIZE) {
			int configIndex = (col - COLUMN_FIXED_SIZE) / COLUMN_SECTION_SIZE;
			int colOffset = col % COLUMN_SECTION_SIZE; 
			InstrumentConfig config = experiment.getInstrumentConfigs().get(configIndex);
			AcquisitionSetting setting = entry.getConfigSettings().get(config);
			if (colOffset == ConfigColumn.TRANSMISSION_RUN.getOffset()) {
				// Set run transmission
				setting.setRunTransmission((Boolean) value);
			} else if(colOffset == ConfigColumn.SCATTERING_RUN.getOffset()) {
				// Set run scattering				
				setting.setRunScattering((Boolean) value);
			} else if (colOffset == ConfigColumn.PRESET.getOffset()) {
				try {
					Long preset = (Long) numberFormat.parse(((String) value).toUpperCase());
					setting.setPreset(preset);
				} catch (Exception e) {
					// TODO: display error?
				}
			}
		}
	}
	/**
	 * Helper method to retrieve the associated setting from a cell.
	 * 
	 * @param col
	 * @param row
	 * @return
	 */
	public AcquisitionSetting getAcquisitionSetting(int col, int row) {
		if (row < 2) {
			// Header
			return null;
		}
		int entryIndex = row - 2;
		if (acquisition.getEntries().size() <= entryIndex) {
			// Out of range
			return null;
		}
		// Get acquisition entry
		AcquisitionEntry entry = acquisition.getEntries().get(entryIndex);
		if (col >= COLUMN_FIXED_SIZE) {
			int configIndex = (col - COLUMN_FIXED_SIZE) / COLUMN_SECTION_SIZE;
			int colOffset = col % COLUMN_SECTION_SIZE; 
			InstrumentConfig config = experiment.getInstrumentConfigs().get(configIndex);
			AcquisitionSetting setting = entry.getConfigSettings().get(config);
			if(colOffset == ConfigColumn.SCATTERING.getOffset()) {
				return setting;
			}
		}
		return null;
	}

	/*************************************************************************
	 * 
	 * Table physical size properties
	 * 
	 *************************************************************************/
	
	@Override
	public int getInitialColumnWidth(int column) {
		if (column < COLUMN_FIXED_SIZE) {
			if (column == GlobalColumn.RUN.getIndex()) {
				return 20;
			} else if (column == GlobalColumn.SEQUENCE.getIndex()) {
				return 70;
			} else if (column == GlobalColumn.POSITION.getIndex()) {
				return 55;
			} else if (column == GlobalColumn.SAMPLE_NAME.getIndex()) {
				return 100;
			} else if (column == GlobalColumn.THICKNESS.getIndex()) {
				return 70;
			}
		} else {
			int columnOffset = column % COLUMN_SECTION_SIZE;
			if (columnOffset == ConfigColumn.TRANSMISSION_RUN.getOffset()) {
				return 20;
			} else if (columnOffset == ConfigColumn.TRANSMISSION.getOffset()) {
				return 90;
			} else if (columnOffset == ConfigColumn.SCATTERING_RUN.getOffset()) {
				return 20;
			} else if (columnOffset == ConfigColumn.SCATTERING.getOffset()) {
				return 75;
			} else if (columnOffset == ConfigColumn.PRESET.getOffset()) {
				return 80;
			}
		}
		// Default
		return 80;
	}

	@Override
	public int getInitialRowHeight(int row) {
		return 20;
	}

	@Override
	public int getRowHeightMinimum() {
		return 20;
	}
	
	/*************************************************************************
	 * 
	 * Table layout and dimension properties
	 * 
	 *************************************************************************/
	
	@Override
	public int doGetColumnCount() {
		return experiment.getInstrumentConfigs().size() * COLUMN_SECTION_SIZE + COLUMN_SECTION_SIZE;
	}
	
	@Override
	public int doGetRowCount() {
		return acquisition.getEntries().size() + 2;
	}
	
	@Override
	public int getFixedHeaderColumnCount() {
		return 0;
	}

	@Override
	public int getFixedHeaderRowCount() {
		// Reserves 2 fixed row
		return 2;
	}

	@Override
	public int getFixedSelectableColumnCount() {
		// Reserves fixed columns
		return COLUMN_FIXED_SIZE;
	}

	@Override
	public int getFixedSelectableRowCount() {
		return 0;
	}

	@Override
	public boolean isColumnResizable(int col) {
		// [GUMTREE-422] New resizable column logic
		// No resize for sample checkbox
		if (col == 0) {
			return false;
		}
		// No resize for individual run checkbox
		if (col >= COLUMN_SECTION_SIZE
				&& (col % COLUMN_SECTION_SIZE == ConfigColumn.TRANSMISSION_RUN.getOffset() || 
					col % COLUMN_SECTION_SIZE == ConfigColumn.SCATTERING_RUN.getOffset())) {
			return false;
		}
		return true;
//		if (col == GlobalColumn.SAMPLE_NAME.getIndex()
//				|| col == GlobalColumn.THICKNESS.getIndex()) {
//			// Allow resize for sample name and thickness columns
//			return true;
//		}
//		if (col >= COLUMN_SECTION_SIZE
//				&& (col % COLUMN_SECTION_SIZE == ConfigColumn.TRANSMISSION.getOffset()
//						|| col % COLUMN_SECTION_SIZE == ConfigColumn.PRESET.getOffset())) {
//			// Allow resize on transmission (config name) or preset column
//			return true;
//		}
//		return false;
	}

	@Override
	public boolean isRowResizable(int row) {
		// TODO Auto-generated method stub
		return false;
	}
	
	// Support row / column spanning
	public Point doBelongsToCell(int col, int row) {
		if (row == ROW_HEADER_GROUP) {
			if (col < COLUMN_SECTION_SIZE && col >= 0) {
				// Span only the first 5 fixed col (not all due to scrolling problem in KTable)
				return new Point((col / COLUMN_FIXED_SIZE) * COLUMN_FIXED_SIZE, row);
			} else {
				// KTabe may ask for col < 0 (this is a bug?)
				return new Point(col, row);
			}
		} else {
			return new Point(col, row);
		}
//		return new Point(col, row);
	}
	
}
