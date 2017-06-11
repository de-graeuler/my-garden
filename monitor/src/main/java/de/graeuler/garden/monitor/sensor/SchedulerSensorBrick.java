package de.graeuler.garden.monitor.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import com.tinkerforge.TinkerforgeException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.model.TFDevice;
import java.io.Serializable;

public abstract class SchedulerSensorBrick<T extends Device> implements SensorHandler{
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private ScheduledExecutorService scheduler;
	private List<ScheduledFuture<?>> futures = new ArrayList<>();
	private T brick = null;
	private DataCollector dataCollector;
		
	@Inject
	SchedulerSensorBrick(AppConfig config, DataCollector dataCollector, ScheduledExecutorService scheduler) {
		this.dataCollector = dataCollector;
		this.scheduler = scheduler;
	}

	protected abstract Class<T> getBrickClass();
	protected abstract void initBrick(TFDevice device, IPConnection conn) throws TimeoutException, NotConnectedException;


	@Override
	public boolean isAccepted(TFDevice device, IPConnection conn) {
		if (getBrick() != null) {
			return false;
		}
		if ( ! ( conn.getConnectionState() == IPConnection.CONNECTION_STATE_CONNECTED) ) {
			return false;
		}
		if ( ! device.classIsA(getBrickClass())) {
			return false;
		}
		try {
			initBrick(device, conn);
		} catch (TimeoutException | NotConnectedException e) {
			logError(e);
		}
		return true;
	}

	protected void logError(TinkerforgeException e) {
		log.error("Unable to initialize Brick with device. {} occurred: {}", e.getClass().getSimpleName(), e.getMessage());
	}
	
	@Override
	public void halt() {
		for(ScheduledFuture<?> future : futures) {
			future.cancel(true);
		}
	}
	
	protected void schedule(Runnable runnable, long period, TimeUnit unit) {
		this.futures.add(this.scheduler.scheduleAtFixedRate(runnable, 0, period, unit));
	}
	
	protected void sendToCollector(Map<String, Serializable> data) {
		dataCollector.collect(data);
	}

	protected void sendToCollector(String string, Serializable valueOf) {
		dataCollector.collect(string, valueOf);
	}

	public T getBrick() {
		return brick;
	}

	public void setBrick(T brick) {
		this.brick = brick;
	}

}
