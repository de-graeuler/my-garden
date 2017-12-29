package de.graeuler.garden.monitor.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.MonitorService;
import de.graeuler.garden.monitor.util.Bytes;
import de.graeuler.garden.monitor.util.VnStatPos;

public class NetworkTrafficMonitorService implements MonitorService, Runnable {

	private DataCollector dataCollector;
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> scheduledConsumptionCheckHandle;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private TimeUnit netCheckTimeUnit;
	private Integer netCheckTimeRate;
	private double bytesThreshold;
	private double upperTrafficThreshold = 0.0;
	private double lowerTrafficThreshold = 0.0;
	
	private final String vnstatOnelineCommand;
	private final String vnstat_language_tag;

	@Inject
	public NetworkTrafficMonitorService(AppConfig config, DataCollector dataCollector, ScheduledExecutorService scheduler) {
		this.vnstatOnelineCommand = (String) AppConfig.Key.NETWORK_VNSTAT_CMD.from(config);
		this.vnstat_language_tag = (String) AppConfig.Key.NETWORK_VNSTAT_LANG_TAG.from(config);
		this.netCheckTimeRate = (Integer) AppConfig.Key.NET_TIME_RATE.from(config);
		this.netCheckTimeUnit = (TimeUnit) AppConfig.Key.NET_TIME_UNIT.from(config);
		this.bytesThreshold   = (Integer) AppConfig.Key.NET_VOL_CHG_THD.from(config);
		this.dataCollector = dataCollector;
		this.scheduler = scheduler;
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
					Bytes.format(this.lowerTrafficThreshold), Bytes.format(this.upperTrafficThreshold));
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
				log.info("Monthly network traffic {} left threshold range of {} to {}", Bytes.format(bytes), 
						Bytes.format(this.lowerTrafficThreshold), Bytes.format(this.upperTrafficThreshold));
				dataCollector.collect("month-total-traffic", bytes);
				setNewThresholds(bytes); 
			}
		}
	}

	private boolean hasLeftThresholds(double bytes) {
		return bytes > this.upperTrafficThreshold || bytes < this.lowerTrafficThreshold;
	}

	protected double readTrafficBytes() {
		String output = readVnStatOneLineOutput();
		try {
			return VnStatPos.MONTH_TOTAL.fromVnStatResult(output, this.vnstat_language_tag);
		} catch (ParseException ex) {
			log.error("Unable to parse vnStat result: {}", ex.getMessage());
		}
		return -1;
	}

	private String readVnStatOneLineOutput() {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(vnstatOnelineCommand);
			Process p = processBuilder.start();
			try(BufferedReader stdInput = new BufferedReader(new 
			         InputStreamReader(p.getInputStream()))) {
				String output = stdInput.readLine();
				p.waitFor(5, TimeUnit.SECONDS);
				if (p.isAlive()) {
					p.destroyForcibly();
					log.warn("VnStat needed to be killed forcefully.");
				}
				return output;
			}
		} catch (IOException e) {
			log.error("Unable to execute vnstat. {} ", e.getMessage());
		} catch (InterruptedException e) {
			log.error("Waiting for vnstat to terminate was interrupted.");
		}
		return null;
	}
	
	private void scheduleBandwidthConsumptionCheck() {
		this.scheduledConsumptionCheckHandle = this.scheduler.scheduleAtFixedRate(this, 0, this.netCheckTimeRate, this.netCheckTimeUnit);
	}
	
	private void cancelBandwidthConsumptionCheck() {
		this.scheduledConsumptionCheckHandle.cancel(false);
	}
	
}
