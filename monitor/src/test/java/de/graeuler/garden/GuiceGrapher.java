package de.graeuler.garden;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.graphviz.GraphvizGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;

public class GuiceGrapher {

	public static void main(String[] args) {
		Injector appInjector = Guice.createInjector(new ApplicationModule());

		try {
			graph("garden-monitor.dot", appInjector);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		System.out.println("Done.");
	}

	public static void graph(String filename, Injector injector) throws IOException {
		try (PrintWriter out = new PrintWriter(new File(filename), "UTF-8")) {
			Injector graphInjector = Guice.createInjector(new GraphvizModule());
			GraphvizGrapher grapher = graphInjector.getInstance(GraphvizGrapher.class);
			grapher.setOut(out);
			grapher.setRankdir("TB");
			grapher.graph(injector);
		}
	}

}
