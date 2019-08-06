package de.graeuler.garden.monitor.sensor;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import com.tinkerforge.TinkerforgeException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.model.TinkerforgeDevice;

/**
 * @author bernhard
 *
 * @param <T>
 */
public abstract class AbstractSensorHandler<T extends Device> implements SensorHandler {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private T brick = null;
	private DataCollector<DataRecord> dataCollector;
		
	@Inject
	AbstractSensorHandler(AppConfig config, DataCollector<DataRecord> dataCollector) {
		this.dataCollector = dataCollector;
	}

	/**
	 * This is used by isAccepted to get the Tinkerforge device class this sensor is responsible for. 
	 * @return class of the Tinkerforge device this Sensor accepts.
	 */
	protected abstract Class<T> getBrickClass();
	
	/**
	 * This is called by isAccepted, if the device is accepted by this Sensor. It should be used to 
	 * setup the brick <T>, for example by setting thresholds and/or adding value change listeners.
	 * To get the Device instance accepted by isAccepted(...) call getBrick().    
	 *    
	 * @param device
	 * @param conn
	 * @throws TimeoutException
	 * @throws NotConnectedException
	 */
	protected abstract void initBrick() throws TimeoutException, NotConnectedException;

	/**
	 * Returns an instance of the generic type T. 
	 * @param uid unique id of the device
	 * @param conn connection to the brick daemon
	 * @return new "T"(uid, conn)
	 */
	protected abstract T constructBrick(String uid, IPConnection conn);

	@Override
	public boolean doesAccept(TinkerforgeDevice device, IPConnection conn) {
		if( device == null ) {
			return false;
		}
		try {
			if (getBrick() != null) {
				return device.getUid().equals(getBrick().getIdentity().uid);
			}
			if ( ! ( conn.getConnectionState() == IPConnection.CONNECTION_STATE_CONNECTED) ) {
				return false;
			}
			if ( ! device.classIsA(getBrickClass())) {
				return false;
			}
			this.brick = constructBrick(device.getUid(), conn);
			initBrick();
		} catch (TimeoutException | NotConnectedException e) {
			logError(e);
		}
		return true;
	}

	protected void logError(TinkerforgeException e) {
		log.error("Unable to initialize Brick with device. {} occurred: {}", e.getClass().getSimpleName(), e.getMessage());
	}
	
	@Override
	public void sendToCollector(String string, Serializable valueOf) {
		dataCollector.collect(string, valueOf);
	}

	public T getBrick() {
		return brick;
	}

}
