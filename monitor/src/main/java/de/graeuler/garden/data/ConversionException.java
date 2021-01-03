package de.graeuler.garden.data;

import java.io.IOException;

public class ConversionException extends Exception {

	public ConversionException(IOException e) {
		super(e);
	}

	private static final long serialVersionUID = 7657371900016351164L;

}
