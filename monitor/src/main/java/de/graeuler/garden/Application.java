package de.graeuler.garden;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Application {

	public static void main(String[] args) {
			
		Injector injector = Guice.createInjector(
				new ApplicationModule()
		);
		
		GardenMonitor gardenMonitor = injector.getInstance(GardenMonitor.class);
		
		gardenMonitor.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				gardenMonitor.stop();
			}
		});
	}

}
