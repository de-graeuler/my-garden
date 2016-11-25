package de.graeuler.garden;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.graeuler.garden.monitor.model.TFDeviceTest;

@RunWith(Suite.class)
@SuiteClasses({ TFDeviceTest.class })
public class AllTests {

}
