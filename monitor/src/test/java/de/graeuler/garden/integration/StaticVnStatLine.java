package de.graeuler.garden.integration;

import java.io.IOException;
import java.util.List;

import de.graeuler.garden.monitor.util.CommandLineReader;

public class StaticVnStatLine implements CommandLineReader {

	@Override
	public String readFromCommand(List<String> vnstatOnelineCommand) throws IOException {
		return "1;wlan0;2016-11-26;4.69 MB;424 KB;5.10 MB;0.52 kbit/s;2016-11;129.53 MB;25.61 MB;154.14 MB;0.57 kbit/s;129.53 MB;25.61 MB;155.14 MB";
	}

}
