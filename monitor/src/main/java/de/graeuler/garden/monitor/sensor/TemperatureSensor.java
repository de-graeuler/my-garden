package de.graeuler.garden.monitor.sensor;

import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.BrickletTemperature.TemperatureCallbackThreshold;
import com.tinkerforge.BrickletTemperature.TemperatureReachedListener;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.monitor.model.TFDevice;

public class TemperatureSensor extends SchedulerSensorBrick<BrickletTemperature> implements TemperatureReachedListener {

	private int thresholdDegC = 1;
	private int debouncePeriodMs = 1000;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	TemperatureSensor(AppConfig config, DataCollector dataCollector, ScheduledExecutorService scheduler) {
		super(config, dataCollector, scheduler);
		this.thresholdDegC    = (int) config.get(AppConfig.Key.TEMP_CHG_THD);
		this.debouncePeriodMs = (int) config.get(AppConfig.Key.TEMP_DEBOUNCE);
	}

	@Override
	protected Class<BrickletTemperature> getBrickClass() {
		return BrickletTemperature.class;
	}
	
	@Override
	protected void initBrick(TFDevice device, IPConnection conn) throws TimeoutException, NotConnectedException {
		BrickletTemperature b = new BrickletTemperature(device.getUid(), conn);
		setBrick(b);
		b.addTemperatureReachedListener(this);
		b.setDebouncePeriod(this.debouncePeriodMs);
		short temperature = b.getTemperature();
		updateTemperatureThreshold(temperature);
		sendToCollector(temperature);
/* This block is printing the current temperature every 10 seconds.  enable it for debugging. * /
		schedule(new Runnable() {
			@Override
			public void run() {
				try {
					log.info("Current temperature: {}", getBrick().getTemperature());
				} catch (TimeoutException | NotConnectedException e) {
					log.error("Unable to read temperature.");
				}
			}
		}, 10, TimeUnit.SECONDS);
/* */
	}

	protected void sendToCollector(short temperature) {
		super.sendToCollector("outside-temperature", Double.valueOf(0.01 * temperature));
	}

	protected void updateTemperatureThreshold(short temperature) throws TimeoutException, NotConnectedException {
		TemperatureCallbackThreshold threshold = getBrick().getTemperatureCallbackThreshold();
		if (temperature < threshold.min || temperature > threshold.max) {
			short lwrLimit = (short) (temperature - 0.5 * thresholdDegC * 100);
			short uprLimit = (short) (temperature + 0.5 * thresholdDegC * 100);
			log.info("Temperature {} left threshold range of {} - {}. Setting threshold range to {} - {}",
					temperature, threshold.min, threshold.max, lwrLimit, uprLimit);
			getBrick().setTemperatureCallbackThreshold('o', lwrLimit, uprLimit);
		} else {
			log.info("Temperature {} in threshold range of {} - {}", temperature, threshold.min, threshold.max);
		}
	}

	@Override
	public void temperatureReached(short temperature) {
		try {
			updateTemperatureThreshold(temperature);
		} catch (TimeoutException | NotConnectedException e) {
			// This very unlikely, because a connectivity issue must have occurred immediately after this 
			// method was called. For this reason a proper exception handling is quite hard to do. So: 
			super.logError(e);
		}
		sendToCollector(temperature);
	}

}
