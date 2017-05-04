package de.graeuler.garden.monitor.sensor;

import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.BrickletDistanceUS;
import com.tinkerforge.BrickletDistanceUS.DistanceCallbackThreshold;
import com.tinkerforge.BrickletDistanceUS.DistanceReachedListener;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.model.TFDevice;

public class WaterLevelSensor extends SchedulerSensorBrick<BrickletDistanceUS> implements SensorHandler, DistanceReachedListener {

	private int thresholdCm = 20;
	private short movingAverage = 60;
	private int debouncePeriodMs = 10000;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Inject
	WaterLevelSensor(AppConfig config,DataCollector dataCollector, ScheduledExecutorService scheduler) {
		super(config, dataCollector, scheduler);
		this.thresholdCm = (int) AppConfig.Key.WATERLVL_CHG_THD.from(config);
		int ma = (int) AppConfig.Key.WATERLVL_MOVING_AVG.from(config);
		if (ma >= 0 || ma <= 100) {
			this.movingAverage = (short) ma;
		}
		this.debouncePeriodMs = (int) AppConfig.Key.WATERLVL_DEBOUNCE.from(config);
	}

	@Override
	protected Class<BrickletDistanceUS> getBrickClass() {
		return BrickletDistanceUS.class;
	}

	@Override
	protected void initBrick(TFDevice device, IPConnection conn) throws TimeoutException, NotConnectedException {
		BrickletDistanceUS b = new BrickletDistanceUS(device.getUid(), conn);
		setBrick(b);
		b.addDistanceReachedListener(this);
		b.setDebouncePeriod(this.debouncePeriodMs);
		b.setMovingAverage(this.movingAverage);
		int distance = b.getDistanceValue();
		updateThreshold(distance);
		sendToCollector(distance);
/* This block is printing the current temperature every 10 seconds.  enable it for debugging. * /
		schedule(new Runnable() {
			@Override
			public void run()  {
				try {
					log.info("Current distance: {}", getBrick().getDistanceValue());
				} catch (TimeoutException | NotConnectedException e) {
					log.error("Unable to read distance.");
				}
			}
		}, 10, TimeUnit.SECONDS);
/* */
	}

	private void updateThreshold(int distance) throws TimeoutException, NotConnectedException {
		DistanceCallbackThreshold threshold = getBrick().getDistanceCallbackThreshold();
		if (distance < threshold.min || distance > threshold.max) {
			int lwrLimit = (int) (Math.max(0, distance - 0.5 * thresholdCm * 10));
			int uprLimit = (int) (distance + 0.5 * thresholdCm * 10);
			log.info("Distance {} left threshold range of {} - {}. Setting threshold range to {} - {}",
					distance, threshold.min, threshold.max, lwrLimit, uprLimit);
			getBrick().setDistanceCallbackThreshold('o', lwrLimit, uprLimit);
		} else {
			log.info("Distance {} in threshold range of {} - {}", distance, threshold.min, threshold.max);
		}
	}

	@Override
	public void distanceReached(int distance) {
		try {
			updateThreshold(distance);
		} catch (TimeoutException | NotConnectedException e) {
			// This is very unlikely, because a connectivity issue must have occurred immediately after this 
			// method was called. For this reason a proper exception handling is quite hard to do. So: 
			super.logError(e);
		}
		sendToCollector(distance);
	}

	protected void sendToCollector(int distance) {
		super.sendToCollector("water-lvl-distance", Double.valueOf(0.1 * distance));
	}

}
