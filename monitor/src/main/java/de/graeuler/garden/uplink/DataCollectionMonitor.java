package de.graeuler.garden.uplink;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.data.DataRecordProcessor;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.DataConverter;
import de.graeuler.garden.interfaces.MonitorService;

public class DataCollectionMonitor implements Runnable, MonitorService {

	private ScheduledExecutorService scheduler;
	private DataConverter<Collection<DataRecord>, String> converter;
	private Uplink<String> uplink;
	private DataCollector dataCollector;

	private int collectTimeRate;
	private TimeUnit collectTimeUnit;
	private int collectBlockSize;

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private ScheduledFuture<?> dataUploaderHandle;

	@Inject
	DataCollectionMonitor(AppConfig config, DataCollector dataCollector, DataConverter<Collection<DataRecord>, String> converter, Uplink<String> uplink,
			ScheduledExecutorService scheduler) {
		this.uplink = uplink;
		this.scheduler = scheduler;
		this.converter = converter;
		this.dataCollector = dataCollector;
		
		this.collectTimeUnit  = (TimeUnit) ConfigurationKeys.COLLECT_TIME_UNIT.from(config);
		this.collectTimeRate  = (Integer)  ConfigurationKeys.COLLECT_TIME_RATE.from(config);
		this.collectBlockSize = (Integer)  ConfigurationKeys.COLLECT_BLOCK_SIZE.from(config); 

	}
		
	@Override
	public void run() {
			
		this.dataCollector.processCollectedRecords(new DataRecordProcessor<DataRecord>() {
			@Override
			public boolean call(Collection<DataRecord> dataset) {
				String jsonDataString = converter.convert(dataset);
				boolean dataPushed = uplink.pushData(jsonDataString);
				if ( ! dataPushed ) {
					log.error("Unable to push dataset of {} records to the uplink.", dataset.size());
					return false;
				}
				return true;
			}
		}, collectBlockSize);

	}

	@Override
	public void monitor() {
		dataUploaderHandle = this.scheduler.scheduleAtFixedRate(this, 0, this.collectTimeRate, this.collectTimeUnit);
		this.scheduler.execute(() -> {
			try {
				if (dataUploaderHandle.isCancelled()) {
					dataUploaderHandle.get();
				}
			} catch(Exception e) {
				log.error("Data Upload Error", e);
			}
		});
	}

	@Override
	public void shutdown() {
		if (dataUploaderHandle != null) {
			dataUploaderHandle.cancel(false);
		}
	}

}
