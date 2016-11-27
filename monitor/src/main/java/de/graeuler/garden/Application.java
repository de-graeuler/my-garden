package de.graeuler.garden;

import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;

import de.graeuler.garden.interfaces.MonitorService;

public class Application {

	private Set<MonitorService> monitorServices;

	@Inject
	Application(Set<MonitorService> monitorServices)
	{
		this.monitorServices = monitorServices;
	}
	
	
	private void start() {
		for(MonitorService service : this.monitorServices) {
			service.monitor();
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
