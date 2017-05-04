package de.graeuler.garden.config;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interface defines the default configuration. Implementing classes may load modified values from other sources
 * like property files. The {@link Key} enum embedded in this interface provides methods to read values from AppConfig 
 * implementations like this:
 * 
 * <pre>
 * {@code AppConfig.Key.API_TOKEN.from(new AppConfig()); }
 * </pre>
 * 
 * @author bernhard.graeuler
 *
 */
public interface AppConfig {
	
	/**
	 * @param key The key of the configuration value 
	 * @return Returns the value assigned to the key, 
	 */
	public Object get(Key key);
	public Object get(Key key, Object defaultValue);
	
	/**
	 * The AppConfig.Key enumeration holds the default configuration. Each enum predefines a string representation,
	 * a default value and an optional converter implementing the {@link ConfigValueConverter} interface.
	 * 
	 * @author bernhard.graeuler
	 *
	 */
	public enum Key {

		// keys,             external property names               and default values      , converter
		TF_DAEMON_HOST      ("tinkerforge.brickdaemon.host"      , "localhost"             , null), 
		TF_DAEMON_PORT      ("tinkerforge.brickdaemon.port"      , 4223                    , new IntegerConverter()), 

		DC_STORE_PATH       ("datacollector.store-path"          , "./data"                , null),
		DC_STORE_FILE       ("datacollector.store-file"          , "data-collector.json"   , null), 
		
		UPLINK_ADRESS       ("uplink.address"                    , "http://localhost/collect/garden" , null), 
		API_TOKEN           ("uplink.api-token"                  , "default-token"         , null),
		
		COLLECT_TIME_RATE   ("collect.time.rate"                 , 6                       , new IntegerConverter()),
		COLLECT_TIME_UNIT   ("collect.time.unit"                 , TimeUnit.HOURS          , new TimeUnitConverter()),

		WATERLVL_CHG_THD    ("waterlevel.change.threshold.cm"    , 2                       , new IntegerConverter()),
		WATERLVL_DEBOUNCE   ("waterlevel.debounce.period.ms"     , 10000                   , new IntegerConverter()),
		WATERLVL_MOVING_AVG ("waterlevel.moving.average"         , 60                      , new IntegerConverter()),
		
		TEMP_CHG_THD        ("temperature.change.threshold.degc" , 1                       , new IntegerConverter()),
		TEMP_DEBOUNCE       ("temperature.debounce.period.ms"    , 1000                    , new IntegerConverter()),
		
		NETWORK_VNSTAT_CMD  ("net.vnstat.oneline.command"        , "vnstat --oneline"      , null),
		NET_TIME_RATE       ("net.check.time.rate"               , 1                       , new IntegerConverter()),
		NET_TIME_UNIT       ("net.check.time.unit"               , TimeUnit.MINUTES        , new TimeUnitConverter()),
		NET_VOL_CHG_THD     ("net.volume.change.threshold.bytes" , 102400                  , new IntegerConverter()),
		;

		private Logger log = LoggerFactory.getLogger(Key.class);
		
		private String key;
		private Object defaultValue;
		private ConfigValueConverter configKeyConverter;

		public String getKey() {return key;}
		public Object getDefaultValue() {return defaultValue;}
		
		/**
		 * Reads the value of this key from the given configuration.
		 * @param configuration where the value should be read from.
		 * @return the configuration value converted by this Keys converter.
		 */
		public Object from(AppConfig configuration) {
			return this.getConvertedValue(configuration.get(this));
		}

		/**
		 * Similar to from, but returns the default value if this key is not available in the given configuration.
		 * @param config configuration where the value should be read from.
		 * @param defaultValue the default value if this key is not available.
		 * @return the converted (given default) value
		 */
		public Object from(AppConfig config, Object defaultValue) {
			return this.getConvertedValue(config.get(this, defaultValue));
		}
		
		Key(String key, Object defaultValue, ConfigValueConverter configKeyConverter) {
			this.key = key;
			this.defaultValue = defaultValue;
			this.configKeyConverter = configKeyConverter;
		}

		private Object getConvertedValue(Object value) {
			if(null == this.configKeyConverter) {
				return value;
			} else {
				try {
					return this.configKeyConverter.convert(value);
				} catch (Exception e) {
					log.warn("Unable to convert {}... using {}.", 
							String.format("%1.25s", value.toString()),
							this.configKeyConverter.getClass().getName());
					return this.getDefaultValue();
				}
			}
		}
		
	}

}
