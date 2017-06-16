package de.graeuler.garden.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.data.model.DataRecord;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.DataConverter;
import de.graeuler.garden.uplink.Uplink;

@Singleton
public class GardenDataCollector implements DataCollector, Runnable {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
//	private Path dataLocationPath;
	private final List<DataRecord<?>> data = Collections.synchronizedList(new ArrayList<> ());
	private ScheduledExecutorService scheduler;
	private DataConverter<List<DataRecord<?>>, String> converter;
	private DataPersister<DataRecord<Serializable>> persister;

	private Uplink<String> uplink;

	private int collectTimeRate;

	private TimeUnit collectTimeUnit;

	@Inject
	GardenDataCollector(AppConfig config, DataConverter<List<DataRecord<?>>, String> converter, Uplink<String> uplink, ScheduledExecutorService scheduler, DataPersister<DataRecord<Serializable>> persister) {
		this.uplink = uplink;
		this.scheduler = scheduler;
		this.converter = converter;
		this.persister = persister;
		
		this.collectTimeUnit = (TimeUnit) AppConfig.Key.COLLECT_TIME_UNIT.from(config);
		this.collectTimeRate = (Integer)  AppConfig.Key.COLLECT_TIME_RATE.from(config);

		// TODO persist collected records to storage on collcet and load them from file on initialize.
//		this.dataLocationPath = FileSystems.getDefault().getPath(
//				(String) config.get(AppConfig.Key.DC_STORE_PATH), 
//				(String) config.get(AppConfig.Key.DC_STORE_FILE)
//				);

		initialize();
	}
	
	private void initialize() {
//		log.info("Load data from {}", this.dataLocationPath);
//		log.warn("Loading data from location path not yet implemented.");
		this.data.addAll(persister.readAll());
		this.scheduler.scheduleAtFixedRate(this, 0, this.collectTimeRate, this.collectTimeUnit);
	}

	@Override
	public void collect(String string, Serializable value) {
		DataRecord<Serializable> record = new DataRecord<>(string, value);
		persister.write(record);
		this.data.add(record);
	}

	@Override
	public void run() {
			
		if (this.data.isEmpty()) return;

		String jsonDataString = converter.convert(this.data);
		if (this.uplink.pushData(jsonDataString)) {
			this.data.clear();
			this.persister.deleteAll();
		} else {
			log.error("Unable to push data to the uplink. Keeping {} records in memory.", this.data.size());
		}

	}
	
}
