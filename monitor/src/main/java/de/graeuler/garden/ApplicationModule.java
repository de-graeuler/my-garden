package de.graeuler.garden;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.json.JsonValue;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.PropertyFileAppConfig;
import de.graeuler.garden.data.DataCollector;
import de.graeuler.garden.data.DataConverter;
import de.graeuler.garden.data.DataPersister;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.data.DerbyDataPersister;
import de.graeuler.garden.data.GardenDataCollector;
import de.graeuler.garden.data.JsonDataConverter;
import de.graeuler.garden.data.SerializableHashDelegate;
import de.graeuler.garden.data.SerializableToSha256;
import de.graeuler.garden.monitor.sensor.TemperatureSensor;
import de.graeuler.garden.monitor.sensor.VoltageCurrentSensor;
import de.graeuler.garden.monitor.sensor.WaterLevelSensor;
import de.graeuler.garden.monitor.service.MonitorService;
import de.graeuler.garden.monitor.service.NetworkTrafficMonitorService;
import de.graeuler.garden.monitor.service.SensorHandler;
import de.graeuler.garden.monitor.service.SensorMonitorService;
import de.graeuler.garden.monitor.service.SensorSystemAccess;
import de.graeuler.garden.monitor.tinkerforge.BrickDaemonFacade;
import de.graeuler.garden.monitor.util.CommandLineReader;
import de.graeuler.garden.monitor.util.VnStatReader;
import de.graeuler.garden.uplink.DataCollectionMonitor;
import de.graeuler.garden.uplink.HttpUplinkService;
import de.graeuler.garden.uplink.Uplink;

public class ApplicationModule extends AbstractModule {

	final Logger log = LoggerFactory.getLogger(ApplicationModule.class);
	
	static final String filename = "res/app.config.properties";

	private static final int THREAD_POOL_SIZE = 8;


	@Override
	protected void configure() {
		bindConfiguration();

		bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(THREAD_POOL_SIZE));

		bind(new TypeLiteral<DataConverter<Collection<DataRecord>, JsonValue>>(){}).to(JsonDataConverter.class);
		bind(SerializableHashDelegate.class).to(SerializableToSha256.class);

		bind(new TypeLiteral<DataCollector<DataRecord>>(){}).to(GardenDataCollector.class);

		bind(CloseableHttpClient.class).toInstance(HttpClients.createDefault());
		bind(new TypeLiteral<Uplink<JsonValue>>(){}).to(HttpUplinkService.class);
		bind(new TypeLiteral<DataPersister<DataRecord>>(){}).to(DerbyDataPersister.class);
		
		bind(CommandLineReader.class).to(getCommandLineReaderType());
		
		bind(SensorSystemAccess.class).to(BrickDaemonFacade.class);
		
		bindMonitorServices(Multibinder.newSetBinder(binder(), MonitorService.class));

		bindSensorHandlers(Multibinder.newSetBinder(binder(), SensorHandler.class));
	}

	protected Class<? extends CommandLineReader> getCommandLineReaderType() {
		return VnStatReader.class;
	}

	protected void bindSensorHandlers(Multibinder<SensorHandler> sensorHandlerBinder) {
		sensorHandlerBinder.addBinding().to(WaterLevelSensor.class);
		sensorHandlerBinder.addBinding().to(TemperatureSensor.class);
		sensorHandlerBinder.addBinding().to(VoltageCurrentSensor.class);
	}

	protected void bindMonitorServices(Multibinder<MonitorService> monitorServiceBinder ) {
		monitorServiceBinder.addBinding().to(SensorMonitorService.class);
		monitorServiceBinder.addBinding().to(NetworkTrafficMonitorService.class);
		monitorServiceBinder.addBinding().to(DataCollectionMonitor.class);
	}

	protected void bindConfiguration() {
		Properties properties = buildProperties();
		bind(Properties.class).toInstance(properties);
		bind(AppConfig.class).to(PropertyFileAppConfig.class);
	}

	private Properties buildProperties() {
		Properties properties = new Properties();
		try {
			log.info("Loading configuration from {}", filename);
			InputStream inputStream  = new FileInputStream(filename);
			properties.load(inputStream);
		} catch (IOException e) {
			log.warn("Property file {} not found in {}. Using default settings.", filename, new File("").getAbsolutePath());
		}
		return properties;
	}

}
