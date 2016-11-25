package de.graeuler.garden;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.graeuler.garden.interfaces.MonitorService;

public class Application {

	public static void main(String[] args) {
			
		Injector injector = Guice.createInjector(
				new ApplicationModule()
		);
		
		injector.getInstance(MonitorService.class).monitor();
				    
	}

}
