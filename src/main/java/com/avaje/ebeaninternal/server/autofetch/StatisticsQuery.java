package com.avaje.ebeaninternal.server.autofetch;

import java.io.Serializable;

import com.avaje.ebean.meta.MetaAutoFetchStatistic.QueryStats;

/**
 * Used to accumulate query execution statistics.
 */
public class StatisticsQuery implements Serializable {
	
	private static final long serialVersionUID = -1133958958072778811L;

	private final String path;
	
	private int exeCount;
	
	private int totalBeanLoaded;
	
	private int totalMicros;
	
	public StatisticsQuery(String path){
		this.path = path;
	}
		
	public QueryStats createPublicMeta() {
		return new QueryStats(path, exeCount, totalBeanLoaded, totalMicros);
	}
	
	public void add(int beansLoaded, int micros) {
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