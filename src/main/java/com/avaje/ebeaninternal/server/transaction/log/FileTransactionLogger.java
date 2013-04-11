package com.avaje.ebeaninternal.server.transaction.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.transaction.TransactionLogBuffer;
import com.avaje.ebeaninternal.server.transaction.TransactionLogBuffer.LogEntry;
import com.avaje.ebeaninternal.server.transaction.TransactionLogWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default transaction logger implementation.
 * <p>
 * File based logger that can switch daily. It will include the date in the
 * files name.
 * </p>
 * <p>
 * Administration Note: If log file switching fails it will send the error to
 * standard out and standard err print streams. This is assumed to be rather
 * unlikely but possible.
 * </p>
 */
public class FileTransactionLogger implements Runnable, TransactionLogWriter {

  private static final Logger logger = LoggerFactory.getLogger(FileTransactionLogger.class);

  /**
   * Used to print stack trace.
   */
  private static final String atString = "        at ";

  /**
   * The newLineChar used instead of NL or CRNL for printing stack traces.
   */
  private final String newLinePlaceholder = "\\r\\n";

  /**
   * The maximum number of stack lines to output. This is just for transaction
   * logging so 5 is fine.
   */
  private final int maxStackTraceLines = 5;

  /**
   * Queue that transactions put their LogBuffers onto and this LogBufferWriter
   * pulls LogBuffers from.
   */
  private final ConcurrentLinkedQueue<TransactionLogBuffer> logBufferQueue = new ConcurrentLinkedQueue<TransactionLogBuffer>();

  private final Object queueMonitor = new Object();
  
  /**
   * Thread that is the sole writer of logBuffer entries to the file.
   */
  private final Thread logWriterThread;

  private final String threadName;

  /**
   * The path of the log file.
   */
  private final String filepath;

  /**
   * The delimiter to use.
   */
  private final String deliminator = ", ";

  /**
   * The prefix of the log file name.
   */
  private final String logFileName;

  /**
   * The file suffix for the logs.
   */
  private final String logFileSuffix;

  /**
   * Shutdown flag.
   */
  private volatile boolean shutdown;
  private volatile boolean shutdownComplete;

  /**
   * The output stream.
   */
  private PrintStream out;

  /**
   * The current file path.
   */
  private String currentPath;

  /**
   * Counter thats incremented when maxBytesPerFile is hit.
   */
  private int fileCounter;

  /**
   * Roughly the max bytes written to a file before we switch. Switch Daily and
   * on hitting max bytes.
   */
  private long maxBytesPerFile;

  /**
   * Roughly the bytes written to a file.
   */
  private long bytesWritten;

  public FileTransactionLogger(String threadName, String dir, String logFileName, int maxBytesPerFile) {
    this(threadName, dir, logFileName, "log", maxBytesPerFile);
  }

  public FileTransactionLogger(String threadName, String dir, String logFileName, String suffix, int maxBytesPerFile) {
    this.threadName = threadName;
    this.logFileName = logFileName;
    this.logFileSuffix = "." + suffix;
    this.maxBytesPerFile = maxBytesPerFile;

    try {
      // get the directory where the log files are going to go
      filepath = makeDirIfRequired(dir);

      switchFile(LogTime.nextDay());

    } catch (Exception e) {
      System.out.println("FATAL ERROR: init of FileLogger: " + e.getMessage());
      System.err.println("FATAL ERROR: init of FileLogger: " + e.getMessage());
      throw new RuntimeException(e);
    }

    logWriterThread = new Thread(this, threadName);
    logWriterThread.setDaemon(true);
  }

  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  public void start() {
    logWriterThread.start();
  }

  public void shutdown() {

    shutdown = true;

    synchronized (logWriterThread) {
      try {
        // wait max 20 seconds
        logWriterThread.wait(20000);
        logger.debug("Shutdown LogBufferWriter " + threadName + " shutdownComplete:" + shutdownComplete);

      } catch (InterruptedException e) {
        logger.debug("InterruptedException:" + e);
      }
    }

    if (!shutdownComplete) {
      String m = "WARNING: Shutdown of LogBufferWriter " + threadName + " not completed.";
      System.err.println(m);
      logger.warn(m);
    }

  }

  public void run() {

    int missCount = 0;

    while (!shutdown || missCount < 10) {
      if (missCount > 50) {

        if (out != null) {
          out.flush();
        }
        try {
          Thread.sleep(20);
        } catch (InterruptedException e) {
          logger.info("Interrupted TxnLogBufferWriter", e);
        }
      }
      synchronized (queueMonitor) {
        if (logBufferQueue.isEmpty()) {
          ++missCount;
        } else {
          TransactionLogBuffer buffer = logBufferQueue.remove();
          write(buffer);
          missCount = 0;
        }
      }
    }

    close();
    shutdownComplete = true;

    synchronized (logWriterThread) {
      logWriterThread.notifyAll();
    }
  }

