package de.graeuler.garden.data;

import java.io.Serializable;

public interface SerializableHashDelegate {

	/**
	 * 
	 * 
	 * @param data
	 * @param hash
	 * @return
	 */
	byte[] serializeAndHash(Serializable data, StringBuffer hash);

}
