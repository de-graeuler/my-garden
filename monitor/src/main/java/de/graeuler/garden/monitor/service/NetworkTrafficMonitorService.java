package de.graeuler.garden.monitor.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.MonitorService;
import de.graeuler.garden.monitor.util.Bytes;
import de.graeuler.garden.monitor.util.CommandLineReader;
import de.graeuler.garden.monitor.util.VnStatPos;

public class NetworkTrafficMonitorService implements MonitorService, Runnable {

	private static final String DATA_KEY = "month-total-traffic";
	private DataCollector<DataRecord> dataCollector;
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> scheduledConsumptionCheckHandle;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private TimeUnit netCheckTimeUnit;
	private Integer netCheckTimeRate;
	private double bytesThreshold;
	private double upperTrafficThreshold = 0.0;
	private double lowerTrafficThreshold = 0.0;
	
	private final List<String> vnstatOnelineCommand;
	private final String vnStatLanguageTag;
	private CommandLineReader commandLineReader;

	@SuppressWarnings("unchecked")
	@Inject
	public NetworkTrafficMonitorService(AppConfig config, DataCollector<DataRecord> dataCollector, ScheduledExecutorService scheduler, CommandLineReader commandLineReader) {
		this.vnstatOnelineCommand = (List<String>) ConfigurationKeys.NETWORK_VNSTAT_CMD.from(config);
		this.vnStatLanguageTag = (String) ConfigurationKeys.NETWORK_VNSTAT_LANG_TAG.from(config);
		this.netCheckTimeRate = (Integer) ConfigurationKeys.NET_TIME_RATE.from(config);
		this.netCheckTimeUnit = (TimeUnit) ConfigurationKeys.NET_TIME_UNIT.from(config);
		this.bytesThreshold   = (Integer) ConfigurationKeys.NET_VOL_CHG_THD.from(config);
		this.dataCollector = dataCollector;
		this.scheduler = scheduler;
		this.commandLineReader = commandLineReader;
	}
	
	@Override
	public void monitor() {
		initThresholds();
		scheduleBandwidthConsumptionCheck();
	}
	
	@Override
	public void shutdown() {
		cancelBandwidthConsumptionCheck();
	}

	private void initThresholds() {
		double bytes = readTrafficBytes();
		if (0 < bytes) {
			setNewThresholds(bytes); 
			log.info ("Network threshold initialized: {} to {}", 
					Bytes.formatSI(this.lowerTrafficThreshold), Bytes.formatSI(this.upperTrafficThreshold));
		}
	}

	private void setNewThresholds(double bytes) {
		this.lowerTrafficThreshold = bytes - 0.5 * this.bytesThreshold;
		this.upperTrafficThreshold = bytes + 0.5 * this.bytesThreshold;
	}

	@Override
	public void run() {
		double bytes = readTrafficBytes();
		if (bytes > 0) {
			if(hasLeftThresholds(bytes)) {
				log.info("Monthly network traffic {} left threshold range of {} to {}", Bytes.formatSI(bytes), 
						Bytes.formatSI(this.lowerTrafficThreshold), Bytes.formatSI(this.upperTrafficThreshold));
				dataCollector.collect(DATA_KEY, bytes);
				setNewThresholds(bytes); 
			}
		}
	}

	protected boolean hasLeftThresholds(double bytes) {
		return bytes > this.upperTrafficThreshold || bytes < this.lowerTrafficThreshold;
	}

	protected double readTrafficBytes() {
		try {
			String output = commandLineReader.readFromCommand(vnstatOnelineCommand);
			return VnStatPos.MONTH_TOTAL.fromVnStatResult(output, this.vnStatLanguageTag);
		} catch (IOException e) {
			log.error("Unable to execute vnstat. {} ", e.getMessage());
		} catch (ParseException ex) {
			log.error("Unable to parse vnStat result: {}", ex.getMessage());
		}
		return -1;
	}
	
	private void scheduleBandwidthConsumptionCheck() {
		this.scheduledConsumptionCheckHandle = this.scheduler.scheduleAtFixedRate(this, 0, this.netCheckTimeRate, this.netCheckTimeUnit);
	}
	
	private void cancelBandwidthConsumptionCheck() {
		this.scheduledConsumptionCheckHandle.cancel(false);
	}
	
}
