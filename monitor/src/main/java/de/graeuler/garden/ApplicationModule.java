package de.graeuler.garden;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.PropertyFileAppConfig;
import de.graeuler.garden.data.GardenDataCollector;
import de.graeuler.garden.data.JsonDataConverter;
import de.graeuler.garden.data.model.DataRecord;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.DataConverter;
import de.graeuler.garden.interfaces.MonitorService;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.sensor.TemperatureSensor;
import de.graeuler.garden.monitor.sensor.WaterLevelSensor;
import de.graeuler.garden.monitor.service.SensorMonitorService;
import de.graeuler.garden.uplink.HttpUplinkService;
import de.graeuler.garden.uplink.Uplink;

public class ApplicationModule extends AbstractModule {

	static final Logger log = LoggerFactory.getLogger(ApplicationModule.class);
	
	static final String filename = "res/app.config.properties";


	@Override
	protected void configure() {
		Properties properties = new Properties();
		try {
			InputStream inputStream  = new FileInputStream(filename);
			properties.load(inputStream);
		} catch (IOException e) {
			log.error("Unable to load configuration file: {}", e.getMessage());
			log.error("Property file {} required in {}", filename, new File("").getAbsolutePath());
		}
		
		bind(Properties.class).toInstance(properties);
		bind(AppConfig.class).to(PropertyFileAppConfig.class);
		bind(new TypeLiteral<DataConverter<List<DataRecord<?>>, String>>(){}).to(JsonDataConverter.class);
		bind(new TypeLiteral<Uplink<String>>(){}).to(HttpUplinkService.class);
		bind(DataCollector.class).to(GardenDataCollector.class);
		bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(4));
		
		Multibinder<MonitorService> monitorServiceBinder = Multibinder.newSetBinder(binder(), MonitorService.class);
		monitorServiceBinder.addBinding().to(SensorMonitorService.class);
//		monitorServiceBinder.addBinding().to(NetworkTrafficMonitorService.class);

		// now bind sensor handlers.
		Multibinder<SensorHandler> sensorHandlerBinder = Multibinder.newSetBinder(binder(), SensorHandler.class);
		sensorHandlerBinder.addBinding().to(WaterLevelSensor.class);
		sensorHandlerBinder.addBinding().to(TemperatureSensor.class);
//		sensorHandlerBinder.addBinding().to(MasterBrickTemperatureSensor.class);
	}

}
