package de.graeuler.garden.testhelpers;

import java.util.EnumMap;

import de.graeuler.garden.config.AppConfig;

public class TestConfig implements AppConfig {

	private EnumMap<AppConfig.Key, Object> config = new EnumMap<>(Key.class);
	
	@Override
	public Object get(Key key) {
		if (config.containsKey(key))
			return config.get(key);
		else 
			return key.getDefaultValue();
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return this.config.getOrDefault(key, defaultValue);
	}
	
	public void set(Key key, Object configValue) {
		config.put(key, configValue);
	}

}
