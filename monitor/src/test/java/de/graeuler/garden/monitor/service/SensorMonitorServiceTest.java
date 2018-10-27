package de.graeuler.garden.monitor.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;

import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;

import de.graeuler.garden.interfaces.SensorHandler;
import de.graeuler.garden.monitor.model.TinkerforgeDevice;
import de.graeuler.garden.monitor.sensor.TemperatureSensor;

public class SensorMonitorServiceTest {

	private Set<SensorHandler> sensorHandlers = new HashSet<>();
	private ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
	private BrickDaemonFacade brickDaemonManager = mock(BrickDaemonFacade.class);
	private IPConnection connection = mock(IPConnection.class);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testDeviceFoundAndChecked() {
		SensorMonitorService service = new SensorMonitorService(this.brickDaemonManager, this.sensorHandlers, this.scheduler);
		TinkerforgeDevice devNull = null;
		TinkerforgeDevice devOk = new TinkerforgeDevice();
		devOk.setUid("123");
		devOk.setDeviceClass(BrickletTemperature.class);
		TinkerforgeDevice devNotOk = new TinkerforgeDevice();
		devOk.setUid("345");
		devOk.setDeviceClass(BrickletTemperature.class);
		SensorHandler sensorDevOk = mock(TemperatureSensor.class);
		SensorHandler sensorDevNok = mock(TemperatureSensor.class);
		when(sensorDevOk.doesAccept(devOk, connection)).thenReturn(true);
		when(sensorDevNok.doesAccept(devNotOk, connection)).thenReturn(false);
		sensorHandlers.add(sensorDevOk);
		sensorHandlers.add(sensorDevNok);
		
		service.onDeviceFound(devOk, connection);
		service.onDeviceFound(devNotOk, connection);
		service.onDeviceFound(devNull, connection);
		
		verify(sensorDevOk, times(1)).doesAccept(devOk, connection);
		verify(sensorDevNok, times(1)).doesAccept(devNotOk, connection);
		verify(sensorDevOk, times(0)).doesAccept(devNull, connection);
		verify(sensorDevNok, times(0)).doesAccept(devNull, connection);
	}
	
}
