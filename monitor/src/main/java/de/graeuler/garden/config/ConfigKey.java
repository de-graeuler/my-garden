package de.graeuler.garden.config;

public interface ConfigKey {

	Object from(AppConfig configuration);
	Object from(AppConfig config, Object defaultValue);
	String getPropertyName();
	Object getDefaultValue();

}
