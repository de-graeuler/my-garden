package de.graeuler.garden.monitor.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.BrickletTemperature.TemperatureReachedListener;
import com.tinkerforge.IPConnection;
import com.tinkerforge.TinkerforgeException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.data.DataCollector;
import de.graeuler.garden.data.DataRecord;

public class TemperatureSensor extends AbstractSensorHandler<BrickletTemperature> implements TemperatureReachedListener {

	private int thresholdDegC = 1;
	private int debouncePeriodMs = 1000;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	TemperatureSensor(AppConfig config, DataCollector<DataRecord> dataCollector) {
		super(config, dataCollector);
		this.thresholdDegC    = (int) ConfigurationKeys.TEMP_CHG_THD.from(config);
		this.debouncePeriodMs = (int) ConfigurationKeys.TEMP_DEBOUNCE.from(config);
	}

	@Override
	protected Class<BrickletTemperature> getBrickClass() {
		return BrickletTemperature.class;
	}
	
	@Override
	protected void initBrick() throws TinkerforgeException  {
		BrickletTemperature b = getBrick();
		short temperature = b.getTemperature();
		b.setDebouncePeriod(this.debouncePeriodMs);
		updateTemperatureThreshold(temperature);
		sendToCollector(temperature);
		b.addTemperatureReachedListener(this);
	}

	protected void sendToCollector(short temperature) {
		super.sendToCollector("outside-temperature", Double.valueOf(0.01 * temperature));
	}

	protected void updateTemperatureThreshold(short temperature) {
		short lwrLimit = (short) (temperature - 0.5 * thresholdDegC * 100);
		short uprLimit = (short) (temperature + 0.5 * thresholdDegC * 100);
		log.info("Setting temperature threshold range to {} - {}", lwrLimit, uprLimit);
		try {
			getBrick().setTemperatureCallbackThreshold(BrickletTemperature.THRESHOLD_OPTION_OUTSIDE, lwrLimit, uprLimit);
		} catch (TinkerforgeException e) {
			// This very unlikely, because a connectivity issue must have occurred immediately after this 
			// method was called. For this reason a proper exception handling is quite hard to do. So: 
			log.error("Unable to set new temperature threshold.", e);
		}
	}

	@Override
	public void temperatureReached(short temperature) {
		sendToCollector(temperature);
		updateTemperatureThreshold(temperature);
	}

	@Override
	protected BrickletTemperature constructBrick(String uid, IPConnection conn) {
		return new BrickletTemperature(uid, conn);
	}

}
