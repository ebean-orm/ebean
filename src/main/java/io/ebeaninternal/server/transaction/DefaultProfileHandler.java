package io.ebeaninternal.server.transaction;

import io.ebean.config.ProfilingConfig;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.api.SpiProfileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

/**
 * Default profile handler.
 * <p>
 * Uses ConcurrentLinkedQueue to minimise contention on threads calling collectTransactionProfile().
 * </p>
 * <p>
 * Uses a sleep backoff on the single threaded consumer that reads the profiles and writes them to files.
 * </p>
 */
public class DefaultProfileHandler implements SpiProfileHandler, Plugin {

  private static final Logger log = LoggerFactory.getLogger(DefaultProfileHandler.class);

  private static final DateTimeFormatter DTF;

  static {
    DTF = new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .appendValue(YEAR, 4)
      .appendValue(MONTH_OF_YEAR, 2)
      .appendValue(DAY_OF_MONTH, 2)
      .appendLiteral('-')
      .appendValue(HOUR_OF_DAY, 2)
      .appendValue(MINUTE_OF_HOUR, 2)
      .appendValue(SECOND_OF_MINUTE, 2)
      .appendLiteral('-')
      .appendValue(MILLI_OF_SECOND, 3)
      .toFormatter();
  }

  /**
   * Low contention choice.
   */
  private final Queue<TransactionProfile> queue = new ConcurrentLinkedQueue<>();

  private final ExecutorService executor;

  private final File dir;

  private final long minMicros;

  private final int[] includeIds;

  private final long profilesPerFile;

  private final boolean verbose;

  private volatile boolean shutdown;

  private long profileCounter;

  /**
   * Slow down polling of transaction profiling queue.
   */
  private int sleepBackoff;

  private Writer out;

  public DefaultProfileHandler(ProfilingConfig config) {
    this.verbose = config.isVerbose();
    this.minMicros = config.getMinimumMicros();
    this.includeIds = config.getIncludeProfileIds();
    this.profilesPerFile = config.getProfilesPerFile();

    // dedicated single threaded executor for consuming the
    // profiling and writing it to file(s)
    this.executor = Executors.newSingleThreadExecutor();
    this.dir = new File(config.getDirectory());
    if (!dir.exists() && !dir.mkdirs()) {
      log.error("failed to mkdirs " + dir.getAbsolutePath());
    }
    incrementFile();
  }

  /**
   * Low contention adding the transaction profile to the queue.
   * Minimise the impact to the normal transaction processing (threads).
   */
  @Override
  public void collectTransactionProfile(TransactionProfile transactionProfile) {
    queue.add(transactionProfile);
  }

  /**
   * Create and return a ProfileStream if we are profiling for the given transaction profileId.
   */
  @Override
  public ProfileStream createProfileStream(int profileId) {

    if (profileId < 1) {
      // not this transaction
      return null;
    }

    if (includeIds.length == 0) {
      return new DefaultProfileStream(profileId, verbose);
    }

    // check if we are profiling this specific transaction profileId, just
    // perform linear search as this is expected to be a small array
    for (int includeId : includeIds) {
      if (includeId == profileId) {
        return new DefaultProfileStream(profileId, verbose);
      }
    }
    return null;
  }

  private void flushCurrentFile() {
    synchronized (this) {
      if (out != null) {
        try {
          out.close();
          out = null;
        } catch (IOException e) {
          log.error("Failed to flush and close transaction profiling file ", e);
        }
      }
    }
  }

  /**
   * Move to the next file to write to.
   */
  private void incrementFile() {
    synchronized (this) {
      flushCurrentFile();
      try {
        String now = DTF.format(LocalDateTime.now());
        File file = new File(dir, "txprofile-" + now + ".tprofile");
        out = new BufferedWriter(new FileWriter(file));
      } catch (IOException e) {
        log.error("Not expected", e);
      }
    }
  }

  /**
   * Main loop for polling the queue and processing profiling messages.
   */
  private void collect() {
    try {
      while (!shutdown) {
        TransactionProfile profile = queue.poll();
        if (profile == null) {
          sleep();

        } else if (include(profile)) {
          write(profile);
        }
      }
      flushCurrentFile();
    } catch (Exception e) {
      log.warn("Error on collect", e);
    }
  }

  /**
   * Write the profile to the current file.
   */
  private void write(TransactionProfile profile) {
    try {
      sleepBackoff = 0;
      ++profileCounter;

      StringBuilder sb = new StringBuilder(80);

      // header
      sb.append(profile.getStartTime()).append(' ')
        .append(profile.getProfileId()).append(' ')
        .append(profile.getTotalMicros()).append(' ');

      // summary
      appendSummary(profile, sb);

      out.write(sb.toString());
      if (verbose) {
        out.write(' ');
        out.write(profile.getData());
      }
      out.write('\n');

      if (profileCounter % profilesPerFile == 0) {
        incrementFile();
        log.debug("profiled {} transactions", profileCounter);
      }
    } catch (IOException e) {
      log.warn("Error writing transaction profiling", e);
    }
  }

  private void appendSummary(TransactionProfile profile, StringBuilder sb) {

    TransactionProfile.Summary summary = profile.getSummary();

    sb.append("z:").append(rate(profile.getTotalMicros(), summary.persistCount + summary.queryCount)).append(' ');
    sb.append("p:").append(rate(summary.persistMicros, summary.persistBeans)).append(' ');
    sb.append("q:").append(rate(summary.queryMicros, summary.queryCount)).append(' ');

    sb.append("qm:").append(summary.queryMax).append(' ');
    sb.append("qt:").append(summary.queryMicros).append(' ');
    sb.append("qc:").append(summary.queryCount).append(' ');
    sb.append("qb:").append(summary.queryBeans).append(' ');

    sb.append("pt:").append(summary.persistMicros).append(' ');
    sb.append("pc:").append(summary.persistCount).append(' ');
    sb.append("pb:").append(summary.persistBeans).append(' ');
    sb.append("po:").append(summary.persistOneCount).append(' ');
    sb.append("pz:").append(rate(summary.persistBeans, summary.persistCount));
  }

  private int rate(long micros, long count) {
    return count < 1 ? 0 : (int) (micros / count);
  }

  /**
   * Return true if the profile should be included (or false for ignored).
   */
  private boolean include(TransactionProfile profile) {
    return profile.getTotalMicros() >= minMicros;
  }

  /**
   * Sleep backing off towards 250 millis when there is no activity.
   * This seems to be simple and decent for our queue consumer.
   */
  private void sleep() {
    try {
      // backoff sleep when nothing is happening
      int sleepFor = Math.min(++sleepBackoff, 250);
      Thread.sleep(sleepFor);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void configure(SpiServer server) {

    StringBuilder sb = new StringBuilder(200);
    sb.append("Bean profile mapping - ");
    for (BeanType<?> type : server.getBeanTypes()) {
      sb.append("profileId:").append(type.getProfileId())
        .append(" ").append(type.getName()).append(", ");
    }
    log.info(sb.toString());
  }

  @Override
  public void online(boolean online) {
    if (online) {
      executor.submit(this::collect);
    }
  }

  @Override
  public void shutdown() {
    shutdown = true;
    log.trace("shutting down");
    try {
      executor.shutdown();
      if (!executor.awaitTermination(4, TimeUnit.SECONDS)) {
        log.info("Shut down timeout exceeded. Terminating profiling consumer thread.");
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Interrupt on shutdown", e);
    }
    flushCurrentFile();
  }
}
