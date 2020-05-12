package de.graeuler.garden.monitor.service;

import com.tinkerforge.IPConnection;

import de.graeuler.garden.monitor.tinkerforge.TinkerforgeDevice;

public interface NewDeviceCallback {

	void onDeviceFound(TinkerforgeDevice device, IPConnection connection);

	void reset();

}
