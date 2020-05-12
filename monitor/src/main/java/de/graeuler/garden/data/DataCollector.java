package de.graeuler.garden.data;

import java.io.Serializable;
import java.util.Collection;

public interface DataCollector<T extends Serializable> {

	public void collect(String dataKey, Serializable valueOf);
	
	long processCollectedRecords(DataProcessor<T> recordProcessor, long blockSize);

	public void removeDataset(Collection<T> dataset);

}
