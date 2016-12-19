package de.graeuler.garden.config;

public interface ConfigValueConverter {
	/**
	 * Tries to convert the given value.
	 * @param value the input value
	 * @return converted value
	 * @throws Exception Any exception can be thrown by the implementing converters.
	 */
	public Object convert(Object value) throws Exception;
}
