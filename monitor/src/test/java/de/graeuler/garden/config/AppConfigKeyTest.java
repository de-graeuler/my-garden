package de.graeuler.garden.config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import de.graeuler.garden.config.AppConfig.Key;

public class AppConfigKeyTest {

	AppConfig config = mock(AppConfig.class);
	
	@Before
	public void setUp() throws Exception {
		when(config.get(Key.API_TOKEN)).thenReturn("ABCD");
	}

	@Test
	public final void testFromAppConfigReturnsSetValue() {
		assertEquals(AppConfig.Key.API_TOKEN.from(config), "ABCD");
	}
	
	@Test
	public final void testFromAppConfigReturnsValidatedValue() {
		int expected = 40;
		when(config.get(Key.WATERLVL_MOVING_AVG)).thenReturn(expected);
		assertEquals(expected, AppConfig.Key.WATERLVL_MOVING_AVG.from(config));
	}
	
	@Test
	public final void testFromAppConfigReturnsDefaultIfValidationFails() {
		Object expected = AppConfig.Key.WATERLVL_MOVING_AVG.getDefaultValue();
		when(config.get(Key.WATERLVL_MOVING_AVG)).thenReturn(150); // Should be limited to 0..100
		assertEquals(expected, AppConfig.Key.WATERLVL_MOVING_AVG.from(config));
	}

	@Test
	public final void testFromAppConfigObjectReturnsGivenDefaultValue() {
		String expected = "expectedValue";
		AppConfig.Key.UPLINK_ADRESS.from(config, expected);
		verify(config).get(AppConfig.Key.UPLINK_ADRESS, expected);
	}

}
