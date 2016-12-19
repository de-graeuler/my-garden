package de.graeuler.garden.monitor.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.graeuler.garden.config.AppConfig;
import de.graeuler.garden.interfaces.DataCollector;
import de.graeuler.garden.interfaces.MonitorService;

public class NetworkTrafficMonitorService implements MonitorService, Runnable {

	private DataCollector dataCollector;
	private ScheduledExecutorService scheduler;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private TimeUnit netCheckTimeUnit;
	private Integer netCheckTimeRate;
	
	@Inject
	public NetworkTrafficMonitorService(AppConfig config, DataCollector dataCollector, ScheduledExecutorService scheduler) {
		this.vnstat_oneline = (String) AppConfig.Key.NETWORK_VNSTAT_CMD.from(config);
		this.netCheckTimeRate = (Integer) AppConfig.Key.NET_TIME_RATE.from(config);
		this.netCheckTimeUnit = (TimeUnit) AppConfig.Key.NET_TIME_UNIT.from(config);
		this.dataCollector = dataCollector;
		this.scheduler = scheduler;
	}
	
	@Override
	public void monitor() {
		scheduleBandwidthConsumptionCheck();
	}

	private final String vnstat_oneline;

	private enum VNSTAT_POS {
		VERSION,INTERFACE,
		TODAY_TS,TODAY_RX,TODAY_TX,TODAY_TOTAL,TODAY_AVG,
		MONTH_TS,MONTH_RX,MONTH_TX,MONTH_TOTAL,MONTH_AVG,
		ALL_TS  ,ALL_RX,  ALL_TX,  ALL_TOTAL
	}
	
	private enum Bytes {
		K(1), M(2), G(3);
		int power;
		Bytes(int power) {
			this.power=power;
		}
		public double getBytes(double value) {
			return value * Math.pow(1024, power);
		}
		public double convertBytes(double bytes){
			return bytes / Math.pow(1024, power);
		}
	}
	
	@Override
	public void run() {
		try {
			Process p = Runtime.getRuntime().exec(vnstat_oneline);
			BufferedReader stdInput = new BufferedReader(new 
	                 InputStreamReader(p.getInputStream()));
			String output = stdInput.readLine();
			if (null != output ) {
				// vnstat --oneline returns an output like this:
				// 1;wlan0;2016-11-26;4.69 MB;424 KB;5.10 MB;0.52 kbit/s;2016-11;129.53 MB;25.61 MB;155.14 MB;0.57 kbit/s;129.53 MB;25.61 MB;155.14 MB
				String[] monthTotal = output.split(";")[NetworkTrafficMonitorService.VNSTAT_POS.MONTH_TOTAL.ordinal()].split("\\s");
				// monthTotal now should hold two entries: the traffic consumption and the unit. In the Above Example: {"155.14", "MB"}
				String unit = Character.toString(monthTotal[1].charAt(0));
				double bytes = Bytes.valueOf(unit).getBytes(Double.valueOf(monthTotal[0]));
				log.info("{} kilobytes transferred on wlan0.", Bytes.K.convertBytes(bytes));
				dataCollector.collect("month-total-traffic", bytes);
			} else {
				log.error("Unable to read result of process execution for {}", vnstat_oneline);
			}
			stdInput.close();
			p.waitFor(5, TimeUnit.SECONDS);
			if (p.isAlive()) {
				p.destroyForcibly();
				log.warn("VnStat needed to be killed forcefully.");
			}
			
		} catch (IOException e) {
			log.error("Unable to execute vnstat. {} ", e.getMessage());
		} catch (InterruptedException e) {
			log.error("Waiting for vnstat to terminate was interrupted.");
		}
	}
;
	
	private void scheduleBandwidthConsumptionCheck() {
		this.scheduler.scheduleAtFixedRate(this, 0, this.netCheckTimeRate, this.netCheckTimeUnit);
	}
	
}
