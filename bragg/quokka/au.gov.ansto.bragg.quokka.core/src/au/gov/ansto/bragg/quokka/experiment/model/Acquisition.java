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

package au.gov.ansto.bragg.quokka.experiment.model;

public abstract class Acquisition extends AbstractModelObject {

	private PropertyList<AcquisitionEntry> entries;
	
	private Experiment experiment;
	
	public Acquisition(Experiment experiment) {
		this.experiment = experiment;
	}
	
	public PropertyList<AcquisitionEntry> getEntries() {
		if (entries == null) {
			entries = new PropertyList<AcquisitionEntry>(this, "entries");
		}
		return entries;
	}

	public Experiment getExperiment() {
		return experiment;
	}

	/*************************************************************************
	 * Helper methods
	 *************************************************************************/
	
	protected void handleNewSample(Sample sample, boolean ordered) {
		AcquisitionEntry entry = new AcquisitionEntry(sample);
		if (ordered) {
			boolean inserted = false;
			for (int i = 0; i < getEntries().size(); i++) {
				if (getEntries().get(i).getSample().getPosition() > sample.getPosition()) {
					getEntries().add(i, entry);
					inserted = true;
					break;
				}
			}
			if (!inserted) {
				getEntries().add(entry);
			}
		} else {
			getEntries().add(entry);
		}
		// Create setting from existing configs
		for (InstrumentConfig config : experiment.getInstrumentConfigs()) {
			entry.getConfigSettings().put(config, new AcquisitionSetting(config));
		}
	}
	
	protected void handleRemovedSample(Sample sample) {
		AcquisitionEntry[] entries = getEntries().toArray(AcquisitionEntry.class);
		for (AcquisitionEntry entry : entries) {
			if (entry.getSample().equals(sample)) {
				getEntries().remove(entry);
			}
		}
	}
	
	protected void handleNewConfig(InstrumentConfig config) {
		for (AcquisitionEntry entry : getEntries()) {
			entry.getConfigSettings().put(config, new AcquisitionSetting(config));
		}
	}
	
	protected void handleRemovedConfig(InstrumentConfig config) {
		for (AcquisitionEntry entry : getEntries()) {
			entry.getConfigSettings().remove(config);
		}
	}

	public void handleDuplicateEntry(int index) {
		AcquisitionEntry entry = getEntries().get(index);
		getEntries().add(index + 1, entry.copy());
	}
	
	public void handleRemoveEntry(int index) {
		getEntries().remove(index);
	}
	
	public void handleSwapEntries(int index1, int index2) {
		// index1 and index2 must be within range
		if (index1 == index2 || (index1 < 0 || index1 >= getEntries().size())
				|| (index2 < 0 || index2 >= getEntries().size())) {
			return;
		}
		// Swap if index1 is larger
		if (index1 > index2) {
			int temp = index1;
			index1 = index2;
			index2 = temp;
		}
		AcquisitionEntry entry1 = getEntries().remove(index1);
		AcquisitionEntry entry2 = getEntries().remove(index2 - 1);
		getEntries().add(index1, entry2);
		getEntries().add(index2, entry1);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Acquisition [entries=");
		builder.append(entries);
		builder.append(", experiment=");
		builder.append(experiment);
		builder.append("]");
		return builder.toString();
	}
	
}
