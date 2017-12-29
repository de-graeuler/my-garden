package de.graeuler.garden;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.graeuler.garden.config.AppConfigKeyTest;
import de.graeuler.garden.data.DataRecordTest;
import de.graeuler.garden.data.DerbyDataPersisterTest;
import de.graeuler.garden.data.JsonDataConverterTest;
import de.graeuler.garden.monitor.config.IntLimitValidatorTest;
import de.graeuler.garden.monitor.model.ConnectReasonTest;
import de.graeuler.garden.monitor.model.ConnectionStateTest;
import de.graeuler.garden.monitor.model.DisconnectReasonTest;
import de.graeuler.garden.monitor.model.TFDeviceTest;
import de.graeuler.garden.monitor.sensor.SchedulerSensorBrickTest;
import de.graeuler.garden.monitor.sensor.TemperatureSensorTest;
import de.graeuler.garden.monitor.sensor.VoltageCurrentSensorTest;
import de.graeuler.garden.monitor.sensor.WaterLevelSensorTest;
import de.graeuler.garden.monitor.service.BrickDaemonFacadeTest;
import de.graeuler.garden.monitor.service.SensorMonitorServiceTest;
import de.graeuler.garden.monitor.util.BytesTest;
import de.graeuler.garden.monitor.util.ObjectSerializationUtilTest;
import de.graeuler.garden.monitor.util.VnStatPosTest;
import de.graeuler.garden.uplink.DataCollectionMonitorTest;
import de.graeuler.garden.uplink.HttpUplinkServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
		AppConfigKeyTest.class, 
		BrickDaemonFacadeTest.class,
		BytesTest.class, 
		ConnectionStateTest.class, 
		ConnectReasonTest.class, 
		DataCollectionMonitorTest.class,
		DataRecordTest.class, 
		DerbyDataPersisterTest.class, 
		DisconnectReasonTest.class,
		HttpUplinkServiceTest.class, 
		IntLimitValidatorTest.class, 
		JsonDataConverterTest.class, 
		ObjectSerializationUtilTest.class, 
		SchedulerSensorBrickTest.class, 
		SensorMonitorServiceTest.class, 
		TemperatureSensorTest.class, 
		TFDeviceTest.class, 
		VnStatPosTest.class,
		VoltageCurrentSensorTest.class, 
		WaterLevelSensorTest.class, 
	})
public class AllTests {
	// JUnit Test suite
}
