package de.graeuler.garden.uplink;

public interface Uplink<T> {
	public boolean pushData(T data);
}
