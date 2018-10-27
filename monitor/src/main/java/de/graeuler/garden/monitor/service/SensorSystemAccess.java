package de.graeuler.garden.monitor.service;

public interface SensorSystemAccess {

	void setNewDeviceCallback(NewDeviceCallback deviceCallback);

	void connect();

	void disconnect();

}
