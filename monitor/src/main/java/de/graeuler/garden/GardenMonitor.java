package de.graeuler.garden;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.monitor.service.MonitorService;

public class GardenMonitor {

	private AppConfig config;
	private ScheduledExecutorService executor;
	private Logger log = LoggerFactory.getLogger(GardenMonitor.class);
	private Set<MonitorService> monitorServices;

	@Inject
	GardenMonitor(AppConfig config, Set<MonitorService> monitorServices, ScheduledExecutorService executor)
	{
		this.config = config;
		this.monitorServices = monitorServices;
		this.executor = executor;
	}

	public void start() {
		this.logConfigSettings();
		monitorServices.forEach(s -> s.monitor());
	}

	public void stop() {
		log.info("Shutdown sequence");
		this.monitorServices.forEach(s -> s.shutdown());
		try {
			this.executor.shutdown();
			this.executor.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			log.error("Scheduler shutdown interrupted. Remaining tasks were aborted.");
		}
	}

	private void logConfigSettings() {
		log.info("Configuration settings used:");
		for(ConfigurationKeys k : ConfigurationKeys.values()) {
			log.info("{}: {}", String.format("%35s",  k.getPropertyName()), k.from(this.config));
		}
	}

}
