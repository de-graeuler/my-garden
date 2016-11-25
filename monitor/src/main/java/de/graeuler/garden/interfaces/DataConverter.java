package de.graeuler.garden.interfaces;

public interface DataConverter<T, U> {
	public U convert(T input);
}
