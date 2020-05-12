package de.graeuler.garden.data;

public interface DataConverter<T, U> {
	public U convert(T input);
}
