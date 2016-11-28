package de.graeuler.garden;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.graeuler.garden.data.model.DataRecordTest;
import de.graeuler.garden.monitor.model.TFDeviceTest;
import de.graeuler.garden.monitor.sensor.SchedulerSensorBrickTest;

@RunWith(Suite.class)
@SuiteClasses({ TFDeviceTest.class, SchedulerSensorBrickTest.class, DataRecordTest.class})
public class AllTests {

}
