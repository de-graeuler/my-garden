package de.graeuler.garden.integration.sensor;

import java.io.Serializable;
import java.util.concurrent.ScheduledExecutorService;

import com.google.inject.Inject;
import com.tinkerforge.IPConnection;

import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.model.TinkerforgeDevice;

public class SimulatedSensor implements SensorHandler {

	@Inject DataCollector<DataRecord> dataCollector;
	@Inject ScheduledExecutorService executorService;

	private String dataIdentifier;
	private int minValue;
	private int maxValue;
	private String unit;

	public SimulatedSensor(String dataIdentifier, int min, int max, String unit) {
		this.dataIdentifier = dataIdentifier;
		this.minValue = min;
		this.maxValue = max;
		this.unit = unit;
	}

	@Override
	public boolean doesAccept(TinkerforgeDevice device, IPConnection conn) {
		return true;
	}

	@Override
	public void sendToCollector(String datakey, Serializable value) {
		dataCollector.collect(datakey, value);
	}

}
