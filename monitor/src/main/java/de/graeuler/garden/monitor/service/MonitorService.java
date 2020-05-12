package de.graeuler.garden.monitor.service;

public interface MonitorService {

	/**
	 * This method is called to start the monitoring.
	 */
	public void monitor();
	
	/**
	 * Calling this method signalizes that the shutdown request to the service.
	 */
	public void shutdown();

}
