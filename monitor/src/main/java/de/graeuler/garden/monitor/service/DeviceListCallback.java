package de.graeuler.garden.monitor.service;

import com.tinkerforge.IPConnection;

import de.graeuler.garden.monitor.model.TFDevice;

public interface DeviceListCallback {

	void onDeviceFound(TFDevice device, IPConnection connection);

	void reset();

}
