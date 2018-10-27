package de.graeuler.garden.testhelpers;

import java.util.EnumMap;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.config.ConfigKey;
import de.graeuler.garden.config.ConfigurationKeys;

public class TestConfig implements AppConfig {

	private EnumMap<ConfigurationKeys, Object> config = new EnumMap<>(ConfigurationKeys.class);
	
	@Override
	public Object get(ConfigKey key) {
		if (config.containsKey(key))
			return config.get(key);
		else 
			return key.getDefaultValue();
	}

	@Override
	public Object get(ConfigKey key, Object defaultValue) {
		return this.config.getOrDefault(key, defaultValue);
	}
	
	public void set(ConfigurationKeys key, Object configValue) {
		config.put(key, configValue);
	}

}
