package de.graeuler.garden.config;

import java.util.concurrent.TimeUnit;

public class TimeUnitConverter implements ConfigValueConverter {

	@Override
	public Object convert(Object value) throws Exception{
		return TimeUnit.valueOf(value.toString().toUpperCase());
	}

}
