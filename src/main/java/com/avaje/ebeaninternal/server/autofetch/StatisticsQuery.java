package com.avaje.ebeaninternal.server.autofetch;

import java.io.Serializable;

/**
 * Used to accumulate query execution statistics.
 */
public class StatisticsQuery implements Serializable {
	
	private static final long serialVersionUID = -1133958958072778811L;

	private final String path;
	
	private long exeCount;
	
	private long totalBeanLoaded;
	
	private long totalMicros;
	
	public StatisticsQuery(String path){
		this.path = path;
	}
	
	public void add(long beansLoaded, long micros) {
		exeCount++;
		totalBeanLoaded += beansLoaded;
		totalMicros += micros;
	}
	
	public String toString() {
		long avgMicros = exeCount == 0 ? 0 : totalMicros / exeCount;
		
		return	"queryExe path["+path+"] count[" + exeCount + "] totalBeansLoaded[" + totalBeanLoaded + "] avgMicros["
					+ avgMicros + "] totalMicros[" + totalMicros + "]";
	}
}