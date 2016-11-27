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

	private AppConfig config;
	private DataCollector dataCollector;
	private ScheduledExecutorService scheduler;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Inject
	public NetworkTrafficMonitorService(AppConfig config, DataCollector dataCollector, ScheduledExecutorService scheduler) {
		this.config = config;
		this.dataCollector = dataCollector;
		this.scheduler = scheduler;
	}
	
	@Override
	public void monitor() {
		scheduleBandwidthConsumptionCheck();
	}

//	private final String VNSTAT_ONELINE = "vnstat -i wlan0 --oneline";
	private final String VNSTAT_ONELINE = "vnstat.bat";
	
	private final int VNSTAT_POS_VERSION     =  0;
	private final int VNSTAT_POS_INTERFACE   =  1;
	private final int VNSTAT_POS_TODAY_TS    =  2;
	private final int VNSTAT_POS_TODAY_RX    =  3;
	private final int VNSTAT_POS_TODAY_TX    =  4;
	private final int VNSTAT_POS_TODAY_TOTAL =  5;
	private final int VNSTAT_POS_TODAY_AVG   =  6;
	private final int VNSTAT_POS_MONTH_TS    =  7;
	private final int VNSTAT_POS_MONTH_RX    =  8;
	private final int VNSTAT_POS_MONTH_TX    =  9;
	private final int VNSTAT_POS_MONTH_TOTAL = 10;
	private final int VNSTAT_POS_MONTH_AVG   = 11;
	private final int VNSTAT_POS_ALL_RX      = 12;
	private final int VNSTAT_POS_ALL_TX      = 13;
	private final int VNSTAT_POS_ALL_TOTAL   = 14;
	
	private enum Bytes {
		K(1), M(2), G(3);
		int power;
		Bytes(int power) {
			this.power=power;
		}
		public double getBytes(double value) {
			return value * Math.pow(1024, power);
		}
	}
	
	@Override
	public void run() {
		try {
			Process p = Runtime.getRuntime().exec(VNSTAT_ONELINE);
			BufferedReader stdInput = new BufferedReader(new 
	                 InputStreamReader(p.getInputStream()));
			String s = stdInput.readLine();
			if (null != s ) {
				String[] monthTotal = s.split(";")[this.VNSTAT_POS_MONTH_TOTAL].split("\\s");
				String unit = String.valueOf(monthTotal[1].charAt(monthTotal[1].indexOf('B') - 1));
				double bytes = Bytes.valueOf(unit).getBytes(Double.valueOf(monthTotal[0]));
				dataCollector.collect("month-total-traffic", bytes);
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
		this.scheduler.scheduleAtFixedRate(this, 0, 30, TimeUnit.SECONDS);
	}
	
}
