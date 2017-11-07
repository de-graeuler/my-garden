package de.graeuler.garden.config;

public class IntLimitValidator implements ConfigValueValidator{

	private int upper;
	private int lower;

	public IntLimitValidator(int lower, int upper) {
		this.lower = lower;
		this.upper = upper;
	}

	@Override
	public boolean isValid(Object value) {
		if ( ! (value instanceof Number)) {
			return false;
		}
		int x = ((Number) value).intValue();
		return x >= this.lower & x <= this.upper;
	}

}
