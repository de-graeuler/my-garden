package de.graeuler.garden.uplink;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.DataConverter;

public class DataUploader implements Runnable {

	private ScheduledExecutorService scheduler;
	private DataConverter<List<DataRecord<?>>, String> converter;
	private Uplink<String> uplink;
	private DataCollector dataCollector;

	private int collectTimeRate;
	private TimeUnit collectTimeUnit;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	DataUploader(AppConfig config, DataCollector dataCollector, DataConverter<List<DataRecord<?>>, String> converter, Uplink<String> uplink,
			ScheduledExecutorService scheduler) {
		this.uplink = uplink;
		this.scheduler = scheduler;
		this.converter = converter;
		this.dataCollector = dataCollector;
		
		this.collectTimeUnit = (TimeUnit) AppConfig.Key.COLLECT_TIME_UNIT.from(config);
		this.collectTimeRate = (Integer)  AppConfig.Key.COLLECT_TIME_RATE.from(config);

		initialize();
	}
		
	private void initialize() {
		this.scheduler.scheduleAtFixedRate(this, 0, this.collectTimeRate, this.collectTimeUnit);
	}

	@Override
	public void run() {
			
		if (this.dataCollector.dataIsEmpty()) return;

		List<DataRecord<?>> dataset = this.dataCollector.getCollectedDataset();
		String jsonDataString = converter.convert(dataset);
		if (this.uplink.pushData(jsonDataString)) {
			this.dataCollector.removeDataset(dataset);
		} else {
			log.error("Unable to push dataset of {} records to the uplink.", dataset.size());
		}

	}

}
