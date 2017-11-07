package de.graeuler.garden.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.DataCollector;

@Singleton
public class GardenDataCollector implements DataCollector {
	
	private final List<DataRecord<?>> data = Collections.synchronizedList(new ArrayList<> ());
	private DataPersister<DataRecord<Serializable>> persister;

	@Inject
	GardenDataCollector(AppConfig config, DataPersister<DataRecord<Serializable>> persister) {
		this.persister = persister;
		
		initialize();
	}
	
	private void initialize() {
		this.data.addAll(persister.readAll());
	}

	@Override
	public void collect(String string, Serializable value) {
		DataRecord<Serializable> record = new DataRecord<>(string, value);
		persister.write(record);
		this.data.add(record);
	}

	@Override
	public boolean dataIsEmpty() {
		return this.data.isEmpty();
	}

	@Override
	public List<DataRecord<?>> getCollectedDataset() {
		return this.data;
	}

	@Override
	public void removeDataset(List<DataRecord<?>> dataset) {
		if(this.data.removeAll(dataset))
			persister.deleteAll();
	}

	
}
