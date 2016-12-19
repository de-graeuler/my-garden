package de.graeuler.garden;

import java.util.Set;

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

	@Inject
	Application(AppConfig config, Set<MonitorService> monitorServices)
	{
		this.config = config;
		this.monitorServices = monitorServices;
	}
	
	
	private void start() {
		this.logConfigSettings();
		for(MonitorService service : this.monitorServices) {
			service.monitor();
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
		
				    
	}

}
