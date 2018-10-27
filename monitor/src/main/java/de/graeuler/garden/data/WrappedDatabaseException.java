package de.graeuler.garden.data;

public class WrappedDatabaseException extends Throwable {

	public WrappedDatabaseException(Throwable e) {
		super(e);
	}

	private static final long serialVersionUID = 8787901020158441761L;

}
