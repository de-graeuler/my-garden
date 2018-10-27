package de.graeuler.garden;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class IntegrationTest {

	private static Injector injector;
	private static GardenMonitor gardenMonitor;

	@BeforeClass
	public static void startApplication() {
		injector = Guice.createInjector(new IntegrationModule(), new );
		gardenMonitor = injector.getInstance(GardenMonitor.class);
		gardenMonitor.start();
	}
	
	@Test
	public void testSimpleValue() {
		assertTrue(true);
	}
	
	
	@AfterClass
	public static void stopApplication() {
		gardenMonitor.stop();
	}

}
