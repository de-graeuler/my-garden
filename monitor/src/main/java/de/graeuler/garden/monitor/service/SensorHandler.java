package de.graeuler.garden.monitor.service;

import java.io.Serializable;

import com.tinkerforge.IPConnection;

import de.graeuler.garden.monitor.tinkerforge.TinkerforgeDevice;

public interface SensorHandler {

	/**
	 * If this sensor handler is able to and capable of processing data of the given device it should return true.
	 * The caller can act on the assumption that this handle will collect data using the given device. 
	 * 
	 * @param device
	 * @param conn
	 * @return
	 */
	boolean doesAccept(TinkerforgeDevice device, IPConnection conn);

	/**
	 * Classes implementing this interface should be able to send serializable data to a 
	 * data collector. They themselves can be responsible to call this method, but it is also possible to implement
	 * an external system calling this method.
	 * 
	 * @param datakey identifies this kind value by a name.   
	 * @param value some form of serializable data.
	 */
	void sendToCollector(String datakey, Serializable value);
}
