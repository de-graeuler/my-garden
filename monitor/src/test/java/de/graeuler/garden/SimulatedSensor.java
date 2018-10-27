package de.graeuler.garden;

import java.io.Serializable;

import com.google.inject.Inject;
import com.tinkerforge.IPConnection;

import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.model.TinkerforgeDevice;

public class SimulatedSensor implements SensorHandler {

	private DataCollector dataCollector;

	@Inject
	SimulatedSensor(DataCollector dataCollector) {
		this.dataCollector = dataCollector;
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
