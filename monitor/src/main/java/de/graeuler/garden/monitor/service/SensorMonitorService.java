package de.graeuler.garden.monitor.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.IPConnection;

import de.graeuler.garden.monitor.tinkerforge.TinkerforgeDevice;

/**
 * @author bernhard.graeuler
 *
 */
public class SensorMonitorService implements MonitorService, NewDeviceCallback {

	private final long DEVICE_LIST_PRINT_DELAY = 1;
	
	private Set<SensorHandler> sensorHandlers;
	
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
	private Map<String,TinkerforgeDevice> deviceList = new HashMap<>();
	private SensorSystemAccess brickDaemonManager;
	
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> deviceListCollectorHandle;

	private DeviceListPrinter deviceListPrinter;


	@Inject
    SensorMonitorService(SensorSystemAccess brickDaemonManager, Set<SensorHandler> sensorHandlers, ScheduledExecutorService scheduler) {
    	this.sensorHandlers = sensorHandlers;
    	this.brickDaemonManager = brickDaemonManager;
    	this.brickDaemonManager.setNewDeviceCallback(this);
    	this.scheduler = scheduler;
    	this.deviceListPrinter = new DeviceListPrinter(this.deviceList);
    }
    
	@Override
	public void monitor() {
		this.deviceListCollectorHandle = scheduler.scheduleWithFixedDelay(deviceListPrinter, 0, DEVICE_LIST_PRINT_DELAY, TimeUnit.SECONDS);
		brickDaemonManager.connect();
	}
	
	@Override
	public void shutdown() {
		brickDaemonManager.disconnect();
		this.deviceListCollectorHandle.cancel(false);
	}

	@Override
	public void onDeviceFound(TinkerforgeDevice device, IPConnection connection) {
		if (null != device) {
			deviceList.put(device.getUid(), device);
			boolean accepted = false;
			for(SensorHandler sensorHandler : this.sensorHandlers) {
				if (accepted = sensorHandler.doesAccept(device, connection)) { 
					break;
				}
			}
			if ( ! accepted ) {
				log.info("no handler found for device {}", device.getUid());
			}
		}
	}

	@Override
	public void reset() {
		this.deviceList.clear();
		
	}
	
}
