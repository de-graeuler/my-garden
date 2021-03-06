package de.graeuler.garden.config;

import java.util.Properties;

import com.google.inject.Inject;

public class PropertyFileAppConfig implements AppConfig {

	private Properties properties;
	
	@Inject
	public PropertyFileAppConfig(Properties properties) {
		this.properties = properties;
	}
	
	@Override
	public Object get(ConfigKey key) {
		return properties.getOrDefault(key.getPropertyName(), key.getDefaultValue());
	}

	@Override
	public Object get(ConfigKey key, Object defaultValue) {
		return properties.getOrDefault(key.getPropertyName(), defaultValue);
	}

}
