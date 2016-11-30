package de.graeuler.garden.config;

import java.util.EnumMap;

public class StaticAppConfig implements AppConfig {

	private EnumMap<AppConfig.Key, Object> config = new EnumMap<>(Key.class);
	
	public StaticAppConfig() {
		
		this.config.put(AppConfig.Key.TF_DAEMON_HOST, "192.168.1.10");
		this.config.put(AppConfig.Key.TF_DAEMON_PORT, Integer.valueOf(4223));
		this.config.put(AppConfig.Key.UPLINK_ADRESS, "http://localhost:8081/datafeed/collect/garden");
		// this.config.put(AppConfig.Key.API_TOKEN, "non-working-token");
	}
	
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

}
