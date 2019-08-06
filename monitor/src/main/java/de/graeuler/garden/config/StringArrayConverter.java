package de.graeuler.garden.config;

import java.util.Arrays;

public class StringArrayConverter implements ConfigValueConverter {

	@Override
	public Object convert(Object value) throws Exception {
		if ( value instanceof String ) {
			return Arrays.asList(((String) value).split("(?<!\\\\), "));
		} else {
			return null;
		}
	}

}
