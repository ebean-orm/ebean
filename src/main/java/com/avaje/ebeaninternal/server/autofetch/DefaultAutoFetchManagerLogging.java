package com.avaje.ebeaninternal.server.autofetch;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Handles the logging aspects for the DefaultAutoFetchListener.
 * <p>
 * Note that java util logging loggers generally should not be serialised and
 * that is one of the main reasons for pulling out the logging to this class.
 * </p>
 */
public class DefaultAutoFetchManagerLogging {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAutoFetchManagerLogging.class);

	private final DefaultAutoFetchManager manager;

	private final int updateFreqInSecs;
	
	public DefaultAutoFetchManagerLogging(ServerConfig serverConfig, DefaultAutoFetchManager profileListener) {

		this.manager = profileListener;
		this.updateFreqInSecs = serverConfig.getAutofetchConfig().getProfileUpdateFrequency();
	}
	
	public void init(SpiEbeanServer ebeanServer) {
	  ebeanServer.getBackgroundExecutor().executePeriodically(new UpdateProfile(), updateFreqInSecs, TimeUnit.SECONDS);
	}

	private final class UpdateProfile implements Runnable {
		public void run() {
			manager.updateTunedQueryInfo();
		}
	}

  public void logInfo(String msg, Throwable e) {
    logger.info(msg, e);
  }

  public void logError(String msg, Throwable e) {
    logger.error(msg, e);
  }

	public void logSummary(String summaryInfo) {
		
		String msg = "\"Summary\",\""+summaryInfo+"\",,,,";		
		logger.debug(msg);
	}

	public void logChanged(TunedQueryInfo tunedFetch, OrmQueryDetail newQueryDetail) {
		
		String msg = tunedFetch.getLogOutput(newQueryDetail);
		logger.debug(msg);
	}

	public void logNew(TunedQueryInfo tunedFetch) {

		String msg = tunedFetch.getLogOutput(null);
	  logger.debug(msg);
	}
}
