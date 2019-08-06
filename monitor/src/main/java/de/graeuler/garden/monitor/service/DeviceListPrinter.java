package de.graeuler.garden.monitor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.graeuler.garden.monitor.model.TinkerforgeDevice;

public class DeviceListPrinter implements Runnable {

	private static final String ROOT_MASTER_UID = "0";

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	int devListCount = 0;
	boolean printedOnce = false;
	private final String indentString = "--";

	private Map<String,TinkerforgeDevice> deviceList = new HashMap<>();
	
	public DeviceListPrinter(Map<String,TinkerforgeDevice> deviceList) {
		this.deviceList = deviceList;
	}
	
	@Override
	public void run() {
		if (deviceList.isEmpty()) {
			this.printedOnce = false;
			
		} 
		else {
			if (deviceList.size() > this.devListCount) {
				this.devListCount = deviceList.size();
			}
			if ( ! printedOnce & deviceList.size() == this.devListCount) {
				printRecursive(ROOT_MASTER_UID, "");
				this.printedOnce = true;
			}
		}
	}

	private void printRecursive(String connectedToUid, String indent) {
		List<String> uids = findConnectedDevices(connectedToUid);
		for(String uid : uids) {
			TinkerforgeDevice d = deviceList.get(uid);
			log.info(String.format(" %-6s[%-6s]: %-30s at position %s", indent, d.getUid(), d.getDeviceClass().getSimpleName(), d.getPosition()));
			printRecursive(uid, indent + indentString);
		}
	}

	private List<String> findConnectedDevices(String connectedToUid) {
		return deviceList.values().stream()
				.filter(d -> connectedToUid.equals(d.getConnectedTo()))
				.sorted((d1, d2) -> Integer.compare(d1.getPosition(), d2.getPosition()))
				.map(TinkerforgeDevice::getUid)
				.collect(Collectors.toList());
	}

}