  public void log(TransactionLogBuffer logBuffer) {
    logBufferQueue.add(logBuffer);
  }

  private void write(TransactionLogBuffer logBuffer) {

    // check to see if we need to switch file?
    LogTime logTime = LogTime.get();
    if (logTime.isNextDay()) {
      logTime = LogTime.nextDay();
      switchFile(logTime);
    }

    if (bytesWritten > maxBytesPerFile) {
      ++fileCounter;
      switchFile(logTime);
    }

    String txnId = logBuffer.getTransactionId();

    List<LogEntry> messages = logBuffer.messages();
    for (int i = 0; i < messages.size(); i++) {
      LogEntry msg = messages.get(i);
      printMessage(logTime, txnId, msg);
    }
  }

  private void printMessage(LogTime logTime, String txnId, LogEntry logEntry) {

    String msg = logEntry.getMsg();
    int len = msg.length();
    if (len == 0) {
      return;
    }

    // add overhead + content
    bytesWritten += 16;
    bytesWritten += len;

    if (txnId != null) {
      bytesWritten += 7;
      bytesWritten += txnId.length();
      out.append("txn[");
      out.append(txnId);
      out.append("]");
      out.append(deliminator);
    }

    out.append(logTime.getTimestamp(logEntry.getTimestamp()));
    out.append(deliminator);
    out.append(msg).append(" ");
    out.append("\n");
  }

  /**
   * Recursively output the Throwable stack trace to the log.
   * 
   * @param sb
   *          the buffer to write the stack trace to
   * @param e
   *          the source throwable
   * @param isCause
   *          flag to indicate if this is the top level throwable or a cause
   */
  protected void printThrowable(StringBuilder sb, Throwable e, boolean isCause) {
    if (e != null) {
      if (isCause) {
        sb.append("Caused by: ");
      }
      sb.append(e.getClass().getName());
      sb.append(":");
      sb.append(e.getMessage()).append(newLinePlaceholder);

      StackTraceElement[] ste = e.getStackTrace();
      int outputStackLines = ste.length;
      int notShownCount = 0;
      if (ste.length > maxStackTraceLines) {
        outputStackLines = maxStackTraceLines;
        notShownCount = ste.length - outputStackLines;
      }
      for (int i = 0; i < outputStackLines; i++) {
        sb.append(atString);
        sb.append(ste[i].toString()).append(newLinePlaceholder);
      }
      if (notShownCount > 0) {
        sb.append("        ... ");
        sb.append(notShownCount);
        sb.append(" more").append(newLinePlaceholder);
      }
      Throwable cause = e.getCause();
      if (cause != null) {
        printThrowable(sb, cause, true);
      }
    }
  }

  private String newFileName(LogTime logTime) {
    return filepath + File.separator + logFileName + logTime.getYMD() + "-" + fileCounter + logFileSuffix;
  }

  /**
   * Switch the file to log to.
   */
  protected void switchFile(LogTime logTime) {

    try {
      long currentFileLength = 0;
      String newFilePath = null;

      // skip a file if it already has max bytes
      do {
        newFilePath = newFileName(logTime);
        File f = new File(newFilePath);
        if (!f.exists()) {
          currentFileLength = 0;
        } else {
          if (f.length() < maxBytesPerFile * 0.8) {
            currentFileLength = f.length();
          } else {
            ++fileCounter;
            newFilePath = null;
          }
        }
      } while (newFilePath == null);

      if (!newFilePath.equals(currentPath)) {
        PrintStream newOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(newFilePath, true)));

        close();

        bytesWritten = currentFileLength;
        currentPath = newFilePath;
        out = newOut;
      }

    } catch (IOException e) {
      e.printStackTrace();
      logger.error("Error switch log file", e);
    }
  }

  /**
   * Close the file output stream being used for logging.
   */
  private void close() {
    if (out != null) {
      out.flush();
      out.close();
    }
  }

  /**
   * Returns the directory path of the log file.
   */
  protected String makeDirIfRequired(String dir) {

    File f = new File(dir);
    if (f.exists()) {
      if (!f.isDirectory()) {
        String msg = "Transaction logs directory is a file? " + dir;
        throw new PersistenceException(msg);
      }
    } else {
      if (!f.mkdirs()) {
        String msg = "Failed to create transaction logs directory " + dir;
        logger.error(msg);
      }
    }
    return dir;
  }

}
