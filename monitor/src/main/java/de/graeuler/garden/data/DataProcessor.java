package de.graeuler.garden.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;

public interface DataProcessor<T extends Serializable> extends Function<Collection<T>, Boolean> {

}
