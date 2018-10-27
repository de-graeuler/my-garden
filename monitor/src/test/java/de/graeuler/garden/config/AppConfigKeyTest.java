package de.graeuler.garden.config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class AppConfigKeyTest {

	AppConfig config = mock(AppConfig.class);
	
	@Before
	public void setUp() throws Exception {
		when(config.get(ConfigurationKeys.API_TOKEN)).thenReturn("ABCD");
	}

	@Test
	public final void testFromAppConfigReturnsSetValue() {
		assertEquals(ConfigurationKeys.API_TOKEN.from(config), "ABCD");
	}
	
	@Test
	public final void testFromAppConfigReturnsValidatedValue() {
		int expected = 40;
		when(config.get(ConfigurationKeys.WATERLVL_MOVING_AVG)).thenReturn(expected);
		assertEquals(expected, ConfigurationKeys.WATERLVL_MOVING_AVG.from(config));
	}
	
	@Test
	public final void testFromAppConfigReturnsDefaultIfValidationFails() {
		Object expected = ConfigurationKeys.WATERLVL_MOVING_AVG.getDefaultValue();
		when(config.get(ConfigurationKeys.WATERLVL_MOVING_AVG)).thenReturn(150); // Should be limited to 0..100
		assertEquals(expected, ConfigurationKeys.WATERLVL_MOVING_AVG.from(config));
	}

	@Test
	public final void testFromAppConfigObjectReturnsGivenDefaultValue() {
		String expected = "expectedValue";
		ConfigurationKeys.UPLINK_ADRESS.from(config, expected);
		verify(config).get(ConfigurationKeys.UPLINK_ADRESS, expected);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public final void testFromAppConfigStringListValue() {
		List<String> expected = Arrays.asList("vnstat", "--oneline");
		when(config.get(ConfigurationKeys.NETWORK_VNSTAT_CMD)).thenReturn("vnstat, --oneline");
		Object value = ConfigurationKeys.NETWORK_VNSTAT_CMD.from(config);
		assertTrue(value instanceof List);
		assertEquals(expected.get(0), ((List<String>) value).get(0));
		assertEquals(expected.get(1), ((List<String>) value).get(1));
	}

}
