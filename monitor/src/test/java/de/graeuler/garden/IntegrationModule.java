package de.graeuler.garden;

import com.google.inject.multibindings.Multibinder;

import de.graeuler.garden.interfaces.SensorHandler;

public class IntegrationModule extends ApplicationModule {

	@Override
	protected void bindSensorHandlers(Multibinder<SensorHandler> sensorHandlerBinder) {
		sensorHandlerBinder.addBinding().to(SimulatedSensor.class);
	}
	
}
