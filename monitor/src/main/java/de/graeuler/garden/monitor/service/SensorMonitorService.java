package de.graeuler.garden.monitor.service;

import java.io.IOException;
import java.net.UnknownHostException;
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
import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.IPConnection;
import com.tinkerforge.IPConnection.ConnectedListener;
import com.tinkerforge.IPConnection.DisconnectedListener;
import com.tinkerforge.IPConnection.EnumerateListener;
import com.tinkerforge.NotConnectedException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.MonitorService;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.model.ConnectReason;
import de.graeuler.garden.monitor.model.ConnectionState;
import de.graeuler.garden.monitor.model.DisconnectReason;
import de.graeuler.garden.monitor.model.TFDevice;

/**
 * @author bernhard.graeuler
 *
 */
public class SensorMonitorService implements MonitorService, EnumerateListener, ConnectedListener, DisconnectedListener{
	
	private static final long DEVICE_LIST_PRINT_DELAY = 1;
	private Set<SensorHandler> sensorHandlers;
	
	private String brickdaemonHost;
    private int brickdaemonPort;
    private long CONNECT_RETRY_WAIT_TIME = 5;
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Inject
    SensorMonitorService(AppConfig config, Set<SensorHandler> sensorHandlers, ScheduledExecutorService scheduler) {
    	this.scheduler = scheduler;
    	this.sensorHandlers = sensorHandlers;
    	this.brickdaemonHost = (String) AppConfig.Key.TF_DAEMON_HOST.from(config);
    	this.brickdaemonPort = (Integer) AppConfig.Key.TF_DAEMON_PORT.from(config);
    }
    
	private IPConnection connection = new IPConnection();
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> connStatePrinterHandle;
	private ScheduledFuture<?> deviceListCollectorHandle;	
	
	private Map<String,TFDevice> deviceList = new HashMap<>();

	@Override
	public void monitor() {
		try {
			connection.addConnectedListener(this);
			connection.addEnumerateListener(this);
			connection.addDisconnectedListener(this);
			scheduleConnectionStatePrinter();
			while(true) {
				try {
					connection.connect(brickdaemonHost, brickdaemonPort);
					break;
				} catch (AlreadyConnectedException e) {
					log.warn("Already connected: {}", e.getMessage());
				} catch (UnknownHostException e) {
					log.warn("Unknown host: {}", e.getMessage());
				} catch (IOException e) {
					log.info("Unable to connect. Retrying in 10s...");
					TimeUnit.SECONDS.sleep(CONNECT_RETRY_WAIT_TIME);
				}
			}
			System.in.read();
			connection.close();
		} catch (IOException | InterruptedException e) {
			log.error(e.getMessage());
			cancelConnStatePrinter();
			connection.removeDisconnectedListener(this);
			connection.removeEnumerateListener(this);
			connection.removeConnectedListener(this);
		}
		this.scheduler.shutdown();
		
	}


	
	private Runnable connStatePrinter = new Runnable() {
		ConnectionState lastState = null;
		@Override
		public void run() {
			short currentState = connection.getConnectionState();
			if ( ! ConnectionState.by(currentState).equals(this.lastState)) {
				this.lastState = ConnectionState.by(currentState);
				log.info("Connection state: {}", lastState.getOutput());
			}
		}
	};
	
	private void scheduleConnectionStatePrinter() {
		// poll connection state every n time units.
		this.connStatePrinterHandle = this.scheduler.scheduleWithFixedDelay(connStatePrinter, 1, 500, TimeUnit.MILLISECONDS);
	}

	private void cancelConnStatePrinter() {
		this.connStatePrinterHandle.cancel(false);
	}

	
	
	private Runnable deviceListCollector = new Runnable() {

		String indentString = "--";
		int devListCount = 0;
		boolean printed = false;
		
		private void printRecursive(String connectedToUid, String indent) {
			List<String> uids = deviceList.values().stream().filter(d -> connectedToUid.equals(d.getConnectedTo())).map(TFDevice::getUid).collect(Collectors.toList());
			for(String uid : uids) {
				TFDevice d = deviceList.get(uid);
				log.info(" {}{}: {} at position {}", indent, d.getUid(), d.getDeviceClass().getSimpleName(), d.getPosition());
				printRecursive(uid, indent + indentString);
			}
		}
		
		@Override
		public void run() {
			try {
				if (0 == deviceList.size()) {
					this.printed = false;
					connection.enumerate();
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
			} catch (NotConnectedException e) {
				log.error("Device enumeration triggered while disconnected.");
			}
		}
		
	};

	private void scheduleDeviceListCollector() {
		this.deviceList.clear();
		this.deviceListCollectorHandle = scheduler.scheduleWithFixedDelay(deviceListCollector, 0, DEVICE_LIST_PRINT_DELAY, TimeUnit.SECONDS);
	}

	private void cancelDeviceListCollector() {
		this.deviceListCollectorHandle.cancel(false);
	}
	
	@Override
	public void enumerate(String uid, String connectedUid, char position, short[] hwv,
			short[] fwv, int deviceIdentifier, short enumerationType) {
		if (enumerationType == IPConnection.ENUMERATION_TYPE_DISCONNECTED) {
			log.error("Device with uid {} was disconnected.", uid);
			this.deviceList.clear();
			return;
		}
		TFDevice device = TFDevice.create(uid, connectedUid, position, hwv, fwv, deviceIdentifier, enumerationType);
		if (null != device) {
			deviceList.put(uid, device);
			for(SensorHandler h : this.sensorHandlers) {
				if (h.isAccepted(device, connection)) { 
					break;
				}
			}
		}
	}

	@Override
	public void connected(short reason) {
		log.info("Connection caused by: {}", ConnectReason.by(reason).getOutput());
		scheduleDeviceListCollector();
	}

	@Override
	public void disconnected(short reason) {
		log.info("Disconnect caused by: {}", DisconnectReason.by(reason).getOutput());
		cancelDeviceListCollector();
	}
	
	

}
