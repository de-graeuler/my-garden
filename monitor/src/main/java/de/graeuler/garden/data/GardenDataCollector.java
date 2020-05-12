package de.graeuler.garden.data;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GardenDataCollector implements DataCollector<DataRecord>{
	
	private DataPersister<DataRecord> persister;

	@Inject
	GardenDataCollector(DataPersister<DataRecord> persister) {
		this.persister = persister;
	}
	
	@Override
	public void collect(String string, Serializable value) {
		DataRecord record = new DataRecord(string, value);
		persister.write(record);
	}

	@Override
	public long processCollectedRecords(DataProcessor<DataRecord> recordProcessor, long blockSize) {
		long result = 0;
		try(DataIterator<DataRecord> recordIterator = persister.iterate()) {
			Collection<DataRecord> recordBlock = new ArrayDeque<DataRecord>((int) blockSize);
			while(recordIterator.hasNext()) {
				long i = blockSize;
				while(i-- > 0 && recordIterator.hasNext()) {
					recordBlock.add(recordIterator.next());
				}
				if (recordProcessor.apply(recordBlock)) {
					result += persister.deleteAll(recordBlock);
					recordBlock.clear();
				} else {
					break;
				}
			}
		}
		return result;
	}
	
	@Override
	public void removeDataset(Collection<DataRecord> dataset) {
		persister.deleteAll(dataset);
	}

	
}
