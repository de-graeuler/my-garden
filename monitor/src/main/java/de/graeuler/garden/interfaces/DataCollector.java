package de.graeuler.garden.interfaces;

import java.io.Serializable;
import java.util.Collection;

import de.graeuler.garden.data.DataProcessor;

public interface DataCollector<T extends Serializable> {

	public void collect(String dataKey, Serializable valueOf);
	
	long processCollectedRecords(DataProcessor<T> recordProcessor, long blockSize);

	public void removeDataset(Collection<T> dataset);

}
