package de.graeuler.garden.integration;

import com.google.inject.multibindings.Multibinder;

import de.graeuler.garden.ApplicationModule;
import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.integration.sensor.SimulatedSensor;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.util.CommandLineReader;

public class IntegrationModule extends ApplicationModule {

	@Override
	protected void bindConfiguration() {
		bind(AppConfig.class).to(IntegrationConfig.class);
	}
	
	@Override
	protected void bindSensorHandlers(Multibinder<SensorHandler> sensorHandlerBinder) {
		sensorHandlerBinder.addBinding().toInstance(new SimulatedSensor("water-level", 1, 2000, "mm"));
		sensorHandlerBinder.addBinding().toInstance(new SimulatedSensor("temperature", 5, 25, "deg"));
	}
	
	@Override
	protected Class<? extends CommandLineReader> getCommandLineReaderType() {
		return StaticVnStatLine.class;
	}
	
}
