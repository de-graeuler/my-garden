package de.graeuler.garden;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.StaticAppConfig;
import de.graeuler.garden.data.GardenDataCollector;
import de.graeuler.garden.data.JsonDataConverter;
import de.graeuler.garden.data.model.DataRecord;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.DataConverter;
import de.graeuler.garden.interfaces.MonitorService;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.sensor.TemperatureSensor;
import de.graeuler.garden.monitor.sensor.WaterLevelSensor;
import de.graeuler.garden.monitor.service.NetworkTrafficMonitorService;
import de.graeuler.garden.monitor.service.SensorMonitorService;
import de.graeuler.garden.uplink.HttpUplinkService;
import de.graeuler.garden.uplink.Uplink;

public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AppConfig.class).to(StaticAppConfig.class);
		bind(new TypeLiteral<DataConverter<List<DataRecord<?>>, String>>(){}).to(JsonDataConverter.class);
		bind(new TypeLiteral<Uplink<String>>(){}).to(HttpUplinkService.class);
		bind(DataCollector.class).to(GardenDataCollector.class);
		bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(4));
		
		Multibinder<MonitorService> monitorServiceBinder = Multibinder.newSetBinder(binder(), MonitorService.class);
		monitorServiceBinder.addBinding().to(SensorMonitorService.class);
		monitorServiceBinder.addBinding().to(NetworkTrafficMonitorService.class);

		// now bind sensor handlers.
		Multibinder<SensorHandler> sensorHandlerBinder = Multibinder.newSetBinder(binder(), SensorHandler.class);
		sensorHandlerBinder.addBinding().to(WaterLevelSensor.class);
		sensorHandlerBinder.addBinding().to(TemperatureSensor.class);
//		sensorHandlerBinder.addBinding().to(MasterBrickTemperatureSensor.class);
	}

}
