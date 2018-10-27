package de.graeuler.garden.monitor.service;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.monitor.model.ConnectReason;
import de.graeuler.garden.monitor.model.ConnectionState;
import de.graeuler.garden.monitor.model.DisconnectReason;
import de.graeuler.garden.monitor.model.TinkerforgeDevice;

public class BrickDaemonFacade implements SensorSystemAccess, TinkerforgeSystemListeners
{

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private final long CONNECT_RETRY_WAIT_TIME = 5;
    
	private String brickdaemonHost;
    private int brickdaemonPort;

	private NewDeviceCallback newDeviceCallback = null;

    private IPConnection connection;
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> connStatePrinterHandle;
	private ScheduledFuture<?> brickDaemonConnectionManagerHandle;


	@Inject
	BrickDaemonFacade(AppConfig config, ScheduledExecutorService scheduler, IPConnection connection) {
		this.connection = connection;
    	this.scheduler = scheduler;
    	this.brickdaemonHost = (String) ConfigurationKeys.TF_DAEMON_HOST.from(config);
    	this.brickdaemonPort = (Integer) ConfigurationKeys.TF_DAEMON_PORT.from(config);
	}
	
	@Override
	public void setNewDeviceCallback(NewDeviceCallback callback) {
		this.newDeviceCallback = callback;
	}
	
	@Override
	public void connect() {
		connection.addConnectedListener(this);
		connection.addEnumerateListener(this);
		connection.addDisconnectedListener(this);
		scheduleConnectionStatePrinter();
		runBrickDaemonConnectionHandler();
    }

	@Override
	public void disconnect() {
		cancelConnStatePrinter();
		cancelBrickDaemonConnectionManager();
		try {
			connection.close();
		}
		catch (IOException e) {
			log.error(e.getMessage());
		} 
		connection.removeDisconnectedListener(this);
		connection.removeEnumerateListener(this);
		connection.removeConnectedListener(this);
	}

	@Override
	public void connected(short reason) {
		log.info("Connected ({})", ConnectReason.by(reason).getOutput());
		try {
			connection.enumerate();
		} catch (NotConnectedException e) {
			log.error("Device enumeration triggered while disconnected.");
		}
	}

	@Override
	public void disconnected(short reason) {
		log.info("Disconnected ({})", DisconnectReason.by(reason).getOutput());
		newDeviceCallback.reset();
	}

	/**
	 * This is triggered by the connection.enumerate() call. The method is called for each device responding to the enumerate trigger.
	 * 
	 * @see com.tinkerforge.IPConnection.EnumerateListener#enumerate(java.lang.String, java.lang.String, char, short[], short[], int, short)
	 */
	@Override
	public void enumerate(String uid, String connectedUid, char position, short[] hwv,
			short[] fwv, int deviceIdentifier, short enumerationType) {
		if (enumerationType == IPConnection.ENUMERATION_TYPE_DISCONNECTED) {
			log.error("Device with uid {} was disconnected.", uid);
			return;
		}
		TinkerforgeDevice device = TinkerforgeDevice.create(uid, connectedUid, position, hwv, fwv, deviceIdentifier, enumerationType);
		callNewDeviceCallback(device);
	}

	private void callNewDeviceCallback(TinkerforgeDevice device) {
		if ( null != newDeviceCallback) {
			newDeviceCallback.onDeviceFound(device, connection);
		}
	}

	private void scheduleConnectionStatePrinter() {
		this.connStatePrinterHandle = this.scheduler.scheduleWithFixedDelay(connStatePrinter, 1, 500, TimeUnit.MILLISECONDS);
	}

	private void cancelConnStatePrinter() {
		this.connStatePrinterHandle.cancel(false);
	}
	
	private void runBrickDaemonConnectionHandler() {
		this.brickDaemonConnectionManagerHandle = this.scheduler.schedule(brickDaemonConnectionManager, 0, TimeUnit.SECONDS);
	}

	private void cancelBrickDaemonConnectionManager() {
		this.brickDaemonConnectionManagerHandle.cancel(true);
	}

	private Runnable brickDaemonConnectionManager = () -> {
		while(true) {
			try {
				connection.connect(brickdaemonHost, brickdaemonPort);
				break;
			} catch (AlreadyConnectedException e) {
				log.warn("Already connected: {}", e.getMessage());
			} catch (IOException e) {
				log.warn("Brick daemon not available: {}. Retrying in 10s...", e.getMessage());
				try {
					TimeUnit.SECONDS.sleep(CONNECT_RETRY_WAIT_TIME);
				} catch (InterruptedException ie) {
					log.error("Interrupted during wait for reconnect period.");
				}
			}
		}
	};
	
	private Runnable connStatePrinter = new Runnable() {
		ConnectionState lastState = null;
		@Override
		public void run() {
			short currentState = connection.getConnectionState();
			if ( ! ConnectionState.by(currentState).equals(this.lastState)) {
				this.lastState = ConnectionState.by(currentState);
				log.debug("Connection state: {}", lastState.getOutput());
			}
		}
	};


}
