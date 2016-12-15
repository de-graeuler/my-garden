package de.graeuler.garden.data;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
	
	private Path dataLocationPath;
	private List<DataRecord<?>> data = Collections.synchronizedList(new ArrayList<> ());
	private ScheduledExecutorService scheduler;
	private DataConverter<List<DataRecord<?>>, String> converter;

	private Uplink<String> uplink;

	private int collectTimeRate;

	private TimeUnit collectTimeUnit;

	@Inject
	GardenDataCollector(AppConfig config, DataConverter<List<DataRecord<?>>, String> converter, Uplink<String> uplink, ScheduledExecutorService scheduler) {
		this.uplink = uplink;
		this.scheduler = scheduler;
		this.converter = converter;
		
		this.collectTimeUnit = TimeUnit.valueOf(((String) config.get(AppConfig.Key.COLLECT_TIME_UNIT)).toUpperCase());
		this.collectTimeRate = (int) config.get(AppConfig.Key.COLLECT_TIME_RATE);
		
		this.dataLocationPath = FileSystems.getDefault().getPath(
				(String) config.get(AppConfig.Key.DC_STORE_PATH), 
				(String) config.get(AppConfig.Key.DC_STORE_FILE)
				);

		initialize();
	}
	
	private void initialize() {
//		log.info("Load data from {}", this.dataLocationPath);
//		log.warn("Loading data from location path not yet implemented.");
		log.info("Uploading collected data every {} {}", this.collectTimeRate, this.collectTimeUnit);
		this.scheduler.scheduleAtFixedRate(this, 0, this.collectTimeRate, this.collectTimeUnit);
	}





	@Override
	public void collect(Map<String, Object> data) {
		for(String key : data.keySet()) {
			this.collect(key, data.get(key));
		}
	}

	@Override
	public void collect(String string, Object value) {
		DataRecord<Object> record = new DataRecord<Object>(string, value);
		synchronized (this.data) {
			this.data.add(record);
		}
	}

	@Override
	public void run() {
		synchronized (this.data) { // adding data records should be synchronized 
			
			if (this.data.size() == 0) return;
			
			String jsonDataString = converter.convert(this.data);
			if (this.uplink.pushData(jsonDataString)) {
				this.data.clear();
			} else {
				log.error("Unable to push data to the uplink.");
			}
		}
		
	}
	
}
