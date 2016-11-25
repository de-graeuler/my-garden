package de.graeuler.garden.monitor.sensor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.BrickMaster;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.model.TFDevice;

public class MasterBrickTemperatureSensor extends SchedulerSensorBrick<BrickMaster> implements SensorHandler { 
// Mentioning the interface, because implementing it is required. Extending the abstract base class is not.

	@Inject
	MasterBrickTemperatureSensor(DataCollector dataCollector, ScheduledExecutorService scheduler) {
		super(dataCollector, scheduler);
	}

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	protected Class<BrickMaster> getBrickClass() {
		return BrickMaster.class;
	}

	@Override
	protected void initBrick(TFDevice device, IPConnection conn) throws TimeoutException, NotConnectedException {
		setBrick(new BrickMaster(device.getUid(), conn));

		super.schedule(new Runnable() {

			@Override
			public void run() {
				try {
					short t = getBrick().getChipTemperature();
					sendToCollector("master-chip-temperature", Short.valueOf(t));
				} catch (TimeoutException | NotConnectedException e) {
					log.error("Unable to read master chip temperature: {}", e.getMessage());
				}
			}
		}, 10, TimeUnit.SECONDS);
	}

}
