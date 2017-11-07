package de.graeuler.garden.interfaces;

import java.io.Serializable;
import java.util.List;

import de.graeuler.garden.data.DataRecord;

public interface DataCollector {

	public void collect(String string, Serializable valueOf);

	public boolean dataIsEmpty();

	public List<DataRecord<?>> getCollectedDataset();

	public void removeDataset(List<DataRecord<?>> dataset);
	
}
