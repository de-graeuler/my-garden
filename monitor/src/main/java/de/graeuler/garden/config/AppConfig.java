package de.graeuler.garden.config;

public interface AppConfig{
	
	public enum Key {

		// keys and default values
		TF_DAEMON_HOST ("tinkerforge.brickdaemon.host",      "localhost"              ), 
		TF_DAEMON_PORT ("tinkerforge.brickdaemon.port",      4332                     ), 
		UPLINK_ADRESS  ("uplink.adress"               ,      "http://localhost/garden"), 
		DC_STORE_PATH  ("datacollector_store_path"    ,      "./data"   ),
		DC_STORE_FILE  ("datacollector_store_file"    ,      "data-collector.json"   ),
		;

		private String key;
		private Object defaultValue;

		public String getKey() {return key;}
		public Object getDefaultValue() {return defaultValue;}

		Key(String key, Object defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}
	}

	/**
	 * @param key The key of the configuration value 
	 * @return Returns the value assigned to the key, 
	 */
	public Object get(Key key);
	public Object get(Key key, Object defaultValue);
	
}
