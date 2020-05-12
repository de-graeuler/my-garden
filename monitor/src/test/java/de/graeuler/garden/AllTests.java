package de.graeuler.garden;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.graeuler.garden.config.AppConfigKeyTest;
import de.graeuler.garden.data.DataRecordTest;
import de.graeuler.garden.data.DerbyDataPersisterTest;
import de.graeuler.garden.data.GardenDataCollectorTest;
import de.graeuler.garden.data.JsonDataConverterTest;
import de.graeuler.garden.data.SerializableHasherTest;
import de.graeuler.garden.data.SqliteDataPersisterTest;
import de.graeuler.garden.monitor.config.IntLimitValidatorTest;
import de.graeuler.garden.monitor.sensor.SchedulerSensorBrickTest;
import de.graeuler.garden.monitor.sensor.TemperatureSensorTest;
import de.graeuler.garden.monitor.sensor.VoltageCurrentSensorTest;
import de.graeuler.garden.monitor.sensor.WaterLevelSensorTest;
import de.graeuler.garden.monitor.service.NetworkTrafficMonitorServiceTest;
import de.graeuler.garden.monitor.service.SensorMonitorServiceTest;
import de.graeuler.garden.monitor.tinkerforge.BrickDaemonFacadeTest;
import de.graeuler.garden.monitor.tinkerforge.ConnectReasonTest;
import de.graeuler.garden.monitor.tinkerforge.ConnectionStateTest;
import de.graeuler.garden.monitor.tinkerforge.DisconnectReasonTest;
import de.graeuler.garden.monitor.tinkerforge.TFDeviceTest;
import de.graeuler.garden.monitor.util.BytesTest;
import de.graeuler.garden.monitor.util.ObjectSerializationUtilTest;
import de.graeuler.garden.monitor.util.VnStatPosTest;
import de.graeuler.garden.uplink.DataCollectionMonitorTest;
import de.graeuler.garden.uplink.HttpUplinkServiceTest;

//@RunWith(Suite.class)
//@SuiteClasses({
//		AppConfigKeyTest.class, 
//		BrickDaemonFacadeTest.class,
//		BytesTest.class, 
//		ConnectionStateTest.class, 
//		ConnectReasonTest.class, 
//		DataCollectionMonitorTest.class,
//		DataRecordTest.class, 
//		DerbyDataPersisterTest.class,
//		DisconnectReasonTest.class,
////		FileBasedDerbyPersisterTest.class, // real world db test, other db tests run in memory 
//		GardenDataCollectorTest.class,
//		HttpUplinkServiceTest.class, 
//		IntLimitValidatorTest.class, 
//		JsonDataConverterTest.class,
//		NetworkTrafficMonitorServiceTest.class,
//		ObjectSerializationUtilTest.class, 
//		SchedulerSensorBrickTest.class, 
//		SensorMonitorServiceTest.class, 
//		SerializableHasherTest.class,
//		SqliteDataPersisterTest.class,
//		TemperatureSensorTest.class, 
//		TFDeviceTest.class, 
//		VnStatPosTest.class,
//		VoltageCurrentSensorTest.class, 
//		WaterLevelSensorTest.class, 
//	})
public class AllTests {
	// JUnit Test suite
}
