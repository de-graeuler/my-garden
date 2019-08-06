package de.graeuler.garden.integration;

import java.util.concurrent.TimeUnit;

import de.graeuler.garden.config.ConfigurationKeys;
import de.graeuler.garden.testhelpers.TestConfig;

public class IntegrationConfig extends TestConfig {

	public IntegrationConfig() {
		set(ConfigurationKeys.UPLINK_ADRESS, "http://localhost:60080/collect/garden");
		set(ConfigurationKeys.UPLINK_STATUS, "http://localhost:60080/status");
		set(ConfigurationKeys.DC_JDBC_CONNECTION, "jdbc:derby:memory:integrationDB;create=true");
		set(ConfigurationKeys.COLLECT_TIME_UNIT, TimeUnit.SECONDS);
		set(ConfigurationKeys.COLLECT_TIME_RATE, 1);
		set(ConfigurationKeys.COLLECT_BLOCK_SIZE, 4000);
	}
	
}
