package de.graeuler.garden;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.graeuler.garden.config.AppConfigKeyTest;
import de.graeuler.garden.data.DerbyDataPersisterTest;
import de.graeuler.garden.data.JsonDataConverterTest;
import de.graeuler.garden.data.model.DataRecordTest;
import de.graeuler.garden.monitor.config.IntLimitValidatorTest;
import de.graeuler.garden.monitor.model.ConnectReasonTest;
import de.graeuler.garden.monitor.model.ConnectionStateTest;
import de.graeuler.garden.monitor.model.DisconnectReasonTest;
import de.graeuler.garden.monitor.model.TFDeviceTest;
import de.graeuler.garden.monitor.sensor.SchedulerSensorBrickTest;
import de.graeuler.garden.monitor.sensor.TemperatureSensorTest;
import de.graeuler.garden.monitor.sensor.VoltageCurrentSensorTest;
import de.graeuler.garden.monitor.sensor.WaterLevelSensorTest;
import de.graeuler.garden.monitor.util.BytesTest;
import de.graeuler.garden.monitor.util.ObjectSerializationUtilTest;
import de.graeuler.garden.uplink.DataUploaderTest;
import de.graeuler.garden.uplink.HttpUplinkServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
		AppConfigKeyTest.class, IntLimitValidatorTest.class, HttpUplinkServiceTest.class, JsonDataConverterTest.class, 
		ConnectionStateTest.class, ConnectReasonTest.class, DisconnectReasonTest.class,
		TFDeviceTest.class, SchedulerSensorBrickTest.class, 
		VoltageCurrentSensorTest.class, TemperatureSensorTest.class, WaterLevelSensorTest.class, 
		DataRecordTest.class, BytesTest.class, ObjectSerializationUtilTest.class, 
		DerbyDataPersisterTest.class, DataUploaderTest.class
	})
public class AllTests {
	// JUnit Test suite
}
