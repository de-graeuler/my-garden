package de.graeuler.garden.monitor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.IPConnection;

import de.graeuler.garden.interfaces.MonitorService;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.model.TFDevice;

/**
 * @author bernhard.graeuler
 *
 */
public class SensorMonitorService implements MonitorService, DeviceListCallback {
	
	private Set<SensorHandler> sensorHandlers;
	
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
	private final long DEVICE_LIST_PRINT_DELAY = 1;

	private Map<String,TFDevice> deviceList = new HashMap<>();
	private BrickDaemonManager brickDaemonManager;
	private ScheduledFuture<?> deviceListCollectorHandle;

	private ScheduledExecutorService scheduler;

	@Inject
    SensorMonitorService(BrickDaemonManager brickDaemonManager, Set<SensorHandler> sensorHandlers, ScheduledExecutorService scheduler) {
    	this.sensorHandlers = sensorHandlers;
    	this.brickDaemonManager = brickDaemonManager;
    	this.brickDaemonManager.setDeviceListCallback(this);
    	this.scheduler = scheduler;
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
	public void onDeviceFound(TFDevice device, IPConnection connection) {
		if (null != device) {
			deviceList.put(device.getUid(), device);
			boolean accepted = false;
			for(SensorHandler h : this.sensorHandlers) {
				if (accepted = h.isAccepted(device, connection)) { 
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
	
	private final String indentString = "--";
	private void printRecursive(String connectedToUid, String indent) {
		List<String> uids = deviceList.values().stream().filter(d -> connectedToUid.equals(d.getConnectedTo())).map(TFDevice::getUid).collect(Collectors.toList());
		for(String uid : uids) {
			TFDevice d = deviceList.get(uid);
			log.info(String.format(" %-6s[%-6s]: %-30s at position %s", indent, d.getUid(), d.getDeviceClass().getSimpleName(), d.getPosition()));
			printRecursive(uid, indent + indentString);
		}
	}

	private Runnable deviceListPrinter = new Runnable() {

		int devListCount = 0;
		boolean printed = false;
		
		@Override
		public void run() {
			if (0 == deviceList.size()) {
				this.printed = false;
				
			} 
			else {
				if (deviceList.size() > this.devListCount) {
					this.devListCount = deviceList.size();
				}
				if ( ! printed & deviceList.size() == this.devListCount) {
					printRecursive("0", "");
					this.printed = true;
				}
			}
		}
		
	};

}
