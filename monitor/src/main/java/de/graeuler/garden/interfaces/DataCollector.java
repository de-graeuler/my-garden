package de.graeuler.garden.interfaces;

import java.io.Serializable;
import java.util.Collection;

import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.data.DataRecordProcessor;

public interface DataCollector {

	public void collect(String dataKey, Serializable valueOf);
	
	long processCollectedRecords(DataRecordProcessor<DataRecord> recordProcessor, long blockSize);

	public void removeDataset(Collection<DataRecord> dataset);

}
