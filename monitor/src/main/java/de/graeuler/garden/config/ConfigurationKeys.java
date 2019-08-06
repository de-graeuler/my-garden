package de.graeuler.garden.config;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This enumeration holds the default configuration. Each enum predefines a string representation,
 * a default value, an optional converter implementing the {@link ConfigValueConverter} interface,
 * and an optional array of configuration validators implementing the {@link ConfigValueValidator} interface.
 * 
 * @author bernhard.graeuler
 *
 */
public enum ConfigurationKeys implements ConfigKey {

	// key,              external property name              , default value           , converter   , { validator,... }
	TF_DAEMON_HOST      ("tinkerforge.brickdaemon.host"      , "localhost"             , null        , null), 
	TF_DAEMON_PORT      ("tinkerforge.brickdaemon.port"      , 4223                    , new IntegerConverter(), null), 

	DC_JDBC_CONNECTION  ("datacollector.jdbc-connect-string" , "jdbc:derby:data;create=true", null   , null),		
	UPLINK_ADRESS       ("uplink.address"                    , "http://localhost/collect/garden" , null, null), 
	UPLINK_STATUS       ("uplink.status"                     , "http://localhost/status" , null, null), 
	API_TOKEN           ("uplink.api-token"                  , "default-token"         , null        , null),
	
	COLLECT_TIME_RATE   ("collect.time.rate"                 , 4                       , new IntegerConverter(), null),
	COLLECT_TIME_UNIT   ("collect.time.unit"                 , TimeUnit.HOURS          , new TimeUnitConverter(), null),
	COLLECT_BLOCK_SIZE  ("collect.block.size"                , 500                     , new IntegerConverter(), null), 
	
	WATERLVL_CHG_THD    ("waterlevel.change.threshold.cm"    , 2                       , new IntegerConverter(), null),
	WATERLVL_DEBOUNCE   ("waterlevel.debounce.period.ms"     , 10000                   , new IntegerConverter(), null),
	WATERLVL_MOVING_AVG ("waterlevel.moving.average"         , 60                      , new IntegerConverter(), 
			new ConfigValueValidator[] { new IntLimitValidator(0, 100) }),
	
	TEMP_CHG_THD        ("temperature.change.threshold.degc" , 1                       , new IntegerConverter(), null),
	TEMP_DEBOUNCE       ("temperature.debounce.period.ms"    , 1000                    , new IntegerConverter(), null),
	
	NETWORK_VNSTAT_CMD  ("net.vnstat.oneline.command"        , "vnstat, --oneline"     , new StringArrayConverter(),  null),
	NETWORK_VNSTAT_LANG_TAG("net.vnstat.language.tag"        , "en"                    , null        , null),
	NET_TIME_RATE       ("net.check.time.rate"               , 1                       , new IntegerConverter(), null),
	NET_TIME_UNIT       ("net.check.time.unit"               , TimeUnit.MINUTES        , new TimeUnitConverter(), null),
	NET_VOL_CHG_THD     ("net.volume.change.threshold.bytes" , 102400                  , new IntegerConverter(), null),
	
	CURRENT_CHG_THD     ("current.change.threshold.mamp"     , 10                      , new IntegerConverter(), null),
	VOLTAGE_CHG_THD     ("voltage.change.threshold.mvolt"    , 1000                    , new IntegerConverter(), null), 
	;

	private Logger log = LoggerFactory.getLogger(ConfigurationKeys.class);
	
	private String propertyName;
	private Object defaultValue;
	private ConfigValueConverter configKeyConverter;

	private ConfigValueValidator[] configValueValidators;

	@Override
	public String getPropertyName() {return propertyName;}
	
	@Override
	public Object getDefaultValue() {return defaultValue;}
	
	/**
	 * 
	 * @param propertyName Identifier of this configuration key 
	 * @param defaultValue
	 * @param converter
	 * @param validators Are used to check the returned configuration value.
	 */
	ConfigurationKeys(String propertyName, Object defaultValue, ConfigValueConverter converter, ConfigValueValidator[] validators) {
		this.propertyName = propertyName;
		this.defaultValue = defaultValue;
		this.configKeyConverter = converter;
		this.configValueValidators = validators;
	}

	/**
	 * Reads the value of this key from the given configuration.
	 * @param configuration where the value should be read from.
	 * @return the configuration value converted by this Keys converter.
	 */
	@Override
	public Object from(AppConfig configuration) {
		Object value = this.getConvertedValue(configuration.get(this));
		return validate(value);
	}

	/**
	 * Similar to from, but returns the default value if this key is not available in the given configuration.
	 * @param config configuration where the value should be read from.
	 * @param defaultValue the default value if this key is not available.
	 * @return the converted (given default) value
	 */
	@Override
	public Object from(AppConfig config, Object defaultValue) {
		Object value = this.getConvertedValue(config.get(this, defaultValue));
		return validate(value);
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
	
	private Object validate(Object value) {
		ConfigValueValidator[] validators = this.getConfigValidators();
		boolean isValid = true;
		for ( ConfigValueValidator v : validators ) {
			if ( v.isValid(value) ) {
				continue;
			} else {
				isValid = false;
				break;
			}
		}
		if ( isValid ) {
			return value;
		} else {
			log.warn("Invalid configuration value provided for {}. Using the default.", this.getPropertyName());
			return this.getDefaultValue();
		}
	}

	private ConfigValueValidator[] getConfigValidators() {
		if ( null == this.configValueValidators ) {
			return new ConfigValueValidator[] {};
		} else {
			return this.configValueValidators;
		}
	}			
}