package de.graeuler.garden.monitor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VnStatReader implements CommandLineReader {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public String readFromCommand(List<String> vnstatOnelineCommand) throws IOException {
		final ProcessBuilder processBuilder = new ProcessBuilder(vnstatOnelineCommand);
		Process p = processBuilder.start();
		try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			String output = stdInput.readLine();
			try {
				p.waitFor(5, TimeUnit.SECONDS);
				if (p.isAlive()) {
					p.destroyForcibly();
					log.warn("VnStat needed to be killed forcefully.");
				}
			} catch (InterruptedException e) {
				log.error("Waiting for vnstat to terminate was interrupted.");
			}
			return output;
		}
	}

}
