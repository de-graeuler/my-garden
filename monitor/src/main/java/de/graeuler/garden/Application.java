package de.graeuler.garden;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.AppConfig.Key;
import de.graeuler.garden.interfaces.MonitorService;

public class Application {

	private Set<MonitorService> monitorServices;
	private AppConfig config;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private ScheduledExecutorService executor;

	@Inject
	Application(AppConfig config, Set<MonitorService> monitorServices, ScheduledExecutorService executor)
	{
		this.config = config;
		this.monitorServices = monitorServices;
		this.executor = executor;
	}
	
	
	private void start() {
		this.logConfigSettings();
		monitorServices.forEach(s -> s.monitor());
	}
	
	private void stop() {
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
		for(Key k : AppConfig.Key.values()) {
			log.info("{}: {}", String.format("%35s",  k.getKey()), k.from(this.config));
		}
	}


	public static void main(String[] args) {
			
		Injector injector = Guice.createInjector(
				new ApplicationModule()
		);
		
		Application app = injector.getInstance(Application.class);
		app.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				app.stop();
			}
		});
	}

}
