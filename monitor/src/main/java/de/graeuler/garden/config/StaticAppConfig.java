package de.graeuler.garden.config;

import java.util.EnumMap;

public class StaticAppConfig implements AppConfig {

	private EnumMap<ConfigurationKeys, Object> config = new EnumMap<>(ConfigurationKeys.class);
	
	public StaticAppConfig() {
		
		this.config.put(ConfigurationKeys.TF_DAEMON_HOST, "localhost");
		this.config.put(ConfigurationKeys.UPLINK_ADRESS, "http://localhost:8081/datafeed/collect/garden");
		// this.config.put(AppConfig.Key.API_TOKEN, "non-working-token");
	}
	
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

}
