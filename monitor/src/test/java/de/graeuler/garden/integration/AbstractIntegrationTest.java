package de.graeuler.garden.integration;

import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import de.graeuler.garden.GardenMonitor;
import de.graeuler.garden.data.DataPersister;
import de.graeuler.garden.data.DataRecord;
import de.graeuler.garden.monitor.service.SensorHandler;

public abstract class AbstractIntegrationTest {

	private static Logger log = LoggerFactory.getLogger(AbstractIntegrationTest.class);
	
	private static Injector injector;
	private static GardenMonitor gardenMonitor;
	private static Set<SensorHandler> sensorHandlers;
	
	private static HttpServer httpServer;
	private final static int LISTEN_PORT = 60080;
	private static CollectRequestHandler collectRequest = new CollectRequestHandler(); 
	private static HttpRequestHandler systemStatusRequest = new SystemStatusRequestHandler();

	@BeforeClass
	public static void startApplication() {
		startHttpServer();
		
		injector = Guice.createInjector(new IntegrationModule());
		sensorHandlers = injector.getInstance(Key.get(new TypeLiteral<Set<SensorHandler>>(){}));
		gardenMonitor = injector.getInstance(GardenMonitor.class);
		gardenMonitor.start();
	}
	
	@Before
	public void setUp() {
		collectRequest.reset();
	}
	
	@AfterClass
	public static void stopApplication() {
		gardenMonitor.stop();
		stopHttpServer();
	}

	protected static void stopHttpServer() {
		httpServer.shutdown(1, TimeUnit.SECONDS);
		log.info("Http Server shut down.");
	}

	protected static void startHttpServer() {
		try {
			httpServer = ServerBootstrap.bootstrap()
					.setLocalAddress(InetAddress.getLoopbackAddress())
					.setListenerPort(LISTEN_PORT)
					.setServerInfo("Collector HttpService")
					.registerHandler("/collect/garden*", collectRequest)
					.registerHandler("/status", systemStatusRequest)
					.create();
			httpServer.start();
			log.info("Http Server started. Listening at: {}:{}", httpServer.getInetAddress(), httpServer.getLocalPort());
		}catch(IOException e) {
			fail("Unable to start Http Server! Errormessage: " + e.getMessage());
		}
	}

	protected DataPersister<DataRecord> dataPersisterInstance() {
		return injector.getInstance(Key.get(new TypeLiteral<DataPersister<DataRecord>>(){}));
	}

	protected <T> void waitForCondition(long maxWaitMilliseconds, Supplier<T> supplier, Predicate<T> condition) throws InterruptedException {
		final long startTimeMillis = System.currentTimeMillis();
		Thread conditionTester = new Thread(() -> {
			try {
				while( ! condition.test(supplier.get()) ) {
					TimeUnit.MILLISECONDS.sleep(250);
				};
			} catch (InterruptedException e) {
				return;
			}
		});
		conditionTester.start();
		conditionTester.join(maxWaitMilliseconds+100);
		long currentTimeMillis = System.currentTimeMillis();
		assertThat("Wait time exceeded.", currentTimeMillis - startTimeMillis, lessThan(maxWaitMilliseconds));
	}

	public static Set<SensorHandler> getSensorHandlers() {
		return sensorHandlers;
	}

	public static CollectRequestHandler getRequestHandler() {
		return collectRequest;
	}

}
