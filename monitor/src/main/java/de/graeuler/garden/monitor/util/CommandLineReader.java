package de.graeuler.garden.monitor.util;

import java.io.IOException;
import java.util.List;

public interface CommandLineReader {

	String readFromCommand(List<String> vnstatOnelineCommand) throws IOException;

}
