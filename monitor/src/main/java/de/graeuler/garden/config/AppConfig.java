package de.graeuler.garden.config;

/**
 * This interface defines the default configuration. Implementing classes may load modified values from other sources
 * like property files. The {@link ConfigurationKeys} enum embedded in this interface provides methods to read values from AppConfig 
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
	 * @return Must return the configuration value assigned to the key. 
	 */
	public Object get(ConfigKey key);
	public Object get(ConfigKey key, Object defaultValue);

}
