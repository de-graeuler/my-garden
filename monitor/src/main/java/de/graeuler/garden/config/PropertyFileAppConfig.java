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
	public Object get(Key key) {
		return properties.getOrDefault(key.getKey(), key.getDefaultValue());
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return properties.getOrDefault(key.getKey(), defaultValue);
	}

}
