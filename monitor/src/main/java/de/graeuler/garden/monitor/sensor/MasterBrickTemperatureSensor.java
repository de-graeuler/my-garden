package de.graeuler.garden.monitor.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.BrickMaster;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.SensorHandler;

public class MasterBrickTemperatureSensor extends AbstractSensorHandler<BrickMaster> implements SensorHandler { 
// Mentioning the interface, because implementing it is required. Extending the abstract base class is not.

	private ScheduledExecutorService scheduler;
	private List<ScheduledFuture<?>> futures = new ArrayList<>();

	@Inject
	MasterBrickTemperatureSensor(AppConfig config, DataCollector dataCollector, ScheduledExecutorService scheduler) {
		super(config, dataCollector);
	}

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	protected Class<BrickMaster> getBrickClass() {
		return BrickMaster.class;
	}

	@Override
	protected void initBrick() throws TimeoutException, NotConnectedException {
		
		this.schedule(new Runnable() {

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

	@Override
	protected BrickMaster constructBrick(String uid, IPConnection conn) {
		return new BrickMaster(uid, conn);
	}

	protected void schedule(Runnable runnable, long period, TimeUnit unit) {
		this.futures.add(this.scheduler.scheduleAtFixedRate(runnable, 0, period, unit));
	}

	public void halt() {
		for(ScheduledFuture<?> future : futures) {
			future.cancel(true);
		}
	}
	
	
}
