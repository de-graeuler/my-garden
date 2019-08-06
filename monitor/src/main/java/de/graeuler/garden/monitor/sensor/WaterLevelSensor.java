package de.graeuler.garden.monitor.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.BrickletDistanceUS;
import com.tinkerforge.BrickletDistanceUS.DistanceReachedListener;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.interfaces.DataCollector;

public class WaterLevelSensor extends AbstractSensorHandler<BrickletDistanceUS> implements DistanceReachedListener {

	private int thresholdCm = 20;
	private short movingAverage = 60;
	private int debouncePeriodMs = 10000;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Inject
	WaterLevelSensor(AppConfig config,DataCollector<DataRecord> dataCollector) {
		super(config, dataCollector);
		this.thresholdCm = (int) ConfigurationKeys.WATERLVL_CHG_THD.from(config);
		int ma = (int) ConfigurationKeys.WATERLVL_MOVING_AVG.from(config);
		this.movingAverage = (short) ma;
		this.debouncePeriodMs = (int) ConfigurationKeys.WATERLVL_DEBOUNCE.from(config);
	}

	@Override
	protected Class<BrickletDistanceUS> getBrickClass() {
		return BrickletDistanceUS.class;
	}

	@Override
	protected void initBrick() throws TimeoutException, NotConnectedException {
		BrickletDistanceUS b = getBrick();
		b.setDebouncePeriod(this.debouncePeriodMs);
		b.setMovingAverage(this.movingAverage);
		
		int distance = b.getDistanceValue();
		updateThreshold(distance);
		sendToCollector(distance);
		b.addDistanceReachedListener(this);
	}

	private void updateThreshold(int distance) {
		int lwrLimit = (int) (Math.max(0, distance - 0.5 * thresholdCm * 10));
		int uprLimit = (int) (distance + 0.5 * thresholdCm * 10);
		log.info("Setting threshold range to {} - {}", lwrLimit, uprLimit);
		try {
			getBrick().setDistanceCallbackThreshold(BrickletDistanceUS.THRESHOLD_OPTION_OUTSIDE, lwrLimit, uprLimit);
		} catch (TimeoutException | NotConnectedException e) {
			// This is very unlikely, because a connectivity issue must have occurred immediately after this 
			// method was called. For this reason a proper exception handling is quite hard to do. So: 
			super.logError(e);
		}
	}

	@Override
	public void distanceReached(int distance) {
		sendToCollector(distance);
		updateThreshold(distance);
	}

	protected void sendToCollector(int distance) {
		super.sendToCollector("water-lvl-distance", Double.valueOf(0.1 * distance));
	}

	@Override
	protected BrickletDistanceUS constructBrick(String uid, IPConnection conn) {
		return new BrickletDistanceUS(uid, conn);
	}

}
