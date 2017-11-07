package de.graeuler.garden.monitor.config;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.graeuler.garden.config.IntLimitValidator;

public class IntLimitValidatorTest {

	IntLimitValidator validator = new IntLimitValidator(1, 100);
	
	@Before
	public void setUp() {
		
	}
	
	@Test
	public final void testIsValidDetectsCorrectType() {
		assertTrue(validator.isValid(50));
		assertTrue(validator.isValid(50.32));
		assertFalse(validator.isValid("Test"));
	}
	
	@Test 
	public final void testIsValidDetectsCorrectRange() {
		assertTrue(validator.isValid(1));
		assertTrue(validator.isValid(100));
		assertFalse(validator.isValid(-1));
		assertFalse(validator.isValid(0));
		assertFalse(validator.isValid(101));
		assertFalse(validator.isValid(Integer.MAX_VALUE));
	}
	

}
