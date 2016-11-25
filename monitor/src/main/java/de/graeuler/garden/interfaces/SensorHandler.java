package de.graeuler.garden.interfaces;

import com.tinkerforge.IPConnection;

import de.graeuler.garden.monitor.model.TFDevice;

public interface SensorHandler {

	boolean isAccepted(TFDevice device, IPConnection conn);

	void halt();
	
}
