package de.graeuler.garden.config;

public interface AppConfig{
	
	public enum Key {

		// keys and default values
		TF_DAEMON_HOST ("tinkerforge.brickdaemon.host",      "localhost"           ), 
		TF_DAEMON_PORT ("tinkerforge.brickdaemon.port",      4223                  ), 

		DC_STORE_PATH  ("datacollector.store-path"    ,      "./data"              ),
		DC_STORE_FILE  ("datacollector.store-file"    ,      "data-collector.json" ), 
		
		UPLINK_ADRESS  ("uplink.address"              ,      "http://localhost"    ), 
		API_TOKEN      ("uplink.api-token"            ,      "default-token"       ),
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
