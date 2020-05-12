package de.graeuler.garden.monitor.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.tinkerforge.BrickletVoltageCurrent;
import com.tinkerforge.BrickletVoltageCurrent.CurrentReachedListener;
import com.tinkerforge.BrickletVoltageCurrent.VoltageReachedListener;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.data.DataCollector;
import de.graeuler.garden.data.DataRecord;

public class VoltageCurrentSensor extends AbstractSensorHandler<BrickletVoltageCurrent>
		implements CurrentReachedListener, VoltageReachedListener {

	private static final String CURRENT = "current";
	private static final String VOLTAGE = "voltage";

	private int voltageThreshold = 1000; // mV
	private int currentThreshold = 10; // mA

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	VoltageCurrentSensor(AppConfig config, DataCollector<DataRecord> dataCollector) {
		super(config, dataCollector);
		this.currentThreshold = (int) ConfigurationKeys.CURRENT_CHG_THD.from(config);
		this.voltageThreshold = (int) ConfigurationKeys.VOLTAGE_CHG_THD.from(config);
	}

	@Override
	protected Class<BrickletVoltageCurrent> getBrickClass() {
		return BrickletVoltageCurrent.class;
	}

	@Override
	protected void initBrick() throws TimeoutException, NotConnectedException {
		BrickletVoltageCurrent b = getBrick();
		int current = b.getCurrent();
		int voltage = b.getVoltage();

		updateCurrentThreshold(current);
		updateVoltageThreshold(voltage);

		sendCurrentToCollector(current);
		sendVoltageToCollector(voltage);

		b.addCurrentReachedListener(this);
		b.addVoltageReachedListener(this);
	}

	@Override
	protected BrickletVoltageCurrent constructBrick(String uid, IPConnection conn) {
		return new BrickletVoltageCurrent(uid, conn);
	}

	@Override
	public void currentReached(int current) {
		sendCurrentToCollector(current);
		updateCurrentThreshold(current);
	}

	@Override
	public void voltageReached(int voltage) {
		sendVoltageToCollector(voltage);
		updateVoltageThreshold(voltage);
	}

	private void sendCurrentToCollector(int current) {
		sendToCollector(CURRENT, Double.valueOf(current / 1000.0));
	}

	private void sendVoltageToCollector(int voltage) {
		sendToCollector(VOLTAGE, Double.valueOf(voltage / 1000.0));
	}

	protected void updateCurrentThreshold(int current) {
		int lwrLimit = current - (currentThreshold / 2);
		int uprLimit = current + (currentThreshold / 2);
		log.info("{} mA: Setting current threshold range to {} - {}", current, lwrLimit, uprLimit);
		try {
			getBrick().setCurrentCallbackThreshold(BrickletVoltageCurrent.THRESHOLD_OPTION_OUTSIDE, lwrLimit, uprLimit);
		} catch (TimeoutException | NotConnectedException e) {
			super.logError(e);
		}
	}

	protected void updateVoltageThreshold(int voltage) {
		BrickletVoltageCurrent b = getBrick();
		try {
			int lwrLimit = voltage - (voltageThreshold / 2);
			int uprLimit = voltage + (voltageThreshold / 2);
			b.setVoltageCallbackThreshold(BrickletVoltageCurrent.THRESHOLD_OPTION_OUTSIDE, lwrLimit, uprLimit);
			log.info("{} mV: Setting voltage threshold range to {} - {}", voltage, lwrLimit, uprLimit);
		} catch (TimeoutException | NotConnectedException e) {
			logError(e);
		}
	}

}
