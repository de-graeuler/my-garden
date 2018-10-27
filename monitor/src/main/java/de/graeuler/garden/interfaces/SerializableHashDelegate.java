package de.graeuler.garden.interfaces;

import java.io.Serializable;

public interface SerializableHashDelegate {

	byte[] hash(Serializable data, StringBuffer hash);

}
