package de.graeuler.garden.config;

public class IntegerConverter implements ConfigValueConverter {

	@Override
	public Object convert(Object value) throws Exception {
		return Integer.parseInt(value.toString());
	}

}
